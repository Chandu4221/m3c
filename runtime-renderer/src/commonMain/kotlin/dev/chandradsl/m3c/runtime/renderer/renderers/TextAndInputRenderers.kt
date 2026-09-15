package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeTextStyle

@Composable
fun RenderText(
    node: ComposableNode.TextNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val style = node.typography?.toComposeTextStyle() ?: LocalTextStyle.current
    val textColor = node.color?.toComposeColor() ?: Color.Unspecified
    val fontSize = node.fontSize?.let { it.value.sp } ?: style.fontSize

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Text",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Text(
            text = node.text,
            modifier = node.modifiers.toComposeModifier(),
            style = style,
            fontSize = fontSize,
            color = textColor
        )
    }
}

@Composable
fun RenderTextField(
    node: ComposableNode.TextFieldNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "TextField",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        TextField(
            value = node.value,
            onValueChange = { newValue ->
                onIntent(WorkspaceIntent.UpdateNode(node.copy(value = newValue)))
            },
            modifier = node.modifiers.toComposeModifier(),
            enabled = node.enabled,
            readOnly = node.readOnly,
            isError = node.isError,
            singleLine = node.singleLine,
            label = node.label?.let { { Text(it) } },
            placeholder = node.placeholder?.let { { Text(it) } },
            leadingIcon = node.leadingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent) }
            },
            trailingIcon = node.trailingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent) }
            }
        )
    }
}

@Composable
fun RenderOutlinedTextField(
    node: ComposableNode.OutlinedTextFieldNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "OutlinedTextField",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = node.value,
            onValueChange = { newValue ->
                onIntent(WorkspaceIntent.UpdateNode(node.copy(value = newValue)))
            },
            modifier = node.modifiers.toComposeModifier(),
            enabled = node.enabled,
            readOnly = node.readOnly,
            isError = node.isError,
            singleLine = node.singleLine,
            label = node.label?.let { { Text(it) } },
            placeholder = node.placeholder?.let { { Text(it) } },
            leadingIcon = node.leadingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent) }
            },
            trailingIcon = node.trailingIcon?.let { iconNode ->
                { NodeRenderer(node = iconNode, state = state, onIntent = onIntent) }
            }
        )
    }
}