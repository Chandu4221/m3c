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
import androidx.compose.material3.lightColorScheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.chandradsl.m3c.app.desktop.components.*
import dev.chandradsl.m3c.app.desktop.state.LeftDrawerTab
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

fun main() = application {
    val windowState = rememberWindowState(width = 1440.dp, height = 900.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "m3c studio — Material 3 WYSIWYG Composer",
        state = windowState
    ) {
        val viewModel = remember { StudioViewModel() }

        // JetBrains Jewel Int-UI Standalone Theme (IntelliJ New UI Dark / Light)
        IntUiTheme(isDark = viewModel.isDarkMode) {
            StudioColors.isDark = viewModel.isDarkMode

            val colorScheme = if (viewModel.isDarkMode) {
                darkColorScheme(
                    background = StudioColors.CanvasBackdrop,
                    surface = StudioColors.PanelSurface,
                    surfaceVariant = StudioColors.CardSurface,
                    primary = StudioColors.Primary,
                    onPrimary = StudioColors.TextInverse,
                    onBackground = StudioColors.TextPrimary,
                    onSurface = StudioColors.TextPrimary,
                    outline = StudioColors.BorderSubtle,
                    outlineVariant = StudioColors.BorderActive
                )
            } else {
                lightColorScheme(
                    background = StudioColors.CanvasBackdrop,
                    surface = StudioColors.PanelSurface,
                    surfaceVariant = StudioColors.CardSurface,
                    primary = StudioColors.Primary,
                    onPrimary = StudioColors.TextInverse,
                    onBackground = StudioColors.TextPrimary,
                    onSurface = StudioColors.TextPrimary,
                    outline = StudioColors.BorderSubtle,
                    outlineVariant = StudioColors.BorderActive
                )
            }

            // Material 3 bridge matching active Jewel Int-UI theme (WCAG 2.2 AA Compliant)
            MaterialTheme(colorScheme = colorScheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(StudioColors.CanvasBackdrop)
                    ) {
                        // 1. Top Studio Toolbar
                        StudioToolbar(viewModel = viewModel)

                        // 2. Central Workstation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Left Panel: Tabs for Palette & Hierarchy Tree (Resizable)
                            Column(
                                modifier = Modifier
                                    .width(viewModel.leftPanelWidth)
                                    .fillMaxHeight()
                                    .background(StudioColors.PanelSurface)
                                    .border(width = 1.dp, color = StudioColors.BorderSubtle)
                            ) {
                                // Tab Switcher Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(StudioColors.PanelSurface)
                                        .border(width = 1.dp, color = StudioColors.BorderSubtle)
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

                            // Left-to-Center Vertical Splitter
                            DraggableSplitter(
                                orientation = SplitterOrientation.Vertical,
                                onDelta = viewModel::resizeLeftPanel
                            )

                            // Center: Zoomable/Pannable Device Canvas
                            CanvasViewport(
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )

                            // Center-to-Right Vertical Splitter
                            DraggableSplitter(
                                orientation = SplitterOrientation.Vertical,
                                onDelta = { delta -> viewModel.resizeRightPanel(-delta) }
                            )

                            // Right: Two-Way Property & Modifier Inspector (Resizable)
                            PropertyInspector(
                                viewModel = viewModel
                            )
                        }

                        // 3. Bottom: Code Drawer Horizontal Splitter & Code Preview Drawer
                        if (viewModel.isCodeDrawerOpen) {
                            DraggableSplitter(
                                orientation = SplitterOrientation.Horizontal,
                                onDelta = { delta -> viewModel.resizeCodeDrawer(-delta) }
                            )
                            CodePreviewDrawer(viewModel = viewModel)
                        }
                    }

                    // 4. Global Floating Drag Avatar Overlay
                    viewModel.activeDragItem?.let { dragItem ->
                        val avatarBorder by animateColorAsState(
                            targetValue = if (viewModel.isCanvasDropHovered) StudioColors.Success else StudioColors.Primary,
                            animationSpec = tween(150)
                        )
                        val avatarIconTint by animateColorAsState(
                            targetValue = if (viewModel.isCanvasDropHovered) StudioColors.Success else StudioColors.Primary,
                            animationSpec = tween(150)
                        )

                        Box(
                            modifier = Modifier
                                .offset {
                                    androidx.compose.ui.unit.IntOffset(
                                        x = (viewModel.dragPointerOffset.x - 24).toInt(),
                                        y = (viewModel.dragPointerOffset.y - 24).toInt()
                                    )
                                }
                                .graphicsLayer(scaleX = 1.04f, scaleY = 1.04f, rotationZ = -1.5f)
                                .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioColors.CardSurface)
                                .border(
                                    width = 2.dp,
                                    color = avatarBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = dragItem.icon,
                                    contentDescription = null,
                                    tint = avatarIconTint,
                                    modifier = Modifier.size(StudioSizes.IconStandard)
                                )
                                Text(
                                    text = dragItem.name,
                                    style = StudioTypography.UIBody.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StudioColors.TextPrimary
                                    )
                                )
                            }
                        }
                    }

                    // 5. Global Floating Tree Drag Avatar Overlay
                    viewModel.activeTreeDragNode?.let { treeDrag ->
                        val treeBorder by animateColorAsState(
                            targetValue = if (viewModel.treeDropTargetId != null) StudioColors.Success else StudioColors.Primary,
                            animationSpec = tween(150)
                        )
                        val treeIconTint by animateColorAsState(
                            targetValue = if (viewModel.treeDropTargetId != null) StudioColors.Success else StudioColors.Primary,
                            animationSpec = tween(150)
                        )

                        Box(
                            modifier = Modifier
                                .offset {
                                    androidx.compose.ui.unit.IntOffset(
                                        x = (viewModel.dragPointerOffset.x + 16).toInt(),
                                        y = (viewModel.dragPointerOffset.y + 16).toInt()
                                    )
                                }
                                .graphicsLayer(scaleX = 1.04f, scaleY = 1.04f, rotationZ = -1.5f)
                                .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioColors.CardSurface)
                                .border(
                                    width = 2.dp,
                                    color = treeBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = null,
                                    tint = treeIconTint,
                                    modifier = Modifier.size(StudioSizes.IconStandard)
                                )
                                Column {
                                    Text(
                                        text = treeDrag.label,
                                        style = StudioTypography.UIBody.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StudioColors.TextPrimary
                                        )
                                    )
                                    val actionText = when (viewModel.treeDropPosition) {
                                        dev.chandradsl.m3c.app.desktop.state.TreeDropPosition.INSIDE -> "Drop inside container"
                                        dev.chandradsl.m3c.app.desktop.state.TreeDropPosition.ABOVE -> "Insert before"
                                        dev.chandradsl.m3c.app.desktop.state.TreeDropPosition.BELOW -> "Insert after"
                                        null -> "Drag to reparent or reorder"
                                    }
                                    val actionColor by animateColorAsState(
                                        targetValue = if (viewModel.treeDropTargetId != null) StudioColors.Success else StudioColors.TextSecondary,
                                        animationSpec = tween(150)
                                    )
                                    Text(
                                        text = actionText,
                                        style = StudioTypography.Caption.copy(
                                            color = actionColor
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 6. Global Floating Modifier Drag Avatar Overlay
                    viewModel.activeModifierDrag?.let { dragMod ->
                        val modBorder by animateColorAsState(
                            targetValue = if (viewModel.modifierDropTargetIndex != null) StudioColors.Success else StudioColors.Primary,
                            animationSpec = tween(150)
                        )
                        val badgeBg by animateColorAsState(
                            targetValue = if (viewModel.modifierDropTargetIndex != null) StudioColors.Success else StudioColors.Primary,
                            animationSpec = tween(150)
                        )

                        Box(
                            modifier = Modifier
                                .offset {
                                    androidx.compose.ui.unit.IntOffset(
                                        x = (viewModel.dragPointerOffset.x + 12).toInt(),
                                        y = (viewModel.dragPointerOffset.y + 12).toInt()
                                    )
                                }
                                .graphicsLayer(scaleX = 1.03f, scaleY = 1.03f, rotationZ = 1f)
                                .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioColors.CardSurface)
                                .border(
                                    width = 2.dp,
                                    color = modBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${(viewModel.modifierDropTargetIndex ?: dragMod.fromIndex) + 1}",
                                        style = StudioTypography.Badge.copy(color = StudioColors.TextInverse)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = dragMod.name,
                                        style = StudioTypography.UIBody.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StudioColors.TextPrimary
                                        )
                                    )
                                    if (dragMod.summary.isNotBlank()) {
                                        Text(
                                            text = dragMod.summary,
                                            style = StudioTypography.Caption.copy(
                                                color = StudioColors.TextSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
    val bgColor = if (isActive) StudioColors.ActiveSurface else Color.Transparent
    val tintColor = if (isActive) StudioColors.Primary else StudioColors.TextSecondary

    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tintColor,
            modifier = Modifier.size(StudioSizes.IconMedium)
        )
        Text(
            text = text,
            style = StudioTypography.UIBody.copy(
                color = tintColor,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            ),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
