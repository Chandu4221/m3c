package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId

object TreeMutator {

    fun insertChild(root: ComposableNode, parentId: NodeId, child: ComposableNode, index: Int = -1): ComposableNode {
        if (root.id == parentId) {
            return root.withAddedChild(child, index)
        }
        return root.mapChildren { insertChild(it, parentId, child, index) }
    }

    fun removeNode(root: ComposableNode, targetId: NodeId): ComposableNode? {
        if (root.id == targetId) return null
        return root.filterAndMapChildren(targetId) { removeNode(it, targetId) }
    }

    fun updateModifiers(root: ComposableNode, targetId: NodeId, newModifiers: List<ModifierDef>): ComposableNode {
        if (root.id == targetId) {
            return root.withModifiers(newModifiers)
        }
        return root.mapChildren { updateModifiers(it, targetId, newModifiers) }
    }

    private fun ComposableNode.withAddedChild(child: ComposableNode, index: Int): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.RowNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.BoxNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.ButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.CardNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.TextNode -> this
    }

    private fun ComposableNode.withModifiers(newModifiers: List<ModifierDef>): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(modifiers = newModifiers)
        is ComposableNode.RowNode -> copy(modifiers = newModifiers)
        is ComposableNode.BoxNode -> copy(modifiers = newModifiers)
        is ComposableNode.TextNode -> copy(modifiers = newModifiers)
        is ComposableNode.ButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.CardNode -> copy(modifiers = newModifiers)
    }

    private fun ComposableNode.mapChildren(transform: (ComposableNode) -> ComposableNode): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(children = children.map(transform))
        is ComposableNode.RowNode -> copy(children = children.map(transform))
        is ComposableNode.BoxNode -> copy(children = children.map(transform))
        is ComposableNode.ButtonNode -> copy(content = content.map(transform))
        is ComposableNode.CardNode -> copy(content = content.map(transform))
        is ComposableNode.TextNode -> this
    }

    private fun ComposableNode.filterAndMapChildren(targetId: NodeId, transform: (ComposableNode) -> ComposableNode?): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(children = children.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.RowNode -> copy(children = children.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.BoxNode -> copy(children = children.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.ButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.CardNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.TextNode -> this
    }

    private fun insertAt(list: List<ComposableNode>, item: ComposableNode, index: Int): List<ComposableNode> {
        val mutable = list.toMutableList()
        if (index in 0..mutable.size) {
            mutable.add(index, item)
        } else {
            mutable.add(item)
        }
        return mutable
    }
}