package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.io.DesktopFilePicker
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import java.awt.Frame
import java.io.File

enum class ExportFormat {
    Directory,
    ZipArchive
}

/**
 * Jewel-styled modal dialog for exporting the M3C project as a runnable Android Studio Gradle project.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportProjectDialog(
    viewModel: StudioViewModel,
    parentFrame: Frame? = null,
    onDismiss: () -> Unit
) {
    var projectName by remember { mutableStateOf(viewModel.projectName) }
    var packageName by remember { mutableStateOf(viewModel.packageName) }
    var exportFormat by remember { mutableStateOf(ExportFormat.Directory) }
    var destinationPath by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clickable(enabled = false) {}
                .width(520.dp)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(StudioColors.PanelSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(8.dp))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = StudioColors.Primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Export Standalone Project",
                                style = StudioTypography.ModalTitle
                            )
                            Text(
                                text = "Generate a runnable Android Studio Gradle project",
                                style = StudioTypography.Caption.copy(color = StudioColors.TextSecondary)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = StudioColors.TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onDismiss)
                    )
                }

                // Project Configuration Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Project Name",
                            style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                        )
                        JewelTextField(
                            value = projectName,
                            onValueChange = {
                                projectName = it
                                validationError = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Package Name",
                            style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                        )
                        JewelTextField(
                            value = packageName,
                            onValueChange = {
                                packageName = it
                                validationError = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Export Format Segmented Toggle
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Export Format",
                        style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioColors.ActiveSurface)
                            .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FormatOptionTab(
                            icon = Icons.Default.Folder,
                            title = "Directory (Folder)",
                            subtitle = "Target folder for IDE",
                            isSelected = exportFormat == ExportFormat.Directory,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                exportFormat = ExportFormat.Directory
                                destinationPath = ""
                                validationError = null
                            }
                        )

                        FormatOptionTab(
                            icon = Icons.Default.Archive,
                            title = "ZIP Archive (.zip)",
                            subtitle = "Standalone archive",
                            isSelected = exportFormat == ExportFormat.ZipArchive,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                exportFormat = ExportFormat.ZipArchive
                                destinationPath = ""
                                validationError = null
                            }
                        )
                    }
                }

                // Destination Path with Browse Button
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (exportFormat == ExportFormat.Directory) "Destination Directory" else "Destination ZIP File",
                        style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JewelTextField(
                            value = destinationPath,
                            onValueChange = {
                                destinationPath = it
                                validationError = null
                            },
                            placeholder = {
                                Text(
                                    text = if (exportFormat == ExportFormat.Directory) "Select or enter directory path..." else "Select or enter .zip path...",
                                    style = StudioTypography.Caption.copy(color = StudioColors.TextMuted)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedButton(
                            onClick = {
                                if (exportFormat == ExportFormat.Directory) {
                                    val chosen = DesktopFilePicker.chooseDirectory(parentFrame)
                                    if (chosen != null) {
                                        destinationPath = chosen.absolutePath
                                        validationError = null
                                    }
                                } else {
                                    val defaultName = if (projectName.isNotBlank()) projectName else "Project"
                                    val chosen = DesktopFilePicker.chooseSaveZipFile("$defaultName.zip", parentFrame)
                                    if (chosen != null) {
                                        destinationPath = chosen.absolutePath
                                        validationError = null
                                    }
                                }
                            }
                        ) {
                            Text("Browse...")
                        }
                    }
                }

                // Included Screens Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioColors.CardSurface)
                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Screens Included (${viewModel.screens.size}):",
                            style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Gradle 8.9 • Kotlin 2.1.0 • AGP 8.7.3",
                            style = StudioTypography.Badge.copy(color = StudioColors.TextMuted, fontSize = 9.sp)
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.screens.forEach { screen ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StudioColors.ActiveSurface)
                                    .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = StudioColors.Primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = screen.name,
                                    style = StudioTypography.Caption.copy(fontWeight = FontWeight.Medium)
                                )
                                if (screen.isStartDestination) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Start Destination",
                                        tint = StudioColors.Warning,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Validation Error Banner
                validationError?.let { err ->
                    Text(
                        text = err,
                        style = StudioTypography.Caption.copy(color = StudioColors.Error, fontWeight = FontWeight.SemiBold)
                    )
                }

                // Dialog Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    DefaultButton(
                        onClick = {
                            if (projectName.isBlank()) {
                                validationError = "Project name cannot be empty."
                                return@DefaultButton
                            }
                            if (packageName.isBlank() || !packageName.contains(".")) {
                                validationError = "Package name must be valid (e.g., com.example.app)."
                                return@DefaultButton
                            }
                            if (destinationPath.isBlank()) {
                                validationError = "Please select an export destination."
                                return@DefaultButton
                            }

                            val targetFile = File(destinationPath.trim())
                            val success = when (exportFormat) {
                                ExportFormat.Directory -> {
                                    viewModel.exportProjectToDirectory(
                                        targetDir = targetFile,
                                        customName = projectName.trim(),
                                        customPackage = packageName.trim()
                                    )
                                }
                                ExportFormat.ZipArchive -> {
                                    val zipFile = if (targetFile.name.endsWith(".zip", ignoreCase = true)) {
                                        targetFile
                                    } else {
                                        File(targetFile.parentFile ?: File("."), "${targetFile.name}.zip")
                                    }
                                    viewModel.exportProjectToZip(
                                        zipFile = zipFile,
                                        customName = projectName.trim(),
                                        customPackage = packageName.trim()
                                    )
                                }
                            }

                            if (success) {
                                onDismiss()
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                            Text("Export Project")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatOptionTab(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isSelected) StudioColors.CardSurface else Color.Transparent
    val borderCol = if (isSelected) StudioColors.Primary else Color.Transparent
    val tint = if (isSelected) StudioColors.Primary else StudioColors.TextSecondary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(StudioSizes.IconMedium)
        )
        Column {
            Text(
                text = title,
                style = StudioTypography.Caption.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) StudioColors.TextPrimary else StudioColors.TextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = StudioTypography.Badge.copy(
                    color = StudioColors.TextMuted,
                    fontSize = 9.sp
                )
            )
        }
    }
}
