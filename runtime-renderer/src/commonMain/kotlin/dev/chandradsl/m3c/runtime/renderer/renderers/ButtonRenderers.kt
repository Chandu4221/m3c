package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.InteractiveEvent
import dev.chandradsl.m3c.core.domain.model.hasDescendant
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalInteractiveActionHandler
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalInteractiveMode
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeShape

private fun extractButtonLabel(content: List<ComposableNode>, defaultTag: String): String {
    for (item in content) {
        if (item is ComposableNode.TextNode && item.text.isNotBlank()) {
            return item.text
        }
        if (item is ComposableNode.IconNode && item.iconName.isNotBlank()) {
            return item.iconName
        }
        val nestedChildren = when (item) {
            is ComposableNode.RowNode -> item.children
            is ComposableNode.ColumnNode -> item.children
            is ComposableNode.BoxNode -> item.children
            else -> emptyList()
        }
        if (nestedChildren.isNotEmpty()) {
            val nested = extractButtonLabel(nestedChildren, "")
            if (nested.isNotBlank()) return nested
        }
    }
    return defaultTag
}

@Composable
fun RenderButton(
    node: ComposableNode.ButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    val actionHandler = LocalInteractiveActionHandler.current
    val borderStroke = node.border?.let { BorderStroke(it.width.value.dp, it.color.toComposeColor()) }
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = node.containerColor?.toComposeColor() ?: MaterialTheme.colorScheme.primary,
        contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onPrimary
    )

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Button",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Button(
            onClick = {
                if (!effectiveInteractive) {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                } else {
                    val label = extractButtonLabel(node.content, "Button")
                    actionHandler?.invoke(InteractiveEvent.Click(node.id, "Button", label))
                }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.shape,
            colors = buttonColors,
            elevation = node.elevation?.let { ButtonDefaults.buttonElevation(defaultElevation = it.value.dp) },
            border = borderStroke
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = effectiveInteractive)
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
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    val actionHandler = LocalInteractiveActionHandler.current

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "ElevatedButton",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        ElevatedButton(
            onClick = {
                if (!effectiveInteractive) {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                } else {
                    val label = extractButtonLabel(node.content, "ElevatedButton")
                    actionHandler?.invoke(InteractiveEvent.Click(node.id, "ElevatedButton", label))
                }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.elevatedShape,
            elevation = node.elevation?.let { ButtonDefaults.elevatedButtonElevation(defaultElevation = it.value.dp) }
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = effectiveInteractive)
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
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    val actionHandler = LocalInteractiveActionHandler.current

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "FilledTonalButton",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        FilledTonalButton(
            onClick = {
                if (!effectiveInteractive) {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                } else {
                    val label = extractButtonLabel(node.content, "FilledTonalButton")
                    actionHandler?.invoke(InteractiveEvent.Click(node.id, "FilledTonalButton", label))
                }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.filledTonalShape
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = effectiveInteractive)
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
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    val actionHandler = LocalInteractiveActionHandler.current
    val borderStroke = node.border?.let { BorderStroke(it.width.value.dp, it.color.toComposeColor()) }

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "OutlinedButton",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = {
                if (!effectiveInteractive) {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                } else {
                    val label = extractButtonLabel(node.content, "OutlinedButton")
                    actionHandler?.invoke(InteractiveEvent.Click(node.id, "OutlinedButton", label))
                }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.outlinedShape,
            border = borderStroke
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = effectiveInteractive)
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
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    val actionHandler = LocalInteractiveActionHandler.current

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "TextButton",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        TextButton(
            onClick = {
                if (!effectiveInteractive) {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                } else {
                    val label = extractButtonLabel(node.content, "TextButton")
                    actionHandler?.invoke(InteractiveEvent.Click(node.id, "TextButton", label))
                }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: ButtonDefaults.textShape
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = effectiveInteractive)
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
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    val actionHandler = LocalInteractiveActionHandler.current

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "IconButton",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        IconButton(
            onClick = {
                if (!effectiveInteractive) {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                } else {
                    val label = extractButtonLabel(node.content, "IconButton")
                    actionHandler?.invoke(InteractiveEvent.Click(node.id, "IconButton", label))
                }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        ) {
            node.content.forEach { child ->
                NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = effectiveInteractive)
            }
        }
    }
}

@Composable
fun RenderFloatingActionButton(
    node: ComposableNode.FloatingActionButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val effectiveInteractive = isInteractiveMode || LocalInteractiveMode.current
    val actionHandler = LocalInteractiveActionHandler.current

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "FAB",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = effectiveInteractive,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {
                if (!effectiveInteractive) {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                } else {
                    val label = extractButtonLabel(node.content, "FloatingActionButton")
                    actionHandler?.invoke(InteractiveEvent.Click(node.id, "FloatingActionButton", label))
                }
            },
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: FloatingActionButtonDefaults.shape,
            containerColor = node.containerColor?.toComposeColor() ?: FloatingActionButtonDefaults.containerColor,
            contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = node.elevation?.let { FloatingActionButtonDefaults.elevation(defaultElevation = it.value.dp) }
                ?: FloatingActionButtonDefaults.elevation()
        ) {
            Row {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = effectiveInteractive)
                }
            }
        }
    }
}