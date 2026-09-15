package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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
fun RenderSurface(
    node: ComposableNode.SurfaceNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke = node.border?.let {
        BorderStroke(it.width.value.dp, it.color.toComposeColor())
    }

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Surface",
        isSelected = state.selectedNodeId == node.id,
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
                node.children.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
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
    modifier: Modifier = Modifier
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
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
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
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "ElevatedCard",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        ElevatedCard(
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: CardDefaults.elevatedShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = node.elevation.value.dp)
        ) {
            Column {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
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
    modifier: Modifier = Modifier
) {
    val borderStroke = node.border?.let {
        BorderStroke(it.width.value.dp, it.color.toComposeColor())
    } ?: CardDefaults.outlinedCardBorder()

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "OutlinedCard",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        OutlinedCard(
            modifier = node.modifiers.toComposeModifier(),
            shape = node.shape?.toComposeShape() ?: CardDefaults.outlinedShape,
            border = borderStroke
        ) {
            Column {
                node.content.forEach { child ->
                    NodeRenderer(node = child, state = state, onIntent = onIntent)
                }
            }
        }
    }
}