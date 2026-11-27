import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
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
            signingConfig = signingConfigs.findByName("debug")
            configure<CrashlyticsExtension> {
                nativeSymbolUploadEnabled = true
            }
        }
        release {
            manifestPlaceholders += mapOf()
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
