package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalCanvasContainerBoundsReporter
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalHoveredCanvasParentId
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.state.DevicePreset
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalCanvasContainerBoundsUnregister
import dev.chandradsl.m3c.runtime.renderer.renderers.NodeRenderer

@Composable
fun CanvasViewport(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.workspaceState
    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()
    val backdropInteraction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioColors.CanvasBackdrop)
    ) {
        // ====================================================================
        // 0. Screen Navigation Tab Bar
        // ====================================================================
        ScreenTabBar(viewModel = viewModel)

        // ====================================================================
        // 1. Canvas Control Bar: Presets & Zoom
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(StudioColors.PanelSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Device Preset Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DevicePresetButton(
                    preset = DevicePreset.Phone,
                    icon = Icons.Default.StayCurrentPortrait,
                    isSelected = viewModel.currentDevicePreset == DevicePreset.Phone,
                    onSelect = { viewModel.setDevicePreset(DevicePreset.Phone) }
                )
                DevicePresetButton(
                    preset = DevicePreset.Foldable,
                    icon = Icons.Default.CropLandscape,
                    isSelected = viewModel.currentDevicePreset == DevicePreset.Foldable,
                    onSelect = { viewModel.setDevicePreset(DevicePreset.Foldable) }
                )
                DevicePresetButton(
                    preset = DevicePreset.Tablet,
                    icon = Icons.Default.TabletMac,
                    isSelected = viewModel.currentDevicePreset == DevicePreset.Tablet,
                    onSelect = { viewModel.setDevicePreset(DevicePreset.Tablet) }
                )
                DevicePresetButton(
                    preset = DevicePreset.Desktop,
                    icon = Icons.Default.DesktopWindows,
                    isSelected = viewModel.currentDevicePreset == DevicePreset.Desktop,
                    onSelect = { viewModel.setDevicePreset(DevicePreset.Desktop) }
                )

                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(1.dp)
                        .background(StudioColors.BorderSubtle)
                )

                // Orientation Toggle Button
                CanvasIconButton(
                    icon = if (viewModel.isLandscape) Icons.Default.StayCurrentLandscape else Icons.Default.StayCurrentPortrait,
                    tooltip = if (viewModel.isLandscape) "Switch to Portrait" else "Switch to Landscape",
                    onClick = viewModel::toggleOrientation
                )

                // M3 Window Size Class Indicator Badge
                WindowSizeClassBadge(windowSizeClass = viewModel.currentWindowSizeClass)
            }

            // Right: Zoom Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Interactive Mode Pill
                if (viewModel.isInteractiveMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E3A2F))
                            .border(width = 1.dp, color = StudioColors.Success, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StudioColors.Success)
                        )
                        Text(
                            text = "Interactive Mode",
                            style = StudioTypography.Badge.copy(color = StudioColors.Success)
                        )
                    }
                }

                // Zoom Out Button
                CanvasIconButton(
                    icon = Icons.Default.ZoomOut,
                    tooltip = "Zoom Out",
                    onClick = viewModel::zoomOut
                )

                // Zoom Percentage Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                        .clickable(onClick = viewModel::resetZoom)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(viewModel.canvasZoom * 100).toInt()}%",
                        style = StudioTypography.Badge.copy(color = StudioColors.TextPrimary)
                    )
                }

                // Zoom In Button
                CanvasIconButton(
                    icon = Icons.Default.ZoomIn,
                    tooltip = "Zoom In",
                    onClick = viewModel::zoomIn
                )

                // Fit to Viewport Button
                CanvasIconButton(
                    icon = Icons.Default.FitScreen,
                    tooltip = "Fit to Viewport",
                    onClick = viewModel::fitToViewport
                )

                // Reset Zoom Button
                CanvasIconButton(
                    icon = Icons.Default.RestartAlt,
                    tooltip = "Reset 100%",
                    onClick = viewModel::resetZoom
                )
            }
        }

        // ====================================================================
        // Canvas Stage Area (Scrollable & Zoomable)
        // ====================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    viewModel.updateCanvasBounds(coordinates.boundsInWindow())
                }
                .clickable(
                    interactionSource = backdropInteraction,
                    indication = null
                ) {
                    if (!viewModel.isInteractiveMode) {
                        viewModel.dispatch(WorkspaceIntent.SelectNode(null))
                    }
                }
                .verticalScroll(vScrollState)
                .horizontalScroll(hScrollState),
            contentAlignment = Alignment.TopCenter
        ) {
            val isDraggingComponent = viewModel.activeDragItem != null
            val isDropHovered = viewModel.isCanvasDropHovered

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 32.dp)
                    .graphicsLayer(
                        scaleX = viewModel.canvasZoom,
                        scaleY = viewModel.canvasZoom,
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    )
            ) {
                // Drop Indicator Overlay Banner with smooth enter/exit and color transitions
                AnimatedVisibility(
                    visible = isDraggingComponent,
                    enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { -it / 2 },
                    exit = fadeOut(tween(100)) + slideOutVertically(tween(100)) { -it / 2 }
                ) {
                    val bannerBg by animateColorAsState(
                        targetValue = if (isDropHovered) StudioColors.Success.copy(alpha = 0.15f) else StudioColors.PanelSurface,
                        animationSpec = tween(150)
                    )
                    val bannerBorder by animateColorAsState(
                        targetValue = if (isDropHovered) StudioColors.Success else StudioColors.Primary,
                        animationSpec = tween(150)
                    )
                    val bannerIconTint by animateColorAsState(
                        targetValue = if (isDropHovered) StudioColors.Success else StudioColors.Primary,
                        animationSpec = tween(150)
                    )
                    val bannerTextColor by animateColorAsState(
                        targetValue = if (isDropHovered) StudioColors.Success else StudioColors.TextPrimary,
                        animationSpec = tween(150)
                    )

                    Box(
                        modifier = Modifier
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bannerBg)
                            .border(
                                width = 1.5.dp,
                                color = bannerBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isDropHovered) Icons.Default.AddCircle else Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = bannerIconTint,
                                modifier = Modifier.size(StudioSizes.IconMedium)
                            )
                            val targetName = viewModel.hoveredCanvasParentName
                            val bannerText = when {
                                !isDropHovered -> "Drag into viewport to insert"
                                targetName != null -> "Release to insert ${viewModel.activeDragItem?.name} into $targetName"
                                else -> "Release to drop ${viewModel.activeDragItem?.name} into screen"
                            }
                            Text(
                                text = bannerText,
                                style = StudioTypography.UIBody.copy(
                                    color = bannerTextColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Device Frame Label
                Text(
                    text = "${viewModel.currentDevicePreset.label} • ${if (viewModel.isLandscape) "Landscape" else "Portrait"} • ${viewModel.effectiveViewportWidth.value.toInt()} × ${viewModel.effectiveViewportHeight.value.toInt()} dp • M3 ${viewModel.currentWindowSizeClass.label}",
                    style = StudioTypography.Caption,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val targetBorderColor = when {
                    viewModel.isInteractiveMode -> StudioColors.Success.copy(alpha = 0.5f)
                    isDraggingComponent && isDropHovered -> StudioColors.Success
                    isDraggingComponent -> StudioColors.Primary.copy(alpha = 0.6f)
                    else -> StudioColors.BorderSubtle
                }
                val frameBorderColor by animateColorAsState(targetBorderColor, tween(180))
                val targetBorderWidth = if (isDraggingComponent && isDropHovered) 3.dp else 2.dp
                val frameBorderWidth by animateDpAsState(targetBorderWidth, tween(180))

                // Simulated Device Frame
                Box(
                    modifier = Modifier
                        .width(viewModel.effectiveViewportWidth)
                        .height(viewModel.effectiveViewportHeight)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = frameBorderWidth,
                            color = frameBorderColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    val colorScheme = if (viewModel.isDarkMode) darkColorScheme() else lightColorScheme()

                    MaterialTheme(colorScheme = colorScheme) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            CompositionLocalProvider(
                                LocalHoveredCanvasParentId provides viewModel.hoveredCanvasParentId,
                                LocalCanvasContainerBoundsReporter provides { id, tag, rect ->
                                    if (viewModel.isContainerTag(tag)) {
                                        viewModel.registerCanvasContainerBounds(id, tag, rect)
                                    }
                                },
                                LocalCanvasContainerBoundsUnregister provides { id ->
                                    viewModel.unregisterCanvasContainerBounds(id)
                                }
                            ) {
                                NodeRenderer(
                                    node = state.rootNode,
                                    state = if (viewModel.isInteractiveMode) state.copy(selectedNodeId = null) else state,
                                    onIntent = { intent ->
                                        if (viewModel.isInteractiveMode && intent is WorkspaceIntent.SelectNode) {
                                            return@NodeRenderer
                                        }
                                        viewModel.dispatch(intent)
                                    },
                                    isInteractiveMode = viewModel.isInteractiveMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicePresetButton(
    preset: DevicePreset,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val bgColor = if (isSelected) StudioColors.CardSurface else Color.Transparent
    val tintColor = if (isSelected) StudioColors.Primary else StudioColors.TextSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isSelected) StudioColors.BorderActive else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = preset.label,
            tint = tintColor,
            modifier = Modifier.size(StudioSizes.IconMedium)
        )
        Text(
            text = preset.label,
            style = StudioTypography.Caption.copy(
                color = tintColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun CanvasIconButton(
    icon: ImageVector,
    tooltip: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(StudioColors.ActiveSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = StudioColors.TextPrimary,
            modifier = Modifier.size(StudioSizes.IconSmall)
        )
    }
}

@Composable
private fun WindowSizeClassBadge(
    windowSizeClass: dev.chandradsl.m3c.app.desktop.state.WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val (bg, border, text) = when (windowSizeClass) {
        dev.chandradsl.m3c.app.desktop.state.WindowSizeClass.Compact -> Triple(Color(0xFF162D23), StudioColors.Success, Color(0xFF4EBE88))
        dev.chandradsl.m3c.app.desktop.state.WindowSizeClass.Medium -> Triple(Color(0xFF332610), Color(0xFFE5A83B), Color(0xFFFFD166))
        dev.chandradsl.m3c.app.desktop.state.WindowSizeClass.Expanded -> Triple(Color(0xFF231E3D), Color(0xFF8C7CFF), Color(0xFFB8AEFF))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(width = 1.dp, color = border.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(text)
        )
        Text(
            text = "${windowSizeClass.badgeText} (${windowSizeClass.rangeDescription})",
            style = StudioTypography.Badge.copy(color = text, fontWeight = FontWeight.SemiBold)
        )
    }
}
