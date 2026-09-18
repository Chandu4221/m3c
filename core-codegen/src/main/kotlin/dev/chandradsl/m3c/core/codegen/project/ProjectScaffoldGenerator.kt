package dev.chandradsl.m3c.core.codegen.project

import dev.chandradsl.m3c.core.codegen.ComposeCodeGenerator
import dev.chandradsl.m3c.core.domain.model.M3cProject
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Represents a single generated file in the project scaffold.
 */
data class GeneratedProjectFile(
    val relativePath: String,
    val content: String,
    val isExecutable: Boolean = false
)

/**
 * Generates complete, runnable Android Studio / Compose Multiplatform project scaffolds.
 * Can export directly to a target directory or as a compressed .zip archive.
 */
object ProjectScaffoldGenerator {

    /**
     * Generates the list of all files that make up a compilable Android project.
     */
    fun generateProjectFiles(project: M3cProject): List<GeneratedProjectFile> {
        val files = mutableListOf<GeneratedProjectFile>()
        val packageName = project.packageName.ifBlank { "com.example.app" }
        val projectName = project.name.ifBlank { "M3cApp" }
        val packagePath = packageName.replace('.', '/')

        // 1. Root Gradle & Project Files
        files += GeneratedProjectFile(
            relativePath = "settings.gradle.kts",
            content = generateSettingsGradle(projectName)
        )
        files += GeneratedProjectFile(
            relativePath = "build.gradle.kts",
            content = generateRootBuildGradle()
        )
        files += GeneratedProjectFile(
            relativePath = ".gitignore",
            content = generateGitignore()
        )

        // 2. Gradle Wrapper
        files += GeneratedProjectFile(
            relativePath = "gradle/wrapper/gradle-wrapper.properties",
            content = generateGradleWrapperProperties()
        )
        files += GeneratedProjectFile(
            relativePath = "gradle/libs.versions.toml",
            content = generateLibsVersionsToml()
        )
        files += GeneratedProjectFile(
            relativePath = "gradlew",
            content = generateGradlewScript(),
            isExecutable = true
        )
        files += GeneratedProjectFile(
            relativePath = "gradlew.bat",
            content = generateGradlewBatScript(),
            isExecutable = true
        )

        // 3. App Module Configuration
        files += GeneratedProjectFile(
            relativePath = "app/build.gradle.kts",
            content = generateAppBuildGradle(packageName)
        )
        files += GeneratedProjectFile(
            relativePath = "app/proguard-rules.pro",
            content = "# Add project specific ProGuard rules here.\n"
        )
        files += GeneratedProjectFile(
            relativePath = "app/src/main/AndroidManifest.xml",
            content = generateAndroidManifest(projectName)
        )

        // 4. UI Theme System
        files += GeneratedProjectFile(
            relativePath = "app/src/main/java/$packagePath/ui/theme/Color.kt",
            content = generateColorTheme(packageName)
        )
        files += GeneratedProjectFile(
            relativePath = "app/src/main/java/$packagePath/ui/theme/Type.kt",
            content = generateTypeTheme(packageName)
        )
        files += GeneratedProjectFile(
            relativePath = "app/src/main/java/$packagePath/ui/theme/Theme.kt",
            content = generateTheme(packageName)
        )

        // 5. MainActivity
        files += GeneratedProjectFile(
            relativePath = "app/src/main/java/$packagePath/MainActivity.kt",
            content = generateMainActivity(packageName)
        )

        // 6. Navigation Graph
        val screens = if (project.screens.isEmpty()) {
            listOf(M3cScreen("main", "MainScreen", "main", dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode(), true))
        } else {
            project.screens
        }

        val navGraphCode = ComposeCodeGenerator.generateNavGraphCodeString(
            packageName = "$packageName.ui.navigation",
            screens = screens,
            graphName = "AppNavHost",
            screensPackage = "$packageName.ui.screens"
        )
        files += GeneratedProjectFile(
            relativePath = "app/src/main/java/$packagePath/ui/navigation/AppNavHost.kt",
            content = navGraphCode
        )

        // 7. Individual Screen Files
        for (screen in screens) {
            val screenCode = ComposeCodeGenerator.generateCodeString(
                packageName = "$packageName.ui.screens",
                componentName = screen.name,
                rootNode = screen.rootNode
            )
            files += GeneratedProjectFile(
                relativePath = "app/src/main/java/$packagePath/ui/screens/${screen.name}.kt",
                content = screenCode
            )
        }

        return files
    }

