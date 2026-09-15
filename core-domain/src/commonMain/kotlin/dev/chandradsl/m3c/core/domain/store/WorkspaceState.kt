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
    data class InsertChild(
        val parentId: NodeId,
        val node: ComposableNode,
        val index: Int = -1
    ) : WorkspaceIntent

    data class RemoveNode(val targetId: NodeId) : WorkspaceIntent

    data class UpdateModifiers(
        val targetId: NodeId,
        val modifiers: List<ModifierDef>
    ) : WorkspaceIntent

    data class SelectNode(val targetId: NodeId?) : WorkspaceIntent

    data object Undo : WorkspaceIntent
    data object Redo : WorkspaceIntent
}