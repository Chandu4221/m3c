package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.allDirectChildren
import dev.chandradsl.m3c.core.domain.schema.ComponentRegistry
import dev.chandradsl.m3c.core.domain.schema.StandardSlots
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import dev.chandradsl.m3c.core.domain.scope.childScope

object TreeMutator {

    /**
     * Finds the immediate parent ComposableNode of [targetId] in the tree, or null if [targetId] is root or not found.
     */
    fun findParent(root: ComposableNode, targetId: NodeId): ComposableNode? {
        if (root.id == targetId) return null
        for (child in root.allDirectChildren) {
            if (child.id == targetId) return root
            val parent = findParent(child, targetId)
            if (parent != null) return parent
        }
        return null
    }

    /**
     * Finds the ComposableNode matching [targetId] anywhere in the tree, or null if not found.
     */
    fun findNode(root: ComposableNode, targetId: NodeId): ComposableNode? {
        if (root.id == targetId) return root
        for (child in root.allDirectChildren) {
            val found = findNode(child, targetId)
            if (found != null) return found
        }
        return null
    }

    /**
     * Filters [modifiers] to ensure all scoped modifiers are valid within [parentScope].
     * Universal modifiers are always preserved.
     */
    fun sanitizeModifiers(modifiers: List<ModifierDef>, parentScope: ContainerScope): List<ModifierDef> {
        return modifiers.filter { def ->
            def.requiredScope == ContainerScope.None || def.requiredScope == parentScope
        }
    }

    /**
     * Sanitizes a node's modifiers against the required scope of its container.
     */
    fun sanitizeNodeModifiersForScope(node: ComposableNode, parentScope: ContainerScope): ComposableNode {
        val sanitized = sanitizeModifiers(node.modifiers, parentScope)
        return if (sanitized != node.modifiers) node.withModifiers(sanitized) else node
    }

    fun insertChild(root: ComposableNode, parentId: NodeId, child: ComposableNode, index: Int = -1): ComposableNode {
        if (root.id == parentId) {
            val sanitizedChild = sanitizeNodeModifiersForScope(child, root.childScope)
            return root.withAddedChild(sanitizedChild, index)
        }
        return root.mapChildren { insertChild(it, parentId, child, index) }
    }

    fun setSlot(root: ComposableNode, parentId: NodeId, slotName: String, slotNode: ComposableNode?): ComposableNode {
        if (root.id == parentId) {
            val componentDef = ComponentRegistry.findByNode(root)
            val slotDef = componentDef?.slots?.find {
                it.id.equals(slotName, ignoreCase = true) || it.displayName.equals(slotName, ignoreCase = true)
            }
            val targetScope = slotDef?.providedScope ?: ContainerScope.None
            val sanitizedSlotNode = slotNode?.let { sanitizeNodeModifiersForScope(it, targetScope) }
            val canonicalSlotId = slotDef?.id ?: slotName
            return root.withSlot(canonicalSlotId, sanitizedSlotNode)
        }
        return root.mapChildren { setSlot(it, parentId, slotName, slotNode) }
    }

    fun updateNode(root: ComposableNode, updatedNode: ComposableNode): ComposableNode {
        val parent = findParent(root, updatedNode.id)
        val parentScope = parent?.childScope ?: ContainerScope.None
        val sanitizedNode = sanitizeNodeModifiersForScope(updatedNode, parentScope)

        if (root.id == sanitizedNode.id) {
            return sanitizedNode
        }
        return root.mapChildren { updateNode(it, sanitizedNode) }
    }

    fun removeNode(root: ComposableNode, targetId: NodeId): ComposableNode? {
        if (root.id == targetId) return null
        return root.filterAndMapChildren(targetId) { removeNode(it, targetId) }
    }

    fun updateModifiers(root: ComposableNode, targetId: NodeId, newModifiers: List<ModifierDef>): ComposableNode {
        val parent = findParent(root, targetId)
        val parentScope = parent?.childScope ?: ContainerScope.None
        val sanitizedModifiers = sanitizeModifiers(newModifiers, parentScope)
        return applyModifiers(root, targetId, sanitizedModifiers)
    }

