package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.allDirectChildren
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import dev.chandradsl.m3c.core.domain.scope.childScope
import dev.chandradsl.m3c.core.domain.store.TreeMutator
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.core.domain.store.WorkspaceStore

class DocumentController(initialRoot: ComposableNode) {

    private val store = WorkspaceStore(initialRoot)

    var workspaceState: WorkspaceState by mutableStateOf(store.state)
        private set

    var targetedSlot: Pair<NodeId, String>? by mutableStateOf(null)
        private set

    val selectedNode: ComposableNode?
        get() {
            val id = workspaceState.selectedNodeId ?: return null
            return TreeMutator.findNode(workspaceState.rootNode, id)
        }

    fun dispatch(intent: WorkspaceIntent, onRootMutated: (() -> Unit)? = null) {
        val oldRoot = store.state.rootNode
        store.dispatch(intent)
        workspaceState = store.state
        if (store.state.rootNode != oldRoot) {
            onRootMutated?.invoke()
        }
    }

    fun setTargetSlot(parentId: NodeId, slotName: String) {
        targetedSlot = Pair(parentId, slotName)
    }

    fun clearTargetSlot() {
        targetedSlot = null
    }

    fun insertComponent(newNode: ComposableNode) {
        val slot = targetedSlot
        if (slot != null) {
            dispatch(WorkspaceIntent.SetSlot(parentId = slot.first, slotName = slot.second, node = newNode))
            targetedSlot = null
            dispatch(WorkspaceIntent.SelectNode(newNode.id))
            return
        }

        val targetParent = selectedNode ?: workspaceState.rootNode
        val parentId = if (isContainerNode(targetParent)) {
            targetParent.id
        } else {
            TreeMutator.findParent(workspaceState.rootNode, targetParent.id)?.id ?: workspaceState.rootNode.id
        }
        dispatch(WorkspaceIntent.InsertChild(parentId = parentId, node = newNode))
        dispatch(WorkspaceIntent.SelectNode(newNode.id))
    }

    fun deleteSelectedNode() {
        val id = workspaceState.selectedNodeId ?: return
        if (id == workspaceState.rootNode.id) return
        dispatch(WorkspaceIntent.RemoveNode(id))
    }

    fun duplicateSelectedNode() {
        val selected = selectedNode ?: return
        duplicateNode(selected.id)
    }

    fun duplicateNode(nodeId: NodeId) {
        val node = TreeMutator.findNode(workspaceState.rootNode, nodeId) ?: return
        if (node.id == workspaceState.rootNode.id) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        val cloned = cloneWithNewIds(node)
        val insertIndex = if (index >= 0) index + 1 else -1
        dispatch(WorkspaceIntent.InsertChild(parentId = parent.id, node = cloned, index = insertIndex))
        dispatch(WorkspaceIntent.SelectNode(cloned.id))
    }

