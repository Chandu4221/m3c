package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.command.CommandHistory
import dev.chandradsl.m3c.core.domain.command.DeleteNodeCommand
import dev.chandradsl.m3c.core.domain.command.EditorCommand
import dev.chandradsl.m3c.core.domain.command.InsertNodeCommand
import dev.chandradsl.m3c.core.domain.command.SetSlotCommand
import dev.chandradsl.m3c.core.domain.command.UpdateModifiersCommand
import dev.chandradsl.m3c.core.domain.command.UpdateNodeCommand
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.hasDescendant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkspaceStore(
    initialRoot: ComposableNode,
    private val computationDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val commandHistory = CommandHistory()
    private val intentChannel = Channel<WorkspaceIntent>(Channel.UNLIMITED)

    private val _stateFlow = MutableStateFlow(WorkspaceState(rootNode = initialRoot))
    val stateFlow: StateFlow<WorkspaceState> = _stateFlow.asStateFlow()

    var state: WorkspaceState
        get() = _stateFlow.value
        private set(value) {
            _stateFlow.value = value
        }

    fun start(scope: CoroutineScope): Job {
        return scope.launch {
            for (intent in intentChannel) {
                processIntent(intent)
            }
        }
    }

    fun dispatch(intent: WorkspaceIntent) {
        intentChannel.trySend(intent)
    }

    private suspend fun processIntent(intent: WorkspaceIntent) {
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
                val previousSelectedId = state.selectedNodeId
                val nodeBeingRemoved = TreeMutator.findNode(state.rootNode, intent.targetId)
                val selectionWasInsideRemovedSubtree = previousSelectedId != null &&
                    (previousSelectedId == intent.targetId || nodeBeingRemoved?.hasDescendant(previousSelectedId) == true)
                executeCommand(DeleteNodeCommand.create(state.rootNode, intent.targetId))
                if (selectionWasInsideRemovedSubtree) {
                    _stateFlow.update { it.copy(selectedNodeId = null) }
                }
            }
            is WorkspaceIntent.UpdateModifiers -> {
                executeCommand(UpdateModifiersCommand.create(state.rootNode, intent.targetId, intent.modifiers))
            }
            is WorkspaceIntent.SelectNode -> {
                _stateFlow.update { it.copy(selectedNodeId = intent.targetId) }
            }
            is WorkspaceIntent.Undo -> performUndo()
            is WorkspaceIntent.Redo -> performRedo()
            is WorkspaceIntent.LoadDocument -> loadDocument(intent.rootNode)
        }
    }

    private suspend fun loadDocument(newRoot: ComposableNode) {
        commandHistory.clear()
        _stateFlow.update {
            it.copy(
                rootNode = newRoot,
                selectedNodeId = null,
                canUndo = false,
                canRedo = false
            )
        }
    }

    private suspend fun executeCommand(command: EditorCommand) {
        val newRoot = withContext(computationDispatcher) {
            commandHistory.execute(command, state.rootNode)
        }
        _stateFlow.update {
            it.copy(
                rootNode = newRoot,
                canUndo = commandHistory.canUndo,
                canRedo = commandHistory.canRedo
            )
        }
    }

    private suspend fun performUndo() {
        val previousRoot = withContext(computationDispatcher) {
            commandHistory.undo(state.rootNode)
        }
        _stateFlow.update {
            it.copy(
                rootNode = previousRoot,
                canUndo = commandHistory.canUndo,
                canRedo = commandHistory.canRedo
            )
        }
    }

    private suspend fun performRedo() {
        val nextRoot = withContext(computationDispatcher) {
            commandHistory.redo(state.rootNode)
        }
        _stateFlow.update {
            it.copy(
                rootNode = nextRoot,
                canUndo = commandHistory.canUndo,
                canRedo = commandHistory.canRedo
            )
        }
    }
}