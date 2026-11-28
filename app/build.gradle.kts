import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.io.File
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
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
    abstract val runtimeClasspathName: Property<String>

    @get:Input
    abstract val appVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val configuration = project.configurations.getByName(runtimeClasspathName.get())
        val entries = linkedMapOf<String, List<String>>()
        configuration.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            val id = artifact.moduleVersion.id
            val key = "${id.group}:${id.name}"
            if (!entries.containsKey(key)) {
                val licenses = findLicenses(id.group, id.name, id.version)
                entries[key] = listOf(id.name, id.group, id.version, licenses.first, licenses.second)
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
        body.append("        LibraryLicense(\"Karasu Lab Android\", \"com.karasu256.karasulab\", \"${appVersion.get()}\", \"MIT License\", \"https://opensource.org/license/mit\"),\n")
        entries.values.sortedBy { it[0].lowercase() }.forEach { entry ->
            body.append("        LibraryLicense(${entry.joinToString(", ") { value -> "\"${escape(value)}\"" }}),\n")
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
            project.gradle.gradleUserHomeDir,
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

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

val appVersionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
val appVersionName = project.findProperty("versionName")?.toString() ?: "1.0"

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

    flavorDimensions += "distribution"

    productFlavors {
        create("gms") {
            dimension = "distribution"
            isDefault = true
            buildConfigField("boolean", "GMS_AVAILABLE", "true")
        }
        create("nogms") {
            dimension = "distribution"
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
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

androidComponents {
    onVariants { variant ->
        val capitalized = variant.name.replaceFirstChar { it.uppercase() }
        val licensesTask = tasks.register<GenerateLicensesTask>("generate${capitalized}Licenses") {
            outputs.upToDateWhen { false }
            runtimeClasspathName.set("${variant.name}RuntimeClasspath")
            appVersion.set(appVersionName)
            outputDirectory.set(layout.buildDirectory.dir("generated/licenses/${variant.name}"))
        }
        variant.sources.java?.addGeneratedSourceDirectory(licensesTask, GenerateLicensesTask::outputDirectory)
    }
}
