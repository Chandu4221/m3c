package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef

data class WorkspaceState(
    val rootNode: ComposableNode,
    val selectedNodeId: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

sealed interface WorkspaceIntent {
    data class InsertChild(
        val parentId: String,
        val node: ComposableNode,
        val index: Int = -1
    ) : WorkspaceIntent

    data class RemoveNode(val targetId: String) : WorkspaceIntent

    data class UpdateModifiers(
        val targetId: String,
        val modifiers: List<ModifierDef>
    ) : WorkspaceIntent

    data class SelectNode(val targetId: String?) : WorkspaceIntent

    data object Undo : WorkspaceIntent
    data object Redo : WorkspaceIntent
}