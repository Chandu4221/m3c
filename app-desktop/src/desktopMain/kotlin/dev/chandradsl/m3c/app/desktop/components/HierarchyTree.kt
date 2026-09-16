package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.state.TreeDropPosition
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ViewAgenda
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.lazy.tree.BasicLazyTree
import org.jetbrains.jewel.foundation.lazy.tree.Tree
import org.jetbrains.jewel.foundation.lazy.tree.TreeGeneratorScope
import org.jetbrains.jewel.foundation.lazy.tree.buildTree
import org.jetbrains.jewel.foundation.lazy.tree.rememberTreeState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Badge
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.OutlinedSlimButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.theme.treeStyle

data class AstTreeNodeData(
    val node: ComposableNode,
    val slotLabel: String? = null
)

@OptIn(ExperimentalJewelApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HierarchyTree(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.workspaceState
    val selectedNodeId = state.selectedNodeId
    val tree = remember(state.rootNode) { buildJewelTree(state.rootNode) }
    val treeState = rememberTreeState()

    // Initialize all container nodes as open
    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(state.rootNode.id) {
        if (!initialized) {
            treeState.openNodes(collectAllContainerIds(state.rootNode).toList())
            initialized = true
        }
    }

    // Sync external selection with Jewel LazyTree selection
    LaunchedEffect(selectedNodeId) {
        val selKey = selectedNodeId?.value
        treeState.selectedKeys = if (selKey != null) setOf(selKey) else emptySet()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(StudioColors.PanelSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle)
            .padding(14.dp)
    ) {
        // Jewel GroupHeader with Expand/Collapse All buttons
        GroupHeader(
            text = "COMPONENT TREE",
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            endComponent = {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Tooltip(tooltip = { Text("Expand All") }) {
                        IconButton(
                            onClick = {
                                treeState.openNodes(collectAllContainerIds(state.rootNode).toList())
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand All",
                                tint = StudioColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Tooltip(tooltip = { Text("Collapse All") }) {
                        IconButton(
                            onClick = {
                                treeState.openNodes = emptySet()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Collapse All",
                                tint = StudioColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        )

        // Selected Node Quick Action Bar (Jewel Components)
        if (selectedNodeId != null && selectedNodeId != state.rootNode.id && !viewModel.isInteractiveMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioColors.CardSurface)
                    .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Tooltip(tooltip = { Text("Move Up") }) {
                        IconButton(
                            onClick = { viewModel.moveNodeUp(selectedNodeId) },
                            enabled = viewModel.canMoveUp(selectedNodeId),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Move Up",
                                tint = if (viewModel.canMoveUp(selectedNodeId)) StudioColors.TextPrimary else StudioColors.TextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                        }
                    }
                    Tooltip(tooltip = { Text("Move Down") }) {
                        IconButton(
                            onClick = { viewModel.moveNodeDown(selectedNodeId) },
                            enabled = viewModel.canMoveDown(selectedNodeId),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Move Down",
                                tint = if (viewModel.canMoveDown(selectedNodeId)) StudioColors.TextPrimary else StudioColors.TextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                        }
                    }
                    Tooltip(tooltip = { Text("Duplicate") }) {
                        IconButton(
                            onClick = { viewModel.duplicateNode(selectedNodeId) },
                            enabled = true,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate",
                                tint = StudioColors.TextPrimary,
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Tooltip(tooltip = { Text("Move out of parent container") }) {
                        OutlinedSlimButton(
                            onClick = { viewModel.moveNodeOut(selectedNodeId) },
                            enabled = viewModel.canMoveOut(selectedNodeId)
                        ) {
                            Text("Out")
                        }
                    }
                    Tooltip(tooltip = { Text("Move into preceding container") }) {
                        OutlinedSlimButton(
                            onClick = { viewModel.moveNodeIn(selectedNodeId) },
                            enabled = viewModel.canMoveIn(selectedNodeId)
                        ) {
                            Text("In")
                        }
                    }
                    Tooltip(tooltip = { Text("Wrap in Column") }) {
                        OutlinedSlimButton(
                            onClick = { viewModel.wrapInContainer(selectedNodeId, "Column") }
                        ) {
                            Text("Col")
                        }
                    }
                    Tooltip(tooltip = { Text("Wrap in Row") }) {
                        OutlinedSlimButton(
                            onClick = { viewModel.wrapInContainer(selectedNodeId, "Row") }
                        ) {
                            Text("Row")
                        }
                    }
                    Tooltip(tooltip = { Text("Delete") }) {
                        IconButton(
                            onClick = { viewModel.deleteSelectedNode() },
                            enabled = true,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = StudioColors.Error,
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                        }
                    }
                }
            }
        }

        // Jewel BasicLazyTree Component with custom chevrons to prevent missing icon artifacts
        val style = JewelTheme.treeStyle
        val colors = style.colors
        val metrics = style.metrics

        BasicLazyTree(
            tree = tree,
            elementBackgroundFocused = colors.backgroundActive,
            elementBackgroundSelectedFocused = colors.backgroundSelectedActive,
            elementBackgroundSelected = colors.backgroundSelected,
            indentSize = metrics.indentSize,
            elementBackgroundCornerSize = metrics.simpleListItemMetrics.selectionBackgroundCornerSize,
            elementPadding = metrics.simpleListItemMetrics.outerPadding,
            elementContentPadding = metrics.simpleListItemMetrics.innerPadding,
            elementMinHeight = metrics.elementMinHeight,
            chevronContentGap = metrics.chevronContentGap,
            onElementClick = { element ->
                if (!viewModel.isInteractiveMode) {
                    viewModel.dispatch(WorkspaceIntent.SelectNode(element.data.node.id))
                }
            },
            onElementDoubleClick = {},
            onSelectionChange = {},
            chevronContent = { elementState ->
                Icon(
                    imageVector = if (elementState.isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (elementState.isExpanded) "Collapse" else "Expand",
                    tint = if (elementState.isSelected) StudioColors.TextPrimary else StudioColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp),
            treeState = treeState,
            nodeContent = { element ->
                val node = element.data.node
                val slotLabel = element.data.slotLabel
                val isRoot = node.id == viewModel.workspaceState.rootNode.id
                val isDraggable = !viewModel.isInteractiveMode && !isRoot

                var itemBoundsInWindow by remember { mutableStateOf<Rect?>(null) }
                var itemPositionInWindow by remember { mutableStateOf(Offset.Zero) }

                val activeDrag = viewModel.activeTreeDragNode
                val isBeingDragged = activeDrag?.nodeId == node.id
                val isDropTarget = viewModel.treeDropTargetId == node.id
                val dropPos = if (isDropTarget) viewModel.treeDropPosition else null
                val isInsideTarget = (isDropTarget && dropPos == TreeDropPosition.INSIDE) ||
                    (viewModel.hoveredCanvasParentId == node.id && viewModel.activeDragItem != null)

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
                                .padding(vertical = 2.dp, horizontal = 4.dp),
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

                    // Tree Node Row Content
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isInsideTarget -> StudioColors.Success.copy(alpha = 0.15f)
                                    isBeingDragged -> StudioColors.ActiveSurface.copy(alpha = 0.35f)
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = if (isInsideTarget) 1.5.dp else 0.dp,
                                color = if (isInsideTarget) StudioColors.Success else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .then(
                                if (isDraggable) {
                                    Modifier.pointerInput(node.id) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            var isDragStarted = false
                                            var totalMovement = Offset.Zero
                                            val touchSlop = viewConfiguration.touchSlop

                                            try {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                                    if (change.changedToUp()) {
                                                        if (isDragStarted) {
                                                            viewModel.endTreeDrag()
                                                        }
                                                        break
                                                    }

                                                    val dragAmount = change.position - change.previousPosition
                                                    totalMovement += dragAmount

                                                    if (!isDragStarted) {
                                                        if (totalMovement.getDistance() > touchSlop) {
                                                            isDragStarted = true
                                                            val windowOffset = itemPositionInWindow + change.position
                                                            viewModel.startTreeDrag(node, windowOffset, getNodeLabel(node))
                                                            change.consume()
                                                        }
                                                    } else {
                                                        change.consume()
                                                        viewModel.updateTreeDrag(dragAmount)
                                                    }
                                                }
                                            } finally {
                                                if (isDragStarted && viewModel.activeTreeDragNode != null) {
                                                    viewModel.cancelTreeDrag()
                                                }
                                            }
                                        }
                                    }
                                } else Modifier
                            )
                            .padding(vertical = 3.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Component Icon
                        Icon(
                            imageVector = getNodeIcon(node),
                            contentDescription = null,
                            tint = if (isInsideTarget) StudioColors.Success else StudioColors.Primary,
                            modifier = Modifier.size(StudioSizes.IconSmall)
                        )

                        // Slot badge (Jewel Badge)
                        if (slotLabel != null) {
                            Badge {
                                Text(slotLabel)
                            }
                        }

                        // Component Label
                        Text(
                            text = getNodeLabel(node),
                            style = StudioTypography.UIBody.copy(
                                fontWeight = if (isInsideTarget) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Inside drop badge or subtle node ID
                        if (isInsideTarget) {
                            Text(
                                text = "↳ Drop Inside",
                                style = StudioTypography.Badge.copy(color = StudioColors.Success, fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Text(
                                text = "#${node.id.value.takeLast(4)}",
                                style = StudioTypography.Caption.copy(
                                    color = StudioColors.TextMuted.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    // Insertion indicator BELOW node
                    if (isDropTarget && dropPos == TreeDropPosition.BELOW) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
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
            }
        )
    }
}

private fun TreeGeneratorScope<AstTreeNodeData>.populateAstTree(node: ComposableNode, slotLabel: String?) {
    val children = getChildNodesWithSlots(node)
    val itemData = AstTreeNodeData(node, slotLabel)
    if (children.isEmpty()) {
        addLeaf(itemData, id = node.id.value)
    } else {
        addNode(itemData, id = node.id.value) {
            children.forEach { (child, childSlot) ->
                populateAstTree(child, childSlot)
            }
        }
    }
}

fun buildJewelTree(rootNode: ComposableNode): Tree<AstTreeNodeData> = buildTree {
    populateAstTree(rootNode, null)
}

fun getChildNodesWithSlots(node: ComposableNode): List<Pair<ComposableNode, String?>> = when (node) {
    is ComposableNode.ColumnNode -> node.children.map { it to null }
    is ComposableNode.RowNode -> node.children.map { it to null }
    is ComposableNode.BoxNode -> node.children.map { it to null }
    is ComposableNode.SurfaceNode -> node.children.map { it to null }
    is ComposableNode.CardNode -> node.content.map { it to null }
    is ComposableNode.ElevatedCardNode -> node.content.map { it to null }
    is ComposableNode.OutlinedCardNode -> node.content.map { it to null }
    is ComposableNode.ButtonNode -> node.content.map { it to null }
    is ComposableNode.ElevatedButtonNode -> node.content.map { it to null }
    is ComposableNode.FilledTonalButtonNode -> node.content.map { it to null }
    is ComposableNode.OutlinedButtonNode -> node.content.map { it to null }
    is ComposableNode.TextButtonNode -> node.content.map { it to null }
    is ComposableNode.IconButtonNode -> node.content.map { it to null }
    is ComposableNode.FloatingActionButtonNode -> node.content.map { it to null }
    is ComposableNode.ScaffoldNode -> buildList {
        node.topBar?.let { add(it to "topBar") }
        node.bottomBar?.let { add(it to "bottomBar") }
        node.floatingActionButton?.let { add(it to "fab") }
        node.content?.let { add(it to "content") }
    }
    is ComposableNode.TopAppBarNode -> buildList {
        add(node.title to "title")
        node.navigationIcon?.let { add(it to "navIcon") }
        node.actions.forEach { add(it to "action") }
    }
    is ComposableNode.NavigationBarNode -> node.items.map { it to null }
    is ComposableNode.NavigationBarItemNode -> buildList {
        add(node.icon to "icon")
        node.label?.let { add(it to "label") }
    }
    is ComposableNode.TextFieldNode -> buildList {
        node.leadingIcon?.let { add(it to "leading") }
        node.trailingIcon?.let { add(it to "trailing") }
    }
    is ComposableNode.OutlinedTextFieldNode -> buildList {
        node.leadingIcon?.let { add(it to "leading") }
        node.trailingIcon?.let { add(it to "trailing") }
    }
    else -> emptyList()
}

fun collectAllContainerIds(node: ComposableNode): Set<String> {
    val ids = mutableSetOf<String>()
    fun walk(n: ComposableNode) {
        val children = getChildNodesWithSlots(n)
        if (children.isNotEmpty()) {
            ids.add(n.id.value)
            children.forEach { walk(it.first) }
        }
    }
    walk(node)
    return ids
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
    is ComposableNode.TopAppBarNode -> Icons.Default.ViewAgenda
    is ComposableNode.NavigationBarNode -> Icons.Default.Navigation
    is ComposableNode.NavigationBarItemNode -> Icons.Default.TouchApp
}
