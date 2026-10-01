// =============================================================
// build.gradle.kts  (ROOT level — top of the project)
// =============================================================
// Every Android project has TWO build.gradle.kts files:
//
//   1. This one (root) — applies settings to the WHOLE project
//   2. app/build.gradle.kts — settings specific to the app
//
// This root file just declares which "plugins" (build tools)
// are available. The "apply false" means "don't use them yet,
// just make them available for sub-projects like :app".
// =============================================================

plugins {
    // Android Gradle Plugin — the tool that builds Android apps
    // Version 8.6.1 is a stable release from 2024
    id("com.android.application") version "8.6.1" apply false

    // Kotlin plugin — Kotlin is the programming language we use
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // Compose Compiler plugin — needed for Jetpack Compose (our UI toolkit)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
