package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier

@Composable
fun RenderCheckbox(
    node: ComposableNode.CheckboxNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Checkbox",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Checkbox(
            checked = node.checked,
            onCheckedChange = { isChecked ->
                onIntent(WorkspaceIntent.UpdateNode(node.copy(checked = isChecked)))
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderSwitch(
    node: ComposableNode.SwitchNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Switch",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Switch(
            checked = node.checked,
            onCheckedChange = { isChecked ->
                onIntent(WorkspaceIntent.UpdateNode(node.copy(checked = isChecked)))
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderRadioButton(
    node: ComposableNode.RadioButtonNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "RadioButton",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        RadioButton(
            selected = node.selected,
            onClick = {
                onIntent(WorkspaceIntent.UpdateNode(node.copy(selected = !node.selected)))
            },
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderSlider(
    node: ComposableNode.SliderNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Slider",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Slider(
            value = node.value,
            onValueChange = { newValue ->
                onIntent(WorkspaceIntent.UpdateNode(node.copy(value = newValue)))
            },
            valueRange = node.valueRangeStart..node.valueRangeEnd,
            steps = node.steps,
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}

@Composable
fun RenderCircularProgressIndicator(
    node: ComposableNode.CircularProgressIndicatorNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = node.color?.toComposeColor() ?: ProgressIndicatorDefaults.circularColor
    val trackColor = node.trackColor?.toComposeColor() ?: ProgressIndicatorDefaults.circularDeterminateTrackColor
    val currentProgress = node.progress

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "CircularProgress",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        if (currentProgress != null) {
            CircularProgressIndicator(
                progress = { currentProgress },
                modifier = node.modifiers.toComposeModifier(),
                color = indicatorColor,
                trackColor = trackColor,
                strokeWidth = node.strokeWidth.value.dp
            )
        } else {
            CircularProgressIndicator(
                modifier = node.modifiers.toComposeModifier(),
                color = indicatorColor,
                trackColor = trackColor,
                strokeWidth = node.strokeWidth.value.dp
            )
        }
    }
}

@Composable
fun RenderLinearProgressIndicator(
    node: ComposableNode.LinearProgressIndicatorNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = node.color?.toComposeColor() ?: ProgressIndicatorDefaults.linearColor
    val trackColor = node.trackColor?.toComposeColor() ?: ProgressIndicatorDefaults.linearTrackColor
    val currentProgress = node.progress

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "LinearProgress",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        if (currentProgress != null) {
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = node.modifiers.toComposeModifier(),
                color = indicatorColor,
                trackColor = trackColor
            )
        } else {
            LinearProgressIndicator(
                modifier = node.modifiers.toComposeModifier(),
                color = indicatorColor,
                trackColor = trackColor
            )
        }
    }
}

@Composable
fun RenderRangeSlider(
    node: ComposableNode.RangeSliderNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier,
    isInteractiveMode: Boolean = false
) {
    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "RangeSlider",
        isSelected = state.selectedNodeId == node.id,
        isInteractiveMode = isInteractiveMode,
        drillDownOnlyWhenSelected = true,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        RangeSlider(
            value = node.startValue..node.endValue,
            onValueChange = { range ->
                if (isInteractiveMode) {
                    onIntent(WorkspaceIntent.UpdateNode(node.copy(startValue = range.start, endValue = range.endInclusive)))
                }
            },
            steps = node.steps,
            enabled = node.enabled,
            modifier = node.modifiers.toComposeModifier()
        )
    }
}