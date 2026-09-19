package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.TreeDropPosition

class DragController {

    // 1. Palette Drag & Drop (Palette -> Canvas)
    var activeDragItem: DraggedPaletteItem? by mutableStateOf(null)
    var dragPointerOffset: Offset by mutableStateOf(Offset.Zero)
    var isCanvasDropHovered: Boolean by mutableStateOf(false)
    var canvasBoundsInWindow: Rect by mutableStateOf(Rect.Zero)

    var hoveredCanvasParentId: NodeId? by mutableStateOf(null)
    var hoveredCanvasParentName: String? by mutableStateOf(null)

    private val canvasContainerBounds = mutableMapOf<NodeId, Pair<Rect, String>>()

    fun registerCanvasContainerBounds(nodeId: NodeId, name: String, bounds: Rect, rootNodeId: NodeId) {
        canvasContainerBounds[nodeId] = Pair(bounds, name)
        if (activeDragItem != null && isCanvasDropHovered) {
            updateCanvasHoveredParent(rootNodeId)
        }
    }

    fun unregisterCanvasContainerBounds(nodeId: NodeId, rootNodeId: NodeId) {
        canvasContainerBounds.remove(nodeId)
        if (hoveredCanvasParentId == nodeId) {
            updateCanvasHoveredParent(rootNodeId)
        }
    }

    fun updateCanvasHoveredParent(rootNodeId: NodeId) {
        if (activeDragItem == null || !isCanvasDropHovered) {
            hoveredCanvasParentId = null
            hoveredCanvasParentName = null
            return
        }

        val containingContainers = canvasContainerBounds.filter { (_, entry) ->
            entry.first.contains(dragPointerOffset)
        }

        if (containingContainers.isNotEmpty()) {
            val best = containingContainers.minByOrNull { it.value.first.width * it.value.first.height }
            if (best != null) {
                hoveredCanvasParentId = best.key
                hoveredCanvasParentName = best.value.second
                return
            }
        }

        if (canvasContainerBounds.isNotEmpty()) {
            val nearest = canvasContainerBounds.minByOrNull { (_, entry) ->
                val center = entry.first.center
                val dx = center.x - dragPointerOffset.x
                val dy = center.y - dragPointerOffset.y
                dx * dx + dy * dy
            }
            if (nearest != null) {
                hoveredCanvasParentId = nearest.key
                hoveredCanvasParentName = nearest.value.second
                return
            }
        }

        hoveredCanvasParentId = rootNodeId
        hoveredCanvasParentName = "Screen"
    }

    fun startPaletteDrag(item: DraggedPaletteItem, initialOffset: Offset, rootNodeId: NodeId) {
        activeDragItem = item
        dragPointerOffset = initialOffset
        isCanvasDropHovered = canvasBoundsInWindow.contains(initialOffset)
        updateCanvasHoveredParent(rootNodeId)
    }

    fun updatePaletteDrag(delta: Offset, rootNodeId: NodeId) {
        val newOffset = dragPointerOffset + delta
        dragPointerOffset = newOffset
        isCanvasDropHovered = canvasBoundsInWindow.contains(newOffset)
        updateCanvasHoveredParent(rootNodeId)
    }

    fun updateCanvasBounds(bounds: Rect) {
        canvasBoundsInWindow = bounds
    }

    fun endPaletteDrag(): DraggedPaletteItem? {
        val item = activeDragItem
        activeDragItem = null
        isCanvasDropHovered = false
        hoveredCanvasParentId = null
        hoveredCanvasParentName = null
        return item
    }

    fun cancelPaletteDrag() {
        activeDragItem = null
        isCanvasDropHovered = false
        hoveredCanvasParentId = null
        hoveredCanvasParentName = null
    }

    // 2. Tree Drag & Drop (Hierarchy Reordering / Reparenting)
    var activeTreeDragNode: DraggedTreeNode? by mutableStateOf(null)
    var treeDropTargetId: NodeId? by mutableStateOf(null)
    var treeDropPosition: TreeDropPosition? by mutableStateOf(null)

    fun startTreeDrag(nodeId: NodeId, label: String, initialOffset: Offset) {
        activeTreeDragNode = DraggedTreeNode(nodeId, label)
        dragPointerOffset = initialOffset
        treeDropTargetId = null
        treeDropPosition = null
    }

    fun updateTreeDrag(delta: Offset) {
        dragPointerOffset += delta
    }

    fun updateTreeDropTarget(targetId: NodeId?, position: TreeDropPosition?, isValid: Boolean) {
        if (targetId == null || position == null || !isValid) {
            treeDropTargetId = null
            treeDropPosition = null
        } else {
            treeDropTargetId = targetId
            treeDropPosition = position
        }
    }

    fun cancelTreeDrag() {
        activeTreeDragNode = null
        treeDropTargetId = null
        treeDropPosition = null
    }

    // 3. Modifier Drag & Drop (Inspector Reordering)
    var activeModifierDrag: DraggedModifier? by mutableStateOf(null)
    var modifierDropTargetIndex: Int? by mutableStateOf(null)

    fun startModifierDrag(modifier: DraggedModifier, initialOffset: Offset) {
        activeModifierDrag = modifier
        dragPointerOffset = initialOffset
        modifierDropTargetIndex = modifier.fromIndex
    }

    fun updateModifierDrag(delta: Offset, targetIndex: Int?) {
        dragPointerOffset += delta
        modifierDropTargetIndex = targetIndex
    }

    fun cancelModifierDrag() {
        activeModifierDrag = null
        modifierDropTargetIndex = null
    }

    // 4. On-Canvas Drag & Drop Reordering
    var activeCanvasDragNodeId: NodeId? by mutableStateOf(null)
    var canvasDropTargetId: NodeId? by mutableStateOf(null)
    var canvasDropPosition: TreeDropPosition? by mutableStateOf(null)

    private val _canvasNodeBounds = mutableMapOf<NodeId, CanvasNodeInfo>()
    val allCanvasNodes: Map<NodeId, CanvasNodeInfo> get() = _canvasNodeBounds

    fun registerCanvasNodeBounds(nodeId: NodeId, tag: String, bounds: Rect, parentLayout: String?) {
        _canvasNodeBounds[nodeId] = CanvasNodeInfo(nodeId, tag, bounds, parentLayout)
    }

    fun unregisterCanvasNodeBounds(nodeId: NodeId) {
        _canvasNodeBounds.remove(nodeId)
        if (canvasDropTargetId == nodeId) {
            canvasDropTargetId = null
            canvasDropPosition = null
        }
    }

    fun startCanvasDrag(nodeId: NodeId, initialOffset: Offset) {
        activeCanvasDragNodeId = nodeId
        dragPointerOffset = initialOffset
        canvasDropTargetId = null
        canvasDropPosition = null
    }

    fun updateCanvasDrag(delta: Offset) {
        dragPointerOffset += delta
    }

    fun setCanvasDropTarget(targetId: NodeId?, position: TreeDropPosition?) {
        canvasDropTargetId = targetId
        canvasDropPosition = position
    }

    fun cancelCanvasDrag() {
        activeCanvasDragNodeId = null
        canvasDropTargetId = null
        canvasDropPosition = null
    }
}

data class CanvasNodeInfo(
    val nodeId: NodeId,
    val tag: String,
    val bounds: Rect,
    val parentLayout: String? = null
)
