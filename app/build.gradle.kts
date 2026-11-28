import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.io.File
import java.net.URL
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

abstract class GenerateLicensesTask : DefaultTask() {
    @get:Input
    abstract val dependencyCoordinates: ListProperty<String>

    @get:Input
    abstract val gradleUserHome: Property<String>

    @get:Input
    abstract val appVersion: Property<String>

    @get:Input
    abstract val fetchLicenseTexts: Property<Boolean>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val artifactFiles: ListProperty<File>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private val licenseTextCache = mutableMapOf<String, String?>()

    private val appLicenseText = """
MIT License

Copyright (c) 2026 Karasu Lab

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
""".trimIndent()

    @TaskAction
    fun generate() {
        val entries = linkedMapOf<String, List<String>>()
        dependencyCoordinates.get().forEach { coordinate ->
            val parts = coordinate.split(":")
            if (parts.size >= 3) {
                val group = parts[0]
                val name = parts[1]
                val version = parts.drop(2).joinToString(":")
                val key = "$group:$name"
                if (!entries.containsKey(key)) {
                    val licenses = findLicenses(group, name, version)
                    val text = licenseTextFor(licenses.first, licenses.second)
                    entries[key] = listOf(name, group, version, licenses.first, licenses.second, text)
                }
            }
        }
        val output = outputDirectory.get().asFile
        val directory = File(output, "com/karasu256/karasulab/data")
        directory.mkdirs()
        val file = File(directory, "GeneratedLicenseCatalog.kt")
        val body = StringBuilder()
        body.append("package com.karasu256.karasulab.data\n\n")
        body.append("object GeneratedLicenseCatalog {\n")
        body.append("    val libraries: List<LibraryLicense> = listOf(\n")
        body.append("        LibraryLicense(\"Karasu Lab Android\", \"com.karasu256.karasulab\", \"${appVersion.get()}\", \"MIT License\", \"https://opensource.org/license/mit\", \"${escapeText(appLicenseText)}\"),\n")
        entries.values.sortedBy { it[0].lowercase() }.forEach { entry ->
            val fields = entry.dropLast(1).joinToString(", ") { value -> "\"${escape(value)}\"" }
            body.append("        LibraryLicense($fields, \"${escapeText(entry.last())}\"),\n")
        }
        body.append("    )\n")
        body.append("}\n")
        file.writeText(body.toString())
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("$", "\\$")
        .replace("\n", " ")
        .replace("\r", " ")

    private fun escapeText(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("$", "\\$")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n", "\\n")
        .replace("\t", "\\t")

    private fun spdxId(licenseName: String, licenseUrl: String): String? {
        val name = licenseName.lowercase()
        val url = licenseUrl.lowercase()
        return when {
            "spdx.org/licenses/" in url -> licenseUrl.substringAfter("spdx.org/licenses/").trimEnd('/', '.').substringBefore('.')
            "apache" in name && "2.0" in name -> "Apache-2.0"
            "apache" in url && "license-2.0" in url -> "Apache-2.0"
            name == "mit" || "mit license" in name || url.endsWith("/mit") || "mit-license" in url -> "MIT"
            "bsd-3-clause" in name || "bsd 3-clause" in name -> "BSD-3-Clause"
            "bsd-2-clause" in name || "bsd 2-clause" in name -> "BSD-2-Clause"
            "eclipse public license" in name && "2.0" in name -> "EPL-2.0"
            "eclipse public license" in name && "1.0" in name -> "EPL-1.0"
            "mozilla public license" in name && "2.0" in name -> "MPL-2.0"
            "lesser general public license" in name && "2.1" in name -> "LGPL-2.1-only"
            "lesser general public license" in name && "3" in name -> "LGPL-3.0-only"
            "general public license" in name && "2" in name -> "GPL-2.0-only"
            "general public license" in name && "3" in name -> "GPL-3.0-only"
            else -> null
        }
    }

    private fun licenseTextFor(licenseName: String, licenseUrl: String): String {
        val id = spdxId(licenseName, licenseUrl)
        if (id != null && fetchLicenseTexts.get()) {
            val fetched = licenseTextCache.getOrPut(id) {
                runCatching { URL("https://spdx.org/licenses/$id.txt").readText() }.getOrNull()
            }
            if (!fetched.isNullOrBlank()) return fetched
        }
        return buildString {
            if (licenseName.isNotEmpty()) append("# ").append(licenseName).append("\n\n")
            if (licenseUrl.isNotEmpty()) append(licenseUrl)
        }
    }

    private fun findLicenses(group: String, name: String, version: String, depth: Int = 0): Pair<String, String> {
        val pom = findPom(group, name, version) ?: return "" to ""
        val document = runCatching {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom)
        }.getOrNull() ?: return "" to ""
        val licenses = document.getElementsByTagName("license")
        if (licenses.length > 0) {
            val element = licenses.item(0) as Element
            val licenseName = element.getElementsByTagName("name").item(0)?.textContent?.trim().orEmpty()
            val licenseUrl = element.getElementsByTagName("url").item(0)?.textContent?.trim().orEmpty()
            if (licenseName.isNotEmpty()) return licenseName to licenseUrl
        }
        if (depth < 3) {
            val parents = document.getElementsByTagName("parent")
            if (parents.length > 0) {
                val parent = parents.item(0) as Element
                val parentGroup = parent.getElementsByTagName("groupId").item(0)?.textContent?.trim().orEmpty()
                val parentName = parent.getElementsByTagName("artifactId").item(0)?.textContent?.trim().orEmpty()
                val parentVersion = parent.getElementsByTagName("version").item(0)?.textContent?.trim().orEmpty()
                if (parentGroup.isNotEmpty() && parentName.isNotEmpty() && parentVersion.isNotEmpty()) {
                    return findLicenses(parentGroup, parentName, parentVersion, depth + 1)
                }
            }
        }
        return "" to ""
    }

    private fun findPom(group: String, name: String, version: String): File? {
        val base = File(
            gradleUserHome.get(),
            "caches/modules-2/files-2.1/$group/$name/$version",
        )
        if (!base.exists()) return null
        return base.walkTopDown().firstOrNull { it.isFile && it.name == "$name-$version.pom" }
    }
}

val requestedTaskNames = gradle.startParameter.taskNames
val buildingNoGmsOnly = requestedTaskNames.any { it.contains("nogms", ignoreCase = true) } &&
    requestedTaskNames.none { it.replace("nogms", "", ignoreCase = true).contains("gms", ignoreCase = true) }
if (!buildingNoGmsOnly) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

tasks.configureEach {
    if (name.startsWith("process") && name.contains("Nogms") && name.endsWith("GoogleServices")) {
        enabled = false
    }
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

val releasePleaseManifest = rootProject.file(".release-please-manifest.json")
val releasePleaseVersion = if (releasePleaseManifest.exists()) {
    Regex("\"\\.\"\\s*:\\s*\"([^\"]+)\"").find(releasePleaseManifest.readText())?.groupValues?.get(1)
} else {
    null
}

val isCiBuild = providers.environmentVariable("CI").orNull == "true"

val releasePleaseVersionParts = (releasePleaseVersion ?: "1.0.0").split(".").map { it.toIntOrNull() ?: 0 }
val commitCount = runCatching {
    providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        workingDir = rootProject.projectDir
    }.standardOutput.asText.get().trim().toInt()
}.getOrNull() ?: 0
val derivedVersionCode = if (releasePleaseVersionParts.size >= 3) {
    val semverPart = releasePleaseVersionParts[0] * 10000 + releasePleaseVersionParts[1] * 100 + releasePleaseVersionParts[2]
    2_000_000_000 + commitCount * 1000 + semverPart % 1000
} else {
    2_000_000_000 + commitCount * 1000
}
val appVersionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: derivedVersionCode
val baseVersionName = releasePleaseVersion ?: "1.0.0"
val shortCommit = runCatching {
    providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        workingDir = rootProject.projectDir
    }.standardOutput.asText.get().trim()
}.getOrNull()?.ifBlank { null } ?: "unknown"
val appVersionName = "$baseVersionName-$shortCommit+$appVersionCode"

