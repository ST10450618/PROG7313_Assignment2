// Root build.gradle.kts — plugin declarations only (apply false)
// All plugin applications happen in app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)  apply false
    alias(libs.plugins.kotlin.android)       apply false
    alias(libs.plugins.hilt.android)         apply false
    alias(libs.plugins.ksp)                  apply false
}
