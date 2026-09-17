package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.allDirectChildren
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState

fun ComposableNode.hasDescendant(targetId: NodeId?): Boolean {
    if (targetId == null) return false
    return allDirectChildren.any { it.id == targetId || it.hasDescendant(targetId) }
}

@Composable
fun NodeRenderer(
    node: ComposableNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    when (node) {
        // 1. Foundation Layouts
        is ComposableNode.ColumnNode -> RenderColumn(node, state, onIntent, modifier)
        is ComposableNode.RowNode -> RenderRow(node, state, onIntent, modifier)
        is ComposableNode.BoxNode -> RenderBox(node, state, onIntent, modifier)
        is ComposableNode.SpacerNode -> RenderSpacer(node, state, onIntent, modifier)

        // 2. Surfaces & Cards
        is ComposableNode.SurfaceNode -> RenderSurface(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.CardNode -> RenderCard(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.ElevatedCardNode -> RenderElevatedCard(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.OutlinedCardNode -> RenderOutlinedCard(node, state, onIntent, modifier, isInteractiveMode)

        // 3. Buttons & Actions
        is ComposableNode.ButtonNode -> RenderButton(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.ElevatedButtonNode -> RenderElevatedButton(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.FilledTonalButtonNode -> RenderFilledTonalButton(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.OutlinedButtonNode -> RenderOutlinedButton(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.TextButtonNode -> RenderTextButton(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.IconButtonNode -> RenderIconButton(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.FloatingActionButtonNode -> RenderFloatingActionButton(node, state, onIntent, modifier, isInteractiveMode)

        // 4. Text & Inputs
        is ComposableNode.TextNode -> RenderText(node, state, onIntent, modifier)
        is ComposableNode.TextFieldNode -> RenderTextField(node, state, onIntent, modifier)
        is ComposableNode.OutlinedTextFieldNode -> RenderOutlinedTextField(node, state, onIntent, modifier)

        // 5. Selection & Progress Indicators
        is ComposableNode.CheckboxNode -> RenderCheckbox(node, state, onIntent, modifier)
        is ComposableNode.SwitchNode -> RenderSwitch(node, state, onIntent, modifier)
        is ComposableNode.RadioButtonNode -> RenderRadioButton(node, state, onIntent, modifier)
        is ComposableNode.SliderNode -> RenderSlider(node, state, onIntent, modifier)
        is ComposableNode.CircularProgressIndicatorNode -> RenderCircularProgressIndicator(node, state, onIntent, modifier)
        is ComposableNode.LinearProgressIndicatorNode -> RenderLinearProgressIndicator(node, state, onIntent, modifier)

        // 6. Scaffolding & Navigation
        is ComposableNode.ScaffoldNode -> RenderScaffold(node, state, onIntent, modifier)
        is ComposableNode.TopAppBarNode -> RenderTopAppBar(node, state, onIntent, modifier)
        is ComposableNode.NavigationBarNode -> RenderNavigationBar(node, state, onIntent, modifier)
        is ComposableNode.NavigationBarItemNode -> {
            // Standalone fallback: Wrap in Row so RowScope.RenderNavigationBarItem can render
            Row {
                RenderNavigationBarItem(node, state, onIntent, modifier)
            }
        }
        is ComposableNode.BottomAppBarNode -> RenderBottomAppBar(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.NavigationRailNode -> RenderNavigationRail(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.NavigationRailItemNode -> RenderNavigationRailItem(node, state, onIntent, modifier, isInteractiveMode)

        // 7. Chips & Badges
        is ComposableNode.AssistChipNode -> RenderAssistChip(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.FilterChipNode -> RenderFilterChip(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.InputChipNode -> RenderInputChip(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.SuggestionChipNode -> RenderSuggestionChip(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.BadgeNode -> RenderBadge(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.BadgedBoxNode -> RenderBadgedBox(node, state, onIntent, modifier, isInteractiveMode)

        // 8. Dialogs & Range Controls
        is ComposableNode.RangeSliderNode -> RenderRangeSlider(node, state, onIntent, modifier, isInteractiveMode)
        is ComposableNode.AlertDialogNode -> RenderAlertDialog(node, state, onIntent, modifier, isInteractiveMode)

        // 9. Dividers & Utilities
        is ComposableNode.HorizontalDividerNode -> RenderHorizontalDivider(node, state, onIntent, modifier)
        is ComposableNode.VerticalDividerNode -> RenderVerticalDivider(node, state, onIntent, modifier)
    }
}