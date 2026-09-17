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

    @Serializable
    @SerialName("surface")
    data class SurfaceNode(
        override val id: NodeId = NodeId.generate("surf"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val shape: ShapeDef? = null,
        val color: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val tonalElevation: DpVal = DpVal.Zero,
        val shadowElevation: DpVal = DpVal.Zero,
        val border: BorderDef? = null,
        val children: List<ComposableNode> = emptyList()
    ) : ComposableNode

    // ========================================================================
    // 2. Material 3 Cards
    // ========================================================================

    @Serializable
    @SerialName("card")
    data class CardNode(
        override val id: NodeId = NodeId.generate("crd"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val shape: ShapeDef? = null,
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val elevation: DpVal = DpVal(1f),
        val border: BorderDef? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("elevated_card")
    data class ElevatedCardNode(
        override val id: NodeId = NodeId.generate("el_crd"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val shape: ShapeDef? = null,
        val elevation: DpVal = DpVal(6f),
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("outlined_card")
    data class OutlinedCardNode(
        override val id: NodeId = NodeId.generate("out_crd"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val shape: ShapeDef? = null,
        val border: BorderDef? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    // ========================================================================
    // 3. Material 3 Buttons & Actions
    // ========================================================================

    @Serializable
    @SerialName("button")
    data class ButtonNode(
        override val id: NodeId = NodeId.generate("btn"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val shape: ShapeDef? = null,
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val elevation: DpVal? = null,
        val border: BorderDef? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("elevated_button")
    data class ElevatedButtonNode(
        override val id: NodeId = NodeId.generate("el_btn"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val shape: ShapeDef? = null,
        val elevation: DpVal? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("filled_tonal_button")
    data class FilledTonalButtonNode(
        override val id: NodeId = NodeId.generate("tonal_btn"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val shape: ShapeDef? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("outlined_button")
    data class OutlinedButtonNode(
        override val id: NodeId = NodeId.generate("out_btn"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val shape: ShapeDef? = null,
        val border: BorderDef? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("text_button")
    data class TextButtonNode(
        override val id: NodeId = NodeId.generate("txt_btn"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val shape: ShapeDef? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("icon_button")
    data class IconButtonNode(
        override val id: NodeId = NodeId.generate("icon_btn"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val enabled: Boolean = true,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("floating_action_button")
    data class FloatingActionButtonNode(
        override val id: NodeId = NodeId.generate("fab"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val shape: ShapeDef? = null,
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val elevation: DpVal? = null,
        val content: List<ComposableNode> = emptyList()
    ) : ComposableNode

    // ========================================================================
    // 4. Material 3 Text & Input
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
    @SerialName("text_field")
    data class TextFieldNode(
        override val id: NodeId = NodeId.generate("tf"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val value: String = "",
        val label: String? = null,
        val placeholder: String? = null,
        val leadingIcon: ComposableNode? = null,
        val trailingIcon: ComposableNode? = null,
        val enabled: Boolean = true,
        val readOnly: Boolean = false,
        val isError: Boolean = false,
        val singleLine: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("outlined_text_field")
    data class OutlinedTextFieldNode(
        override val id: NodeId = NodeId.generate("out_tf"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val value: String = "",
        val label: String? = null,
        val placeholder: String? = null,
        val leadingIcon: ComposableNode? = null,
        val trailingIcon: ComposableNode? = null,
        val enabled: Boolean = true,
        val readOnly: Boolean = false,
        val isError: Boolean = false,
        val singleLine: Boolean = true
    ) : ComposableNode

    // ========================================================================
    // 5. Selection & Progress Indicators
    // ========================================================================

    @Serializable
    @SerialName("checkbox")
    data class CheckboxNode(
        override val id: NodeId = NodeId.generate("cb"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val checked: Boolean = false,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("switch")
    data class SwitchNode(
        override val id: NodeId = NodeId.generate("sw"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val checked: Boolean = false,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("radio_button")
    data class RadioButtonNode(
        override val id: NodeId = NodeId.generate("rb"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val selected: Boolean = false,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("slider")
    data class SliderNode(
        override val id: NodeId = NodeId.generate("slider"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val value: Float = 0f,
        val valueRangeStart: Float = 0f,
        val valueRangeEnd: Float = 1f,
        val steps: Int = 0,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("circular_progress_indicator")
    data class CircularProgressIndicatorNode(
        override val id: NodeId = NodeId.generate("circ_prog"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val progress: Float? = null, // null for indeterminate
        val color: ColorSource? = null,
        val trackColor: ColorSource? = null,
        val strokeWidth: DpVal = DpVal(4f)
    ) : ComposableNode

    @Serializable
    @SerialName("linear_progress_indicator")
    data class LinearProgressIndicatorNode(
        override val id: NodeId = NodeId.generate("lin_prog"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val progress: Float? = null, // null for indeterminate
        val color: ColorSource? = null,
        val trackColor: ColorSource? = null
    ) : ComposableNode

    // ========================================================================
    // 6. Layout Utilities
    // ========================================================================

    @Serializable
    @SerialName("spacer")
    data class SpacerNode(
        override val id: NodeId = NodeId.generate("spc"),
        override val modifiers: List<ModifierDef> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("horizontal_divider")
    data class HorizontalDividerNode(
        override val id: NodeId = NodeId.generate("h_div"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val thickness: DpVal = DpVal(1f),
        val color: ColorSource? = null
    ) : ComposableNode

    @Serializable
    @SerialName("vertical_divider")
    data class VerticalDividerNode(
        override val id: NodeId = NodeId.generate("v_div"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val thickness: DpVal = DpVal(1f),
        val color: ColorSource? = null
    ) : ComposableNode

    // ========================================================================
    // 7. Scaffolding & Navigation Bars
    // ========================================================================

    @Serializable
    @SerialName("scaffold")
    data class ScaffoldNode(
        override val id: NodeId = NodeId.generate("scaffold"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val topBar: ComposableNode? = null,
        val bottomBar: ComposableNode? = null,
        val floatingActionButton: ComposableNode? = null,
        val content: ComposableNode? = null
    ) : ComposableNode

    @Serializable
    @SerialName("top_app_bar")
    data class TopAppBarNode(
        override val id: NodeId = NodeId.generate("top_bar"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val title: ComposableNode,
        val navigationIcon: ComposableNode? = null,
        val actions: List<ComposableNode> = emptyList(),
        val containerColor: ColorSource? = null,
        val titleContentColor: ColorSource? = null
    ) : ComposableNode

    @Serializable
    @SerialName("navigation_bar")
    data class NavigationBarNode(
        override val id: NodeId = NodeId.generate("nav_bar"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val tonalElevation: DpVal = DpVal(3f),
        val items: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("navigation_bar_item")
    data class NavigationBarItemNode(
        override val id: NodeId = NodeId.generate("nav_item"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val selected: Boolean = false,
        val icon: ComposableNode,
        val label: ComposableNode? = null,
        val alwaysShowLabel: Boolean = true,
        val enabled: Boolean = true
    ) : ComposableNode

    // 8. Chips & Badges
    @Serializable
    @SerialName("assist_chip")
    data class AssistChipNode(
        override val id: NodeId = NodeId.generate("chip"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val label: String = "Assist",
        val leadingIcon: ComposableNode? = null,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("filter_chip")
    data class FilterChipNode(
        override val id: NodeId = NodeId.generate("chip"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val label: String = "Filter",
        val selected: Boolean = false,
        val leadingIcon: ComposableNode? = null,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("input_chip")
    data class InputChipNode(
        override val id: NodeId = NodeId.generate("chip"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val label: String = "Input",
        val selected: Boolean = false,
        val leadingIcon: ComposableNode? = null,
        val trailingIcon: ComposableNode? = null,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("suggestion_chip")
    data class SuggestionChipNode(
        override val id: NodeId = NodeId.generate("chip"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val label: String = "Suggestion",
        val icon: ComposableNode? = null,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("badge")
    data class BadgeNode(
        override val id: NodeId = NodeId.generate("badge"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val text: String? = null
    ) : ComposableNode

    @Serializable
    @SerialName("badged_box")
    data class BadgedBoxNode(
        override val id: NodeId = NodeId.generate("badged_box"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val badge: ComposableNode? = null,
        val content: ComposableNode? = null
    ) : ComposableNode

    // 9. Additional Navigation
    @Serializable
    @SerialName("bottom_app_bar")
    data class BottomAppBarNode(
        override val id: NodeId = NodeId.generate("bottom_bar"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val actions: List<ComposableNode> = emptyList(),
        val floatingActionButton: ComposableNode? = null
    ) : ComposableNode

    @Serializable
    @SerialName("navigation_rail")
    data class NavigationRailNode(
        override val id: NodeId = NodeId.generate("nav_rail"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val containerColor: ColorSource? = null,
        val contentColor: ColorSource? = null,
        val header: ComposableNode? = null,
        val items: List<ComposableNode> = emptyList()
    ) : ComposableNode

    @Serializable
    @SerialName("navigation_rail_item")
    data class NavigationRailItemNode(
        override val id: NodeId = NodeId.generate("rail_item"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val selected: Boolean = false,
        val icon: ComposableNode,
        val label: ComposableNode? = null,
        val alwaysShowLabel: Boolean = true,
        val enabled: Boolean = true
    ) : ComposableNode

    // 10. Additional Controls & Dialogs
    @Serializable
    @SerialName("range_slider")
    data class RangeSliderNode(
        override val id: NodeId = NodeId.generate("range_sld"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val startValue: Float = 0.2f,
        val endValue: Float = 0.8f,
        val steps: Int = 0,
        val enabled: Boolean = true
    ) : ComposableNode

    @Serializable
    @SerialName("alert_dialog")
    data class AlertDialogNode(
        override val id: NodeId = NodeId.generate("dialog"),
        override val modifiers: List<ModifierDef> = emptyList(),
        val icon: ComposableNode? = null,
        val title: ComposableNode? = null,
        val text: ComposableNode? = null,
        val confirmButton: ComposableNode? = null,
        val dismissButton: ComposableNode? = null,
        val containerColor: ColorSource? = null
    ) : ComposableNode
}

/**
 * Returns all direct child nodes contained within this ComposableNode across all child lists and slots.
 */
val ComposableNode.allDirectChildren: List<ComposableNode>
    get() = when (this) {
        is ComposableNode.ColumnNode -> children
        is ComposableNode.RowNode -> children
        is ComposableNode.BoxNode -> children
        is ComposableNode.SurfaceNode -> children
        is ComposableNode.NavigationBarNode -> items
        is ComposableNode.CardNode -> content
        is ComposableNode.ElevatedCardNode -> content
        is ComposableNode.OutlinedCardNode -> content
        is ComposableNode.ButtonNode -> content
        is ComposableNode.ElevatedButtonNode -> content
        is ComposableNode.FilledTonalButtonNode -> content
        is ComposableNode.OutlinedButtonNode -> content
        is ComposableNode.TextButtonNode -> content
        is ComposableNode.IconButtonNode -> content
        is ComposableNode.FloatingActionButtonNode -> content
        is ComposableNode.ScaffoldNode -> listOfNotNull(topBar, bottomBar, floatingActionButton, content)
        is ComposableNode.TopAppBarNode -> listOf(title) + listOfNotNull(navigationIcon) + actions
        is ComposableNode.TextFieldNode -> listOfNotNull(leadingIcon, trailingIcon)
        is ComposableNode.OutlinedTextFieldNode -> listOfNotNull(leadingIcon, trailingIcon)
        is ComposableNode.NavigationBarItemNode -> listOf(icon) + listOfNotNull(label)
        is ComposableNode.AssistChipNode -> listOfNotNull(leadingIcon)
        is ComposableNode.FilterChipNode -> listOfNotNull(leadingIcon)
        is ComposableNode.InputChipNode -> listOfNotNull(leadingIcon, trailingIcon)
        is ComposableNode.SuggestionChipNode -> listOfNotNull(icon)
        is ComposableNode.BadgedBoxNode -> listOfNotNull(badge, content)
        is ComposableNode.BottomAppBarNode -> actions + listOfNotNull(floatingActionButton)
        is ComposableNode.NavigationRailNode -> listOfNotNull(header) + items
        is ComposableNode.NavigationRailItemNode -> listOf(icon) + listOfNotNull(label)
        is ComposableNode.AlertDialogNode -> listOfNotNull(icon, title, text, confirmButton, dismissButton)
        else -> emptyList()
    }

/**
 * Returns true if this node or any transitive descendant matches [targetId].
 */
fun ComposableNode.hasDescendant(targetId: NodeId?): Boolean {
    if (targetId == null) return false
    return allDirectChildren.any { it.id == targetId || it.hasDescendant(targetId) }
}