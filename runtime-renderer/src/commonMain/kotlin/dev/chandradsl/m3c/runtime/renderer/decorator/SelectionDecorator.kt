package dev.chandradsl.m3c.runtime.renderer.decorator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.core.domain.model.NodeId

val LocalCanvasContainerBoundsReporter = compositionLocalOf<((NodeId, String, Rect) -> Unit)?> { null }
val LocalHoveredCanvasParentId = compositionLocalOf<NodeId?> { null }

@Composable
fun SelectionDecorator(
    nodeId: NodeId,
    nodeTag: String,
    isSelected: Boolean,
    isInteractiveMode: Boolean = false,
    isChildSelected: Boolean = false,
    drillDownOnlyWhenSelected: Boolean = false,
    onSelect: (NodeId) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isInteractiveMode) {
        // Pure preview mode: render raw content with zero editor overhead
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val interactionSource = remember { MutableInteractionSource() }
    val reporter = LocalCanvasContainerBoundsReporter.current
    val hoveredCanvasParentId = LocalHoveredCanvasParentId.current
    val isHoveredDropTarget = hoveredCanvasParentId == nodeId

    val borderColor = when {
        isHoveredDropTarget -> Color(0xFF499C54) // Bright emerald / success green
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }
    val borderWidth = when {
        isHoveredDropTarget -> 2.5.dp
        isSelected -> 2.dp
        else -> 1.dp
    }

    val shouldIntercept = drillDownOnlyWhenSelected && !isSelected && !isChildSelected

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                reporter?.invoke(nodeId, nodeTag, coords.boundsInWindow())
            }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null // No ripple so it feels like an editor selection
            ) {
                onSelect(nodeId)
            }
    ) {
        content()

        // Two-Tier Selection Interceptor Overlay:
        // When not yet selected and no descendant is selected, intercept clicks on this container
        // so clicking any child (e.g. text/icon inside a button) selects this container first.
        if (shouldIntercept) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onSelect(nodeId)
                    }
            )
        }

        // Selection Tag pill in the top-start corner when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(bottomEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = nodeTag,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 9.sp,
                    lineHeight = 10.sp
                )
            }
        }

        // Drop Target Indicator Badge in top-end corner when hovered during drag & drop
        if (isHoveredDropTarget) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 6.dp))
                    .background(Color(0xFF499C54))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "↳ Insert into $nodeTag",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp
                )
            }
        }
    }
}