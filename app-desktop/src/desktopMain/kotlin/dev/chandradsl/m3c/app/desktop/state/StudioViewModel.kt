package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.codegen.ComposeCodeGenerator
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import dev.chandradsl.m3c.core.domain.scope.childScope
import dev.chandradsl.m3c.core.domain.store.TreeMutator
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.core.domain.store.WorkspaceStore

data class DraggedPaletteItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val factory: () -> ComposableNode
)

data class DraggedTreeNode(
    val nodeId: NodeId,
    val label: String
)

data class DraggedModifier(
    val nodeId: NodeId,
    val fromIndex: Int,
    val name: String,
    val summary: String
)

enum class TreeDropPosition {
    INSIDE, ABOVE, BELOW
}

enum class LeftDrawerTab {
    Palette,
    Hierarchy
}

enum class DevicePreset(val label: String, val width: Dp, val height: Dp) {
    PhonePortrait("Phone Portrait", 390.dp, 844.dp),
    PhoneLandscape("Phone Landscape", 844.dp, 390.dp),
    Tablet("Tablet", 800.dp, 1280.dp),
    Desktop("Desktop", 1024.dp, 768.dp)
}

class StudioViewModel {

    // 1. Initial starter screen (A clean Material 3 Scaffold with TopAppBar, FAB & Column)
    private val initialRoot: ComposableNode = ComposableNode.ScaffoldNode(
        id = NodeId("scaffold_root"),
        topBar = ComposableNode.TopAppBarNode(
            id = NodeId("top_bar"),
            title = ComposableNode.TextNode(
                id = NodeId("title_txt"),
                text = "My M3 Screen",
                typography = TypographyToken.TitleLarge
            ),
            containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
        ),
        floatingActionButton = ComposableNode.FloatingActionButtonNode(
            id = NodeId("main_fab"),
            shape = ShapeDef.Token(ShapeToken.Large),
            containerColor = ColorSource.Theme(ColorToken.PrimaryContainer),
            content = listOf(ComposableNode.TextNode(text = "+"))
        ),
        content = ComposableNode.ColumnNode(
            id = NodeId("main_content_col"),
            modifiers = listOf(
                ModifierDef.FillMaxSize(),
                ModifierDef.Padding.all(DpVal(16f))
            ),
            children = listOf(
                ComposableNode.TextNode(
                    id = NodeId("welcome_txt"),
                    text = "Welcome to Material 3 Studio!",
                    typography = TypographyToken.HeadlineSmall
                )
            )
        )
    )

    private val store = WorkspaceStore(initialRoot)

    // 2. Reactive Workspace State
    var workspaceState: WorkspaceState by mutableStateOf(store.state)
        private set

    // 3. Studio UI Settings
    var isDarkMode: Boolean by mutableStateOf(true)
    var isInteractiveMode: Boolean by mutableStateOf(false)
        private set
    var isCodeDrawerOpen: Boolean by mutableStateOf(false)
    var leftDrawerTab: LeftDrawerTab by mutableStateOf(LeftDrawerTab.Palette)

    // 4. Resizable Panel Dimensions
    var leftPanelWidth: Dp by mutableStateOf(260.dp)
    var rightPanelWidth: Dp by mutableStateOf(300.dp)
    var codeDrawerHeight: Dp by mutableStateOf(260.dp)

    fun resizeLeftPanel(deltaDp: Float) {
        val newWidth = (leftPanelWidth.value + deltaDp).coerceIn(180f, 500f)
        leftPanelWidth = newWidth.dp
    }

    fun resizeRightPanel(deltaDp: Float) {
        val newWidth = (rightPanelWidth.value + deltaDp).coerceIn(240f, 550f)
        rightPanelWidth = newWidth.dp
    }

    fun resizeCodeDrawer(deltaDp: Float) {
        val newHeight = (codeDrawerHeight.value + deltaDp).coerceIn(120f, 600f)
        codeDrawerHeight = newHeight.dp
    }

    fun updateInteractiveMode(enabled: Boolean) {
        isInteractiveMode = enabled
        if (enabled) {
            dispatch(WorkspaceIntent.SelectNode(null))
        }
    }

    // 5. Canvas Device Presets & Zoom
    var currentDevicePreset: DevicePreset by mutableStateOf(DevicePreset.PhonePortrait)
    var canvasZoom: Float by mutableStateOf(1.0f)