    private fun applyModifiers(root: ComposableNode, targetId: NodeId, sanitizedModifiers: List<ModifierDef>): ComposableNode {
        if (root.id == targetId) {
            return root.withModifiers(sanitizedModifiers)
        }
        return root.mapChildren { applyModifiers(it, targetId, sanitizedModifiers) }
    }

    // ========================================================================
    // Internal Tree Operations
    // ========================================================================

    private fun ComposableNode.withAddedChild(child: ComposableNode, index: Int): ComposableNode = when (this) {
        is ComposableNode.ColumnNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.RowNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.BoxNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.SurfaceNode -> copy(children = insertAt(children, child, index))
        is ComposableNode.NavigationBarNode -> when (child) {
            is ComposableNode.NavigationBarItemNode -> copy(items = insertAt(items, child, index))
            else -> copy(items = insertAt(items, ComposableNode.NavigationBarItemNode(icon = child, label = ComposableNode.TextNode(text = "Item")), index))
        }
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
        is ComposableNode.ScaffoldNode -> when (child) {
            is ComposableNode.TopAppBarNode -> copy(topBar = child)
            is ComposableNode.NavigationBarNode,
            is ComposableNode.BottomAppBarNode -> copy(bottomBar = child)
            is ComposableNode.FloatingActionButtonNode -> copy(floatingActionButton = child)
            else -> {
                val currentContent = content
                when {
                    currentContent == null -> copy(content = child)
                    currentContent is ComposableNode.ColumnNode ||
                    currentContent is ComposableNode.RowNode ||
                    currentContent is ComposableNode.BoxNode ||
                    currentContent is ComposableNode.SurfaceNode -> {
                        copy(content = currentContent.withAddedChild(child, index))
                    }
                    else -> copy(content = ComposableNode.ColumnNode(children = listOf(currentContent, child)))
                }
            }
        }
        is ComposableNode.BottomAppBarNode -> when (child) {
            is ComposableNode.FloatingActionButtonNode -> copy(floatingActionButton = child)
            else -> copy(actions = insertAt(actions, child, index))
        }
        is ComposableNode.NavigationRailNode -> when (child) {
            is ComposableNode.NavigationRailItemNode -> copy(items = insertAt(items, child, index))
            else -> if (header == null) copy(header = child) else copy(items = insertAt(items, child, index))
        }
        is ComposableNode.BadgedBoxNode -> when (child) {
            is ComposableNode.BadgeNode -> copy(badge = child)
            else -> copy(content = child)
        }
        is ComposableNode.TopAppBarNode -> when (child) {
            is ComposableNode.IconButtonNode -> copy(actions = insertAt(actions, child, index))
            is ComposableNode.TextNode -> copy(title = child)
            else -> copy(actions = insertAt(actions, child, index))
        }
        is ComposableNode.AlertDialogNode -> when (child) {
            is ComposableNode.ButtonNode,
            is ComposableNode.TextButtonNode -> if (confirmButton == null) copy(confirmButton = child) else copy(dismissButton = child)
            is ComposableNode.TextNode -> when {
                title == null -> copy(title = child)
                text == null -> copy(text = child)
                else -> copy(text = child)
            }
            else -> if (icon == null) copy(icon = child) else copy(text = child)
        }
        is ComposableNode.TextNode -> this
        is ComposableNode.TextFieldNode -> this
        is ComposableNode.OutlinedTextFieldNode -> this
        is ComposableNode.CheckboxNode -> this
        is ComposableNode.SwitchNode -> this
        is ComposableNode.RadioButtonNode -> this
        is ComposableNode.SliderNode -> this
        is ComposableNode.CircularProgressIndicatorNode -> this
        is ComposableNode.LinearProgressIndicatorNode -> this
        is ComposableNode.SpacerNode -> this
        is ComposableNode.HorizontalDividerNode -> this
        is ComposableNode.VerticalDividerNode -> this
        is ComposableNode.NavigationBarItemNode -> this
        is ComposableNode.AssistChipNode -> this
        is ComposableNode.FilterChipNode -> this
        is ComposableNode.InputChipNode -> this
        is ComposableNode.SuggestionChipNode -> this
        is ComposableNode.BadgeNode -> this
        is ComposableNode.NavigationRailItemNode -> this
        is ComposableNode.RangeSliderNode -> this
    }

