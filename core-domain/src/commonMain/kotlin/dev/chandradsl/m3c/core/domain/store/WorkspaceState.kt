package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId

data class WorkspaceState(
    val rootNode: ComposableNode,
    val selectedNodeId: NodeId? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
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

    /** Loads a new document into the workspace, resetting history and selection */
    data class LoadDocument(val rootNode: ComposableNode) : WorkspaceIntent
}