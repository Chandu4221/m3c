package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.jewel.ui.component.Badge
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.state.DraggedPaletteItem
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import java.awt.Cursor
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerticalDistribute
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.WebAsset
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken

data class PaletteItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val factory: () -> ComposableNode
)

@Composable
fun ComponentPalette(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(StudioColors.PanelSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle)
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Layout Containers
        PaletteCategory(
            title = "LAYOUT CONTAINERS",
            items = listOf(
                PaletteItem("Column", "Vertical layout container", Icons.Default.ViewColumn) {
                    ComposableNode.ColumnNode(modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))))
                },
                PaletteItem("Row", "Horizontal layout container", Icons.Default.ViewStream) {
                    ComposableNode.RowNode(modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))))
                },
                PaletteItem("Box", "Freeform stacking layout", Icons.Default.Layers) {
                    ComposableNode.BoxNode()
                },
                PaletteItem("Spacer", "Flexible layout gap", Icons.Default.SpaceBar) {
                    ComposableNode.SpacerNode(modifiers = listOf(ModifierDef.Height(DpVal(16f))))
                }
            ),
            viewModel = viewModel
        )

        // 2. Surfaces & Cards
        PaletteCategory(
            title = "SURFACES & CARDS",
            items = listOf(
                PaletteItem("Surface", "Material elevation canvas", Icons.Default.WebAsset) {
                    ComposableNode.SurfaceNode()
                },
                PaletteItem("Card", "Filled container card", Icons.Default.CropPortrait) {
                    ComposableNode.CardNode(
                        modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                        content = listOf(ComposableNode.TextNode(text = "Card Content"))
                    )
                },
                PaletteItem("Elevated Card", "Card with shadow elevation", Icons.AutoMirrored.Filled.FeaturedPlayList) {
                    ComposableNode.ElevatedCardNode(
                        modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                        content = listOf(ComposableNode.TextNode(text = "Elevated Card"))
                    )
                },
                PaletteItem("Outlined Card", "Card with subtle border", Icons.Default.CheckBoxOutlineBlank) {
                    ComposableNode.OutlinedCardNode(
                        modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                        content = listOf(ComposableNode.TextNode(text = "Outlined Card"))
                    )
                }
            ),
            viewModel = viewModel
        )

        // 3. Buttons & Actions
        PaletteCategory(
            title = "BUTTONS & ACTIONS",
            items = listOf(
                PaletteItem("Button", "Filled primary action", Icons.Default.SmartButton) {
                    ComposableNode.ButtonNode(content = listOf(ComposableNode.TextNode(text = "Button")))
                },
                PaletteItem("Elevated Button", "Button with shadow elevation", Icons.Default.AdsClick) {
                    ComposableNode.ElevatedButtonNode(content = listOf(ComposableNode.TextNode(text = "Elevated")))
                },
                PaletteItem("Filled Tonal Button", "Medium emphasis tonal button", Icons.Default.Highlight) {
                    ComposableNode.FilledTonalButtonNode(content = listOf(ComposableNode.TextNode(text = "Tonal Button")))
                },
                PaletteItem("Outlined Button", "Bordered action button", Icons.Default.CropLandscape) {
                    ComposableNode.OutlinedButtonNode(content = listOf(ComposableNode.TextNode(text = "Outlined")))
                },
                PaletteItem("Text Button", "Low emphasis text button", Icons.Default.TextFormat) {
                    ComposableNode.TextButtonNode(content = listOf(ComposableNode.TextNode(text = "Text Button")))
                },
                PaletteItem("Icon Button", "Compact icon trigger", Icons.Default.TouchApp) {
                    ComposableNode.IconButtonNode(content = listOf(ComposableNode.TextNode(text = "★")))
                },
                PaletteItem("FAB", "Floating action button", Icons.Default.AddCircle) {
                    ComposableNode.FloatingActionButtonNode(
                        shape = ShapeDef.Token(ShapeToken.Large),
                        content = listOf(ComposableNode.TextNode(text = "+"))
                    )
                }
            ),
            viewModel = viewModel
        )

        // 4. Text & Inputs
        PaletteCategory(
            title = "TEXT & INPUTS",
            items = listOf(
                PaletteItem("Text", "Typography display label", Icons.Default.TextFields) {
                    ComposableNode.TextNode(text = "Label Text", typography = TypographyToken.BodyMedium)
                },
                PaletteItem("TextField", "Filled input text field", Icons.Default.EditNote) {
                    ComposableNode.TextFieldNode(label = "Input Label")
                },
                PaletteItem("Outlined TextField", "Bordered input text field", Icons.Default.EditNote) {
                    ComposableNode.OutlinedTextFieldNode(label = "Input Label")
                }
            ),
            viewModel = viewModel
        )

        // 5. Selection & Feedback
        PaletteCategory(
            title = "SELECTION & FEEDBACK",
            items = listOf(
                PaletteItem("Checkbox", "Binary multi-select control", Icons.Default.CheckBox) {
                    ComposableNode.CheckboxNode(checked = true)
                },
                PaletteItem("Switch", "Toggle state switch", Icons.Default.ToggleOn) {
                    ComposableNode.SwitchNode(checked = true)
                },
                PaletteItem("RadioButton", "Single selection option", Icons.Default.RadioButtonChecked) {
                    ComposableNode.RadioButtonNode(selected = true)
                },
                PaletteItem("Slider", "Continuous range slider", Icons.Default.LinearScale) {
                    ComposableNode.SliderNode(value = 0.5f)
                },
                PaletteItem("Circular Progress", "Radial loading spinner", Icons.Default.Autorenew) {
                    ComposableNode.CircularProgressIndicatorNode()
                },
                PaletteItem("Linear Progress", "Horizontal loading bar", Icons.Default.HorizontalRule) {
                    ComposableNode.LinearProgressIndicatorNode(progress = 0.6f)
                }
            ),
            viewModel = viewModel
        )

        // 6. Dividers
        PaletteCategory(
            title = "DIVIDERS",
            items = listOf(
                PaletteItem("Horizontal Divider", "Horizontal separating line", Icons.Default.HorizontalDistribute) {
                    ComposableNode.HorizontalDividerNode()
                },
                PaletteItem("Vertical Divider", "Vertical separating line", Icons.Default.VerticalDistribute) {
                    ComposableNode.VerticalDividerNode(modifiers = listOf(ModifierDef.Height(DpVal(24f))))
                }
            ),
            viewModel = viewModel
        )
    }
}