    fun zoomIn() {
        val next = ((canvasZoom + 0.15f) * 100).toInt() / 100f
        canvasZoom = next.coerceIn(0.4f, 2.0f)
    }

    fun zoomOut() {
        val next = ((canvasZoom - 0.15f) * 100).toInt() / 100f
        canvasZoom = next.coerceIn(0.4f, 2.0f)
    }

    fun resetZoom() {
        canvasZoom = 1.0f
    }

    fun fitToViewport() {
        canvasZoom = 0.75f
    }

    fun setDevicePreset(preset: DevicePreset) {
        currentDevicePreset = preset
    }

    // 6. Global Drag & Drop Engine (Palette -> Canvas)
    var activeDragItem: DraggedPaletteItem? by mutableStateOf(null)
    var dragPointerOffset: Offset by mutableStateOf(Offset.Zero)
    var isCanvasDropHovered: Boolean by mutableStateOf(false)
    var canvasBoundsInWindow: Rect by mutableStateOf(Rect.Zero)

    // Canvas Nearest Parent Snapping & Highlighting
    var hoveredCanvasParentId: NodeId? by mutableStateOf(null)
    var hoveredCanvasParentName: String? by mutableStateOf(null)

    private val canvasContainerBounds = mutableMapOf<NodeId, Pair<Rect, String>>()

    fun registerCanvasContainerBounds(nodeId: NodeId, name: String, bounds: Rect) {
        canvasContainerBounds[nodeId] = Pair(bounds, name)
        if (activeDragItem != null && isCanvasDropHovered) {
            updateCanvasHoveredParent()
        }
    }

    fun unregisterCanvasContainerBounds(nodeId: NodeId) {
        canvasContainerBounds.remove(nodeId)
        if (hoveredCanvasParentId == nodeId) {
            updateCanvasHoveredParent()
        }
    }

    fun isContainerTag(tag: String): Boolean = when (tag.lowercase()) {
        "column", "row", "box", "surface", "card", "elevatedcard", "outlinedcard",
        "button", "elevatedbutton", "filledtonalbutton", "outlinedbutton",
        "textbutton", "iconbutton", "fab", "floatingactionbutton", "scaffold", "navigationbar" -> true
        else -> false
    }

    private fun updateCanvasHoveredParent() {
        if (activeDragItem == null || !isCanvasDropHovered) {
            hoveredCanvasParentId = null
            hoveredCanvasParentName = null
            return
        }

        // 1. Find all registered containers enclosing the drag pointer
        val containingContainers = canvasContainerBounds.filter { (_, entry) ->
            entry.first.contains(dragPointerOffset)
        }

        if (containingContainers.isNotEmpty()) {
            // Select the innermost container (smallest area)
            val best = containingContainers.minByOrNull { it.value.first.width * it.value.first.height }
            if (best != null) {
                hoveredCanvasParentId = best.key
                hoveredCanvasParentName = best.value.second
                return
            }
        }

        // 2. If pointer is inside the canvas frame but outside any sub-container,
        // snap to the nearest container by Euclidean distance to center
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

        hoveredCanvasParentId = workspaceState.rootNode.id
        hoveredCanvasParentName = "Screen"
    }

    fun startPaletteDrag(item: DraggedPaletteItem, initialOffset: Offset) {
        if (isInteractiveMode) return
        activeDragItem = item
        dragPointerOffset = initialOffset
        isCanvasDropHovered = canvasBoundsInWindow.contains(initialOffset)
        updateCanvasHoveredParent()
    }

    fun updatePaletteDrag(delta: Offset) {
        val newOffset = dragPointerOffset + delta
        dragPointerOffset = newOffset
        isCanvasDropHovered = canvasBoundsInWindow.contains(newOffset)
        updateCanvasHoveredParent()
    }

    fun updateCanvasBounds(bounds: Rect) {
        canvasBoundsInWindow = bounds
    }

    fun endPaletteDrag() {
        val item = activeDragItem
        val targetParentId = hoveredCanvasParentId ?: run {
            val selected = selectedNode
            if (selected != null && isContainerNode(selected)) {
                selected.id
            } else if (selected != null) {
                findParentRecursive(workspaceState.rootNode, selected.id)?.id ?: workspaceState.rootNode.id
            } else {
                workspaceState.rootNode.id
            }
        }
        if (item != null && isCanvasDropHovered) {
            val newNode = item.factory()
            dispatch(WorkspaceIntent.InsertChild(parentId = targetParentId, node = newNode))
            dispatch(WorkspaceIntent.SelectNode(newNode.id))
        }
        activeDragItem = null
        isCanvasDropHovered = false
        hoveredCanvasParentId = null
        hoveredCanvasParentName = null
    }

