package dev.chandradsl.m3c.runtime.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderBox
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderCard
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderColumn
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderElevatedCard
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderOutlinedCard
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderRow
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderSpacer
import dev.chandradsl.m3c.runtime.renderer.renderers.RenderSurface

@Composable
fun NodeRenderer(
    node: ComposableNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (node) {
        // 1. Foundation Layouts
        is ComposableNode.ColumnNode -> RenderColumn(node, state, onIntent, modifier)
        is ComposableNode.RowNode -> RenderRow(node, state, onIntent, modifier)
        is ComposableNode.BoxNode -> RenderBox(node, state, onIntent, modifier)
        is ComposableNode.SpacerNode -> RenderSpacer(node, state, onIntent, modifier)

        // 2. Surfaces & Cards
        is ComposableNode.SurfaceNode -> RenderSurface(node, state, onIntent, modifier)
        is ComposableNode.CardNode -> RenderCard(node, state, onIntent, modifier)
        is ComposableNode.ElevatedCardNode -> RenderElevatedCard(node, state, onIntent, modifier)
        is ComposableNode.OutlinedCardNode -> RenderOutlinedCard(node, state, onIntent, modifier)

        // (We will add the branches for Buttons, Text, Selection, and Scaffold as we create each file)
        else -> Unit
    }
}