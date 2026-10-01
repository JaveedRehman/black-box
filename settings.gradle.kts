// =============================================================
// settings.gradle.kts
// =============================================================
// This file tells Gradle (the build system) two things:
//   1. What this project is called
//   2. Where to download the libraries (dependencies) from
//
// You don't need to touch this file unless you rename the app.
// =============================================================

// The name of your project (shown in Android Studio)
rootProject.name = "PomodoroApp"

// Tell Gradle where to find Android and Kotlin libraries
pluginManagement {
    repositories {
        google()          // Google's own libraries (Android, Jetpack, Compose)
        mavenCentral()    // The world's biggest open-source library repository
        gradlePluginPortal() // Gradle's own plugin repository
    }
}

// Tell Gradle where to download app dependencies from
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Tell Gradle that "app" is a sub-project (module) to build.
// Most Android apps have just one module called "app".
include(":app")