    fun wrapInContainer(nodeId: NodeId, containerType: String) {
        val node = TreeMutator.findNode(workspaceState.rootNode, nodeId) ?: return
        if (node.id == workspaceState.rootNode.id) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId)
        val newContainer = when (containerType) {
            "Row" -> ComposableNode.RowNode(children = listOf(node))
            "Box" -> ComposableNode.BoxNode(children = listOf(node))
            else -> ComposableNode.ColumnNode(children = listOf(node))
        }
        if (parent != null) {
            val siblings = parent.allDirectChildren
            val index = siblings.indexOfFirst { it.id == nodeId }
            dispatch(WorkspaceIntent.RemoveNode(nodeId))
            dispatch(WorkspaceIntent.InsertChild(parentId = parent.id, node = newContainer, index = index))
        } else {
            dispatch(WorkspaceIntent.UpdateNode(newContainer))
        }
        dispatch(WorkspaceIntent.SelectNode(newContainer.id))
    }

    fun wrapInColumn(nodeId: NodeId) {
        val target = TreeMutator.findNode(workspaceState.rootNode, nodeId) ?: return
        if (nodeId == workspaceState.rootNode.id) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        val newContainer = ComposableNode.ColumnNode(children = listOf(target))

        dispatch(WorkspaceIntent.RemoveNode(nodeId))
        dispatch(WorkspaceIntent.InsertChild(parentId = parent.id, node = newContainer, index = index))
        dispatch(WorkspaceIntent.SelectNode(newContainer.id))
    }

    fun wrapInRow(nodeId: NodeId) {
        val target = TreeMutator.findNode(workspaceState.rootNode, nodeId) ?: return
        if (nodeId == workspaceState.rootNode.id) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        val newContainer = ComposableNode.RowNode(children = listOf(target))

        dispatch(WorkspaceIntent.RemoveNode(nodeId))
        dispatch(WorkspaceIntent.InsertChild(parentId = parent.id, node = newContainer, index = index))
        dispatch(WorkspaceIntent.SelectNode(newContainer.id))
    }

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
        is ComposableNode.ScaffoldNode,
        is ComposableNode.NavigationBarNode,
        is ComposableNode.BottomAppBarNode,
        is ComposableNode.NavigationRailNode,
        is ComposableNode.BadgedBoxNode,
        is ComposableNode.TopAppBarNode,
        is ComposableNode.AlertDialogNode -> true
        else -> false
    }

    fun moveNodeRelative(sourceId: NodeId, targetNodeId: NodeId, placeAfter: Boolean) {
        val root = workspaceState.rootNode
        val sourceNode = TreeMutator.findNode(root, sourceId) ?: return
        val targetParent = TreeMutator.findParent(root, targetNodeId) ?: return
        val siblings = targetParent.allDirectChildren
        val targetIdx = siblings.indexOfFirst { it.id == targetNodeId }
        if (targetIdx < 0) return
        val insertIdx = if (placeAfter) targetIdx + 1 else targetIdx

        dispatch(WorkspaceIntent.RemoveNode(sourceId))
        dispatch(WorkspaceIntent.InsertChild(parentId = targetParent.id, node = sourceNode, index = insertIdx))
        dispatch(WorkspaceIntent.SelectNode(sourceId))
    }

    fun moveNodeInto(sourceId: NodeId, targetContainerId: NodeId) {
        val root = workspaceState.rootNode
        val sourceNode = TreeMutator.findNode(root, sourceId) ?: return
        val targetContainer = TreeMutator.findNode(root, targetContainerId) ?: return
        if (!isContainerNode(targetContainer)) return

        dispatch(WorkspaceIntent.RemoveNode(sourceId))
        dispatch(WorkspaceIntent.InsertChild(parentId = targetContainerId, node = sourceNode, index = -1))
        dispatch(WorkspaceIntent.SelectNode(sourceId))
    }

    fun canMoveUp(nodeId: NodeId): Boolean {
        if (nodeId == workspaceState.rootNode.id) return false
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return false
        val siblings = parent.allDirectChildren
        return siblings.indexOfFirst { it.id == nodeId } > 0
    }

    fun canMoveDown(nodeId: NodeId): Boolean {
        if (nodeId == workspaceState.rootNode.id) return false
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return false
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        return index >= 0 && index < siblings.size - 1
    }

    fun moveNodeUp(nodeId: NodeId) {
        if (!canMoveUp(nodeId)) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index <= 0) return
        moveNodeRelative(sourceId = nodeId, targetNodeId = siblings[index - 1].id, placeAfter = false)
    }

    fun moveNodeDown(nodeId: NodeId) {
        if (!canMoveDown(nodeId)) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index < 0 || index >= siblings.size - 1) return
        moveNodeRelative(sourceId = nodeId, targetNodeId = siblings[index + 1].id, placeAfter = true)
    }

    fun canMoveOut(nodeId: NodeId): Boolean {
        if (nodeId == workspaceState.rootNode.id) return false
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return false
        val grandParent = TreeMutator.findParent(workspaceState.rootNode, parent.id)
        return grandParent != null && isContainerNode(grandParent)
    }

    fun moveNodeOut(nodeId: NodeId) {
        if (!canMoveOut(nodeId)) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return
        moveNodeRelative(sourceId = nodeId, targetNodeId = parent.id, placeAfter = true)
    }

    fun canMoveIn(nodeId: NodeId): Boolean {
        if (nodeId == workspaceState.rootNode.id) return false
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return false
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index <= 0) return false
        val prevSibling = siblings[index - 1]
        return isContainerNode(prevSibling)
    }

    fun moveNodeIn(nodeId: NodeId) {
        if (!canMoveIn(nodeId)) return
        val parent = TreeMutator.findParent(workspaceState.rootNode, nodeId) ?: return
        val siblings = parent.allDirectChildren
        val index = siblings.indexOfFirst { it.id == nodeId }
        if (index <= 0) return
        val prevSibling = siblings[index - 1]
        moveNodeInto(sourceId = nodeId, targetContainerId = prevSibling.id)
    }

    fun reorderModifier(targetId: NodeId, fromIndex: Int, toIndex: Int) {
        val targetNode = TreeMutator.findNode(workspaceState.rootNode, targetId) ?: return
        val currentModifiers = targetNode.modifiers.toMutableList()
        if (fromIndex !in currentModifiers.indices || toIndex !in currentModifiers.indices) return
        val item = currentModifiers.removeAt(fromIndex)
        currentModifiers.add(toIndex, item)
        dispatch(WorkspaceIntent.UpdateModifiers(targetId = targetId, modifiers = currentModifiers))
    }

    fun removeModifier(targetId: NodeId, index: Int) {
        val targetNode = TreeMutator.findNode(workspaceState.rootNode, targetId) ?: return
        val newModifiers = targetNode.modifiers.filterIndexed { i, _ -> i != index }
        dispatch(WorkspaceIntent.UpdateModifiers(targetId = targetId, modifiers = newModifiers))
    }

    fun addModifier(targetId: NodeId, modifierDef: ModifierDef) {
        val targetNode = TreeMutator.findNode(workspaceState.rootNode, targetId) ?: return
        dispatch(WorkspaceIntent.UpdateModifiers(targetId = targetId, modifiers = targetNode.modifiers + modifierDef))
    }

    fun getParentScope(nodeId: NodeId): ContainerScope {
        val parent = TreeMutator.findParent(store.state.rootNode, nodeId)
        return parent?.childScope ?: ContainerScope.None
    }

    fun undo() = dispatch(WorkspaceIntent.Undo)
    fun redo() = dispatch(WorkspaceIntent.Redo)

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
}
