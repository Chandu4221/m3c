package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalCanvasParentContainerType
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalInteractiveMode
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.*

@Composable
fun RenderColumn(
    node: ComposableNode.ColumnNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Column",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Column(
            modifier = node.modifiers.toComposeModifier(),
            verticalArrangement = node.verticalArrangement.toComposeArrangement(),
            horizontalAlignment = node.horizontalAlignment.toComposeAlignment()
        ) {
            CompositionLocalProvider(LocalCanvasParentContainerType provides "Column") {
                node.children.forEach { child ->
                    val childModifier = toComposeColumnModifier(child.modifiers)
                    NodeRenderer(node = child, state = state, onIntent = onIntent, modifier = childModifier, isInteractiveMode = effectiveInteractive)
                }
            }
        }
    }
}

@Composable
fun RenderRow(
    node: ComposableNode.RowNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Row",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Row(
            modifier = node.modifiers.toComposeModifier(),
            horizontalArrangement = node.horizontalArrangement.toComposeArrangement(),
            verticalAlignment = node.verticalAlignment.toComposeAlignment()
        ) {
            CompositionLocalProvider(LocalCanvasParentContainerType provides "Row") {
                node.children.forEach { child ->
                    val childModifier = toComposeRowModifier(child.modifiers)
                    NodeRenderer(node = child, state = state, onIntent = onIntent, modifier = childModifier, isInteractiveMode = effectiveInteractive)
                }
            }
        }
    }
}

@Composable
fun RenderBox(
    node: ComposableNode.BoxNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Box",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Box(
            modifier = node.modifiers.toComposeModifier(),
            contentAlignment = node.contentAlignment.toComposeAlignment(),
            propagateMinConstraints = node.propagateMinConstraints
        ) {
            CompositionLocalProvider(LocalCanvasParentContainerType provides "Box") {
                node.children.forEach { child ->
                    val childModifier = toComposeBoxModifier(child.modifiers)
                    NodeRenderer(node = child, state = state, onIntent = onIntent, modifier = childModifier, isInteractiveMode = effectiveInteractive)
                }
            }
        }
    }
}

@Composable
fun RenderSpacer(
    node: ComposableNode.SpacerNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Spacer",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Spacer(modifier = node.modifiers.toComposeModifier())
    }
}