plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Use the exact match for Kotlin 2.2.10
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
}