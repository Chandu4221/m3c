package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.command.EditorCommand
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId

data class HistoryTimelineItem(
    val stepIndex: Int,
    val description: String,
    val isCurrent: Boolean = false,
    val isFuture: Boolean = false
)

data class WorkspaceState(
    val rootNode: ComposableNode,
    val selectedNodeId: NodeId? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val lastUndoDescription: String? = null,
    val nextRedoDescription: String? = null,
    val historyTimeline: List<HistoryTimelineItem> = emptyList()
)

sealed interface WorkspaceIntent {
    /** Inserts a child into a container node (Column, Row, Box, Surface, Card, Button, etc.) */
    data class InsertChild(
        val parentId: NodeId,
        val node: ComposableNode,
        val index: Int = -1
    ) : WorkspaceIntent

    /** Sets or clears a named slot on a component (e.g. Scaffold topBar, TextField leadingIcon) */
    data class SetSlot(
        val parentId: NodeId,
        val slotName: String,
        val node: ComposableNode?
    ) : WorkspaceIntent

    /** Removes a node anywhere in the tree by its ID */
    data class RemoveNode(val targetId: NodeId) : WorkspaceIntent

    /** Replaces a node's properties while preserving identity */
    data class UpdateNode(val node: ComposableNode) : WorkspaceIntent

    /** Replaces only the modifier chain for a target node */
    data class UpdateModifiers(
        val targetId: NodeId,
        val modifiers: List<ModifierDef>
    ) : WorkspaceIntent

    /** Selects or deselects a node */
    data class SelectNode(val targetId: NodeId?) : WorkspaceIntent

    data object Undo : WorkspaceIntent
    data object Redo : WorkspaceIntent

    /** Jumps directly to an arbitrary step index in the command history */
    data class JumpToHistory(val targetStep: Int) : WorkspaceIntent

    /** Loads a new document into the workspace, optionally restoring history stacks */
    data class LoadDocument(
        val rootNode: ComposableNode,
        val undoStack: List<EditorCommand> = emptyList(),
        val redoStack: List<EditorCommand> = emptyList()
    ) : WorkspaceIntent
}