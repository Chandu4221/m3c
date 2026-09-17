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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.jewel.ui.component.Badge
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.WebAsset
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.schema.ComponentCategory
import dev.chandradsl.m3c.core.domain.schema.ComponentRegistry
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.Feedback

fun resolveComponentIcon(iconName: String): ImageVector = when (iconName) {
    "view_column" -> Icons.Default.ViewColumn
    "view_stream" -> Icons.Default.ViewStream
    "layers" -> Icons.Default.Layers
    "space_bar" -> Icons.Default.SpaceBar
    "web_asset" -> Icons.Default.WebAsset
    "crop_portrait" -> Icons.Default.CropPortrait
    "featured_play_list" -> Icons.AutoMirrored.Filled.FeaturedPlayList
    "check_box_outline_blank" -> Icons.Default.CheckBoxOutlineBlank
    "smart_button" -> Icons.Default.SmartButton
    "ads_click" -> Icons.Default.AdsClick
    "highlight" -> Icons.Default.Highlight
    "crop_landscape" -> Icons.Default.CropLandscape
    "text_format" -> Icons.Default.TextFormat
    "touch_app" -> Icons.Default.TouchApp
    "add_circle" -> Icons.Default.AddCircle
    "text_fields" -> Icons.Default.TextFields
    "edit_note" -> Icons.Default.EditNote
    "check_box" -> Icons.Default.CheckBox
    "toggle_on" -> Icons.Default.ToggleOn
    "radio_button_checked" -> Icons.Default.RadioButtonChecked
    "linear_scale" -> Icons.Default.LinearScale
    "autorenew" -> Icons.Default.Autorenew
    "horizontal_rule" -> Icons.Default.HorizontalRule
    "horizontal_distribute" -> Icons.Default.HorizontalDistribute
    "vertical_distribute" -> Icons.Default.VerticalDistribute
    "view_agenda" -> Icons.Default.ViewAgenda
    "label" -> Icons.AutoMirrored.Filled.Label
    "notifications" -> Icons.Default.CircleNotifications
    "view_sidebar" -> Icons.AutoMirrored.Filled.ViewSidebar
    "feedback" -> Icons.Default.Feedback
    else -> Icons.Default.WebAsset
}

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
        // Registry-Driven Categories & Components (Single Source of Truth)
        ComponentCategory.entries.forEach { category ->
            val definitions = ComponentRegistry.byCategory(category)
            if (definitions.isNotEmpty()) {
                PaletteCategory(
                    title = category.displayName,
                    items = definitions.map { def ->
                        PaletteItem(
                            name = def.displayName,
                            description = def.description,
                            icon = resolveComponentIcon(def.iconName),
                            factory = { def.createDefault() }
                        )
                    },
                    viewModel = viewModel
                )
            }
        }
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

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(160)) + fadeIn(tween(160)),
            exit = shrinkVertically(tween(130)) + fadeOut(tween(130))
        ) {
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
    val isBeingDragged = viewModel.activeDragItem?.name == item.name
    val cardAlpha by animateFloatAsState(if (isBeingDragged) 0.35f else 1.0f, tween(150))
    var cardPositionInWindow by remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = cardAlpha)
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
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    style = StudioTypography.Caption.copy(
                        color = if (isInteractive) StudioColors.TextMuted else StudioColors.TextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
