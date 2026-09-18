package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementHorizontalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementVerticalDef
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.schema.ComponentRegistry
import dev.chandradsl.m3c.core.domain.schema.ComponentType
import dev.chandradsl.m3c.core.domain.schema.SlotCardinality
import dev.chandradsl.m3c.core.domain.schema.SlotDefinition
import dev.chandradsl.m3c.core.domain.schema.getSlotChildren
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent

@Composable
fun PropertyInspector(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val selectedNode = viewModel.selectedNode
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .width(viewModel.rightPanelWidth)
            .fillMaxHeight()
            .background(StudioColors.PanelSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Guard for Interactive Mode: Lock editing and inform user
        if (viewModel.isInteractiveMode) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(StudioColors.ActiveSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = StudioColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Interactive Mode Active",
                        style = StudioTypography.ComponentTitle,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Styling edits are locked during interactive preview. Click components on the canvas to test buttons, inputs, and states directly.",
                        style = StudioTypography.Caption,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { viewModel.updateInteractiveMode(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioColors.Primary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesignServices,
                            contentDescription = null,
                            tint = StudioColors.TextInverse,
                            modifier = Modifier.size(StudioSizes.IconSmall).padding(end = 4.dp)
                        )
                        Text(
                            text = "Switch to Design Mode",
                            style = StudioTypography.UIBody.copy(
                                color = StudioColors.TextInverse,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            return@Column
        }

        // 2. Empty Selection State
        if (selectedNode == null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select any component on canvas or tree to inspect properties",
                    style = StudioTypography.Caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return@Column
        }

        // 3. Selected Node Header
        val componentDef = ComponentRegistry.findByNode(selectedNode)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = componentDef?.displayName ?: (selectedNode::class.simpleName?.replace("Node", "") ?: "Component"),
                    style = StudioTypography.ComponentTitle
                )
                Text(
                    text = "ID: ${selectedNode.id.value}",
                    style = StudioTypography.Caption
                )
            }

            componentDef?.let { def ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = def.category.displayName,
                        style = StudioTypography.Caption.copy(
                            color = StudioColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(StudioColors.BorderSubtle))

        // 3b. Slots Section (for slotted components: Scaffold, TopAppBar, TextField, NavigationBarItem, etc.)
        componentDef?.let { def ->
            if (def.slots.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SLOTS",
                        style = StudioTypography.SectionHeader
                    )

                    def.slots.forEach { slot ->
                        SlotField(
                            slot = slot,
                            selectedNode = selectedNode,
                            viewModel = viewModel
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(StudioColors.BorderSubtle))
            }
        }

        // 4. Specific Component Property Editors
        when (selectedNode) {
            is ComposableNode.TextNode -> {
                InspectorField(label = "Text Content") {
                    InspectorTextInput(
                        value = selectedNode.text,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(text = it))) }
                    )
                }
                InspectorField(label = "Typography Scale") {
                    EnumSelector(
                        values = TypographyToken.entries,
                        selected = selectedNode.typography ?: TypographyToken.BodyMedium,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(typography = it))) }
                    )
                }
            }

            is ComposableNode.IconNode -> {
                InspectorField(label = "Material Icon") {
                    MaterialIconPicker(
                        selectedIconName = selectedNode.iconName,
                        onSelectIcon = { newName ->
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(iconName = newName)))
                        }
                    )
                }
                InspectorField(label = "Custom Icon Name (Manual Override)") {
                    InspectorTextInput(
                        value = selectedNode.iconName,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(iconName = it))) }
                    )
                }
                InspectorField(label = "Content Description (Accessibility)") {
                    InspectorTextInput(
                        value = selectedNode.contentDescription ?: "",
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(contentDescription = it.ifBlank { null }))) }
                    )
                }
            }

            is ComposableNode.TextFieldNode -> {
                InspectorField(label = "Input Value") {
                    InspectorTextInput(
                        value = selectedNode.value,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(value = it))) }
                    )
                }
                InspectorField(label = "Placeholder") {
                    InspectorTextInput(
                        value = selectedNode.placeholder ?: "",
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(placeholder = it.ifBlank { null }))) }
                    )
                }
                InspectorSwitchField(
                    label = "Single Line",
                    checked = selectedNode.singleLine,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(singleLine = it))) }
                )
            }

            is ComposableNode.OutlinedTextFieldNode -> {
                InspectorField(label = "Input Value") {
                    InspectorTextInput(
                        value = selectedNode.value,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(value = it))) }
                    )
                }
                InspectorField(label = "Placeholder") {
                    InspectorTextInput(
                        value = selectedNode.placeholder ?: "",
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(placeholder = it.ifBlank { null }))) }
                    )
                }
                InspectorSwitchField(
                    label = "Single Line",
                    checked = selectedNode.singleLine,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(singleLine = it))) }
                )
            }

            is ComposableNode.CardNode -> {
                InspectorField(label = "Elevation") {
                    DpOptionChips(
                        options = listOf(0f, 1f, 2f, 4f, 8f),
                        selected = selectedNode.elevation.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(elevation = DpVal(it)))) }
                    )
                }
            }

            is ComposableNode.ElevatedCardNode -> {
                InspectorField(label = "Elevation") {
                    DpOptionChips(
                        options = listOf(2f, 4f, 6f, 8f, 12f),
                        selected = selectedNode.elevation.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(elevation = DpVal(it)))) }
                    )
                }
            }

            is ComposableNode.SurfaceNode -> {
                InspectorField(label = "Tonal Elevation") {
                    DpOptionChips(
                        options = listOf(0f, 1f, 2f, 4f, 8f),
                        selected = selectedNode.tonalElevation.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(tonalElevation = DpVal(it)))) }
                    )
                }
                InspectorField(label = "Shadow Elevation") {
                    DpOptionChips(
                        options = listOf(0f, 2f, 4f, 8f, 16f),
                        selected = selectedNode.shadowElevation.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(shadowElevation = DpVal(it)))) }
                    )
                }
            }

            is ComposableNode.SliderNode -> {
                InspectorField(label = "Value (${(selectedNode.value * 100).toInt()}%)") {
                    Slider(
                        value = selectedNode.value,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(value = it))) },
                        valueRange = selectedNode.valueRangeStart..selectedNode.valueRangeEnd,
                        steps = selectedNode.steps,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioColors.Primary,
                            activeTrackColor = StudioColors.Primary
                        )
                    )
                }
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.LinearProgressIndicatorNode -> {
                val current = selectedNode.progress ?: 0.5f
                InspectorField(label = "Progress (${(current * 100).toInt()}%)") {
                    Slider(
                        value = current,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(progress = it))) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioColors.Primary,
                            activeTrackColor = StudioColors.Primary
                        )
                    )
                }
            }

            is ComposableNode.CircularProgressIndicatorNode -> {
                val current = selectedNode.progress ?: 0.5f
                InspectorField(label = "Progress (${(current * 100).toInt()}%)") {
                    Slider(
                        value = current,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(progress = it))) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioColors.Primary,
                            activeTrackColor = StudioColors.Primary
                        )
                    )
                }
            }

            is ComposableNode.HorizontalDividerNode -> {
                InspectorField(label = "Thickness") {
                    DpOptionChips(
                        options = listOf(1f, 2f, 4f),
                        selected = selectedNode.thickness.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(thickness = DpVal(it)))) }
                    )
                }
            }

            is ComposableNode.VerticalDividerNode -> {
                InspectorField(label = "Thickness") {
                    DpOptionChips(
                        options = listOf(1f, 2f, 4f),
                        selected = selectedNode.thickness.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(thickness = DpVal(it)))) }
                    )
                }
            }

            is ComposableNode.FloatingActionButtonNode -> {
                InspectorField(label = "Shape") {
                    EnumSelector(
                        values = ShapeToken.entries,
                        selected = (selectedNode.shape as? ShapeDef.Token)?.token ?: ShapeToken.Large,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(shape = ShapeDef.Token(it)))) }
                    )
                }
            }

            is ComposableNode.NavigationBarItemNode -> {
                InspectorSwitchField(
                    label = "Selected",
                    checked = selectedNode.selected,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(selected = it))) }
                )
                InspectorSwitchField(
                    label = "Always Show Label",
                    checked = selectedNode.alwaysShowLabel,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(alwaysShowLabel = it))) }
                )
            }

            is ComposableNode.NavigationBarNode -> {
                InspectorField(label = "Tonal Elevation") {
                    DpOptionChips(
                        options = listOf(0f, 1f, 3f, 6f, 8f),
                        selected = selectedNode.tonalElevation.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(tonalElevation = DpVal(it)))) }
                    )
                }
                InspectorField(label = "Navigation Items (${selectedNode.items.size})") {
                    Button(
                        onClick = {
                            val newItem = ComponentRegistry.findByType(ComponentType.NavigationBarItem)?.createDefault()
                            if (newItem != null) {
                                viewModel.dispatch(WorkspaceIntent.InsertChild(
                                    parentId = selectedNode.id,
                                    node = newItem
                                ))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioColors.Primary,
                            contentColor = StudioColors.TextInverse
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Navigation Item",
                            tint = StudioColors.TextInverse,
                            modifier = Modifier.size(StudioSizes.IconSmall).padding(end = 4.dp)
                        )
                        Text(
                            text = "Add Navigation Item",
                            style = StudioTypography.Caption.copy(
                                color = StudioColors.TextInverse,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            is ComposableNode.BottomAppBarNode -> {
                InspectorField(label = "Actions (${selectedNode.actions.size})") {
                    Button(
                        onClick = {
                            val newItem = ComponentRegistry.findByType(ComponentType.IconButton)?.createDefault()
                            if (newItem != null) {
                                viewModel.dispatch(WorkspaceIntent.InsertChild(
                                    parentId = selectedNode.id,
                                    node = newItem
                                ))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioColors.Primary,
                            contentColor = StudioColors.TextInverse
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Action Button",
                            tint = StudioColors.TextInverse,
                            modifier = Modifier.size(StudioSizes.IconSmall).padding(end = 4.dp)
                        )
                        Text(
                            text = "Add Action Button",
                            style = StudioTypography.Caption.copy(
                                color = StudioColors.TextInverse,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                InspectorField(label = "Floating Action Button") {
                    Text(
                        text = if (selectedNode.floatingActionButton != null) "Configured" else "None",
                        style = StudioTypography.Caption,
                        color = StudioColors.TextSecondary
                    )
                }
            }

            is ComposableNode.ScaffoldNode -> {
                InspectorField(label = "Scaffold Slots") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "TopBar: ${if (selectedNode.topBar != null) "Configured (${selectedNode.topBar!!::class.simpleName?.removeSuffix("Node")})" else "Empty"}",
                            style = StudioTypography.Caption,
                            color = StudioColors.TextSecondary
                        )
                        Text(
                            text = "BottomBar: ${if (selectedNode.bottomBar != null) "Configured (${selectedNode.bottomBar!!::class.simpleName?.removeSuffix("Node")})" else "Empty"}",
                            style = StudioTypography.Caption,
                            color = StudioColors.TextSecondary
                        )
                        Text(
                            text = "FAB: ${if (selectedNode.floatingActionButton != null) "Configured (${selectedNode.floatingActionButton!!::class.simpleName?.removeSuffix("Node")})" else "Empty"}",
                            style = StudioTypography.Caption,
                            color = StudioColors.TextSecondary
                        )
                        Text(
                            text = "Content: ${if (selectedNode.content != null) "Configured (${selectedNode.content!!::class.simpleName?.removeSuffix("Node")})" else "Empty"}",
                            style = StudioTypography.Caption,
                            color = StudioColors.TextSecondary
                        )
                    }
                }
            }

            is ComposableNode.ColumnNode -> {
                InspectorField(label = "Vertical Arrangement") {
                    EnumSelector(
                        values = ArrangementVerticalDef.entries,
                        selected = selectedNode.verticalArrangement,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(verticalArrangement = it))) }
                    )
                }
                InspectorField(label = "Horizontal Alignment") {
                    EnumSelector(
                        values = AlignmentHorizontalDef.entries,
                        selected = selectedNode.horizontalAlignment,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(horizontalAlignment = it))) }
                    )
                }
            }

            is ComposableNode.RowNode -> {
                InspectorField(label = "Horizontal Arrangement") {
                    EnumSelector(
                        values = ArrangementHorizontalDef.entries,
                        selected = selectedNode.horizontalArrangement,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(horizontalArrangement = it))) }
                    )
                }
                InspectorField(label = "Vertical Alignment") {
                    EnumSelector(
                        values = AlignmentVerticalDef.entries,
                        selected = selectedNode.verticalAlignment,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(verticalAlignment = it))) }
                    )
                }
            }

            is ComposableNode.BoxNode -> {
                InspectorField(label = "Content Alignment") {
                    EnumSelector(
                        values = AlignmentDef.entries,
                        selected = selectedNode.contentAlignment,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(contentAlignment = it))) }
                    )
                }
                InspectorSwitchField(
                    label = "Propagate Min Constraints",
                    checked = selectedNode.propagateMinConstraints,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(propagateMinConstraints = it))) }
                )
            }

            is ComposableNode.ButtonNode -> {
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.ElevatedButtonNode -> {
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.FilledTonalButtonNode -> {
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.OutlinedButtonNode -> {
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.TextButtonNode -> {
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.IconButtonNode -> {
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.SwitchNode -> {
                InspectorSwitchField(
                    label = "Checked",
                    checked = selectedNode.checked,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(checked = it))) }
                )
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.CheckboxNode -> {
                InspectorSwitchField(
                    label = "Checked",
                    checked = selectedNode.checked,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(checked = it))) }
                )
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.RadioButtonNode -> {
                InspectorSwitchField(
                    label = "Selected",
                    checked = selectedNode.selected,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(selected = it))) }
                )
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.AssistChipNode -> {
                InspectorField(label = "Label") {
                    InspectorTextInput(
                        value = selectedNode.label,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(label = it))) }
                    )
                }
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.FilterChipNode -> {
                InspectorField(label = "Label") {
                    InspectorTextInput(
                        value = selectedNode.label,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(label = it))) }
                    )
                }
                InspectorSwitchField(
                    label = "Selected",
                    checked = selectedNode.selected,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(selected = it))) }
                )
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.InputChipNode -> {
                InspectorField(label = "Label") {
                    InspectorTextInput(
                        value = selectedNode.label,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(label = it))) }
                    )
                }
                InspectorSwitchField(
                    label = "Selected",
                    checked = selectedNode.selected,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(selected = it))) }
                )
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.SuggestionChipNode -> {
                InspectorField(label = "Label") {
                    InspectorTextInput(
                        value = selectedNode.label,
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(label = it))) }
                    )
                }
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.BadgeNode -> {
                InspectorField(label = "Badge Text") {
                    InspectorTextInput(
                        value = selectedNode.text ?: "",
                        onValueChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(text = it.ifBlank { null }))) }
                    )
                }
            }

            is ComposableNode.NavigationRailItemNode -> {
                InspectorSwitchField(
                    label = "Selected",
                    checked = selectedNode.selected,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(selected = it))) }
                )
                InspectorSwitchField(
                    label = "Always Show Label",
                    checked = selectedNode.alwaysShowLabel,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(alwaysShowLabel = it))) }
                )
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            is ComposableNode.RangeSliderNode -> {
                InspectorField(label = "Start Value (${(selectedNode.startValue * 100).toInt()}%)") {
                    Slider(
                        value = selectedNode.startValue,
                        onValueChange = {
                            val newStart = it.coerceAtMost(selectedNode.endValue)
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(startValue = newStart)))
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioColors.Primary,
                            activeTrackColor = StudioColors.Primary
                        )
                    )
                }
                InspectorField(label = "End Value (${(selectedNode.endValue * 100).toInt()}%)") {
                    Slider(
                        value = selectedNode.endValue,
                        onValueChange = {
                            val newEnd = it.coerceAtLeast(selectedNode.startValue)
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(endValue = newEnd)))
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioColors.Primary,
                            activeTrackColor = StudioColors.Primary
                        )
                    )
                }
                InspectorSwitchField(
                    label = "Enabled",
                    checked = selectedNode.enabled,
                    onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) }
                )
            }

            else -> {
                Text(
                    text = "No custom properties for this component type.",
                    style = StudioTypography.Caption
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(StudioColors.BorderSubtle))

        // 5. Modifiers Section
        ModifierInspector(viewModel = viewModel, node = selectedNode)
    }
}

