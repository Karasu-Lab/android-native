pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
val requestedTaskNames = gradle.startParameter.taskNames
val buildingFdroidOnly = requestedTaskNames.any { it.contains("fdroid", ignoreCase = true) } &&
    requestedTaskNames.none { it.contains("standard", ignoreCase = true) }

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (!buildingFdroidOnly) {
            maven { url = uri("https://jitpack.io") }
        }
    }
}

rootProject.name = "KarasuLab"
include(":app")
 