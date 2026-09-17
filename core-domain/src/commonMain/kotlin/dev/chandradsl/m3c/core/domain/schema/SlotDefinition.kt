package dev.chandradsl.m3c.core.domain.schema

/**
 * Declarative description of a named slot accepted by a component (e.g. topBar, floatingActionButton).
 */
data class SlotDefinition(
    val id: String,
    val displayName: String,
    val cardinality: SlotCardinality = SlotCardinality.Single,
    val acceptedTypes: Set<ComponentType> = emptySet()
)

enum class SlotCardinality {
    Single,
    List
}