@Composable
fun InspectorSwitchField(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = StudioTypography.Caption.copy(
                fontWeight = FontWeight.Medium,
                color = StudioColors.TextPrimary
            )
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StudioColors.TextInverse,
                checkedTrackColor = StudioColors.Primary,
                checkedBorderColor = StudioColors.Primary,
                uncheckedThumbColor = StudioColors.TextSecondary,
                uncheckedTrackColor = StudioColors.ActiveSurface,
                uncheckedBorderColor = StudioColors.BorderSubtle
            )
        )
    }
}

@Composable
fun InspectorField(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = StudioTypography.Caption.copy(
                fontWeight = FontWeight.Medium,
                color = StudioColors.TextPrimary
            )
        )
        content()
    }
}

@Composable
fun InspectorTextInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = StudioTypography.InputText,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = StudioColors.TextPrimary,
            unfocusedTextColor = StudioColors.TextPrimary,
            focusedContainerColor = StudioColors.CardSurface,
            unfocusedContainerColor = StudioColors.CardSurface,
            focusedBorderColor = StudioColors.BorderActive,
            unfocusedBorderColor = StudioColors.BorderSubtle,
            cursorColor = StudioColors.Primary
        )
    )
}

@Composable
fun DpOptionChips(
    options: List<Float>,
    selected: Float,
    onSelect: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isChosen = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isChosen) StudioColors.Primary else StudioColors.CardSurface)
                    .border(
                        width = 1.dp,
                        color = if (isChosen) StudioColors.Primary else StudioColors.BorderSubtle,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelect(option) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${option.toInt()}dp",
                    style = StudioTypography.Caption.copy(
                        color = if (isChosen) StudioColors.TextInverse else StudioColors.TextPrimary,
                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

@Composable
fun <T : Enum<T>> EnumSelector(
    values: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        values.take(6).chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowItems.forEach { item ->
                    val isChosen = item == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isChosen) StudioColors.Primary else StudioColors.CardSurface)
                            .border(
                                width = 1.dp,
                                color = if (isChosen) StudioColors.Primary else StudioColors.BorderSubtle,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelect(item) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.name,
                            style = StudioTypography.Caption.copy(
                                color = if (isChosen) StudioColors.TextInverse else StudioColors.TextPrimary,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotField(
    slot: SlotDefinition,
    selectedNode: ComposableNode,
    viewModel: StudioViewModel
) {
    val slotChildren = selectedNode.getSlotChildren(slot.id)
    val isPopulated = slotChildren.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StudioColors.CardSurface)
            .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slot.displayName,
                    style = StudioTypography.Caption.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = StudioColors.TextPrimary
                    )
                )
                if (slot.cardinality == SlotCardinality.List) {
                    Text(
                        text = "[List]",
                        style = StudioTypography.Caption.copy(color = StudioColors.TextMuted)
                    )
                }
            }

            if (!isPopulated || slot.cardinality == SlotCardinality.List) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(4.dp))
                        .clickable {
                            val candidateType = slot.acceptedTypes.firstOrNull() ?: ComponentType.Text
                            val newNode = ComponentRegistry.findByType(candidateType)?.createDefault()
                            if (newNode != null) {
                                viewModel.dispatch(
                                    WorkspaceIntent.SetSlot(
                                        parentId = selectedNode.id,
                                        slotName = slot.id,
                                        node = newNode
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isPopulated) "+ Add" else "+ Assign",
                        style = StudioTypography.Caption.copy(
                            color = StudioColors.Primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        if (isPopulated) {
            slotChildren.forEach { child ->
                val childDef = ComponentRegistry.findByNode(child)
                val childLabel = childDef?.displayName ?: (child::class.simpleName?.replace("Node", "") ?: "Component")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.dispatch(WorkspaceIntent.SelectNode(child.id)) },
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "↳ $childLabel",
                            style = StudioTypography.UIBody.copy(fontWeight = FontWeight.Medium),
                            color = StudioColors.TextPrimary
                        )
                        Text(
                            text = "#${child.id.value.takeLast(4)}",
                            style = StudioTypography.Caption.copy(color = StudioColors.TextMuted)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .clickable {
                                if (slot.cardinality == SlotCardinality.List) {
                                    viewModel.dispatch(WorkspaceIntent.RemoveNode(child.id))
                                } else {
                                    viewModel.dispatch(
                                        WorkspaceIntent.SetSlot(
                                            parentId = selectedNode.id,
                                            slotName = slot.id,
                                            node = null
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            style = StudioTypography.Caption.copy(
                                color = StudioColors.TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Empty (not assigned)",
                style = StudioTypography.Caption.copy(color = StudioColors.TextMuted)
            )
        }
    }
}

