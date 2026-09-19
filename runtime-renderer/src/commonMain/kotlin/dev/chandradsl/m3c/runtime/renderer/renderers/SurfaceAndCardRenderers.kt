package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.hasDescendant
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.LocalCanvasParentContainerType
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeShape

@Composable
fun RenderSurface(
    node: ComposableNode.SurfaceNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val borderStroke = node.border?.let {
        BorderStroke(it.width.value.dp, it.color.toComposeColor())
    }

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Surface",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Surface(
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: RectangleShape,
            color = node.color?.toComposeColor() ?: MaterialTheme.colorScheme.surface,
            contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onSurface,
            tonalElevation = node.tonalElevation.value.dp,
            shadowElevation = node.shadowElevation.value.dp,
            border = borderStroke
        ) {
            Column {
                CompositionLocalProvider(LocalCanvasParentContainerType provides "Column") {
                    node.children.forEach { child ->
                        NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }
            }
        }
    }
}

@Composable
fun RenderCard(
    node: ComposableNode.CardNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val borderStroke = node.border?.let {
        BorderStroke(it.width.value.dp, it.color.toComposeColor())
    }
    val cardColors = CardDefaults.cardColors(
        containerColor = node.containerColor?.toComposeColor() ?: MaterialTheme.colorScheme.surfaceVariant,
        contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onSurfaceVariant
    )

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Card",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Card(
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: CardDefaults.shape,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = node.elevation.value.dp),
            border = borderStroke
        ) {
            Column {
                CompositionLocalProvider(LocalCanvasParentContainerType provides "Column") {
                    node.content.forEach { child ->
                        NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }
            }
        }
    }
}

@Composable
fun RenderElevatedCard(
    node: ComposableNode.ElevatedCardNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "ElevatedCard",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        ElevatedCard(
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: CardDefaults.elevatedShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = node.elevation.value.dp)
        ) {
            Column {
                CompositionLocalProvider(LocalCanvasParentContainerType provides "Column") {
                    node.content.forEach { child ->
                        NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }
            }
        }
    }
}

@Composable
fun RenderOutlinedCard(
    node: ComposableNode.OutlinedCardNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val borderStroke = node.border?.let {
        BorderStroke(it.width.value.dp, it.color.toComposeColor())
    } ?: CardDefaults.outlinedCardBorder()

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "OutlinedCard",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        OutlinedCard(
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: CardDefaults.outlinedShape,
            border = borderStroke
        ) {
            Column {
                CompositionLocalProvider(LocalCanvasParentContainerType provides "Column") {
                    node.content.forEach { child ->
                        NodeRenderer(node = child, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }
            }
        }
    }
}

@Composable
fun RenderAlertDialog(
    node: ComposableNode.AlertDialogNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val containerColor = node.containerColor?.toComposeColor() ?: MaterialTheme.colorScheme.surfaceContainerHigh

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "AlertDialog",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        isChildSelected = node.hasDescendant(state.selectedNodeId),
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Surface(
            modifier = node.modifiers.toComposeModifier(),
            shape = MaterialTheme.shapes.extraLarge,
            color = containerColor,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                node.icon?.let { iconNode ->
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        NodeRenderer(node = iconNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }

                // Title
                node.title?.let { titleNode ->
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        NodeRenderer(node = titleNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }

                // Body Text
                node.text?.let { textNode ->
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        NodeRenderer(node = textNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }

                // Action Buttons (Dismiss & Confirm)
                if (node.confirmButton != null || node.dismissButton != null) {
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        node.dismissButton?.let { dismissNode ->
                            NodeRenderer(node = dismissNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                        }
                        node.confirmButton?.let { confirmNode ->
                            NodeRenderer(node = confirmNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                        }
                    }
                }
            }
        }
    }
}