    fun cancelPaletteDrag() {
        activeDragItem = null
        isCanvasDropHovered = false
        hoveredCanvasParentId = null
        hoveredCanvasParentName = null
    }

    // 6b. Tree Hierarchy Drag & Drop
    var activeTreeDragNode: DraggedTreeNode? by mutableStateOf(null)
    var treeDropTargetId: NodeId? by mutableStateOf(null)
    var treeDropPosition: TreeDropPosition? by mutableStateOf(null)

    fun startTreeDrag(node: ComposableNode, initialOffset: Offset, label: String) {
        if (isInteractiveMode) return
        if (node.id == workspaceState.rootNode.id) return
        activeTreeDragNode = DraggedTreeNode(node.id, label)
        dragPointerOffset = initialOffset
        treeDropTargetId = null
        treeDropPosition = null
    }

    fun updateTreeDrag(delta: Offset) {
        dragPointerOffset += delta
    }

    fun updateTreeDropTarget(targetId: NodeId?, position: TreeDropPosition?) {
        val dragged = activeTreeDragNode ?: return
        if (targetId == null || position == null) {
            treeDropTargetId = null
            treeDropPosition = null
            return
        }
        if (canDropTreeNode(dragged.nodeId, targetId, position)) {
            treeDropTargetId = targetId
            treeDropPosition = position
        } else {
            treeDropTargetId = null
            treeDropPosition = null
        }
    }

    fun endTreeDrag() {
        val dragged = activeTreeDragNode
        val targetId = treeDropTargetId
        val position = treeDropPosition
        if (dragged != null && targetId != null && position != null) {
            when (position) {
                TreeDropPosition.INSIDE -> moveNodeInto(dragged.nodeId, targetId)
                TreeDropPosition.ABOVE -> moveNodeRelative(dragged.nodeId, targetId, placeAfter = false)
                TreeDropPosition.BELOW -> moveNodeRelative(dragged.nodeId, targetId, placeAfter = true)
            }
        }
        activeTreeDragNode = null
        treeDropTargetId = null
        treeDropPosition = null
    }

    fun cancelTreeDrag() {
        activeTreeDragNode = null
        treeDropTargetId = null
        treeDropPosition = null
    }

    // 6c. Modifier Drag & Reorder
    var activeModifierDrag: DraggedModifier? by mutableStateOf(null)
    var modifierDropTargetIndex: Int? by mutableStateOf(null)

    fun startModifierDrag(nodeId: NodeId, fromIndex: Int, initialOffset: Offset, name: String, summary: String) {
        if (isInteractiveMode) return
        activeModifierDrag = DraggedModifier(nodeId, fromIndex, name, summary)
        dragPointerOffset = initialOffset
        modifierDropTargetIndex = fromIndex
    }

    fun updateModifierDrag(delta: Offset) {
        dragPointerOffset += delta
    }

    fun updateModifierDropTarget(targetIndex: Int) {
        if (activeModifierDrag != null && modifierDropTargetIndex != targetIndex) {
            modifierDropTargetIndex = targetIndex
        }
    }

    fun endModifierDrag() {
        val drag = activeModifierDrag
        val to = modifierDropTargetIndex
        if (drag != null && to != null && drag.fromIndex != to) {
            reorderModifier(drag.nodeId, drag.fromIndex, to)
        }
        activeModifierDrag = null
        modifierDropTargetIndex = null
    }

    fun cancelModifierDrag() {
        activeModifierDrag = null
        modifierDropTargetIndex = null
    }

    // 6. Slot Targeting
    var targetedSlot: Pair<NodeId, String>? by mutableStateOf(null)

    fun setTargetSlot(parentId: NodeId, slotName: String) {
        targetedSlot = Pair(parentId, slotName)
    }

    fun clearTargetSlot() {
        targetedSlot = null
    }

    // 7. Live Generated Code
    var generatedCode: String by mutableStateOf(generateCode())
        private set

