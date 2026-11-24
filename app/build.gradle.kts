import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingProperty(name: String): String? = providers.gradleProperty(name).orNull

val signingStoreFile: File? = signingProperty("signing.storeFile")
    ?.replaceFirst(Regex("^~"), Regex.escapeReplacement(System.getProperty("user.home")))
    ?.let(::file)
    ?.takeIf { it.exists() }

val signingStorePassword: String? = localProperties.getProperty("signing.storePassword")
val signingDebugKeyPassword: String? = localProperties.getProperty("signing.debugKeyPassword")
val signingReleaseKeyPassword: String? = localProperties.getProperty("signing.releaseKeyPassword")

val gpgHome: File = listOfNotNull(System.getenv("GNUPGHOME")?.let(::File), rootProject.projectDir.resolve(".gpg"))
    .firstOrNull { it.resolve("com.karasu256.karasulab.keystore.gpg").exists() }
    ?: rootProject.projectDir.resolve(".gpg")

fun gpgDecrypt(name: String, passphrase: String? = null): ByteArray? {
    val encrypted = gpgHome.resolve(name).takeIf { it.exists() } ?: return null
    val passphraseFile = passphrase?.let {
        layout.buildDirectory.file("gpg-signing/passphrase").get().asFile.apply {
            parentFile.mkdirs()
            createNewFile()
            setReadable(false, false)
            setReadable(true, true)
            writeText(it)
        }
    }
    val exec = providers.exec {
        val args = mutableListOf("gpg", "--homedir", gpgHome.path, "--batch", "--quiet")
        if (passphraseFile != null) args += listOf("--pinentry-mode", "loopback", "--passphrase-file", passphraseFile.path)
        commandLine(args + listOf("--decrypt", encrypted.path))
        isIgnoreExitValue = true
    }
    val output = exec.standardOutput.asBytes.get()
    val exitValue = exec.result.get().exitValue
    passphraseFile?.delete()
    if (exitValue == 0) return output
    val message = "gpg failed to decrypt $name with GNUPGHOME=${gpgHome.path}"
    check(System.getProperty("idea.sync.active") == "true") { message }
    logger.warn("$message (IDE sync continues without the gpg signing key)")
    return null
}

val gpgDebugPassword: String? = gpgDecrypt("com.karasu256.karasulab.password.gpg")
    ?.toString(Charsets.UTF_8)
    ?.trim()

val gpgDebugKeystore: File? = gpgDebugPassword?.let {
    gpgDecrypt("com.karasu256.karasulab.keystore.gpg", passphrase = it)?.let { bytes ->
        layout.buildDirectory.file("gpg-signing/debug.keystore").get().asFile.also { file ->
            file.parentFile.mkdirs()
            file.writeBytes(bytes)
        }
    }
}

android {
    namespace = "com.karasu256.karasulab"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.karasu256.karasulab"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_HOST", "\"api.karasu256.com\"")
        buildConfigField("String", "AUTH_HOST", "\"api-auth.karasu256.com\"")
    }

    signingConfigs {
        getByName("debug") {
            if (gpgDebugKeystore != null && gpgDebugPassword != null) {
                storeFile = gpgDebugKeystore
                storePassword = gpgDebugPassword
                keyAlias = "debug"
                keyPassword = gpgDebugPassword
            } else if (signingStoreFile != null && signingStorePassword != null && signingDebugKeyPassword != null) {
                storeFile = signingStoreFile
                storePassword = signingStorePassword
                keyAlias = "debug"
                keyPassword = signingDebugKeyPassword
            } else {
                storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
        if (signingStoreFile != null && signingStorePassword != null && signingReleaseKeyPassword != null) {
            create("release") {
                storeFile = signingStoreFile
                storePassword = signingStorePassword
                keyAlias = "release"
                keyPassword = signingReleaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
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
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
