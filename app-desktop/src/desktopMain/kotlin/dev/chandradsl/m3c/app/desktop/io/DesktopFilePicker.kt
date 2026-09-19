package dev.chandradsl.m3c.app.desktop.io

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

object DesktopFilePicker {

    /**
     * Opens a native file dialog for selecting an existing .m3c file.
     * Returns the chosen [File] or null if canceled.
     */
    fun chooseOpenProjectFile(parentFrame: Frame? = null): File? {
        val dialog = FileDialog(parentFrame, "Open M3C Project", FileDialog.LOAD).apply {
            filenameFilter = FilenameFilter { _, name -> name.endsWith(".m3c", ignoreCase = true) }
            isVisible = true
        }

        val dir = dialog.directory ?: return null
        val file = dialog.file ?: return null
        return File(dir, file)
    }

    /**
     * Opens a native file dialog for saving a project to a .m3c file.
     * Ensures the returned file ends with `.m3c`.
     * Returns the chosen [File] or null if canceled.
     */
    fun chooseSaveProjectFile(defaultName: String = "Untitled", parentFrame: Frame? = null): File? {
        val initialFile = if (defaultName.endsWith(".m3c", ignoreCase = true)) defaultName else "$defaultName.m3c"
        val dialog = FileDialog(parentFrame, "Save M3C Project", FileDialog.SAVE).apply {
            file = initialFile
            filenameFilter = FilenameFilter { _, name -> name.endsWith(".m3c", ignoreCase = true) }
            isVisible = true
        }

        val dir = dialog.directory ?: return null
        var filename = dialog.file ?: return null
        if (!filename.endsWith(".m3c", ignoreCase = true)) {
            filename = "$filename.m3c"
        }
        return File(dir, filename)
    }

    /**
     * Opens a native folder chooser dialog for selecting a destination directory.
     * Returns the chosen [File] directory or null if canceled.
     */
    fun chooseDirectory(parentFrame: Frame? = null, title: String = "Select Export Destination"): File? {
        val chooser = javax.swing.JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false
        }
        val result = chooser.showOpenDialog(parentFrame)
        return if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else null
    }

    /**
     * Opens a native file dialog for selecting a destination .zip file.
     * Ensures the returned file ends with `.zip`.
     * Returns the chosen [File] or null if canceled.
     */
    fun chooseSaveZipFile(defaultName: String = "Project.zip", parentFrame: Frame? = null): File? {
        val initialFile = if (defaultName.endsWith(".zip", ignoreCase = true)) defaultName else "$defaultName.zip"
        val dialog = FileDialog(parentFrame, "Export Project as ZIP", FileDialog.SAVE).apply {
            file = initialFile
            filenameFilter = FilenameFilter { _, name -> name.endsWith(".zip", ignoreCase = true) }
            isVisible = true
        }

        val dir = dialog.directory ?: return null
        var filename = dialog.file ?: return null
        if (!filename.endsWith(".zip", ignoreCase = true)) {
            filename = "$filename.zip"
        }
        return File(dir, filename)
    }

    /**
     * Opens a native file dialog for saving a Kotlin source file (.kt).
     * Ensures the returned file ends with `.kt`.
     * Returns the chosen [File] or null if canceled.
     */
    fun chooseSaveKotlinFile(defaultName: String = "Screen.kt", parentFrame: Frame? = null): File? {
        val initialFile = if (defaultName.endsWith(".kt", ignoreCase = true)) defaultName else "$defaultName.kt"
        val dialog = FileDialog(parentFrame, "Save Kotlin Source File", FileDialog.SAVE).apply {
            file = initialFile
            filenameFilter = FilenameFilter { _, name -> name.endsWith(".kt", ignoreCase = true) }
            isVisible = true
        }

        val dir = dialog.directory ?: return null
        var filename = dialog.file ?: return null
        if (!filename.endsWith(".kt", ignoreCase = true)) {
            filename = "$filename.kt"
        }
        return File(dir, filename)
    }
}

