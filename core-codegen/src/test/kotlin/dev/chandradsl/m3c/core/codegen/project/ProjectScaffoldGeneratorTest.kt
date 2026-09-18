package dev.chandradsl.m3c.core.codegen.project

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.M3cProject
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectScaffoldGeneratorTest {

    private fun createSampleProject(): M3cProject {
        val screen1 = M3cScreen(
            id = "s1",
            name = "HomeScreen",
            route = "home",
            rootNode = ComposableNode.ColumnNode(),
            isStartDestination = true
        )
        val screen2 = M3cScreen(
            id = "s2",
            name = "DetailsScreen",
            route = "details",
            rootNode = ComposableNode.RowNode(),
            isStartDestination = false
        )
        return M3cProject(
            name = "AwesomeShop",
            packageName = "com.awesomeshop.app",
            screens = listOf(screen1, screen2),
            activeScreenId = "s1"
        )
    }

    @Test
    fun testGenerateProjectFiles() {
        val project = createSampleProject()
        val files = ProjectScaffoldGenerator.generateProjectFiles(project)

        val paths = files.map { it.relativePath }
        assertTrue(paths.contains("settings.gradle.kts"))
        assertTrue(paths.contains("build.gradle.kts"))
        assertTrue(paths.contains("gradle/libs.versions.toml"))
        assertTrue(paths.contains("gradle/wrapper/gradle-wrapper.properties"))
        assertTrue(paths.contains("app/build.gradle.kts"))
        assertTrue(paths.contains("app/src/main/AndroidManifest.xml"))
        assertTrue(paths.contains("app/src/main/java/com/awesomeshop/app/MainActivity.kt"))
        assertTrue(paths.contains("app/src/main/java/com/awesomeshop/app/ui/theme/Theme.kt"))
        assertTrue(paths.contains("app/src/main/java/com/awesomeshop/app/ui/navigation/AppNavHost.kt"))
        assertTrue(paths.contains("app/src/main/java/com/awesomeshop/app/ui/screens/HomeScreen.kt"))
        assertTrue(paths.contains("app/src/main/java/com/awesomeshop/app/ui/screens/DetailsScreen.kt"))

        // Check settings.gradle.kts content
        val settings = files.first { it.relativePath == "settings.gradle.kts" }
        assertTrue(settings.content.contains("rootProject.name = \"AwesomeShop\""))
        assertTrue(settings.content.contains("include(\":app\")"))

        // Check app/build.gradle.kts content
        val appBuild = files.first { it.relativePath == "app/build.gradle.kts" }
        assertTrue(appBuild.content.contains("namespace = \"com.awesomeshop.app\""))
        assertTrue(appBuild.content.contains("applicationId = \"com.awesomeshop.app\""))
    }

    @Test
    fun testExportToDirectory() {
        val project = createSampleProject()
        val tempDir = File.createTempFile("m3c_test_export_", "")
        tempDir.delete()
        tempDir.mkdirs()

        try {
            val exportedFiles = ProjectScaffoldGenerator.exportToDirectory(project, tempDir)
            assertTrue(exportedFiles.isNotEmpty())

            val manifest = File(tempDir, "app/src/main/AndroidManifest.xml")
            assertTrue(manifest.exists(), "AndroidManifest.xml must exist")
            assertTrue(manifest.readText().contains("AwesomeShop"))

            val navHost = File(tempDir, "app/src/main/java/com/awesomeshop/app/ui/navigation/AppNavHost.kt")
            assertTrue(navHost.exists(), "AppNavHost.kt must exist")
            assertTrue(navHost.readText().contains("HomeScreen"))
            assertTrue(navHost.readText().contains("DetailsScreen"))

            val homeScreen = File(tempDir, "app/src/main/java/com/awesomeshop/app/ui/screens/HomeScreen.kt")
            assertTrue(homeScreen.exists(), "HomeScreen.kt must exist")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testExportToZip() {
        val project = createSampleProject()
        val tempZip = File.createTempFile("m3c_test_archive_", ".zip")

        try {
            ProjectScaffoldGenerator.exportToZip(project, tempZip)
            assertTrue(tempZip.exists())
            assertTrue(tempZip.length() > 0)

            ZipFile(tempZip).use { zip ->
                val entries = zip.entries().asSequence().map { it.name }.toList()
                assertTrue(entries.contains("AwesomeShop/settings.gradle.kts"))
                assertTrue(entries.contains("AwesomeShop/app/build.gradle.kts"))
                assertTrue(entries.contains("AwesomeShop/app/src/main/AndroidManifest.xml"))
                assertTrue(entries.contains("AwesomeShop/app/src/main/java/com/awesomeshop/app/MainActivity.kt"))
            }
        } finally {
            tempZip.delete()
        }
    }
}
