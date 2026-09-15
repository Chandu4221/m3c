package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.ComposableNode

class WorkspaceStore(initialRoot: ComposableNode) {

    private val undoStack = ArrayDeque<ComposableNode>()
    private val redoStack = ArrayDeque<ComposableNode>()

    var state: WorkspaceState = WorkspaceState(rootNode = initialRoot)
        private set

    fun dispatch(intent: WorkspaceIntent) {
        when (intent) {
            is WorkspaceIntent.InsertChild -> {
                recordMutation { TreeMutator.insertChild(it, intent.parentId, intent.node, intent.index) }
            }
            is WorkspaceIntent.SetSlot -> {
                recordMutation { TreeMutator.setSlot(it, intent.parentId, intent.slotName, intent.node) }
            }
            is WorkspaceIntent.UpdateNode -> {
                recordMutation { TreeMutator.updateNode(it, intent.node) }
            }
            is WorkspaceIntent.RemoveNode -> {
                recordMutation { currentRoot ->
                    TreeMutator.removeNode(currentRoot, intent.targetId) ?: currentRoot
                }
                if (state.selectedNodeId == intent.targetId) {
                    state = state.copy(selectedNodeId = null)
                }
            }
            is WorkspaceIntent.UpdateModifiers -> {
                recordMutation { TreeMutator.updateModifiers(it, intent.targetId, intent.modifiers) }
            }
            is WorkspaceIntent.SelectNode -> {
                state = state.copy(selectedNodeId = intent.targetId)
            }
            is WorkspaceIntent.Undo -> performUndo()
            is WorkspaceIntent.Redo -> performRedo()
        }
    }

    private fun recordMutation(transform: (ComposableNode) -> ComposableNode) {
        val newRoot = transform(state.rootNode)
        if (newRoot != state.rootNode) {
            undoStack.addLast(state.rootNode)
            redoStack.clear()
            state = state.copy(
                rootNode = newRoot,
                canUndo = undoStack.isNotEmpty(),
                canRedo = false
            )
        }
    }

    private fun performUndo() {
        if (undoStack.isEmpty()) return
        val previousRoot = undoStack.removeLast()
        redoStack.addLast(state.rootNode)
        state = state.copy(
            rootNode = previousRoot,
            canUndo = undoStack.isNotEmpty(),
            canRedo = true
        )
    }

    private fun performRedo() {
        if (redoStack.isEmpty()) return
        val nextRoot = redoStack.removeLast()
        undoStack.addLast(state.rootNode)
        state = state.copy(
            rootNode = nextRoot,
            canUndo = true,
            canRedo = redoStack.isNotEmpty()
        )
    }
}