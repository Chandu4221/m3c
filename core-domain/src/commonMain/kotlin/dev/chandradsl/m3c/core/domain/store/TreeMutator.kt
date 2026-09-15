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

    fun setSlot(root: ComposableNode, parentId: NodeId, slotName: String, slotNode: ComposableNode?): ComposableNode {
        if (root.id == parentId) {
            return root.withSlot(slotName, slotNode)
        }
        return root.mapChildren { setSlot(it, parentId, slotName, slotNode) }
    }

    fun updateNode(root: ComposableNode, updatedNode: ComposableNode): ComposableNode {
        if (root.id == updatedNode.id) {
            return updatedNode
        }
        return root.mapChildren { updateNode(it, updatedNode) }
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

    // ========================================================================
    // Internal Tree Operations
    // ========================================================================

    private fun ComposableNode.withAddedChild(child: ComposableNode, index: Int): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.RowNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.BoxNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.SurfaceNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.NavigationBarNode -> copy(items = insertAt(items, child, index))
        is ComposableNode.CardNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.ElevatedCardNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.OutlinedCardNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.ButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.ElevatedButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.FilledTonalButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.OutlinedButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.TextButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.IconButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.FloatingActionButtonNode -> copy(content = insertAt(content, child, index))
        is ComposableNode.ScaffoldNode -> copy(content = child)
        else -> this
    }

    private fun ComposableNode.withSlot(slotName: String, slotNode: ComposableNode?): ComposableNode = when (this) {
        is ComposableNode.ScaffoldNode -> when (slotName.lowercase()) {
            "topbar" -> copy(topBar = slotNode)
            "bottombar" -> copy(bottomBar = slotNode)
            "floatingactionbutton", "fab" -> copy(floatingActionButton = slotNode)
            "content" -> copy(content = slotNode)
            else -> this
        }
        is ComposableNode.TopAppBarNode -> when (slotName.lowercase()) {
            "title" -> if (slotNode != null) copy(title = slotNode) else this
            "navigationicon" -> copy(navigationIcon = slotNode)
            "action" -> if (slotNode != null) copy(actions = actions + slotNode) else this
            else -> this
        }
        is ComposableNode.TextFieldNode -> when (slotName.lowercase()) {
            "leadingicon" -> copy(leadingIcon = slotNode)
            "trailingicon" -> copy(trailingIcon = slotNode)
            else -> this
        }
        is ComposableNode.OutlinedTextFieldNode -> when (slotName.lowercase()) {
            "leadingicon" -> copy(leadingIcon = slotNode)
            "trailingicon" -> copy(trailingIcon = slotNode)
            else -> this
        }
        is ComposableNode.NavigationBarItemNode -> when (slotName.lowercase()) {
            "icon" -> if (slotNode != null) copy(icon = slotNode) else this
            "label" -> copy(label = slotNode)
            else -> this
        }
        else -> this
    }

    private fun ComposableNode.withModifiers(newModifiers: List<ModifierDef>): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(modifiers = newModifiers)
        is ComposableNode.RowNode -> copy(modifiers = newModifiers)
        is ComposableNode.BoxNode -> copy(modifiers = newModifiers)
        is ComposableNode.SurfaceNode -> copy(modifiers = newModifiers)
        is ComposableNode.CardNode -> copy(modifiers = newModifiers)
        is ComposableNode.ElevatedCardNode -> copy(modifiers = newModifiers)
        is ComposableNode.OutlinedCardNode -> copy(modifiers = newModifiers)
        is ComposableNode.ButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.ElevatedButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.FilledTonalButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.OutlinedButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.TextButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.IconButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.FloatingActionButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.TextNode -> copy(modifiers = newModifiers)
        is ComposableNode.TextFieldNode -> copy(modifiers = newModifiers)
        is ComposableNode.OutlinedTextFieldNode -> copy(modifiers = newModifiers)
        is ComposableNode.CheckboxNode -> copy(modifiers = newModifiers)
        is ComposableNode.SwitchNode -> copy(modifiers = newModifiers)
        is ComposableNode.RadioButtonNode -> copy(modifiers = newModifiers)
        is ComposableNode.SliderNode -> copy(modifiers = newModifiers)
        is ComposableNode.CircularProgressIndicatorNode -> copy(modifiers = newModifiers)
        is ComposableNode.LinearProgressIndicatorNode -> copy(modifiers = newModifiers)
        is ComposableNode.SpacerNode -> copy(modifiers = newModifiers)
        is ComposableNode.HorizontalDividerNode -> copy(modifiers = newModifiers)
        is ComposableNode.VerticalDividerNode -> copy(modifiers = newModifiers)
        is ComposableNode.ScaffoldNode -> copy(modifiers = newModifiers)
        is ComposableNode.TopAppBarNode -> copy(modifiers = newModifiers)
        is ComposableNode.NavigationBarNode -> copy(modifiers = newModifiers)
        is ComposableNode.NavigationBarItemNode -> copy(modifiers = newModifiers)
    }

    private fun ComposableNode.mapChildren(transform: (ComposableNode) -> ComposableNode): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(children = children.map(transform))
        is ComposableNode.RowNode -> copy(children = children.map(transform))
        is ComposableNode.BoxNode -> copy(children = children.map(transform))
        is ComposableNode.SurfaceNode -> copy(children = children.map(transform))
        is ComposableNode.NavigationBarNode -> copy(items = items.map(transform))
        is ComposableNode.CardNode -> copy(content = content.map(transform))
        is ComposableNode.ElevatedCardNode -> copy(content = content.map(transform))
        is ComposableNode.OutlinedCardNode -> copy(content = content.map(transform))
        is ComposableNode.ButtonNode -> copy(content = content.map(transform))
        is ComposableNode.ElevatedButtonNode -> copy(content = content.map(transform))
        is ComposableNode.FilledTonalButtonNode -> copy(content = content.map(transform))
        is ComposableNode.OutlinedButtonNode -> copy(content = content.map(transform))
        is ComposableNode.TextButtonNode -> copy(content = content.map(transform))
        is ComposableNode.IconButtonNode -> copy(content = content.map(transform))
        is ComposableNode.FloatingActionButtonNode -> copy(content = content.map(transform))
        is ComposableNode.ScaffoldNode -> copy(
            topBar = topBar?.let(transform),
            bottomBar = bottomBar?.let(transform),
            floatingActionButton = floatingActionButton?.let(transform),
            content = content?.let(transform)
        )
        is ComposableNode.TopAppBarNode -> copy(
            title = transform(title),
            navigationIcon = navigationIcon?.let(transform),
            actions = actions.map(transform)
        )
        is ComposableNode.TextFieldNode -> copy(
            leadingIcon = leadingIcon?.let(transform),
            trailingIcon = trailingIcon?.let(transform)
        )
        is ComposableNode.OutlinedTextFieldNode -> copy(
            leadingIcon = leadingIcon?.let(transform),
            trailingIcon = trailingIcon?.let(transform)
        )
        is ComposableNode.NavigationBarItemNode -> copy(
            icon = transform(icon),
            label = label?.let(transform)
        )
        else -> this
    }

    private fun ComposableNode.filterAndMapChildren(targetId: NodeId, transform: (ComposableNode) -> ComposableNode?): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(children = children.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.RowNode -> copy(children = children.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.BoxNode -> copy(children = children.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.SurfaceNode -> copy(children = children.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.NavigationBarNode -> copy(items = items.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.CardNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.ElevatedCardNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.OutlinedCardNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.ButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.ElevatedButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.FilledTonalButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.OutlinedButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.TextButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.IconButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.FloatingActionButtonNode -> copy(content = content.filter { it.id != targetId }.mapNotNull(transform))
        is ComposableNode.ScaffoldNode -> copy(
            topBar = if (topBar?.id == targetId) null else topBar?.let(transform),
            bottomBar = if (bottomBar?.id == targetId) null else bottomBar?.let(transform),
            floatingActionButton = if (floatingActionButton?.id == targetId) null else floatingActionButton?.let(transform),
            content = if (content?.id == targetId) null else content?.let(transform)
        )
        is ComposableNode.TopAppBarNode -> copy(
            navigationIcon = if (navigationIcon?.id == targetId) null else navigationIcon?.let(transform),
            actions = actions.filter { it.id != targetId }.mapNotNull(transform)
        )
        is ComposableNode.TextFieldNode -> copy(
            leadingIcon = if (leadingIcon?.id == targetId) null else leadingIcon?.let(transform),
            trailingIcon = if (trailingIcon?.id == targetId) null else trailingIcon?.let(transform)
        )
        is ComposableNode.OutlinedTextFieldNode -> copy(
            leadingIcon = if (leadingIcon?.id == targetId) null else leadingIcon?.let(transform),
            trailingIcon = if (trailingIcon?.id == targetId) null else trailingIcon?.let(transform)
        )
        is ComposableNode.NavigationBarItemNode -> copy(
            label = if (label?.id == targetId) null else label?.let(transform)
        )
        else -> this
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