    /**
     * Exports the project directly to the specified target directory on the filesystem.
     */
    fun exportToDirectory(project: M3cProject, targetDir: File): List<File> {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val generatedFiles = generateProjectFiles(project)
        val createdFiles = mutableListOf<File>()

        for (genFile in generatedFiles) {
            val destFile = File(targetDir, genFile.relativePath)
            destFile.parentFile?.mkdirs()
            destFile.writeText(genFile.content, StandardCharsets.UTF_8)
            if (genFile.isExecutable) {
                try {
                    destFile.setExecutable(true, false)
                } catch (_: Exception) {}
            }
            createdFiles += destFile
        }

        return createdFiles
    }

    /**
     * Exports the project as a compressed .zip archive.
     */
    fun exportToZip(project: M3cProject, zipFile: File): File {
        zipFile.parentFile?.mkdirs()
        val rootPrefix = "${project.name.ifBlank { "M3cApp" }}/"
        val generatedFiles = generateProjectFiles(project)

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            for (genFile in generatedFiles) {
                val entryName = rootPrefix + genFile.relativePath
                val entry = ZipEntry(entryName)
                zos.putNextEntry(entry)
                val bytes = genFile.content.toByteArray(StandardCharsets.UTF_8)
                zos.write(bytes, 0, bytes.size)
                zos.closeEntry()
            }
        }