    private fun ComposableNode.withSlot(slotName: String, slotNode: ComposableNode?): ComposableNode = when (this) {
        is ComposableNode.ScaffoldNode -> when (slotName.lowercase().replace(" ", "").replace("_", "")) {
            "topbar", "topappbar" -> copy(topBar = slotNode)
            "bottombar", "bottomappbar", "navigationbar" -> copy(bottomBar = slotNode)
            "floatingactionbutton", "fab" -> copy(floatingActionButton = slotNode)
            "content", "maincontent" -> copy(content = slotNode)
            else -> this
        }
        is ComposableNode.TopAppBarNode -> when (slotName.lowercase()) {
            "title" -> if (slotNode != null) copy(title = slotNode) else this
            "navigationicon" -> copy(navigationIcon = slotNode)
            "action", "actions" -> if (slotNode != null) copy(actions = actions + slotNode) else copy(actions = emptyList())
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
        is ComposableNode.AssistChipNode -> when (slotName.lowercase()) {
            "leadingicon" -> copy(leadingIcon = slotNode)
            else -> this
        }
        is ComposableNode.FilterChipNode -> when (slotName.lowercase()) {
            "leadingicon" -> copy(leadingIcon = slotNode)
            else -> this
        }
        is ComposableNode.InputChipNode -> when (slotName.lowercase()) {
            "leadingicon" -> copy(leadingIcon = slotNode)
            "trailingicon" -> copy(trailingIcon = slotNode)
            else -> this
        }
        is ComposableNode.SuggestionChipNode -> when (slotName.lowercase()) {
            "icon" -> copy(icon = slotNode)
            else -> this
        }
        is ComposableNode.BadgedBoxNode -> when (slotName.lowercase()) {
            "badge" -> copy(badge = slotNode)
            "content" -> copy(content = slotNode)
            else -> this
        }
        is ComposableNode.BottomAppBarNode -> when (slotName.lowercase()) {
            "action", "actions" -> if (slotNode != null) copy(actions = actions + slotNode) else copy(actions = emptyList())
            "floatingactionbutton", "fab" -> copy(floatingActionButton = slotNode)
            else -> this
        }
        is ComposableNode.NavigationRailNode -> when (slotName.lowercase()) {
            "header" -> copy(header = slotNode)
            else -> this
        }
        is ComposableNode.NavigationRailItemNode -> when (slotName.lowercase()) {
            "icon" -> if (slotNode != null) copy(icon = slotNode) else this
            "label" -> copy(label = slotNode)
            else -> this
        }
        is ComposableNode.AlertDialogNode -> when (slotName.lowercase()) {
            "icon" -> copy(icon = slotNode)
            "title" -> copy(title = slotNode)
            "text" -> copy(text = slotNode)
            "confirmbutton", "confirm" -> copy(confirmButton = slotNode)
            "dismissbutton", "dismiss" -> copy(dismissButton = slotNode)
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
        is ComposableNode.AssistChipNode -> copy(modifiers = newModifiers)
        is ComposableNode.FilterChipNode -> copy(modifiers = newModifiers)
        is ComposableNode.InputChipNode -> copy(modifiers = newModifiers)
        is ComposableNode.SuggestionChipNode -> copy(modifiers = newModifiers)
        is ComposableNode.BadgeNode -> copy(modifiers = newModifiers)
        is ComposableNode.BadgedBoxNode -> copy(modifiers = newModifiers)
        is ComposableNode.BottomAppBarNode -> copy(modifiers = newModifiers)
        is ComposableNode.NavigationRailNode -> copy(modifiers = newModifiers)
        is ComposableNode.NavigationRailItemNode -> copy(modifiers = newModifiers)
        is ComposableNode.RangeSliderNode -> copy(modifiers = newModifiers)
        is ComposableNode.AlertDialogNode -> copy(modifiers = newModifiers)
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
        is ComposableNode.AssistChipNode -> copy(leadingIcon = leadingIcon?.let(transform))
        is ComposableNode.FilterChipNode -> copy(leadingIcon = leadingIcon?.let(transform))
        is ComposableNode.InputChipNode -> copy(
            leadingIcon = leadingIcon?.let(transform),
            trailingIcon = trailingIcon?.let(transform)
        )
        is ComposableNode.SuggestionChipNode -> copy(icon = icon?.let(transform))
        is ComposableNode.BadgedBoxNode -> copy(
            badge = badge?.let(transform),
            content = content?.let(transform)
        )
        is ComposableNode.BottomAppBarNode -> copy(
            actions = actions.map(transform),
            floatingActionButton = floatingActionButton?.let(transform)
        )
        is ComposableNode.NavigationRailNode -> copy(
            header = header?.let(transform),
            items = items.map(transform)
        )
        is ComposableNode.NavigationRailItemNode -> copy(
            icon = transform(icon),
            label = label?.let(transform)
        )
        is ComposableNode.AlertDialogNode -> copy(
            icon = icon?.let(transform),
            title = title?.let(transform),
            text = text?.let(transform),
            confirmButton = confirmButton?.let(transform),
            dismissButton = dismissButton?.let(transform)
        )
        is ComposableNode.TextNode -> this
        is ComposableNode.CheckboxNode -> this
        is ComposableNode.SwitchNode -> this
        is ComposableNode.RadioButtonNode -> this
        is ComposableNode.SliderNode -> this
        is ComposableNode.CircularProgressIndicatorNode -> this
        is ComposableNode.LinearProgressIndicatorNode -> this
        is ComposableNode.SpacerNode -> this
        is ComposableNode.HorizontalDividerNode -> this
        is ComposableNode.VerticalDividerNode -> this
        is ComposableNode.BadgeNode -> this
        is ComposableNode.RangeSliderNode -> this
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
        is ComposableNode.AssistChipNode -> copy(
            leadingIcon = if (leadingIcon?.id == targetId) null else leadingIcon?.let(transform)
        )
        is ComposableNode.FilterChipNode -> copy(
            leadingIcon = if (leadingIcon?.id == targetId) null else leadingIcon?.let(transform)
        )
        is ComposableNode.InputChipNode -> copy(
            leadingIcon = if (leadingIcon?.id == targetId) null else leadingIcon?.let(transform),
            trailingIcon = if (trailingIcon?.id == targetId) null else trailingIcon?.let(transform)
        )
        is ComposableNode.SuggestionChipNode -> copy(
            icon = if (icon?.id == targetId) null else icon?.let(transform)
        )
        is ComposableNode.BadgedBoxNode -> copy(
            badge = if (badge?.id == targetId) null else badge?.let(transform),
            content = if (content?.id == targetId) null else content?.let(transform)
        )
        is ComposableNode.BottomAppBarNode -> copy(
            actions = actions.filter { it.id != targetId }.mapNotNull(transform),
            floatingActionButton = if (floatingActionButton?.id == targetId) null else floatingActionButton?.let(transform)
        )
        is ComposableNode.NavigationRailNode -> copy(
            header = if (header?.id == targetId) null else header?.let(transform),
            items = items.filter { it.id != targetId }.mapNotNull(transform)
        )
        is ComposableNode.NavigationRailItemNode -> copy(
            label = if (label?.id == targetId) null else label?.let(transform)
        )
        is ComposableNode.AlertDialogNode -> copy(
            icon = if (icon?.id == targetId) null else icon?.let(transform),
            title = if (title?.id == targetId) null else title?.let(transform),
            text = if (text?.id == targetId) null else text?.let(transform),
            confirmButton = if (confirmButton?.id == targetId) null else confirmButton?.let(transform),
            dismissButton = if (dismissButton?.id == targetId) null else dismissButton?.let(transform)
        )
        is ComposableNode.TextNode -> this
        is ComposableNode.CheckboxNode -> this
        is ComposableNode.SwitchNode -> this
        is ComposableNode.RadioButtonNode -> this
        is ComposableNode.SliderNode -> this
        is ComposableNode.CircularProgressIndicatorNode -> this
        is ComposableNode.LinearProgressIndicatorNode -> this
        is ComposableNode.SpacerNode -> this
        is ComposableNode.HorizontalDividerNode -> this
        is ComposableNode.VerticalDividerNode -> this
        is ComposableNode.BadgeNode -> this
        is ComposableNode.RangeSliderNode -> this
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