    // 8. Selected Node in the AST
    val selectedNode: ComposableNode?
        get() {
            val id = workspaceState.selectedNodeId ?: return null
            return findNodeRecursive(workspaceState.rootNode, id)
        }

    fun dispatch(intent: WorkspaceIntent) {
        val oldRoot = store.state.rootNode
        store.dispatch(intent)
        workspaceState = store.state
        if (store.state.rootNode != oldRoot) {
            generatedCode = generateCode()
        }
    }

    fun insertComponent(newNode: ComposableNode) {
        if (isInteractiveMode) return
        val slot = targetedSlot
        if (slot != null) {
            dispatch(WorkspaceIntent.SetSlot(parentId = slot.first, slotName = slot.second, node = newNode))
            targetedSlot = null
            dispatch(WorkspaceIntent.SelectNode(newNode.id))
            return
        }

        val targetParentId = workspaceState.selectedNodeId ?: workspaceState.rootNode.id
        dispatch(WorkspaceIntent.InsertChild(parentId = targetParentId, node = newNode))
        dispatch(WorkspaceIntent.SelectNode(newNode.id))
    }

    fun deleteSelectedNode() {
        if (isInteractiveMode) return
        val id = workspaceState.selectedNodeId ?: return
        if (id == workspaceState.rootNode.id) return // Don't delete root
        dispatch(WorkspaceIntent.RemoveNode(id))
    }

    // 9. Tree Reordering & Context Operations
    fun canMoveUp(nodeId: NodeId): Boolean {
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return false
        val siblings = getChildrenOf(parent)
        val index = siblings.indexOfFirst { it.id == nodeId }
        return index > 0
    }

    fun canMoveDown(nodeId: NodeId): Boolean {
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return false
        val siblings = getChildrenOf(parent)
        val index = siblings.indexOfFirst { it.id == nodeId }
        return index >= 0 && index < siblings.lastIndex
    }