        return zipFile
    }

    // ========================================================================
    // Internal File Templates
    // ========================================================================

    private fun generateSettingsGradle(projectName: String): String = """
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
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
            }
        }

        rootProject.name = "$projectName"
        include(":app")
    """.trimIndent() + "\n"

    private fun generateRootBuildGradle(): String = """
        // Top-level build file where you can add configuration options common to all sub-projects/modules.
        plugins {
            alias(libs.plugins.android.application) apply false
            alias(libs.plugins.kotlin.android) apply false
            alias(libs.plugins.compose.compiler) apply false
        }
    """.trimIndent() + "\n"

    private fun generateLibsVersionsToml(): String = """
        [versions]
        agp = "8.7.3"
        kotlin = "2.1.0"
        coreKtx = "1.15.0"
        lifecycleRuntimeKtx = "2.8.7"
        activityCompose = "1.9.3"
        composeBom = "2024.12.01"
        navigationCompose = "2.8.5"

        [libraries]
        androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
        androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
        androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
        androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
        androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
        androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
        androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
        androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
        androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
        androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

        [plugins]
        android-application = { id = "com.android.application", version.ref = "agp" }
        kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
        compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
    """.trimIndent() + "\n"

    private fun generateGradleWrapperProperties(): String = """
        distributionBase=GRADLE_USER_HOME
        distributionPath=wrapper/dists
        distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
        networkTimeout=10000
        validateDistributionUrl=true
        zipStoreBase=GRADLE_USER_HOME
        zipStorePath=wrapper/dists
    """.trimIndent() + "\n"

    private fun generateGitignore(): String = """
        *.iml
        .gradle
        /local.properties
        /.idea/caches
        /.idea/libraries
        /.idea/modules.xml
        /.idea/workspace.xml
        /.idea/navEditor.xml
        /.idea/assetWizardSettings.xml
        .DS_Store
        /build
        /captures
        .externalNativeBuild
        .cxx
        local.properties
    """.trimIndent() + "\n"

    private fun generateAppBuildGradle(packageName: String): String = """
        plugins {
            alias(libs.plugins.android.application)
            alias(libs.plugins.kotlin.android)
            alias(libs.plugins.compose.compiler)
        }

        android {
            namespace = "$packageName"
            compileSdk = 35

            defaultConfig {
                applicationId = "$packageName"
                minSdk = 24
                targetSdk = 35
                versionCode = 1
                versionName = "1.0"

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            kotlinOptions {
                jvmTarget = "17"
            }
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.compose.material.icons.extended)
            implementation(libs.androidx.navigation.compose)
        }
    """.trimIndent() + "\n"

    private fun generateAndroidManifest(projectName: String): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">

            <application
                android:allowBackup="true"
                android:icon="@android:drawable/sym_def_app_icon"
                android:label="$projectName"
                android:supportsRtl="true"
                android:theme="@android:style/Theme.Material.Light.NoActionBar">
                <activity
                    android:name=".MainActivity"
                    android:exported="true"
                    android:theme="@android:style/Theme.Material.Light.NoActionBar">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>

        </manifest>
    """.trimIndent() + "\n"

    private fun generateColorTheme(packageName: String): String = """
        package $packageName.ui.theme

        import androidx.compose.ui.graphics.Color

        val Purple80 = Color(0xFFD0BCFF)
        val PurpleGrey80 = Color(0xFFCCC2DC)
        val Pink80 = Color(0xFFEFB8C8)

        val Purple40 = Color(0xFF6650a4)
        val PurpleGrey40 = Color(0xFF625b71)
        val Pink40 = Color(0xFF7D5260)
    """.trimIndent() + "\n"

    private fun generateTypeTheme(packageName: String): String = """
        package $packageName.ui.theme

        import androidx.compose.material3.Typography
        import androidx.compose.ui.text.TextStyle
        import androidx.compose.ui.text.font.FontFamily
        import androidx.compose.ui.text.font.FontWeight
        import androidx.compose.ui.unit.sp

        val Typography = Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            )
        )
    """.trimIndent() + "\n"

    private fun generateTheme(packageName: String): String = """
        package $packageName.ui.theme

        import android.os.Build
        import androidx.compose.foundation.isSystemInDarkTheme
        import androidx.compose.material3.MaterialTheme
        import androidx.compose.material3.darkColorScheme
        import androidx.compose.material3.dynamicDarkColorScheme
        import androidx.compose.material3.dynamicLightColorScheme
        import androidx.compose.material3.lightColorScheme
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.platform.LocalContext

        private val DarkColorScheme = darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80
        )

        private val LightColorScheme = lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40
        )

        @Composable
        fun AppTheme(
            darkTheme: Boolean = isSystemInDarkTheme(),
            dynamicColor: Boolean = true,
            content: @Composable () -> Unit
        ) {
            val colorScheme = when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content
            )
        }
    """.trimIndent() + "\n"

    private fun generateMainActivity(packageName: String): String = """
        package $packageName

        import android.os.Bundle
        import androidx.activity.ComponentActivity
        import androidx.activity.compose.setContent
        import androidx.activity.enableEdgeToEdge
        import androidx.compose.foundation.layout.fillMaxSize
        import androidx.compose.material3.MaterialTheme
        import androidx.compose.material3.Surface
        import androidx.compose.ui.Modifier
        import $packageName.ui.navigation.AppNavHost
        import $packageName.ui.theme.AppTheme

        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContent {
                    AppTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            AppNavHost()
                        }
                    }
                }
            }
        }
    """.trimIndent() + "\n"

    private fun generateGradlewScript(): String = """
        #!/bin/sh
        # Attempt to set APP_HOME
        APP_HOME=${'$'}(cd "${'$'}(dirname "${'$'}0")" && pwd)
        exec "${'$'}APP_HOME/gradle/wrapper/gradle-wrapper.jar" "${'$'}@"
    """.trimIndent() + "\n"

    private fun generateGradlewBatScript(): String = """
        @rem Gradle wrapper script for Windows
        @set APP_HOME=%~dp0
        @java -jar "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" %*
    """.trimIndent() + "\n"
}
