package dev.chandradsl.m3c.app.desktop.state

import java.io.File
import java.util.zip.ZipFile
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StudioViewModelExportTest {

    @Test
    fun testExportDialogVisibilityToggle() {
        val vm = StudioViewModel()
        assertFalse(vm.isExportDialogOpen)

        vm.openExportDialog()
        assertTrue(vm.isExportDialogOpen)

        vm.closeExportDialog()
        assertFalse(vm.isExportDialogOpen)
    }

    @Test
    fun testExportProjectToDirectory() {
        val vm = StudioViewModel()
        vm.projectName = "SampleApp"
        vm.packageName = "com.sample.studio"
        vm.addScreen("ProfileScreen", "profile")

        val tempDir = createTempDirectory("m3c_vm_export_dir").toFile()
        try {
            val success = vm.exportProjectToDirectory(
                targetDir = tempDir,
                customName = "SampleApp",
                customPackage = "com.sample.studio"
            )

            assertTrue(success)
            assertTrue(File(tempDir, "settings.gradle.kts").exists())
            assertTrue(File(tempDir, "build.gradle.kts").exists())
            assertTrue(File(tempDir, "gradlew").exists())
            assertTrue(File(tempDir, "app/build.gradle.kts").exists())
            assertTrue(File(tempDir, "app/src/main/AndroidManifest.xml").exists())
            assertTrue(File(tempDir, "app/src/main/java/com/sample/studio/MainActivity.kt").exists())
            assertTrue(File(tempDir, "app/src/main/java/com/sample/studio/ui/navigation/AppNavHost.kt").exists())
            assertTrue(File(tempDir, "app/src/main/java/com/sample/studio/ui/screens/MainScreen.kt").exists())
            assertTrue(File(tempDir, "app/src/main/java/com/sample/studio/ui/screens/ProfileScreen.kt").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testExportProjectToZip() {
        val vm = StudioViewModel()
        vm.projectName = "ZipExportApp"
        vm.packageName = "com.ziptest.app"
        vm.addScreen("SettingsScreen", "settings")

        val tempDir = createTempDirectory("m3c_vm_export_zip").toFile()
        val zipFile = File(tempDir, "ExportedProject.zip")
        try {
            val success = vm.exportProjectToZip(
                zipFile = zipFile,
                customName = "ZipExportApp",
                customPackage = "com.ziptest.app"
            )

            assertTrue(success)
            assertTrue(zipFile.exists())
            assertTrue(zipFile.length() > 0)

            val zip = ZipFile(zipFile)
            val entryNames = zip.entries().asSequence().map { it.name }.toSet()
            zip.close()

            val prefix = "ZipExportApp"
            assertTrue("$prefix/settings.gradle.kts" in entryNames)
            assertTrue("$prefix/build.gradle.kts" in entryNames)
            assertTrue("$prefix/app/build.gradle.kts" in entryNames)
            assertTrue("$prefix/app/src/main/java/com/ziptest/app/MainActivity.kt" in entryNames)
            assertTrue("$prefix/app/src/main/java/com/ziptest/app/ui/navigation/AppNavHost.kt" in entryNames)
            assertTrue("$prefix/app/src/main/java/com/ziptest/app/ui/screens/MainScreen.kt" in entryNames)
            assertTrue("$prefix/app/src/main/java/com/ziptest/app/ui/screens/SettingsScreen.kt" in entryNames)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
