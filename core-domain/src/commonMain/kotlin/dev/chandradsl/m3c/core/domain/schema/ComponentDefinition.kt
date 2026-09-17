package dev.chandradsl.m3c.core.domain.schema

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.scope.ContainerScope

/**
 * The single source of truth describing a Material 3 Composable component:
 * its semantic type, display metadata, supported properties, slots, container capabilities,
 * and factory for creating default AST nodes.
 */
interface ComponentDefinition {
    val type: ComponentType
    val displayName: String
    val description: String
    val category: ComponentCategory
    val iconName: String
    val properties: List<PropertyDefinition>
    val slots: List<SlotDefinition>
    val acceptsChildren: Boolean
    val allowedScopes: Set<ContainerScope>

    fun createDefault(): ComposableNode
}

/**
 * Standard implementation of [ComponentDefinition].
 */
data class StandardComponentDefinition(
    override val type: ComponentType,
    override val displayName: String,
    override val description: String,
    override val category: ComponentCategory,
    override val iconName: String,
    override val properties: List<PropertyDefinition> = emptyList(),
    override val slots: List<SlotDefinition> = emptyList(),
    override val acceptsChildren: Boolean = false,
    override val allowedScopes: Set<ContainerScope> = emptySet(),
    private val factory: () -> ComposableNode
) : ComponentDefinition {
    override fun createDefault(): ComposableNode = factory()
}
