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
}
