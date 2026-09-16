package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerticalDistribute
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent

@Composable
fun HierarchyTree(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val state = viewModel.workspaceState

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF181825))
            .border(width = 1.dp, color = Color(0xFF313244))
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "COMPONENT TREE",
            color = Color(0xFF6C7086),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        RenderTreeNode(
            node = state.rootNode,
            depth = 0,
            selectedId = if (viewModel.isInteractiveMode) null else state.selectedNodeId,
            isInteractive = viewModel.isInteractiveMode,
            onSelect = {
                if (!viewModel.isInteractiveMode) {
                    viewModel.dispatch(WorkspaceIntent.SelectNode(it))
                }
            }
        )
    }
}

@Composable
private fun RenderTreeNode(
    node: ComposableNode,
    depth: Int,
    selectedId: NodeId?,
    isInteractive: Boolean,
    onSelect: (NodeId) -> Unit,
    slotLabel: String? = null
) {
    val isSelected = node.id == selectedId
    val bgColor = if (isSelected) Color(0xFF313244) else Color.Transparent
    val accentColor = if (isSelected) Color(0xFFCBA6F7) else Color(0xFFCDD6F4)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(enabled = !isInteractive) { onSelect(node.id) }
            .padding(start = (depth * 14).dp, top = 4.dp, bottom = 4.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Material Icon
        Icon(
            imageVector = getNodeIcon(node),
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(14.dp)
        )

        // Slot label (if inside a named slot)
        if (slotLabel != null) {
            Text(
                text = "[$slotLabel]",
                color = Color(0xFFF9E2AF),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Component name
        Text(
            text = getNodeLabel(node),
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        // ID tag
        Text(
            text = node.id.value.takeLast(6),
            color = Color(0xFF585B70),
            fontSize = 9.sp
        )
    }

    // Recursively render children & slots
    when (node) {
        is ComposableNode.ColumnNode -> node.children.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.RowNode -> node.children.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.BoxNode -> node.children.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.SurfaceNode -> node.children.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.CardNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ElevatedCardNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.OutlinedCardNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ButtonNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ElevatedButtonNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.FilledTonalButtonNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.OutlinedButtonNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.TextButtonNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.IconButtonNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.FloatingActionButtonNode -> node.content.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ScaffoldNode -> {
            node.topBar?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "topBar") }
            node.bottomBar?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "bottomBar") }
            node.floatingActionButton?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "fab") }
            node.content?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "content") }
        }
        is ComposableNode.TopAppBarNode -> {
            RenderTreeNode(node.title, depth + 1, selectedId, isInteractive, onSelect, "title")
            node.navigationIcon?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "navIcon") }
            node.actions.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "action") }
        }
        is ComposableNode.NavigationBarNode -> node.items.forEach { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.TextFieldNode -> {
            node.leadingIcon?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "leading") }
            node.trailingIcon?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "trailing") }
        }
        is ComposableNode.OutlinedTextFieldNode -> {
            node.leadingIcon?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "leading") }
            node.trailingIcon?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "trailing") }
        }
        is ComposableNode.NavigationBarItemNode -> {
            RenderTreeNode(node.icon, depth + 1, selectedId, isInteractive, onSelect, "icon")
            node.label?.let { RenderTreeNode(it, depth + 1, selectedId, isInteractive, onSelect, "label") }
        }
        else -> Unit
    }
}

