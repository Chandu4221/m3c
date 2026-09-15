package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ComposableNode {
    val id: NodeId
    val modifiers: List<ModifierDef>

    // ========================================================================
    // 1. Structural Layout Containers
    // ========================================================================

    @Serializable
    @SerialName("column")
    data class ColumnNode(
        override val id: NodeId = NodeId.generate("col"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val verticalArrangement: ArrangementVerticalDef = ArrangementVerticalDef.Top,
        val horizontalAlignment: AlignmentHorizontalDef = AlignmentHorizontalDef.Start,
        val children: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("row")
    data class RowNode(
        override val id: NodeId = NodeId.generate("row"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val horizontalArrangement: ArrangementHorizontalDef = ArrangementHorizontalDef.Start,
        val verticalAlignment: AlignmentVerticalDef = AlignmentVerticalDef.Top,
        val children: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("box")
    data class BoxNode(
        override val id: NodeId = NodeId.generate("box"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val contentAlignment: AlignmentDef = AlignmentDef.TopStart,
        val propagateMinConstraints: Boolean = false,
        val children: List<ComposableNode> = emptyList()
    ) : ComposableNode

    // ========================================================================
    // 2. Material 3 Leaf & Slot Components
    // ========================================================================

    @Serializable
    @SerialName("text")
    data class TextNode(
        override val id: NodeId = NodeId.generate("txt"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val text: String,
        val typography: TypographyToken? = null,
        val fontSize: SpVal? = null,
        val color: ColorSource? = null
    ) : ComposableNode

    @Serializable
    @SerialName("button")
    data class ButtonNode(
        override val id: NodeId = NodeId.generate("btn"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("card")
    data class CardNode(
        override val id: NodeId = NodeId.generate("crd"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val elevation: DpVal = DpVal(1f),
        val shape: ShapeDef? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode
}