android {
    namespace = "com.karasu256.karasulab"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.karasu256.karasulab"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_HOST", "\"api.karasu256.com\"")
        buildConfigField("String", "MC_AUTH_ADDRESS", "\"mc-auth.karasu256.com\"")
        buildConfigField("String", "AUTH_HOST", "\"api-auth.karasu256.com\"")
        buildConfigField("String", "AUTH_UI_HOST", "\"auth.karasu256.com\"")
        buildConfigField(
            "String",
            "GOOGLE_SERVER_CLIENT_ID",
            "\"291377837692-haa4bsiure8qm70ionnaamb356qf6uca.apps.googleusercontent.com\""
        )
    }

    flavorDimensions += listOf("channel", "distribution")

    productFlavors {
        create("standard") {
            dimension = "channel"
            isDefault = true
            buildConfigField("boolean", "FDROID_BUILD", "false")
        }
        create("fdroid") {
            dimension = "channel"
            applicationId = "com.karasu256.karasulab.fdroid"
            buildConfigField("boolean", "FDROID_BUILD", "true")
        }
        create("gms") {
            dimension = "distribution"
            buildConfigField("boolean", "GMS_AVAILABLE", "true")
        }
        create("nogms") {
            dimension = "distribution"
            isDefault = true
            buildConfigField("boolean", "GMS_AVAILABLE", "false")
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file(".keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                val props = Properties().apply {
                    val file = rootProject.file("local.properties")
                    if (file.exists()) file.inputStream().use { load(it) }
                }
                storePassword = props["signing.storePassword"]?.toString()
                    ?: props["SIGNING_STORE_PASSWORD"]?.toString()

                keyAlias = props["SIGNING_ALIAS"]?.toString()
                    ?: props["signing.Alias"]?.toString()
                    ?: "release"

                keyPassword = props["signing.keyPassword"]?.toString()
                    ?: props["SIGNING_KEY_PASSWORD"]?.toString()
                    ?: storePassword
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = (project.findProperty("debuggable") as String?)?.toBoolean() ?: true
            buildConfigField("String", "RELEASE_CHANNEL", "\"internal-test\"")
            buildConfigField("String", "API_HOST", "\"dev-api.karasu256.com\"")
            buildConfigField("String", "AUTH_HOST", "\"dev-api-auth.karasu256.com\"")
            buildConfigField(
                "String",
                "GOOGLE_SERVER_CLIENT_ID",
                "\"521494519407-kufmdvbthsglk6bgiqve72hlbnd4nak2.apps.googleusercontent.com\""
            )
            buildConfigField("String", "ACCOUNTS_HOST", "\"dev-accounts.karasu256.com\"")
            buildConfigField("String", "COOKIE_PREFIX", "\"preview\"")
            buildConfigField("String", "MC_AUTH_HOST", "\"dev-mc-auth.karasu256.com\"")
            buildConfigField("String", "CDN_URL", "\"https://dev-cdn.karasu256.com/\"")
            manifestPlaceholders["accountsHost"] = "dev-accounts.karasu256.com"
            manifestPlaceholders["accountsWildcardHost"] = "*.dev-accounts.karasu256.com"
            val keystoreFile = rootProject.file(".keystore")
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.findByName("release")
            }
            if (pluginManager.hasPlugin("com.google.firebase.crashlytics")) {
                configure<CrashlyticsExtension> {
                    nativeSymbolUploadEnabled = true
                }
            }
        }
        create("localDebug") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".debug"
        }
        release {
            manifestPlaceholders += mapOf()
            buildConfigField("String", "RELEASE_CHANNEL", "\"production\"")
            buildConfigField("String", "API_HOST", "\"api.karasu256.com\"")
            buildConfigField("String", "AUTH_HOST", "\"api-auth.karasu256.com\"")
            buildConfigField(
                "String",
                "GOOGLE_SERVER_CLIENT_ID",
                "\"291377837692-haa4bsiure8qm70ionnaamb356qf6uca.apps.googleusercontent.com\""
            )
            buildConfigField("String", "ACCOUNTS_HOST", "\"accounts.karasu256.com\"")
            buildConfigField("String", "COOKIE_PREFIX", "\"production\"")
            buildConfigField("String", "MC_AUTH_HOST", "\"mc-auth.karasu256.com\"")
            buildConfigField("String", "CDN_URL", "\"https://cdn.karasu256.com/\"")
            manifestPlaceholders["accountsHost"] = "accounts.karasu256.com"
            manifestPlaceholders["accountsWildcardHost"] = "accounts.karasu256.com"
            val keystoreFile = rootProject.file(".keystore")
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.findByName("release")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.browser)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.credentials)
    "gmsImplementation"(libs.androidx.credentials.play.services.auth)
    "gmsImplementation"(libs.googleid)
    "gmsImplementation"(platform(libs.firebase.bom))
    "gmsImplementation"(libs.firebase.auth)
    "gmsImplementation"(libs.firebase.firestore)
    "gmsImplementation"(libs.firebase.crashlytics)
    "gmsImplementation"(libs.firebase.analytics)
    implementation(libs.multiplatform.settings)
    "standardImplementation"(libs.compose.markdown)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

