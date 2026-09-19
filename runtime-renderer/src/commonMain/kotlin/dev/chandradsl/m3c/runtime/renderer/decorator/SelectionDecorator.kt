package dev.chandradsl.m3c.runtime.renderer.decorator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.TreeDropPosition

val LocalCanvasContainerBoundsReporter = compositionLocalOf<((NodeId, String, Rect) -> Unit)?> { null }
val LocalCanvasContainerBoundsUnregister = compositionLocalOf<((NodeId) -> Unit)?> { null }
val LocalHoveredCanvasParentId = compositionLocalOf<NodeId?> { null }

val LocalCanvasNodeBoundsReporter = compositionLocalOf<((NodeId, String, Rect, String?) -> Unit)?> { null }
val LocalCanvasNodeBoundsUnregister = compositionLocalOf<((NodeId) -> Unit)?> { null }
val LocalCanvasParentContainerType = compositionLocalOf<String?> { null }
val LocalCanvasRootNodeId = compositionLocalOf<NodeId?> { null }

val LocalCanvasDragStart = compositionLocalOf<((NodeId, Offset) -> Unit)?> { null }
val LocalCanvasDragDelta = compositionLocalOf<((Offset) -> Unit)?> { null }
val LocalCanvasDragEnd = compositionLocalOf<(() -> Unit)?> { null }
val LocalCanvasDragCancel = compositionLocalOf<(() -> Unit)?> { null }

val LocalActiveCanvasDragNodeId = compositionLocalOf<NodeId?> { null }
val LocalCanvasDropTargetId = compositionLocalOf<NodeId?> { null }
val LocalCanvasDropPosition = compositionLocalOf<TreeDropPosition?> { null }

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
    val unregister = LocalCanvasContainerBoundsUnregister.current
    val hoveredCanvasParentId = LocalHoveredCanvasParentId.current

    val nodeBoundsReporter = LocalCanvasNodeBoundsReporter.current
    val nodeBoundsUnregister = LocalCanvasNodeBoundsUnregister.current
    val parentLayout = LocalCanvasParentContainerType.current
    val rootNodeId = LocalCanvasRootNodeId.current
    val isRootNode = nodeId == rootNodeId

    val activeDragNodeId = LocalActiveCanvasDragNodeId.current
    val canvasDropTargetId = LocalCanvasDropTargetId.current
    val canvasDropPosition = LocalCanvasDropPosition.current
    val onDragStart = LocalCanvasDragStart.current
    val onDragDelta = LocalCanvasDragDelta.current
    val onDragEnd = LocalCanvasDragEnd.current

    var currentCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val isBeingDragged = activeDragNodeId == nodeId
    val isHoveredDropTarget = (hoveredCanvasParentId == nodeId) ||
            (canvasDropTargetId == nodeId && canvasDropPosition == TreeDropPosition.INSIDE)
    val isRelativeDropTarget = canvasDropTargetId == nodeId && canvasDropPosition?.isRelative == true

    val targetBorderColor = when {
        isHoveredDropTarget -> Color(0xFF499C54) // Bright emerald / success green
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }
    val targetBorderWidth = when {
        isHoveredDropTarget -> 2.5.dp
        isSelected -> 2.dp
        else -> 1.dp
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = targetBorderWidth,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )
    val animatedDropTintAlpha by animateFloatAsState(
        targetValue = if (isHoveredDropTarget) 0.08f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )

    val shouldIntercept = drillDownOnlyWhenSelected && !isSelected && !isChildSelected

    DisposableEffect(nodeId) {
        onDispose {
            unregister?.invoke(nodeId)
            nodeBoundsUnregister?.invoke(nodeId)
        }
    }

    val dragModifier = if (!isInteractiveMode && !isRootNode && onDragStart != null) {
        Modifier.pointerInput(nodeId) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var dragStarted = false
                var totalMovement = Offset.Zero
                val touchSlop = viewConfiguration.touchSlop

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        if (dragStarted) {
                            onDragEnd?.invoke()
                        }
                        break
                    }
                    val currentPos = change.position
                    val previousPos = change.previousPosition
                    val delta = currentPos - previousPos
                    if (!dragStarted) {
                        totalMovement += delta
                        if (totalMovement.getDistance() > touchSlop) {
                            dragStarted = true
                            change.consume()
                            val windowPos = currentCoords?.localToWindow(currentPos) ?: currentPos
                            onDragStart(nodeId, windowPos)
                        }
                    } else {
                        change.consume()
                        val curWindow = currentCoords?.localToWindow(currentPos) ?: currentPos
                        val prevWindow = currentCoords?.localToWindow(previousPos) ?: previousPos
                        val windowDelta = curWindow - prevWindow
                        onDragDelta?.invoke(windowDelta)
                    }
                }
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(dragModifier)
            .graphicsLayer {
                alpha = if (isBeingDragged) 0.45f else 1f
            }
            .onGloballyPositioned { coords ->
                currentCoords = coords
                if (!isInteractiveMode) {
                    reporter?.invoke(nodeId, nodeTag, coords.boundsInWindow())
                    nodeBoundsReporter?.invoke(nodeId, nodeTag, coords.boundsInWindow(), parentLayout)
                }
            }
            .background(Color(0xFF499C54).copy(alpha = animatedDropTintAlpha))
            .border(
                width = animatedBorderWidth,
                color = animatedBorderColor,
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

        // Non-intrusive Overlay: sized strictly to match content bounds so badges never expand parent or cause layout shift
        Box(modifier = Modifier.matchParentSize()) {
            // Sibling Drop Indicator Line (ABOVE / BELOW)
            if (isRelativeDropTarget) {
                val isRow = parentLayout == "Row"
                val isAbove = canvasDropPosition == TreeDropPosition.ABOVE
                val alignment = when {
                    isRow && isAbove -> Alignment.CenterStart
                    isRow && !isAbove -> Alignment.CenterEnd
                    !isRow && isAbove -> Alignment.TopCenter
                    else -> Alignment.BottomCenter
                }
                val indicatorModifier = if (isRow) {
                    Modifier
                        .align(alignment)
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF388BFD))
                } else {
                    Modifier
                        .align(alignment)
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF388BFD))
                }
                Box(modifier = indicatorModifier)
            }

            // Selection Tag pill in the top-start corner when selected (smooth animated entrance/exit)
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.85f),
                exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .wrapContentSize(align = Alignment.TopStart, unbounded = true)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = nodeTag,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        maxLines = 1
                    )
                }
            }

            // Drop Target Indicator Badge in top-end corner when hovered during drag & drop
            AnimatedVisibility(
                visible = isHoveredDropTarget,
                enter = fadeIn(tween(140)) + slideInVertically(tween(140)) { -it / 2 } + scaleIn(initialScale = 0.85f),
                exit = fadeOut(tween(100)) + slideOutVertically(tween(100)) { -it / 2 } + scaleOut(targetScale = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .wrapContentSize(align = Alignment.TopEnd, unbounded = true)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomStart = 6.dp))
                        .background(Color(0xFF499C54))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "↳ Insert into $nodeTag",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}