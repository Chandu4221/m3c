package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.EmptySlotPlaceholder
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier

@Composable
fun RenderAssistChip(
    node: ComposableNode.AssistChipNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "AssistChip",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        AssistChip(
            onClick = { if (!isInteractiveMode) onIntent(WorkspaceIntent.SelectNode(node.id)) },
            label = { Text(node.label) },
            leadingIcon = node.leadingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderFilterChip(
    node: ComposableNode.FilterChipNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "FilterChip",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        FilterChip(
            selected = node.selected,
            onClick = {
                if (isInteractiveMode) {
                    onIntent(WorkspaceIntent.UpdateNode(node.copy(selected = !node.selected)))
                } else {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                }
            },
            label = { Text(node.label) },
            leadingIcon = node.leadingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderInputChip(
    node: ComposableNode.InputChipNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "InputChip",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        InputChip(
            selected = node.selected,
            onClick = {
                if (isInteractiveMode) {
                    onIntent(WorkspaceIntent.UpdateNode(node.copy(selected = !node.selected)))
                } else {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                }
            },
            label = { Text(node.label) },
            leadingIcon = node.leadingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            trailingIcon = node.trailingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderSuggestionChip(
    node: ComposableNode.SuggestionChipNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "SuggestionChip",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        SuggestionChip(
            onClick = { if (!isInteractiveMode) onIntent(WorkspaceIntent.SelectNode(node.id)) },
            label = { Text(node.label) },
            icon = node.icon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderBadge(
    node: ComposableNode.BadgeNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val containerColor = node.containerColor?.toComposeColor() ?: BadgeDefaults.containerColor
    val contentColor = node.contentColor?.toComposeColor() ?: contentColorFor(containerColor)

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Badge",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Badge(
            containerColor = containerColor,
            contentColor = contentColor,
            modifier = node.modifiers.toComposeModifier()
        ) {
            val text = node.text
            if (!text.isNullOrBlank()) {
                Text(text = text)
            }
        }
    }
}

@Composable
fun RenderBadgedBox(
    node: ComposableNode.BadgedBoxNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "BadgedBox",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                val badgeNode = node.badge
                if (badgeNode != null) {
                    NodeRenderer(node = badgeNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                } else {
                    Badge()
                }
            },
            modifier = node.modifiers.toComposeModifier()
        ) {
            val contentNode = node.content
            if (contentNode != null) {
                NodeRenderer(node = contentNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
            } else {
                EmptySlotPlaceholder(
                    slotName = "Content",
                    onClick = { onIntent(WorkspaceIntent.SelectNode(node.id)) }
                )
            }
        }
    }
}
