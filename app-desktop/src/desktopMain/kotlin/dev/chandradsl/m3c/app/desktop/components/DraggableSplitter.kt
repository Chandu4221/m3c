package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.awt.Cursor

enum class SplitterOrientation {
    Vertical,   // Divider runs vertically, resizes horizontally (left/right)
    Horizontal  // Divider runs horizontally, resizes vertically (up/down)
}

@Composable
fun DraggableSplitter(
    orientation: SplitterOrientation,
    onDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
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
        isDragging -> Color(0xFFCBA6F7) // Active dragging purple
        isHovered -> Color(0xFF89B4FA)  // Hover accent blue
        else -> Color(0xFF313244)        // Subtle idle border
    }

    val sizeModifier = if (orientation == SplitterOrientation.Vertical) {
        Modifier
            .width(5.dp)
            .fillMaxHeight()
    } else {
        Modifier
            .fillMaxWidth()
            .height(5.dp)
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .background(activeColor)
            .hoverable(interactionSource = interactionSource)
            .pointerHoverIcon(cursor)
            .pointerInput(orientation) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaPx = if (orientation == SplitterOrientation.Vertical) dragAmount.x else dragAmount.y
                        val deltaDp = with(density) { deltaPx.toDp().value }
                        onDelta(deltaDp)
                    }
                )
            }
    )
}
