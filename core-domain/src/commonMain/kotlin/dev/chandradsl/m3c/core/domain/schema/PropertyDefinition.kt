package dev.chandradsl.m3c.core.domain.schema

/**
 * Typed declarative metadata defining a component property, its label, and editing constraints.
 */
sealed interface PropertyDefinition {
    val name: String
    val label: String

    data class StringProperty(
        override val name: String,
        override val label: String,
        val defaultValue: String = ""
    ) : PropertyDefinition

    data class BooleanProperty(
        override val name: String,
        override val label: String,
        val defaultValue: Boolean = false
    ) : PropertyDefinition

    data class FloatProperty(
        override val name: String,
        override val label: String,
        val min: Float = 0f,
        val max: Float = 1f,
        val defaultValue: Float = 0f
    ) : PropertyDefinition

    data class DpProperty(
        override val name: String,
        override val label: String,
        val defaultValue: Float = 0f
    ) : PropertyDefinition

    data class ColorProperty(
        override val name: String,
        override val label: String
    ) : PropertyDefinition

    data class ShapeProperty(
        override val name: String,
        override val label: String
    ) : PropertyDefinition

    data class TypographyProperty(
        override val name: String,
        override val label: String
    ) : PropertyDefinition

    data class EnumProperty<T : Enum<T>>(
        override val name: String,
        override val label: String,
        val values: List<T>,
        val defaultValue: T
    ) : PropertyDefinition
}
