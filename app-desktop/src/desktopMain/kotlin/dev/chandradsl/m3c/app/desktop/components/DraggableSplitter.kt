package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import java.awt.Cursor

enum class SplitterOrientation {
    Vertical,   // Divider runs vertically, resizes horizontally (left/right)
    Horizontal  // Divider runs horizontally, resizes vertically (up/down)
}

@Composable
fun DraggableSplitter(
    orientation: SplitterOrientation,
    onDelta: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onDoubleClick: () -> Unit = {}
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isDragging by remember { mutableStateOf(false) }

    val cursor = remember(orientation) {
        val cursorType = if (orientation == SplitterOrientation.Vertical) {
            Cursor.E_RESIZE_CURSOR
        } else {
            Cursor.N_RESIZE_CURSOR
        }
        PointerIcon(Cursor.getPredefinedCursor(cursorType))
    }

    val activeColor = when {
        isDragging -> StudioColors.Primary
        isHovered -> StudioColors.Primary.copy(alpha = 0.85f)
        else -> StudioColors.BorderSubtle
    }

    // Outer hit zone: 8dp target for easy grabbing matching IntelliJ tool windows
    val hitBoxModifier = if (orientation == SplitterOrientation.Vertical) {
        Modifier
            .width(8.dp)
            .fillMaxHeight()
    } else {
        Modifier
            .fillMaxWidth()
            .height(8.dp)
    }

    // Center divider line width: 1dp idle, 2dp when hovered or actively dragging
    val lineWidth = if (isHovered || isDragging) 2.dp else 1.dp

    Box(
        modifier = modifier
            .then(hitBoxModifier)
            .hoverable(interactionSource = interactionSource)
            .pointerHoverIcon(cursor)
            .pointerInput(orientation, onDoubleClick, onDelta) {
                var lastClickTime = 0L
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val touchSlop = viewConfiguration.touchSlop
                    var totalMovement = Offset.Zero
                    var isDrag = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUp()) {
                            if (isDrag) {
                                isDragging = false
                            } else {
                                val now = System.currentTimeMillis()
                                if (now - lastClickTime < 350L) {
                                    onDoubleClick()
                                    lastClickTime = 0L
                                } else {
                                    lastClickTime = now
                                }
                            }
                            break
                        }

                        val dragAmount = change.position - change.previousPosition
                        totalMovement += dragAmount

                        if (!isDrag) {
                            if (totalMovement.getDistance() > touchSlop) {
                                isDrag = true
                                isDragging = true
                                change.consume()
                            }
                        } else {
                            change.consume()
                            val deltaPx = if (orientation == SplitterOrientation.Vertical) dragAmount.x else dragAmount.y
                            val deltaDp = with(density) { deltaPx.toDp().value }
                            onDelta(deltaDp)
                        }
                    }
                    if (isDragging) {
                        isDragging = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual hairline separator
        Box(
            modifier = if (orientation == SplitterOrientation.Vertical) {
                Modifier
                    .width(lineWidth)
                    .fillMaxHeight()
                    .background(activeColor)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(lineWidth)
                    .background(activeColor)
            }
        )

        // Subtle grip indicator for horizontal drawer splitter
        if (orientation == SplitterOrientation.Horizontal && (isHovered || isDragging)) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(StudioColors.Primary)
            )
        }
    }
}
