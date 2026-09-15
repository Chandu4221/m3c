package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.NodeRenderer
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeShape

@Composable
fun RenderButton(
    node: ComposableNode.ButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke = node.border?.let {
        BorderStroke(it.width.value.dp, it.color.toComposeColor())
    }
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = node.containerColor?.toComposeColor() ?: MaterialTheme.colorScheme.primary,
        contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onPrimary
    )

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Button",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Button(
            onClick = { /* Preview mode */ },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.shape,
            colors = buttonColors,
            elevation = node.elevation?.let { ButtonDefaults.buttonElevation(defaultElevation = it.value.dp) },
            border = borderStroke
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
fun RenderElevatedButton(
    node: ComposableNode.ElevatedButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "ElevatedButton",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        ElevatedButton(
            onClick = { /* Preview mode */ },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.elevatedShape,
            elevation = node.elevation?.let { ButtonDefaults.elevatedButtonElevation(defaultElevation = it.value.dp) }
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
fun RenderFilledTonalButton(
    node: ComposableNode.FilledTonalButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "FilledTonalButton",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        FilledTonalButton(
            onClick = { /* Preview mode */ },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.filledTonalShape
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
fun RenderOutlinedButton(
    node: ComposableNode.OutlinedButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke = node.border?.let {
        BorderStroke(it.width.value.dp, it.color.toComposeColor())
    } ?: ButtonDefaults.outlinedButtonBorder(enabled = node.enabled)

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "OutlinedButton",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = { /* Preview mode */ },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.outlinedShape,
            border = borderStroke
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
fun RenderTextButton(
    node: ComposableNode.TextButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "TextButton",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        TextButton(
            onClick = { /* Preview mode */ },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.textShape
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
fun RenderIconButton(
    node: ComposableNode.IconButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "IconButton",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        IconButton(
            onClick = { /* Preview mode */ },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        ) {
            node.content.forEach { child ->
                NodeRenderer(node = child, state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
fun RenderFloatingActionButton(
    node: ComposableNode.FloatingActionButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "FAB",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = { /* Preview mode */ },
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: FloatingActionButtonDefaults.shape,
            containerColor = node.containerColor?.toComposeColor() ?: FloatingActionButtonDefaults.containerColor,
            contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = node.elevation?.let { FloatingActionButtonDefaults.elevation(defaultElevation = it.value.dp) }
                ?: FloatingActionButtonDefaults.elevation()
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
                }
            }
        }
    }
}