    fun moveNodeUp(nodeId: NodeId) {
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return
        val siblings = getChildrenOf(parent).toMutableList()
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index <= 0) return
        val item = siblings.removeAt(index)
        siblings.add(index - 1, item)
        dispatch(WorkspaceIntent.UpdateNode(updateChildrenOf(parent, siblings)))
    }

    fun moveNodeDown(nodeId: NodeId) {
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return
        val siblings = getChildrenOf(parent).toMutableList()
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index < 0 || index >= siblings.lastIndex) return
        val item = siblings.removeAt(index)
        siblings.add(index + 1, item)
        dispatch(WorkspaceIntent.UpdateNode(updateChildrenOf(parent, siblings)))
    }

    fun duplicateNode(nodeId: NodeId) {
        val node = findNodeRecursive(workspaceState.rootNode, nodeId) ?: return
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return
        val cloned = cloneWithNewIds(node)
        val siblings = getChildrenOf(parent)
        val index = siblings.indexOfFirst { it.id == nodeId }
        val insertIndex = if (index >= 0) index + 1 else -1
        dispatch(WorkspaceIntent.InsertChild(parentId = parent.id, node = cloned, index = insertIndex))
        dispatch(WorkspaceIntent.SelectNode(cloned.id))
    }

    fun wrapInContainer(nodeId: NodeId, containerType: String) {
        val node = findNodeRecursive(workspaceState.rootNode, nodeId) ?: return
        if (node.id == workspaceState.rootNode.id) return
        val newContainer = when (containerType) {
            "Row" -> ComposableNode.RowNode(children = listOf(node))
            "Box" -> ComposableNode.BoxNode(children = listOf(node))
            else -> ComposableNode.ColumnNode(children = listOf(node))
        }
        dispatch(WorkspaceIntent.UpdateNode(newContainer))
        dispatch(WorkspaceIntent.SelectNode(newContainer.id))
    }

    // Hierarchical Drag-and-Drop & Reparenting Operations
    fun isContainerNode(node: ComposableNode): Boolean = when (node) {
        is ComposableNode.ColumnNode,
        is ComposableNode.RowNode,
        is ComposableNode.BoxNode,
        is ComposableNode.SurfaceNode,
        is ComposableNode.CardNode,
        is ComposableNode.ElevatedCardNode,
        is ComposableNode.OutlinedCardNode,
        is ComposableNode.ButtonNode,
        is ComposableNode.ElevatedButtonNode,
        is ComposableNode.FilledTonalButtonNode,
        is ComposableNode.OutlinedButtonNode,
        is ComposableNode.TextButtonNode,
        is ComposableNode.IconButtonNode,
        is ComposableNode.FloatingActionButtonNode,
        is ComposableNode.NavigationBarNode -> true
        else -> false
    }

    fun isDescendant(root: ComposableNode, ancestorId: NodeId, candidateId: NodeId): Boolean {
        val ancestor = findNodeRecursive(root, ancestorId) ?: return false
        return findNodeRecursive(ancestor, candidateId) != null
    }

    fun canDropTreeNode(sourceId: NodeId, targetId: NodeId, position: TreeDropPosition): Boolean {
        if (sourceId == targetId) return false
        if (sourceId == workspaceState.rootNode.id) return false
        if (isDescendant(workspaceState.rootNode, sourceId, targetId)) return false

        return when (position) {
            TreeDropPosition.INSIDE -> {
                val targetNode = findNodeRecursive(workspaceState.rootNode, targetId) ?: return false
                isContainerNode(targetNode)
            }
            TreeDropPosition.ABOVE, TreeDropPosition.BELOW -> {
                if (targetId == workspaceState.rootNode.id) return false
                val targetParent = findParentRecursive(workspaceState.rootNode, targetId) ?: return false
                targetParent.id != sourceId && !isDescendant(workspaceState.rootNode, sourceId, targetParent.id)
            }
        }
    }

    fun moveNodeInto(sourceId: NodeId, targetContainerId: NodeId, insertIndex: Int = -1) {
        if (sourceId == targetContainerId) return
        if (sourceId == workspaceState.rootNode.id) return
        if (isDescendant(workspaceState.rootNode, sourceId, targetContainerId)) return

        val sourceNode = findNodeRecursive(workspaceState.rootNode, sourceId) ?: return
        val targetNode = findNodeRecursive(workspaceState.rootNode, targetContainerId) ?: return
        if (!isContainerNode(targetNode)) return

        val treeWithoutSource = TreeMutator.removeNode(workspaceState.rootNode, sourceId) ?: return
        val finalTree = TreeMutator.insertChild(treeWithoutSource, targetContainerId, sourceNode, insertIndex)

        dispatch(WorkspaceIntent.UpdateNode(finalTree))
        dispatch(WorkspaceIntent.SelectNode(sourceId))
    }

    fun moveNodeRelative(sourceId: NodeId, targetNodeId: NodeId, placeAfter: Boolean) {
        if (sourceId == targetNodeId) return
        if (sourceId == workspaceState.rootNode.id || targetNodeId == workspaceState.rootNode.id) return

        val targetParent = findParentRecursive(workspaceState.rootNode, targetNodeId) ?: return
        if (sourceId == targetParent.id) return
        if (isDescendant(workspaceState.rootNode, sourceId, targetParent.id)) return

        val sourceNode = findNodeRecursive(workspaceState.rootNode, sourceId) ?: return
        val treeWithoutSource = TreeMutator.removeNode(workspaceState.rootNode, sourceId) ?: return

        val newTargetParent = findParentRecursive(treeWithoutSource, targetNodeId) ?: return
        val siblings = getChildrenOf(newTargetParent)
        val targetIndex = siblings.indexOfFirst { it.id == targetNodeId }
        if (targetIndex < 0) return

        val insertIndex = if (placeAfter) targetIndex + 1 else targetIndex
        val finalTree = TreeMutator.insertChild(treeWithoutSource, newTargetParent.id, sourceNode, insertIndex)

        dispatch(WorkspaceIntent.UpdateNode(finalTree))
        dispatch(WorkspaceIntent.SelectNode(sourceId))
    }

    fun canMoveOut(nodeId: NodeId): Boolean {
        if (nodeId == workspaceState.rootNode.id) return false
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return false
        if (parent.id == workspaceState.rootNode.id) return false
        val grandParent = findParentRecursive(workspaceState.rootNode, parent.id) ?: return false
        return isContainerNode(grandParent)
    }

    fun moveNodeOut(nodeId: NodeId) {
        if (!canMoveOut(nodeId)) return
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return
        moveNodeRelative(sourceId = nodeId, targetNodeId = parent.id, placeAfter = true)
    }

    fun canMoveIn(nodeId: NodeId): Boolean {
        if (nodeId == workspaceState.rootNode.id) return false
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return false
        val siblings = getChildrenOf(parent)
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index <= 0) return false
        val prevSibling = siblings[index - 1]
        return isContainerNode(prevSibling)
    }

    fun moveNodeIn(nodeId: NodeId) {
        if (!canMoveIn(nodeId)) return
        val parent = findParentRecursive(workspaceState.rootNode, nodeId) ?: return
        val siblings = getChildrenOf(parent)
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index <= 0) return
        val prevSibling = siblings[index - 1]
        moveNodeInto(sourceId = nodeId, targetContainerId = prevSibling.id)
    }

    // 10. Modifier Reordering & Management
    fun reorderModifier(targetId: NodeId, fromIndex: Int, toIndex: Int) {
        val targetNode = findNodeRecursive(workspaceState.rootNode, targetId) ?: return
        val currentModifiers = targetNode.modifiers.toMutableList()
        if (fromIndex !in currentModifiers.indices || toIndex !in currentModifiers.indices) return
        val item = currentModifiers.removeAt(fromIndex)
        currentModifiers.add(toIndex, item)
        dispatch(WorkspaceIntent.UpdateModifiers(targetId = targetId, modifiers = currentModifiers))
    }

    fun removeModifier(targetId: NodeId, index: Int) {
        val targetNode = findNodeRecursive(workspaceState.rootNode, targetId) ?: return
        val newModifiers = targetNode.modifiers.filterIndexed { i, _ -> i != index }
        dispatch(WorkspaceIntent.UpdateModifiers(targetId = targetId, modifiers = newModifiers))
    }

    fun addModifier(targetId: NodeId, modifierDef: ModifierDef) {
        val targetNode = findNodeRecursive(workspaceState.rootNode, targetId) ?: return
        dispatch(WorkspaceIntent.UpdateModifiers(targetId = targetId, modifiers = targetNode.modifiers + modifierDef))
    }

    fun undo() = dispatch(WorkspaceIntent.Undo)
    fun redo() = dispatch(WorkspaceIntent.Redo)

    private fun generateCode(): String {
        return ComposeCodeGenerator.generateCodeString(
            packageName = "dev.chandradsl.m3c.preview",
            componentName = "MyScreen",
            rootNode = store.state.rootNode
        )
    }

    private fun getChildrenOf(node: ComposableNode): List<ComposableNode> = when (node) {
        is ComposableNode.ColumnNode -> node.children
        is ComposableNode.RowNode -> node.children
        is ComposableNode.BoxNode -> node.children
        is ComposableNode.SurfaceNode -> node.children
        is ComposableNode.CardNode -> node.content
        is ComposableNode.ElevatedCardNode -> node.content
        is ComposableNode.OutlinedCardNode -> node.content
        is ComposableNode.ButtonNode -> node.content
        is ComposableNode.ElevatedButtonNode -> node.content
        is ComposableNode.FilledTonalButtonNode -> node.content
        is ComposableNode.OutlinedButtonNode -> node.content
        is ComposableNode.TextButtonNode -> node.content
        is ComposableNode.IconButtonNode -> node.content
        is ComposableNode.FloatingActionButtonNode -> node.content
        is ComposableNode.NavigationBarNode -> node.items
        else -> emptyList()
    }

    private fun updateChildrenOf(parent: ComposableNode, newChildren: List<ComposableNode>): ComposableNode = when (parent) {
        is ComposableNode.ColumnNode -> parent.copy(children = newChildren)
        is ComposableNode.RowNode -> parent.copy(children = newChildren)
        is ComposableNode.BoxNode -> parent.copy(children = newChildren)
        is ComposableNode.SurfaceNode -> parent.copy(children = newChildren)
        is ComposableNode.CardNode -> parent.copy(content = newChildren)
        is ComposableNode.ElevatedCardNode -> parent.copy(content = newChildren)
        is ComposableNode.OutlinedCardNode -> parent.copy(content = newChildren)
        is ComposableNode.ButtonNode -> parent.copy(content = newChildren)
        is ComposableNode.ElevatedButtonNode -> parent.copy(content = newChildren)
        is ComposableNode.FilledTonalButtonNode -> parent.copy(content = newChildren)
        is ComposableNode.OutlinedButtonNode -> parent.copy(content = newChildren)
        is ComposableNode.TextButtonNode -> parent.copy(content = newChildren)
        is ComposableNode.IconButtonNode -> parent.copy(content = newChildren)
        is ComposableNode.FloatingActionButtonNode -> parent.copy(content = newChildren)
        is ComposableNode.NavigationBarNode -> parent.copy(items = newChildren)
        else -> parent
    }

    fun getParentScope(nodeId: NodeId): ContainerScope {
        val parent = TreeMutator.findParent(store.state.rootNode, nodeId)
        return parent?.childScope ?: ContainerScope.None
    }

    private fun findParentRecursive(current: ComposableNode, targetId: NodeId): ComposableNode? {
        return TreeMutator.findParent(current, targetId)
    }

    private fun cloneWithNewIds(node: ComposableNode): ComposableNode = when (node) {
        is ComposableNode.TextNode -> node.copy(id = NodeId.generate("txt"))
        is ComposableNode.TextFieldNode -> node.copy(id = NodeId.generate("input"))
        is ComposableNode.OutlinedTextFieldNode -> node.copy(id = NodeId.generate("input"))
        is ComposableNode.ButtonNode -> node.copy(id = NodeId.generate("btn"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.ElevatedButtonNode -> node.copy(id = NodeId.generate("btn"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.FilledTonalButtonNode -> node.copy(id = NodeId.generate("btn"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.OutlinedButtonNode -> node.copy(id = NodeId.generate("btn"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.TextButtonNode -> node.copy(id = NodeId.generate("btn"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.IconButtonNode -> node.copy(id = NodeId.generate("btn"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.FloatingActionButtonNode -> node.copy(id = NodeId.generate("fab"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.ColumnNode -> node.copy(id = NodeId.generate("col"), children = node.children.map { cloneWithNewIds(it) })
        is ComposableNode.RowNode -> node.copy(id = NodeId.generate("row"), children = node.children.map { cloneWithNewIds(it) })
        is ComposableNode.BoxNode -> node.copy(id = NodeId.generate("box"), children = node.children.map { cloneWithNewIds(it) })
        is ComposableNode.SurfaceNode -> node.copy(id = NodeId.generate("surf"), children = node.children.map { cloneWithNewIds(it) })
        is ComposableNode.CardNode -> node.copy(id = NodeId.generate("card"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.ElevatedCardNode -> node.copy(id = NodeId.generate("card"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.OutlinedCardNode -> node.copy(id = NodeId.generate("card"), content = node.content.map { cloneWithNewIds(it) })
        is ComposableNode.CheckboxNode -> node.copy(id = NodeId.generate("chk"))
        is ComposableNode.SwitchNode -> node.copy(id = NodeId.generate("sw"))
        is ComposableNode.RadioButtonNode -> node.copy(id = NodeId.generate("rad"))
        is ComposableNode.SliderNode -> node.copy(id = NodeId.generate("sld"))
        is ComposableNode.CircularProgressIndicatorNode -> node.copy(id = NodeId.generate("prog"))
        is ComposableNode.LinearProgressIndicatorNode -> node.copy(id = NodeId.generate("prog"))
        is ComposableNode.SpacerNode -> node.copy(id = NodeId.generate("spc"))
        is ComposableNode.HorizontalDividerNode -> node.copy(id = NodeId.generate("div"))
        is ComposableNode.VerticalDividerNode -> node.copy(id = NodeId.generate("div"))
        else -> node
    }

    private fun findNodeRecursive(current: ComposableNode, targetId: NodeId): ComposableNode? {
        if (current.id == targetId) return current

        when (current) {
            is ComposableNode.ColumnNode -> current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.RowNode -> current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.BoxNode -> current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.SurfaceNode -> current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.NavigationBarNode -> current.items.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.CardNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.ElevatedCardNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.OutlinedCardNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.ButtonNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.ElevatedButtonNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.FilledTonalButtonNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.OutlinedButtonNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.TextButtonNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.IconButtonNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.FloatingActionButtonNode -> current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            is ComposableNode.ScaffoldNode -> {
                current.topBar?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.bottomBar?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.floatingActionButton?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.content?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.TopAppBarNode -> {
                findNodeRecursive(current.title, targetId)?.let { return it }
                current.navigationIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.actions.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.TextFieldNode -> {
                current.leadingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.trailingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.OutlinedTextFieldNode -> {
                current.leadingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.trailingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.NavigationBarItemNode -> {
                findNodeRecursive(current.icon, targetId)?.let { return it }
                current.label?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            else -> Unit
        }
        return null
    }
}