private fun getNodeLabel(node: ComposableNode): String = when (node) {
    is ComposableNode.ColumnNode -> "Column"
    is ComposableNode.RowNode -> "Row"
    is ComposableNode.BoxNode -> "Box"
    is ComposableNode.SurfaceNode -> "Surface"
    is ComposableNode.CardNode -> "Card"
    is ComposableNode.ElevatedCardNode -> "Elevated Card"
    is ComposableNode.OutlinedCardNode -> "Outlined Card"
    is ComposableNode.ButtonNode -> "Button"
    is ComposableNode.ElevatedButtonNode -> "Elevated Button"
    is ComposableNode.FilledTonalButtonNode -> "Tonal Button"
    is ComposableNode.OutlinedButtonNode -> "Outlined Button"
    is ComposableNode.TextButtonNode -> "Text Button"
    is ComposableNode.IconButtonNode -> "Icon Button"
    is ComposableNode.FloatingActionButtonNode -> "FAB"
    is ComposableNode.TextNode -> "Text: \"${node.text.take(12)}\""
    is ComposableNode.TextFieldNode -> "TextField: ${node.label ?: ""}"
    is ComposableNode.OutlinedTextFieldNode -> "OutlinedTextField: ${node.label ?: ""}"
    is ComposableNode.CheckboxNode -> "Checkbox"
    is ComposableNode.SwitchNode -> "Switch"
    is ComposableNode.RadioButtonNode -> "RadioButton"
    is ComposableNode.SliderNode -> "Slider"
    is ComposableNode.CircularProgressIndicatorNode -> "CircularProgress"
    is ComposableNode.LinearProgressIndicatorNode -> "LinearProgress"
    is ComposableNode.SpacerNode -> "Spacer"
    is ComposableNode.HorizontalDividerNode -> "HorizontalDivider"
    is ComposableNode.VerticalDividerNode -> "VerticalDivider"
    is ComposableNode.ScaffoldNode -> "Scaffold"
    is ComposableNode.TopAppBarNode -> "TopAppBar"
    is ComposableNode.NavigationBarNode -> "NavigationBar"
    is ComposableNode.NavigationBarItemNode -> "NavItem"
}

private fun getNodeIcon(node: ComposableNode): ImageVector = when (node) {
    is ComposableNode.ColumnNode -> Icons.Default.ViewColumn
    is ComposableNode.RowNode -> Icons.Default.ViewStream
    is ComposableNode.BoxNode -> Icons.Default.Layers
    is ComposableNode.SurfaceNode -> Icons.Default.WebAsset
    is ComposableNode.CardNode -> Icons.Default.CropPortrait
    is ComposableNode.ElevatedCardNode -> Icons.AutoMirrored.Filled.FeaturedPlayList
    is ComposableNode.OutlinedCardNode -> Icons.Default.CheckBoxOutlineBlank
    is ComposableNode.ButtonNode -> Icons.Default.SmartButton
    is ComposableNode.ElevatedButtonNode -> Icons.Default.AdsClick
    is ComposableNode.FilledTonalButtonNode -> Icons.Default.CropLandscape
    is ComposableNode.OutlinedButtonNode -> Icons.Default.CropLandscape
    is ComposableNode.TextButtonNode -> Icons.Default.TextFormat
    is ComposableNode.IconButtonNode -> Icons.Default.TouchApp
    is ComposableNode.FloatingActionButtonNode -> Icons.Default.AddCircle
    is ComposableNode.TextNode -> Icons.Default.TextFields
    is ComposableNode.TextFieldNode, is ComposableNode.OutlinedTextFieldNode -> Icons.Default.EditNote
    is ComposableNode.CheckboxNode -> Icons.Default.CheckBox
    is ComposableNode.SwitchNode -> Icons.Default.ToggleOn
    is ComposableNode.RadioButtonNode -> Icons.Default.RadioButtonChecked
    is ComposableNode.SliderNode -> Icons.Default.LinearScale
    is ComposableNode.CircularProgressIndicatorNode -> Icons.Default.Autorenew
    is ComposableNode.LinearProgressIndicatorNode -> Icons.Default.HorizontalRule
    is ComposableNode.SpacerNode -> Icons.Default.SpaceBar
    is ComposableNode.HorizontalDividerNode -> Icons.Default.HorizontalDistribute
    is ComposableNode.VerticalDividerNode -> Icons.Default.VerticalDistribute
    is ComposableNode.ScaffoldNode -> Icons.Default.Tab
    is ComposableNode.TopAppBarNode -> Icons.Default.Menu
    is ComposableNode.NavigationBarNode -> Icons.Default.Navigation
    is ComposableNode.NavigationBarItemNode -> Icons.Default.TouchApp
}
