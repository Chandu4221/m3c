package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier

@Composable
fun RenderHorizontalDivider(
    node: ComposableNode.HorizontalDividerNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "HorizontalDivider",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        HorizontalDivider(
            modifier = node.modifiers.toComposeModifier(),
            thickness = node.thickness.value.dp,
            color = node.color?.toComposeColor() ?: DividerDefaults.color
        )
    }
}

@Composable
fun RenderVerticalDivider(
    node: ComposableNode.VerticalDividerNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "VerticalDivider",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        VerticalDivider(
            modifier = node.modifiers.toComposeModifier(),
            thickness = node.thickness.value.dp,
            color = node.color?.toComposeColor() ?: DividerDefaults.color
        )
    }
}