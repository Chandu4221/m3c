plugins {
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
  }

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://packages.jetbrains.team/maven/p/kds/maven")
        maven("https://www.jetbrains.com/intellij-repository/releases")
    }
}