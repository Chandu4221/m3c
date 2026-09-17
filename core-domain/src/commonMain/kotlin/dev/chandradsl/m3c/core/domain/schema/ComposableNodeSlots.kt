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
        is ComposableNode.BottomAppBarNode -> buildList {
            val actionsSlot = slots.find { it.id == StandardSlots.ACTIONS }
            val fabSlot = slots.find { it.id == StandardSlots.FLOATING_ACTION_BUTTON }

            actions.forEach { add(it to actionsSlot) }
            floatingActionButton?.let { add(it to fabSlot) }
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
        is ComposableNode.NavigationRailNode -> buildList {
            val headerSlot = slots.find { it.id == StandardSlots.HEADER }
            header?.let { add(it to headerSlot) }
            items.forEach { add(it to null) }
        }
        is ComposableNode.NavigationRailItemNode -> buildList {
            val iconSlot = slots.find { it.id == StandardSlots.ICON }
            val labelSlot = slots.find { it.id == StandardSlots.LABEL }

            add(icon to iconSlot)
            label?.let { add(it to labelSlot) }
        }
        is ComposableNode.BadgedBoxNode -> buildList {
            val badgeSlot = slots.find { it.id == StandardSlots.BADGE }
            val contentSlot = slots.find { it.id == StandardSlots.CONTENT }

            badge?.let { add(it to badgeSlot) }
            content?.let { add(it to contentSlot) }
        }
        is ComposableNode.AlertDialogNode -> buildList {
            val iconSlot = slots.find { it.id == StandardSlots.ICON }
            val titleSlot = slots.find { it.id == StandardSlots.TITLE }
            val textSlot = slots.find { it.id == StandardSlots.TEXT }
            val confirmSlot = slots.find { it.id == StandardSlots.CONFIRM_BUTTON }
            val dismissSlot = slots.find { it.id == StandardSlots.DISMISS_BUTTON }

            icon?.let { add(it to iconSlot) }
            title?.let { add(it to titleSlot) }
            text?.let { add(it to textSlot) }
            confirmButton?.let { add(it to confirmSlot) }
            dismissButton?.let { add(it to dismissSlot) }
        }
        is ComposableNode.AssistChipNode -> buildList {
            val leadingSlot = slots.find { it.id == StandardSlots.LEADING_ICON }
            leadingIcon?.let { add(it to leadingSlot) }
        }
        is ComposableNode.FilterChipNode -> buildList {
            val leadingSlot = slots.find { it.id == StandardSlots.LEADING_ICON }
            leadingIcon?.let { add(it to leadingSlot) }
        }
        is ComposableNode.InputChipNode -> buildList {
            val leadingSlot = slots.find { it.id == StandardSlots.LEADING_ICON }
            val trailingSlot = slots.find { it.id == StandardSlots.TRAILING_ICON }
            leadingIcon?.let { add(it to leadingSlot) }
            trailingIcon?.let { add(it to trailingSlot) }
        }
        is ComposableNode.SuggestionChipNode -> buildList {
            val iconSlot = slots.find { it.id == StandardSlots.ICON }
            icon?.let { add(it to iconSlot) }
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
    is ComposableNode.BottomAppBarNode -> when (slotId) {
        StandardSlots.ACTIONS -> actions
        StandardSlots.FLOATING_ACTION_BUTTON -> listOfNotNull(floatingActionButton)
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
    is ComposableNode.NavigationRailNode -> when (slotId) {
        StandardSlots.HEADER -> listOfNotNull(header)
        else -> emptyList()
    }
    is ComposableNode.NavigationRailItemNode -> when (slotId) {
        StandardSlots.ICON -> listOf(icon)
        StandardSlots.LABEL -> listOfNotNull(label)
        else -> emptyList()
    }
    is ComposableNode.BadgedBoxNode -> when (slotId) {
        StandardSlots.BADGE -> listOfNotNull(badge)
        StandardSlots.CONTENT -> listOfNotNull(content)
        else -> emptyList()
    }
    is ComposableNode.AlertDialogNode -> when (slotId) {
        StandardSlots.ICON -> listOfNotNull(icon)
        StandardSlots.TITLE -> listOfNotNull(title)
        StandardSlots.TEXT -> listOfNotNull(text)
        StandardSlots.CONFIRM_BUTTON -> listOfNotNull(confirmButton)
        StandardSlots.DISMISS_BUTTON -> listOfNotNull(dismissButton)
        else -> emptyList()
    }
    is ComposableNode.AssistChipNode -> when (slotId) {
        StandardSlots.LEADING_ICON -> listOfNotNull(leadingIcon)
        else -> emptyList()
    }
    is ComposableNode.FilterChipNode -> when (slotId) {
        StandardSlots.LEADING_ICON -> listOfNotNull(leadingIcon)
        else -> emptyList()
    }
    is ComposableNode.InputChipNode -> when (slotId) {
        StandardSlots.LEADING_ICON -> listOfNotNull(leadingIcon)
        StandardSlots.TRAILING_ICON -> listOfNotNull(trailingIcon)
        else -> emptyList()
    }
    is ComposableNode.SuggestionChipNode -> when (slotId) {
        StandardSlots.ICON -> listOfNotNull(icon)
        else -> emptyList()
    }
    else -> emptyList()
}
