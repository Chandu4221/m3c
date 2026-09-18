package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography

@Composable
fun StudioToolbar(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.workspaceState

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(StudioColors.CardSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Branding & Current Selection Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "m3c studio",
                style = StudioTypography.AppTitle
            )

            state.selectedNodeId?.let { selectedId ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Selected: ${selectedId.value}",
                        style = StudioTypography.Caption
                    )
                }
            }
        }

        // Center: History & Mode Toggles
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = "Undo",
                enabled = state.canUndo,
                onClick = { viewModel.undo() }
            )

            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.Redo,
                label = "Redo",
                enabled = state.canRedo,
                onClick = { viewModel.redo() }
            )

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(StudioColors.BorderSubtle)
            )

            // Mode Toggle (Design vs Interactive)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioColors.ActiveSurface)
                    .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                ModeTabButton(
                    icon = Icons.Default.Brush,
                    text = "Design",
                    isActive = !viewModel.isInteractiveMode,
                    onClick = { viewModel.updateInteractiveMode(false) }
                )
                ModeTabButton(
                    icon = Icons.Default.PlayArrow,
                    text = "Interactive",
                    isActive = viewModel.isInteractiveMode,
                    onClick = { viewModel.updateInteractiveMode(true) }
                )
            }
        }

        // Right: Theme, Code & Delete Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Delete Selected Node Button
            if (state.selectedNodeId != null && state.selectedNodeId != state.rootNode.id && !viewModel.isInteractiveMode) {
                ToolbarIconButton(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    enabled = true,
                    activeColor = StudioColors.Error,
                    onClick = { viewModel.deleteSelectedNode() }
                )
            }

            // Theme Toggle (Light / Dark)
            ToolbarIconButton(
                icon = if (viewModel.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                label = if (viewModel.isDarkMode) "Dark" else "Light",
                enabled = true,
                onClick = { viewModel.isDarkMode = !viewModel.isDarkMode }
            )

            // Code Preview Drawer Toggle
            ToolbarIconButton(
                icon = Icons.Default.Code,
                label = if (viewModel.isCodeDrawerOpen) "Hide Code" else "Code",
                enabled = true,
                isActive = viewModel.isCodeDrawerOpen,
                onClick = { viewModel.isCodeDrawerOpen = !viewModel.isCodeDrawerOpen }
            )
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    isActive: Boolean = false,
    activeColor: Color = StudioColors.Primary,
    onClick: () -> Unit
) {
    val targetBg = if (isActive) StudioColors.ActiveSurface else Color.Transparent
    val targetTint = when {
        !enabled -> StudioColors.TextMuted
        isActive -> activeColor
        else -> StudioColors.TextPrimary
    }
    val bgColor by animateColorAsState(targetBg, tween(150))
    val tintColor by animateColorAsState(targetTint, tween(150))

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(StudioSizes.IconMedium)
        )
        Text(
            text = label,
            style = StudioTypography.UIBody.copy(color = tintColor)
        )
    }
}

@Composable
private fun ModeTabButton(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val targetBg = if (isActive) StudioColors.CardSurface else Color.Transparent
    val targetTint = if (isActive) StudioColors.Primary else StudioColors.TextSecondary
    val bgColor by animateColorAsState(targetBg, tween(150))
    val tintColor by animateColorAsState(targetTint, tween(150))

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tintColor,
            modifier = Modifier.size(StudioSizes.IconMedium)
        )
        Text(
            text = text,
            style = StudioTypography.UIBody.copy(color = tintColor)
        )
    }
}
