package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.NodeRenderer
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeAlignment
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeArrangement
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeBoxModifier
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColumnModifier
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeRowModifier

@Composable
fun RenderColumn(
    node: ComposableNode.ColumnNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Column",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Column(
            modifier = node.modifiers.toComposeModifier(),
            verticalArrangement = node.verticalArrangement.toComposeArrangement(),
            horizontalAlignment = node.horizontalAlignment.toComposeAlignment()
        ) {
            node.children.forEach { child ->
                val childModifier = toComposeColumnModifier(child.modifiers)
                NodeRenderer(node = child, state = state, onIntent = onIntent, modifier = childModifier)
            }
        }
    }
}

@Composable
fun RenderRow(
    node: ComposableNode.RowNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Row",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Row(
            modifier = node.modifiers.toComposeModifier(),
            horizontalArrangement = node.horizontalArrangement.toComposeArrangement(),
            verticalAlignment = node.verticalAlignment.toComposeAlignment()
        ) {
            node.children.forEach { child ->
                val childModifier = toComposeRowModifier(child.modifiers)
                NodeRenderer(node = child, state = state, onIntent = onIntent, modifier = childModifier)
            }
        }
    }
}

@Composable
fun RenderBox(
    node: ComposableNode.BoxNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Box",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Box(
            modifier = node.modifiers.toComposeModifier(),
            contentAlignment = node.contentAlignment.toComposeAlignment(),
            propagateMinConstraints = node.propagateMinConstraints
        ) {
            node.children.forEach { child ->
                val childModifier = toComposeBoxModifier(child.modifiers)
                NodeRenderer(node = child, state = state, onIntent = onIntent, modifier = childModifier)
            }
        }
    }
}

@Composable
fun RenderSpacer(
    node: ComposableNode.SpacerNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Spacer",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Spacer(modifier = node.modifiers.toComposeModifier())
    }
}