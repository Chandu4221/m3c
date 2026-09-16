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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel

@Composable
fun StudioToolbar(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.workspaceState

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF1E1E2E))
            .border(width = 1.dp, color = Color(0xFF313244))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ====================================================================
        // Left: Branding & Current Selection Tag
        // ====================================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "m3c studio",
                color = Color(0xFFCBA6F7), // M3 lavender accent
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            state.selectedNodeId?.let { selectedId ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF313244))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Selected: ${selectedId.value}",
                        color = Color(0xFFA6ADC8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ====================================================================
        // Center: History & Mode Toggles
        // ====================================================================
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
                    .height(20.dp)
                    .width(1.dp)
                    .background(Color(0xFF45475A))
            )

            // Mode Toggle (Design vs Interactive)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF313244))
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

        // ====================================================================
        // Right: Theme, Code & Delete Actions
        // ====================================================================
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
                    activeColor = Color(0xFFF38BA8),
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
    activeColor: Color = Color(0xFFCBA6F7),
    onClick: () -> Unit
) {
    val bgColor = if (isActive) Color(0xFF45475A) else Color.Transparent
    val tintColor = when {
        !enabled -> Color(0xFF585B70)
        isActive -> activeColor
        else -> Color(0xFFCDD6F4)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = tintColor,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
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
    val bgColor = if (isActive) Color(0xFF1E1E2E) else Color.Transparent
    val tintColor = if (isActive) Color(0xFFCBA6F7) else Color(0xFFA6ADC8)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tintColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            color = tintColor,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
