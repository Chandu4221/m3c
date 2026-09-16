import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                // Internal Module Dependencies
                implementation(project(":core-domain"))
                implementation(project(":core-codegen"))
                implementation(project(":runtime-renderer"))

                // Desktop Compose Runtime & OS Engine
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.material3)

                // Jewel Standalone IDE-style UI Kit
                implementation(libs.jewel.int.ui.standalone)
                implementation(libs.jewel.int.ui.decorated.window)

                // Coroutines & Lifecycle
                implementation(libs.kotlinx.coroutinesSwing)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                // Material Icons
                implementation(libs.material.icons.extended)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.chandradsl.m3c.app.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "m3c-studio"
            packageVersion = "1.0.0"
        }
    }
}