androidComponents {
    beforeVariants(selector().withBuildType("debug")) {
        it.enable = isCiBuild
    }
    beforeVariants(selector().withBuildType("localDebug")) {
        it.enable = !isCiBuild
    }
    onVariants { variant ->
        val isFdroid = variant.name.startsWith("fdroid")
        val variantLabel = variant.name.removePrefix("standard").replaceFirstChar { it.lowercase() }
        variant.outputs.forEach { output ->
            output.versionCode.set(appVersionCode)
            output.versionName.set("$baseVersionName-$variantLabel-$shortCommit+$appVersionCode")
        }
        val capitalized = variant.name.replaceFirstChar { it.uppercase() }
        val licensesTask = tasks.register<GenerateLicensesTask>("generate${capitalized}Licenses") {
            outputs.upToDateWhen { false }
            dependencyCoordinates.set(
                configurations.getByName("${variant.name}RuntimeClasspath")
                    .incoming.artifacts.resolvedArtifacts
                    .map { results -> results.map { result -> result.id.componentIdentifier.displayName }.distinct() }
            )
            gradleUserHome.set(gradle.gradleUserHomeDir.absolutePath)
            appVersion.set(appVersionName)
            fetchLicenseTexts.set(!isFdroid)
            artifactFiles.set(
                configurations.getByName("${variant.name}RuntimeClasspath")
                    .incoming.artifacts.resolvedArtifacts
                    .map { results -> results.map { result -> result.file } }
            )
            outputDirectory.set(layout.buildDirectory.dir("generated/licenses/${variant.name}"))
        }
        variant.sources.java?.addGeneratedSourceDirectory(licensesTask, GenerateLicensesTask::outputDirectory)
    }
}
