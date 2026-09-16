package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import dev.chandradsl.m3c.app.desktop.state.TreeDropPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
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
    val selectedNodeId = state.selectedNodeId

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(StudioColors.PanelSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle)
            .padding(14.dp)
    ) {
        // Tree Header
        Text(
            text = "COMPONENT TREE",
            style = StudioTypography.SectionHeader,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        // Selected Node Quick Action Bar
        if (selectedNodeId != null && selectedNodeId != state.rootNode.id && !viewModel.isInteractiveMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioColors.CardSurface)
                    .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Move Up
                    TreeActionButton(
                        icon = Icons.Default.ArrowUpward,
                        tooltip = "Move Up",
                        enabled = viewModel.canMoveUp(selectedNodeId),
                        onClick = { viewModel.moveNodeUp(selectedNodeId) }
                    )
                    // Move Down
                    TreeActionButton(
                        icon = Icons.Default.ArrowDownward,
                        tooltip = "Move Down",
                        enabled = viewModel.canMoveDown(selectedNodeId),
                        onClick = { viewModel.moveNodeDown(selectedNodeId) }
                    )
                    // Duplicate
                    TreeActionButton(
                        icon = Icons.Default.ContentCopy,
                        tooltip = "Duplicate",
                        enabled = true,
                        onClick = { viewModel.duplicateNode(selectedNodeId) }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Outdent (Move out of parent container)
                    TreeTextButton(
                        text = "Out",
                        enabled = viewModel.canMoveOut(selectedNodeId)
                    ) { viewModel.moveNodeOut(selectedNodeId) }
                    // Indent (Move into preceding container)
                    TreeTextButton(
                        text = "In",
                        enabled = viewModel.canMoveIn(selectedNodeId)
                    ) { viewModel.moveNodeIn(selectedNodeId) }
                    // Wrap in Column
                    TreeTextButton(text = "Col") { viewModel.wrapInContainer(selectedNodeId, "Column") }
                    // Wrap in Row
                    TreeTextButton(text = "Row") { viewModel.wrapInContainer(selectedNodeId, "Row") }
                    // Delete
                    TreeActionButton(
                        icon = Icons.Default.Delete,
                        tooltip = "Delete",
                        enabled = true,
                        tint = StudioColors.Error,
                        onClick = { viewModel.deleteSelectedNode() }
                    )
                }
            }
        }

        // Scrollable Tree
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RenderTreeNode(
                viewModel = viewModel,
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
}

@Composable
private fun TreeActionButton(
    icon: ImageVector,
    tooltip: String,
    enabled: Boolean,
    tint: Color = StudioColors.TextPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (enabled) StudioColors.ActiveSurface else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (enabled) tint else StudioColors.TextMuted.copy(alpha = 0.4f),
            modifier = Modifier.size(StudioSizes.IconSmall)
        )
    }
}

@Composable
private fun TreeTextButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (enabled) StudioColors.ActiveSurface else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (enabled) StudioColors.BorderSubtle else StudioColors.BorderSubtle.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = StudioTypography.Badge.copy(
                color = if (enabled) StudioColors.TextSecondary else StudioColors.TextMuted.copy(alpha = 0.35f)
            )
        )
    }
}

