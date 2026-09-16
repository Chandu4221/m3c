package dev.chandradsl.m3c.app.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.chandradsl.m3c.app.desktop.components.*
import dev.chandradsl.m3c.app.desktop.state.LeftDrawerTab
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel

fun main() = application {
    val windowState = rememberWindowState(width = 1440.dp, height = 900.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "m3c studio — Material 3 WYSIWYG Composer",
        state = windowState
    ) {
        val viewModel = remember { StudioViewModel() }

        MaterialTheme(
            colorScheme = darkColorScheme(
                background = Color(0xFF11111B),
                surface = Color(0xFF181825),
                primary = Color(0xFFCBA6F7),
                onBackground = Color(0xFFCDD6F4),
                onSurface = Color(0xFFCDD6F4)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF11111B))
            ) {
                // 1. Top Studio Toolbar
                StudioToolbar(viewModel = viewModel)

                // 2. Central Workstation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left Panel: Tabs for Palette & Hierarchy Tree
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF181825))
                            .border(width = 1.dp, color = Color(0xFF313244))
                    ) {
                        // Tab Switcher Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(Color(0xFF181825))
                                .border(width = 1.dp, color = Color(0xFF313244))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LeftTabButton(
                                icon = Icons.Default.Widgets,
                                text = "Palette",
                                isActive = viewModel.leftDrawerTab == LeftDrawerTab.Palette,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.leftDrawerTab = LeftDrawerTab.Palette }
                            )
                            LeftTabButton(
                                icon = Icons.Default.AccountTree,
                                text = "Tree",
                                isActive = viewModel.leftDrawerTab == LeftDrawerTab.Hierarchy,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.leftDrawerTab = LeftDrawerTab.Hierarchy }
                            )
                        }

                        // Active Tab Content
                        if (viewModel.leftDrawerTab == LeftDrawerTab.Palette) {
                            ComponentPalette(viewModel = viewModel, modifier = Modifier.weight(1f))
                        } else {
                            HierarchyTree(viewModel = viewModel, modifier = Modifier.weight(1f))
                        }
                    }

                    // Center: Zoomable/Pannable Device Canvas
                    CanvasViewport(
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )

                    // Right: Two-Way Property & Modifier Inspector
                    PropertyInspector(
                        viewModel = viewModel
                    )
                }

                // 3. Bottom: Real-Time Generated Kotlin Source Code Drawer
                CodePreviewDrawer(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun LeftTabButton(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) Color(0xFF313244) else Color.Transparent
    val tintColor = if (isActive) Color(0xFFCBA6F7) else Color(0xFF6C7086)

    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tintColor,
            modifier = Modifier.width(14.dp)
        )
        Text(
            text = text,
            color = tintColor,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}