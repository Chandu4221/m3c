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
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementHorizontalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementVerticalDef
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.TypographyToken
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
            .background(Color(0xFF181825))
            .border(width = 1.dp, color = Color(0xFF313244))
            .padding(14.dp)
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
                            .background(Color(0xFF313244)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color(0xFFCBA6F7),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Interactive Mode Active",
                        color = Color(0xFFCDD6F4),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Styling edits are locked during interactive preview. Click components on the canvas to test buttons, inputs, and states directly.",
                        color = Color(0xFF6C7086),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { viewModel.updateInteractiveMode(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCBA6F7)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesignServices,
                            contentDescription = null,
                            tint = Color(0xFF11111B),
                            modifier = Modifier.size(14.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "Switch to Design Mode",
                            color = Color(0xFF11111B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            return@Column
        }

        // 2. Empty Selection State
        if (selectedNode == null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select any component on canvas to inspect properties",
                    color = Color(0xFF6C7086),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return@Column
        }

        // 3. Selected Node Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = selectedNode::class.simpleName?.replace("Node", "") ?: "Component",
                    color = Color(0xFFCBA6F7),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${selectedNode.id.value}",
                    color = Color(0xFF6C7086),
                    fontSize = 10.sp
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF313244)))

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
                InspectorField(label = "Single Line") {
                    Switch(
                        checked = selectedNode.singleLine,
                        onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(singleLine = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFCBA6F7))
                    )
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
                InspectorField(label = "Propagate Min Constraints") {
                    Switch(
                        checked = selectedNode.propagateMinConstraints,
                        onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(propagateMinConstraints = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFCBA6F7))
                    )
                }
            }

            is ComposableNode.ButtonNode -> {
                InspectorField(label = "Enabled") {
                    Switch(
                        checked = selectedNode.enabled,
                        onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFCBA6F7))
                    )
                }
            }

            is ComposableNode.SwitchNode -> {
                InspectorField(label = "Checked") {
                    Switch(
                        checked = selectedNode.checked,
                        onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(checked = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFCBA6F7))
                    )
                }
                InspectorField(label = "Enabled") {
                    Switch(
                        checked = selectedNode.enabled,
                        onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(enabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFCBA6F7))
                    )
                }
            }

            is ComposableNode.CheckboxNode -> {
                InspectorField(label = "Checked") {
                    Switch(
                        checked = selectedNode.checked,
                        onCheckedChange = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(checked = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFCBA6F7))
                    )
                }
            }

            else -> {
                Text(
                    text = "No custom properties for this component type.",
                    color = Color(0xFF6C7086),
                    fontSize = 11.sp
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF313244)))

        // 5. Modifiers Section
        ModifierInspector(viewModel = viewModel, node = selectedNode)
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
            color = Color(0xFFA6ADC8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
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
        textStyle = TextStyle(color = Color(0xFFCDD6F4), fontSize = 13.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFFCDD6F4),
            unfocusedTextColor = Color(0xFFCDD6F4),
            focusedContainerColor = Color(0xFF1E1E2E),
            unfocusedContainerColor = Color(0xFF181825),
            focusedBorderColor = Color(0xFFCBA6F7),
            unfocusedBorderColor = Color(0xFF313244),
            cursorColor = Color(0xFFCBA6F7)
        )
    )
}

@Composable
fun <T : Enum<T>> EnumSelector(
    values: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        values.take(6).chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowItems.forEach { item ->
                    val isChosen = item == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isChosen) Color(0xFFCBA6F7) else Color(0xFF1E1E2E))
                            .clickable { onSelect(item) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.name,
                            color = if (isChosen) Color(0xFF1E1E2E) else Color(0xFFCDD6F4),
                            fontSize = 10.sp,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