@Composable
private fun RenderTreeNode(
    viewModel: StudioViewModel,
    node: ComposableNode,
    depth: Int,
    selectedId: NodeId?,
    isInteractive: Boolean,
    onSelect: (NodeId) -> Unit,
    slotLabel: String? = null
) {
    var itemPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var itemBoundsInWindow by remember { mutableStateOf<Rect?>(null) }

    val isSelected = node.id == selectedId
    val isRoot = node.id == viewModel.workspaceState.rootNode.id
    val isDraggable = !isInteractive && !isRoot

    val activeDrag = viewModel.activeTreeDragNode
    val isBeingDragged = activeDrag?.nodeId == node.id
    val isDropTarget = viewModel.treeDropTargetId == node.id
    val dropPos = if (isDropTarget) viewModel.treeDropPosition else null
    val isInsideTarget = isDropTarget && dropPos == TreeDropPosition.INSIDE

    // Real-time hover tracking when another tree node is actively dragged
    LaunchedEffect(viewModel.dragPointerOffset, activeDrag) {
        if (activeDrag != null && !isBeingDragged) {
            val bounds = itemBoundsInWindow
            if (bounds != null && bounds.contains(viewModel.dragPointerOffset)) {
                val relY = viewModel.dragPointerOffset.y - bounds.top
                val h = bounds.height
                val isContainer = viewModel.isContainerNode(node)
                val pos = when {
                    isContainer && relY in (h * 0.25f)..(h * 0.75f) -> TreeDropPosition.INSIDE
                    relY < (if (isContainer) h * 0.25f else h * 0.5f) -> TreeDropPosition.ABOVE
                    else -> TreeDropPosition.BELOW
                }
                viewModel.updateTreeDropTarget(node.id, pos)
            }
        }
    }

    val bgColor = when {
        isInsideTarget -> StudioColors.Success.copy(alpha = 0.15f)
        isBeingDragged -> StudioColors.ActiveSurface.copy(alpha = 0.35f)
        isSelected -> StudioColors.ActiveSurface
        else -> Color.Transparent
    }
    val borderColor = when {
        isInsideTarget -> StudioColors.Success
        isSelected -> StudioColors.BorderActive.copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    val accentColor = when {
        isInsideTarget -> StudioColors.Success
        isSelected -> StudioColors.Primary
        else -> StudioColors.TextPrimary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                itemBoundsInWindow = coords.boundsInWindow()
                itemPositionInWindow = coords.positionInWindow()
            }
    ) {
        // Insertion indicator ABOVE node
        if (isDropTarget && dropPos == TreeDropPosition.ABOVE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (depth * 16).dp, top = 2.dp, bottom = 2.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioColors.Primary)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(StudioColors.Primary)
                )
                Text(
                    text = "↑ Insert Before",
                    style = StudioTypography.Badge.copy(color = StudioColors.Primary)
                )
            }
        }

        // Tree Node Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = if (isInsideTarget) 4.dp else 0.dp, shape = RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(bgColor)
                .border(width = if (isInsideTarget) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
                .clickable(enabled = !isInteractive) { onSelect(node.id) }
                .padding(start = (depth * 16).dp, top = 5.dp, bottom = 5.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Drag handle for non-root nodes
            if (isDraggable) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reparent or reorder",
                    tint = if (isBeingDragged) StudioColors.Primary else StudioColors.TextMuted.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(StudioSizes.IconSmall)
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)))
                        .pointerInput(node.id) {
                            detectDragGestures(
                                onDragStart = { localOffset ->
                                    val windowOffset = itemPositionInWindow + localOffset
                                    viewModel.startTreeDrag(node, windowOffset, getNodeLabel(node))
                                    onSelect(node.id)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    viewModel.updateTreeDrag(dragAmount)
                                },
                                onDragEnd = {
                                    viewModel.endTreeDrag()
                                },
                                onDragCancel = {
                                    viewModel.cancelTreeDrag()
                                }
                            )
                        }
                )
            }

            // Material Icon
            Icon(
                imageVector = getNodeIcon(node),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(StudioSizes.IconMedium)
            )

            // Slot label (if inside a named slot)
            if (slotLabel != null) {
                Text(
                    text = "[$slotLabel]",
                    style = StudioTypography.Badge.copy(color = StudioColors.Warning)
                )
            }

            // Component name
            Text(
                text = getNodeLabel(node),
                style = StudioTypography.UIBody.copy(
                    color = accentColor,
                    fontWeight = if (isSelected || isInsideTarget) FontWeight.SemiBold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f)
            )

            // "Drop inside" badge or ID tag
            if (isInsideTarget) {
                Text(
                    text = "↳ Drop Inside",
                    style = StudioTypography.Badge.copy(color = StudioColors.Success, fontWeight = FontWeight.Bold)
                )
            } else {
                Text(
                    text = node.id.value.takeLast(6),
                    style = StudioTypography.Caption
                )
            }
        }

        // Insertion indicator BELOW node
        if (isDropTarget && dropPos == TreeDropPosition.BELOW) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (depth * 16).dp, top = 2.dp, bottom = 2.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioColors.Primary)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(StudioColors.Primary)
                )
                Text(
                    text = "↓ Insert After",
                    style = StudioTypography.Badge.copy(color = StudioColors.Primary)
                )
            }
        }
    }

    // Recursively render children & slots
    when (node) {
        is ComposableNode.ColumnNode -> node.children.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.RowNode -> node.children.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.BoxNode -> node.children.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.SurfaceNode -> node.children.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.CardNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ElevatedCardNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.OutlinedCardNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ButtonNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ElevatedButtonNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.FilledTonalButtonNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.OutlinedButtonNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.TextButtonNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.IconButtonNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.FloatingActionButtonNode -> node.content.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.ScaffoldNode -> {
            node.topBar?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "topBar") }
            node.bottomBar?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "bottomBar") }
            node.floatingActionButton?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "fab") }
            node.content?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "content") }
        }
        is ComposableNode.TopAppBarNode -> {
            RenderTreeNode(viewModel, node.title, depth + 1, selectedId, isInteractive, onSelect, "title")
            node.navigationIcon?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "navIcon") }
            node.actions.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "action") }
        }
        is ComposableNode.NavigationBarNode -> node.items.forEach { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect) }
        is ComposableNode.TextFieldNode -> {
            node.leadingIcon?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "leading") }
            node.trailingIcon?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "trailing") }
        }
        is ComposableNode.OutlinedTextFieldNode -> {
            node.leadingIcon?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "leading") }
            node.trailingIcon?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "trailing") }
        }
        is ComposableNode.NavigationBarItemNode -> {
            RenderTreeNode(viewModel, node.icon, depth + 1, selectedId, isInteractive, onSelect, "icon")
            node.label?.let { RenderTreeNode(viewModel, it, depth + 1, selectedId, isInteractive, onSelect, "label") }
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
    is ComposableNode.TextNode -> "Text: \"${node.text.take(16)}\""
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
