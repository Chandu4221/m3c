package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.EmptySlotPlaceholder
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier

@Composable
fun RenderScaffold(
    node: ComposableNode.ScaffoldNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Scaffold",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Scaffold(
            modifier = node.modifiers.toComposeModifier(),
            topBar = {
                val topBarNode = node.topBar
                if (topBarNode != null) {
                    NodeRenderer(node = topBarNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                } else if (!isInteractiveMode) {
                    EmptySlotPlaceholder(
                        slotName = "topBar",
                        onClick = { onIntent(WorkspaceIntent.SelectNode(node.id)) }
                    )
                }
            },
            bottomBar = {
                val bottomBarNode = node.bottomBar
                if (bottomBarNode != null) {
                    NodeRenderer(node = bottomBarNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                } else if (!isInteractiveMode && (state.selectedNodeId == node.id || (node.topBar == null && node.content == null))) {
                    EmptySlotPlaceholder(
                        slotName = "bottomBar",
                        onClick = { onIntent(WorkspaceIntent.SelectNode(node.id)) }
                    )
                }
            },
            floatingActionButton = {
                val fabNode = node.floatingActionButton
                if (fabNode != null) {
                    NodeRenderer(node = fabNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                }
            },
            containerColor = node.containerColor?.toComposeColor() ?: MaterialTheme.colorScheme.background,
            contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onBackground
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                val contentNode = node.content
                if (contentNode != null) {
                    NodeRenderer(node = contentNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                } else if (!isInteractiveMode) {
                    EmptySlotPlaceholder(
                        slotName = "Main Content",
                        onClick = { onIntent(WorkspaceIntent.SelectNode(node.id)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderTopAppBar(
    node: ComposableNode.TopAppBarNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    val topBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = node.containerColor?.toComposeColor() ?: MaterialTheme.colorScheme.surface,
        titleContentColor = node.titleContentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onSurface
    )

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "TopAppBar",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        TopAppBar(
            title = {
                NodeRenderer(node = node.title, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
            },
            navigationIcon = {
                val navIcon = node.navigationIcon
                if (navIcon != null) {
                    NodeRenderer(node = navIcon, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                }
            },
            actions = {
                node.actions.forEach { actionNode ->
                    NodeRenderer(node = actionNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                }
            },
            colors = topBarColors,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderNavigationBar(
    node: ComposableNode.NavigationBarNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "NavigationBar",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        NavigationBar(
            modifier = node.modifiers.toComposeModifier(),
            containerColor = node.containerColor?.toComposeColor() ?: MaterialTheme.colorScheme.surfaceContainer,
            contentColor = node.contentColor?.toComposeColor() ?: MaterialTheme.colorScheme.onSurface,
            tonalElevation = node.tonalElevation.value.dp
        ) {
            if (node.items.isEmpty()) {
                if (!isInteractiveMode) {
                    EmptySlotPlaceholder(
                        slotName = "navigation items",
                        onClick = { onIntent(WorkspaceIntent.SelectNode(node.id)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // In Compose M3, this block is already a RowScope!
                node.items.forEach { itemNode ->
                    if (itemNode is ComposableNode.NavigationBarItemNode) {
                        RenderNavigationBarItem(node = itemNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    } else {
                        NodeRenderer(node = itemNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.RenderNavigationBarItem(
    node: ComposableNode.NavigationBarItemNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "NavItem",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier.weight(1f)
    ) {
        NavigationBarItem(
            selected = node.selected,
            onClick = {
                if (isInteractiveMode) {
                    onIntent(WorkspaceIntent.UpdateNode(node.copy(selected = !node.selected)))
                } else {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                }
            },
            icon = {
                NodeRenderer(node = node.icon, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
            },
            label = node.label?.let { labelNode ->
                { NodeRenderer(node = labelNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            alwaysShowLabel = node.alwaysShowLabel,
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderBottomAppBar(
    node: ComposableNode.BottomAppBarNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "BottomAppBar",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        BottomAppBar(
            actions = {
                node.actions.forEach { actionNode ->
                    NodeRenderer(node = actionNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                }
            },
            floatingActionButton = node.floatingActionButton?.let { fabNode ->
                { NodeRenderer(node = fabNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            containerColor = node.containerColor?.toComposeColor() ?: BottomAppBarDefaults.containerColor,
            contentColor = node.contentColor?.toComposeColor() ?: contentColorFor(BottomAppBarDefaults.containerColor),
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderNavigationRail(
    node: ComposableNode.NavigationRailNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "NavigationRail",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        NavigationRail(
            modifier = node.modifiers.toComposeModifier(),
            containerColor = node.containerColor?.toComposeColor() ?: NavigationRailDefaults.ContainerColor,
            contentColor = node.contentColor?.toComposeColor() ?: contentColorFor(NavigationRailDefaults.ContainerColor),
            header = node.header?.let { headerNode ->
                { NodeRenderer(node = headerNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            }
        ) {
            node.items.forEach { itemNode ->
                if (itemNode is ComposableNode.NavigationRailItemNode) {
                    RenderNavigationRailItem(node = itemNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                } else {
                    NodeRenderer(node = itemNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
                }
            }
        }
    }
}

@Composable
fun RenderNavigationRailItem(
    node: ComposableNode.NavigationRailItemNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "RailItem",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        NavigationRailItem(
            selected = node.selected,
            onClick = {
                if (isInteractiveMode) {
                    onIntent(WorkspaceIntent.UpdateNode(node.copy(selected = !node.selected)))
                } else {
                    onIntent(WorkspaceIntent.SelectNode(node.id))
                }
            },
            icon = {
                NodeRenderer(node = node.icon, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode)
            },
            label = node.label?.let { labelNode ->
                { NodeRenderer(node = labelNode, state = state, onIntent = onIntent, isInteractiveMode = isInteractiveMode) }
            },
            alwaysShowLabel = node.alwaysShowLabel,
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}