@Composable
private fun PaletteCategory(
    title: String,
    items: List<PaletteItem>,
    viewModel: StudioViewModel
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GroupHeader(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable { isExpanded = !isExpanded }
                .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                .padding(vertical = 2.dp),
            startComponent = {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = StudioColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            },
            endComponent = {
                Badge {
                    Text("${items.size}")
                }
            }
        )

        if (isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    PaletteComponentCard(item = item, viewModel = viewModel) {
                        viewModel.insertComponent(item.factory())
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteComponentCard(
    item: PaletteItem,
    viewModel: StudioViewModel,
    onClick: () -> Unit
) {
    val isInteractive = viewModel.isInteractiveMode
    var cardPositionInWindow by remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isInteractive) StudioColors.PanelSurface else StudioColors.CardSurface)
            .border(
                width = 1.dp,
                color = StudioColors.BorderSubtle,
                shape = RoundedCornerShape(6.dp)
            )
            .onGloballyPositioned { coordinates ->
                cardPositionInWindow = coordinates.positionInWindow()
            }
            .pointerHoverIcon(if (!isInteractive) PointerIcon(Cursor(Cursor.HAND_CURSOR)) else PointerIcon.Default)
            .then(
                if (!isInteractive) {
                    Modifier.pointerInput(item) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var isDragStarted = false
                            var totalMovement = Offset.Zero
                            val touchSlop = viewConfiguration.touchSlop

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                    if (change.changedToUp()) {
                                        if (!isDragStarted) {
                                            onClick()
                                        } else {
                                            viewModel.endPaletteDrag()
                                        }
                                        break
                                    }

                                    val dragAmount = change.position - change.previousPosition
                                    totalMovement += dragAmount

                                    if (!isDragStarted) {
                                        if (totalMovement.getDistance() > touchSlop) {
                                            isDragStarted = true
                                            val globalStart = cardPositionInWindow + change.position
                                            val dragItem = DraggedPaletteItem(
                                                name = item.name,
                                                description = item.description,
                                                icon = item.icon,
                                                factory = item.factory
                                            )
                                            viewModel.startPaletteDrag(dragItem, globalStart)
                                            change.consume()
                                        }
                                    } else {
                                        change.consume()
                                        viewModel.updatePaletteDrag(dragAmount)
                                    }
                                }
                            } finally {
                                if (isDragStarted && viewModel.activeDragItem != null) {
                                    viewModel.cancelPaletteDrag()
                                }
                            }
                        }
                    }
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = if (isInteractive) StudioColors.TextMuted else StudioColors.Primary,
                modifier = Modifier.size(StudioSizes.IconStandard)
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.name,
                    style = StudioTypography.UIBody.copy(
                        color = if (isInteractive) StudioColors.TextMuted else StudioColors.TextPrimary
                    )
                )
                Text(
                    text = item.description,
                    style = StudioTypography.Caption.copy(
                        color = if (isInteractive) StudioColors.TextMuted else StudioColors.TextSecondary
                    )
                )
            }
        }
    }
}
