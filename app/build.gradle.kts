// =============================================================
// app/build.gradle.kts  (APP level)
// =============================================================
// This file configures how YOUR app is built.
// It answers questions like:
//   - What Android version does it run on?
//   - What libraries does it use?
//   - How is it identified on the Play Store?
// =============================================================

// "plugins" are tools that help build the project.
// We apply the plugins that were declared in the root build.gradle.kts.
plugins {
    id("com.android.application")         // Builds an Android app (.apk file)
    id("org.jetbrains.kotlin.android")    // Enables Kotlin language support
    id("org.jetbrains.kotlin.plugin.compose") // Enables Jetpack Compose UI
}

android {
    // The unique identifier for your app on the Play Store.
    // Must be in "com.yourname.yourapp" format (like a backwards website).
    namespace = "com.pomodoro.app"

    // compileSdk = which Android SDK version to USE when building.
    // 34 = Android 14. Always use the latest stable.
    compileSdk = 34

    defaultConfig {
        // The unique app ID (same as namespace for simple apps)
        applicationId = "com.pomodoro.app"

        // minSdk = the OLDEST Android version your app supports.
        // 24 = Android 7.0 Nougat (covers ~96% of Android devices)
        minSdk = 24

        // targetSdk = the Android version your app is DESIGNED for.
        // Always set to the same as compileSdk.
        targetSdk = 34

        // versionCode = a number that increases with each release.
        // The Play Store uses this to know which version is newer.
        versionCode = 1

        // versionName = the human-readable version shown to users.
        versionName = "1.0"
    }

    buildTypes {
        // "release" is the version you publish to the Play Store.
        // minifyEnabled removes unused code to make the app smaller.
        release {
            isMinifyEnabled = false
        }
    }

    // Java/Kotlin version settings.
    // VERSION_17 means we're using Java 17 features.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // Enable Jetpack Compose — our modern UI toolkit.
    // Without this, you cannot use @Composable functions.
    buildFeatures {
        compose = true
    }
}

// =============================================================
// DEPENDENCIES — The libraries your app uses
// =============================================================
// Think of dependencies like apps on your phone.
// You "install" them here and can use them in your code.
// =============================================================
dependencies {

    // --- AndroidX Core ---
    // "androidx.core" gives you handy Kotlin shortcuts for Android APIs.
    // Without it, a lot of code would be much more verbose.
    implementation("androidx.core:core-ktx:1.13.1")

    // --- Activity Compose ---
    // Connects Android's "Activity" (screen) system to Jetpack Compose.
    // Required for setContent { } to work in MainActivity.
    implementation("androidx.activity:activity-compose:1.9.3")

    // --- Compose BOM (Bill of Materials) ---
    // The BOM makes all Compose libraries use matching, compatible versions.
    // You only need to specify the BOM version; individual library versions
    // are automatically chosen to be compatible with each other.
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)

    // --- Jetpack Compose UI ---
    // The core Compose drawing engine. Required.
    implementation("androidx.compose.ui:ui")

    // --- Compose Graphics ---
    // Tools for drawing custom shapes, gradients, and effects.
    implementation("androidx.compose.ui:ui-graphics")

    // --- Material 3 ---
    // Google's design system. Gives you beautiful, pre-built components
    // like Button, Surface, Text, etc., following Material Design 3 guidelines.
    implementation("androidx.compose.material3:material3")

    // --- ViewModel ---
    // The "brain" of the app. Holds data and survives screen rotations.
    // Without this, your timer would reset every time you flip the phone!
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // --- Lifecycle Runtime ---
    // Connects the app's lifecycle (start, stop, pause) to Compose.
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}
