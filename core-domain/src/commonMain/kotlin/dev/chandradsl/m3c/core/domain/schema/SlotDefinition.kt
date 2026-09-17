package dev.chandradsl.m3c.core.domain.schema

import dev.chandradsl.m3c.core.domain.scope.ContainerScope

/**
 * Declarative description of a named slot accepted by a component (e.g. topBar, floatingActionButton).
 */
data class SlotDefinition(
    val id: String,
    val displayName: String,
    val cardinality: SlotCardinality = SlotCardinality.Single,
    val acceptedTypes: Set<ComponentType> = emptySet(),
    val providedScope: ContainerScope = ContainerScope.None
) {
    /**
     * Returns true if this slot accepts the given component type.
     * An empty [acceptedTypes] set implies any component type is permitted.
     */
    fun accepts(type: ComponentType): Boolean =
        acceptedTypes.isEmpty() || acceptedTypes.contains(type)
}

enum class SlotCardinality {
    Single,
    List
}

/**
 * Standard slot identifiers used across Material 3 components.
 */
object StandardSlots {
    const val TOP_BAR = "topBar"
    const val BOTTOM_BAR = "bottomBar"
    const val FLOATING_ACTION_BUTTON = "floatingActionButton"
    const val CONTENT = "content"
    const val TITLE = "title"
    const val NAVIGATION_ICON = "navigationIcon"
    const val ACTIONS = "actions"
    const val LEADING_ICON = "leadingIcon"
    const val TRAILING_ICON = "trailingIcon"
    const val ICON = "icon"
    const val LABEL = "label"
}

