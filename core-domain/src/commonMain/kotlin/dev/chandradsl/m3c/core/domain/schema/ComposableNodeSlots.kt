package dev.chandradsl.m3c.core.domain.schema

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.allDirectChildren

/**
 * Returns all direct children paired with their owning [SlotDefinition], or null if the child
 * is in a standard children collection (e.g. Column, Row, Box children).
 */
fun ComposableNode.childrenWithSlots(): List<Pair<ComposableNode, SlotDefinition?>> {
    val def = ComponentRegistry.findByNode(this)
    val slots = def?.slots ?: emptyList()

    return when (this) {
        is ComposableNode.ScaffoldNode -> buildList {
            val topBarSlot = slots.find { it.id == StandardSlots.TOP_BAR }
            val bottomBarSlot = slots.find { it.id == StandardSlots.BOTTOM_BAR }
            val fabSlot = slots.find { it.id == StandardSlots.FLOATING_ACTION_BUTTON }
            val contentSlot = slots.find { it.id == StandardSlots.CONTENT }

            topBar?.let { add(it to topBarSlot) }
            bottomBar?.let { add(it to bottomBarSlot) }
            floatingActionButton?.let { add(it to fabSlot) }
            content?.let { add(it to contentSlot) }
        }
        is ComposableNode.TopAppBarNode -> buildList {
            val titleSlot = slots.find { it.id == StandardSlots.TITLE }
            val navIconSlot = slots.find { it.id == StandardSlots.NAVIGATION_ICON }
            val actionsSlot = slots.find { it.id == StandardSlots.ACTIONS }

            add(title to titleSlot)
            navigationIcon?.let { add(it to navIconSlot) }
            actions.forEach { add(it to actionsSlot) }
        }
        is ComposableNode.TextFieldNode -> buildList {
            val leadingSlot = slots.find { it.id == StandardSlots.LEADING_ICON }
            val trailingSlot = slots.find { it.id == StandardSlots.TRAILING_ICON }

            leadingIcon?.let { add(it to leadingSlot) }
            trailingIcon?.let { add(it to trailingSlot) }
        }
        is ComposableNode.OutlinedTextFieldNode -> buildList {
            val leadingSlot = slots.find { it.id == StandardSlots.LEADING_ICON }
            val trailingSlot = slots.find { it.id == StandardSlots.TRAILING_ICON }

            leadingIcon?.let { add(it to leadingSlot) }
            trailingIcon?.let { add(it to trailingSlot) }
        }
        is ComposableNode.NavigationBarItemNode -> buildList {
            val iconSlot = slots.find { it.id == StandardSlots.ICON }
            val labelSlot = slots.find { it.id == StandardSlots.LABEL }

            add(icon to iconSlot)
            label?.let { add(it to labelSlot) }
        }
        else -> allDirectChildren.map { it to null }
    }
}

/**
 * Returns the child nodes currently attached to the given [slotId].
 */
fun ComposableNode.getSlotChildren(slotId: String): List<ComposableNode> = when (this) {
    is ComposableNode.ScaffoldNode -> when (slotId) {
        StandardSlots.TOP_BAR -> listOfNotNull(topBar)
        StandardSlots.BOTTOM_BAR -> listOfNotNull(bottomBar)
        StandardSlots.FLOATING_ACTION_BUTTON -> listOfNotNull(floatingActionButton)
        StandardSlots.CONTENT -> listOfNotNull(content)
        else -> emptyList()
    }
    is ComposableNode.TopAppBarNode -> when (slotId) {
        StandardSlots.TITLE -> listOf(title)
        StandardSlots.NAVIGATION_ICON -> listOfNotNull(navigationIcon)
        StandardSlots.ACTIONS -> actions
        else -> emptyList()
    }
    is ComposableNode.TextFieldNode -> when (slotId) {
        StandardSlots.LEADING_ICON -> listOfNotNull(leadingIcon)
        StandardSlots.TRAILING_ICON -> listOfNotNull(trailingIcon)
        else -> emptyList()
    }
    is ComposableNode.OutlinedTextFieldNode -> when (slotId) {
        StandardSlots.LEADING_ICON -> listOfNotNull(leadingIcon)
        StandardSlots.TRAILING_ICON -> listOfNotNull(trailingIcon)
        else -> emptyList()
    }
    is ComposableNode.NavigationBarItemNode -> when (slotId) {
        StandardSlots.ICON -> listOf(icon)
        StandardSlots.LABEL -> listOfNotNull(label)
        else -> emptyList()
    }
    else -> emptyList()
}
