package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.command.CommandHistory
import dev.chandradsl.m3c.core.domain.command.DeleteNodeCommand
import dev.chandradsl.m3c.core.domain.command.EditorCommand
import dev.chandradsl.m3c.core.domain.command.InsertNodeCommand
import dev.chandradsl.m3c.core.domain.command.SetSlotCommand
import dev.chandradsl.m3c.core.domain.command.UpdateModifiersCommand
import dev.chandradsl.m3c.core.domain.command.UpdateNodeCommand
import dev.chandradsl.m3c.core.domain.model.ComposableNode

class WorkspaceStore(initialRoot: ComposableNode) {

    private val commandHistory = CommandHistory()

    var state: WorkspaceState = WorkspaceState(rootNode = initialRoot)
        private set

    fun dispatch(intent: WorkspaceIntent) {
        when (intent) {
            is WorkspaceIntent.InsertChild -> {
                executeCommand(InsertNodeCommand(intent.parentId, intent.node, intent.index))
            }
            is WorkspaceIntent.SetSlot -> {
                executeCommand(SetSlotCommand.create(state.rootNode, intent.parentId, intent.slotName, intent.node))
            }
            is WorkspaceIntent.UpdateNode -> {
                executeCommand(UpdateNodeCommand.create(state.rootNode, intent.node))
            }
            is WorkspaceIntent.RemoveNode -> {
                executeCommand(DeleteNodeCommand.create(state.rootNode, intent.targetId))
                if (state.selectedNodeId == intent.targetId) {
                    state = state.copy(selectedNodeId = null)
                }
            }
            is WorkspaceIntent.UpdateModifiers -> {
                executeCommand(UpdateModifiersCommand.create(state.rootNode, intent.targetId, intent.modifiers))
            }
            is WorkspaceIntent.SelectNode -> {
                state = state.copy(selectedNodeId = intent.targetId)
            }
            is WorkspaceIntent.Undo -> performUndo()
            is WorkspaceIntent.Redo -> performRedo()
        }
    }

    fun executeCommand(command: EditorCommand) {
        val newRoot = commandHistory.execute(command, state.rootNode)
        state = state.copy(
            rootNode = newRoot,
            canUndo = commandHistory.canUndo,
            canRedo = commandHistory.canRedo
        )
    }

    private fun performUndo() {
        val previousRoot = commandHistory.undo(state.rootNode)
        state = state.copy(
            rootNode = previousRoot,
            canUndo = commandHistory.canUndo,
            canRedo = commandHistory.canRedo
        )
    }

    private fun performRedo() {
        val nextRoot = commandHistory.redo(state.rootNode)
        state = state.copy(
            rootNode = nextRoot,
            canUndo = commandHistory.canUndo,
            canRedo = commandHistory.canRedo
        )
    }
}