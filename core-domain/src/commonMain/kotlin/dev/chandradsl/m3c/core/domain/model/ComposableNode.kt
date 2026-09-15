package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface ComposableNode {
    val id: String
    val modifiers: List<ModifierDef>

    // -------------------------------------------------------------
    // Layout Containers
    // -------------------------------------------------------------

    @Serializable
    data class ColumnNode(
        override val id: String,
        override val modifiers: List<ModifierDef> = emptyList(),
        val verticalArrangement: VerticalArrangementDef = VerticalArrangementDef.Top,
        val horizontalAlignment: AlignmentHorizontalDef = AlignmentHorizontalDef.Start,
        val children: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    data class RowNode(
        override val id: String,
        override val modifiers: List<ModifierDef> = emptyList(),
        val horizontalArrangement: HorizontalArrangementDef = HorizontalArrangementDef.Start,
        val verticalAlignment: AlignmentVerticalDef = AlignmentVerticalDef.Top,
        val children: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    data class BoxNode(
        override val id: String,
        override val modifiers: List<ModifierDef> = emptyList(),
        val children: List<ComposableNode> = emptyList()
    ) : ComposableNode

    // -------------------------------------------------------------
    // Material 3 Leaf & Slot Components
    // -------------------------------------------------------------

    @Serializable
    data class TextNode(
        override val id: String,
        override val modifiers: List<ModifierDef> = emptyList(),
        val text: String,
        val fontSizeSp: Float = 14f,
        val colorHex: Long? = null
    ) : ComposableNode

    @Serializable
    data class ButtonNode(
        override val id: String,
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    data class CardNode(
        override val id: String,
        override val modifiers: List<ModifierDef> = emptyList(),
        val elevationDp: Float = 1f,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode
}