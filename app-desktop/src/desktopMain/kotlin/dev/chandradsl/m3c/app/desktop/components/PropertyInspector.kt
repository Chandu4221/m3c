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
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.OutlinedSlimButton
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementHorizontalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementVerticalDef
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
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

                    DefaultButton(
                        onClick = { viewModel.updateInteractiveMode(false) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DesignServices,
                                contentDescription = null,
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                            Text(
                                text = "Switch to Design Mode",
                                style = StudioTypography.UIBody.copy(fontWeight = FontWeight.Bold)
                            )
                        }
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
            is ComposableNode.ScaffoldNode -> {
                InspectorField(label = "Top App Bar Slot") {
                    SlotStatusRow(
                        slotName = "Top App Bar",
                        childNode = selectedNode.topBar,
                        onAdd = {
                            val newTopBar = ComposableNode.TopAppBarNode(
                                id = NodeId.generate("top_bar"),
                                title = ComposableNode.TextNode(
                                    id = NodeId.generate("title"),
                                    text = "Page Title",
                                    typography = TypographyToken.TitleLarge
                                ),
                                containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "top_bar", newTopBar))
                        },
                        onSelect = { selectedNode.topBar?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "top_bar", null)) }
                    )
                }

                InspectorField(label = "Bottom Navigation Bar Slot") {
                    SlotStatusRow(
                        slotName = "Bottom Bar",
                        childNode = selectedNode.bottomBar,
                        onAdd = {
                            val newNav = ComposableNode.NavigationBarNode(
                                id = NodeId.generate("nav_bar"),
                                items = listOf(
                                    ComposableNode.NavigationBarItemNode(
                                        id = NodeId.generate("nav_item"),
                                        selected = true,
                                        icon = ComposableNode.IconNode(iconName = "Home", contentDescription = "Home"),
                                        label = ComposableNode.TextNode(text = "Home")
                                    ),
                                    ComposableNode.NavigationBarItemNode(
                                        id = NodeId.generate("nav_item"),
                                        selected = false,
                                        icon = ComposableNode.IconNode(iconName = "Person", contentDescription = "Profile"),
                                        label = ComposableNode.TextNode(text = "Profile")
                                    )
                                )
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "bottom_bar", newNav))
                        },
                        onSelect = { selectedNode.bottomBar?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "bottom_bar", null)) }
                    )
                }

                InspectorField(label = "Floating Action Button Slot") {
                    SlotStatusRow(
                        slotName = "FAB",
                        childNode = selectedNode.floatingActionButton,
                        onAdd = {
                            val newFab = ComposableNode.FloatingActionButtonNode(
                                id = NodeId.generate("fab"),
                                shape = ShapeDef.Token(ShapeToken.Large),
                                containerColor = ColorSource.Theme(ColorToken.PrimaryContainer),
                                content = listOf(ComposableNode.IconNode(iconName = "Add", contentDescription = "Add"))
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "floating_action_button", newFab))
                        },
                        onSelect = { selectedNode.floatingActionButton?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "floating_action_button", null)) }
                    )
                }

                selectedNode.content?.let { contentNode ->
                    InspectorField(label = "Main Content Body") {
                        OutlinedButton(
                            onClick = { viewModel.dispatch(WorkspaceIntent.SelectNode(contentNode.id)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("↳ Select Content (${contentNode::class.simpleName?.replace("Node", "")})")
                        }
                    }
                }
            }

            is ComposableNode.TopAppBarNode -> {
                val currentTitleText = (selectedNode.title as? ComposableNode.TextNode)?.text ?: ""
                InspectorField(label = "Title Text") {
                    InspectorTextInput(
                        value = currentTitleText,
                        onValueChange = { newTxt ->
                            val newTitleNode = (selectedNode.title as? ComposableNode.TextNode)?.copy(text = newTxt)
                                ?: ComposableNode.TextNode(id = NodeId.generate("txt"), text = newTxt, typography = TypographyToken.TitleLarge)
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(title = newTitleNode)))
                        }
                    )
                }

                val currentColor = (selectedNode.containerColor as? ColorSource.Theme)?.token
                InspectorField(label = "Container Background") {
                    ColorTokenSelector(
                        selectedToken = currentColor,
                        onSelect = { tok ->
                            val src = tok?.let { ColorSource.Theme(it) }
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(containerColor = src)))
                        }
                    )
                }

                InspectorField(label = "Navigation Icon (Back Button)") {
                    SlotStatusRow(
                        slotName = "Back Navigation Icon",
                        childNode = selectedNode.navigationIcon,
                        onAdd = {
                            val backBtn = ComposableNode.IconButtonNode(
                                id = NodeId.generate("nav_back"),
                                content = listOf(ComposableNode.IconNode(iconName = "ArrowBack", contentDescription = "Back"))
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "navigation_icon", backBtn))
                        },
                        onSelect = { selectedNode.navigationIcon?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "navigation_icon", null)) }
                    )
                }

                InspectorField(label = "Action Icons (${selectedNode.actions.size})") {
                    DefaultButton(
                        onClick = {
                            val newAction = ComposableNode.IconButtonNode(
                                id = NodeId.generate("action_btn"),
                                content = listOf(ComposableNode.IconNode(iconName = "MoreVert", contentDescription = "More"))
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "actions", newAction))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add Action Button")
                    }
                }
            }

            is ComposableNode.AlertDialogNode -> {
                val currentTitle = (selectedNode.title as? ComposableNode.TextNode)?.text ?: ""
                InspectorField(label = "Dialog Title") {
                    InspectorTextInput(
                        value = currentTitle,
                        onValueChange = { newTxt ->
                            val newTitle = ComposableNode.TextNode(
                                id = selectedNode.title?.id ?: NodeId.generate("title"),
                                text = newTxt,
                                typography = TypographyToken.HeadlineSmall
                            )
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(title = newTitle)))
                        }
                    )
                }

                val currentBody = (selectedNode.text as? ComposableNode.TextNode)?.text ?: ""
                InspectorField(label = "Dialog Message") {
                    InspectorTextInput(
                        value = currentBody,
                        onValueChange = { newTxt ->
                            val newText = ComposableNode.TextNode(
                                id = selectedNode.text?.id ?: NodeId.generate("body"),
                                text = newTxt,
                                typography = TypographyToken.BodyMedium
                            )
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(text = newText)))
                        }
                    )
                }

                InspectorField(label = "Dialog Icon") {
                    SlotStatusRow(
                        slotName = "Icon",
                        childNode = selectedNode.icon,
                        onAdd = {
                            val newIcon = ComposableNode.IconNode(
                                id = NodeId.generate("dialog_icon"),
                                iconName = "Info",
                                contentDescription = "Dialog Icon"
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "icon", newIcon))
                        },
                        onSelect = { selectedNode.icon?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "icon", null)) }
                    )
                }

                val confirmBtnText = ((selectedNode.confirmButton as? ComposableNode.TextButtonNode)?.content?.firstOrNull() as? ComposableNode.TextNode)?.text
                    ?: ((selectedNode.confirmButton as? ComposableNode.ButtonNode)?.content?.firstOrNull() as? ComposableNode.TextNode)?.text
                    ?: "Confirm"
                InspectorField(label = "Confirm Button Label") {
                    InspectorTextInput(
                        value = confirmBtnText,
                        onValueChange = { newLabel ->
                            val newBtn = ComposableNode.TextButtonNode(
                                id = selectedNode.confirmButton?.id ?: NodeId.generate("confirm_btn"),
                                content = listOf(ComposableNode.TextNode(text = newLabel))
                            )
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(confirmButton = newBtn)))
                        }
                    )
                }

                InspectorField(label = "Dismiss / Cancel Button") {
                    SlotStatusRow(
                        slotName = "Dismiss Button",
                        childNode = selectedNode.dismissButton,
                        onAdd = {
                            val dismissBtn = ComposableNode.TextButtonNode(
                                id = NodeId.generate("dismiss_btn"),
                                content = listOf(ComposableNode.TextNode(text = "Cancel"))
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "dismiss_button", dismissBtn))
                        },
                        onSelect = { selectedNode.dismissButton?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "dismiss_button", null)) }
                    )
                }

                val currentColor = (selectedNode.containerColor as? ColorSource.Theme)?.token
                InspectorField(label = "Dialog Background") {
                    ColorTokenSelector(
                        selectedToken = currentColor,
                        onSelect = { tok ->
                            val src = tok?.let { ColorSource.Theme(it) }
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(containerColor = src)))
                        }
                    )
                }
            }

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
                InspectorField(label = "Leading Icon") {
                    SlotStatusRow(
                        slotName = "Leading Icon",
                        childNode = selectedNode.leadingIcon,
                        onAdd = {
                            val iconNode = ComposableNode.IconNode(
                                id = NodeId.generate("lead_icon"),
                                iconName = "Search",
                                contentDescription = "Search"
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "leading_icon", iconNode))
                        },
                        onSelect = { selectedNode.leadingIcon?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "leading_icon", null)) }
                    )
                }
                InspectorField(label = "Trailing Icon") {
                    SlotStatusRow(
                        slotName = "Trailing Icon",
                        childNode = selectedNode.trailingIcon,
                        onAdd = {
                            val iconNode = ComposableNode.IconNode(
                                id = NodeId.generate("trail_icon"),
                                iconName = "Clear",
                                contentDescription = "Clear"
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "trailing_icon", iconNode))
                        },
                        onSelect = { selectedNode.trailingIcon?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "trailing_icon", null)) }
                    )
                }
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
                InspectorField(label = "Leading Icon") {
                    SlotStatusRow(
                        slotName = "Leading Icon",
                        childNode = selectedNode.leadingIcon,
                        onAdd = {
                            val iconNode = ComposableNode.IconNode(
                                id = NodeId.generate("lead_icon"),
                                iconName = "Search",
                                contentDescription = "Search"
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "leading_icon", iconNode))
                        },
                        onSelect = { selectedNode.leadingIcon?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "leading_icon", null)) }
                    )
                }
                InspectorField(label = "Trailing Icon") {
                    SlotStatusRow(
                        slotName = "Trailing Icon",
                        childNode = selectedNode.trailingIcon,
                        onAdd = {
                            val iconNode = ComposableNode.IconNode(
                                id = NodeId.generate("trail_icon"),
                                iconName = "Clear",
                                contentDescription = "Clear"
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "trailing_icon", iconNode))
                        },
                        onSelect = { selectedNode.trailingIcon?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "trailing_icon", null)) }
                    )
                }
            }

            is ComposableNode.CardNode -> {
                InspectorField(label = "Elevation") {
                    DpOptionChips(
                        options = listOf(0f, 1f, 2f, 4f, 8f),
                        selected = selectedNode.elevation.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(elevation = DpVal(it)))) }
                    )
                }
                InspectorField(label = "Corner Shape") {
                    EnumSelector(
                        values = ShapeToken.entries,
                        selected = (selectedNode.shape as? ShapeDef.Token)?.token ?: ShapeToken.Medium,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(shape = ShapeDef.Token(it)))) }
                    )
                }
                CardTemplateActions(cardId = selectedNode.id, viewModel = viewModel)
            }

            is ComposableNode.ElevatedCardNode -> {
                InspectorField(label = "Elevation") {
                    DpOptionChips(
                        options = listOf(2f, 4f, 6f, 8f, 12f),
                        selected = selectedNode.elevation.value,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(elevation = DpVal(it)))) }
                    )
                }
                InspectorField(label = "Corner Shape") {
                    EnumSelector(
                        values = ShapeToken.entries,
                        selected = (selectedNode.shape as? ShapeDef.Token)?.token ?: ShapeToken.Medium,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(shape = ShapeDef.Token(it)))) }
                    )
                }
                CardTemplateActions(cardId = selectedNode.id, viewModel = viewModel)
            }

            is ComposableNode.OutlinedCardNode -> {
                InspectorField(label = "Corner Shape") {
                    EnumSelector(
                        values = ShapeToken.entries,
                        selected = (selectedNode.shape as? ShapeDef.Token)?.token ?: ShapeToken.Medium,
                        onSelect = { viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(shape = ShapeDef.Token(it)))) }
                    )
                }
                CardTemplateActions(cardId = selectedNode.id, viewModel = viewModel)
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
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth()
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
                    DefaultButton(
                        onClick = {
                            val newItem = ComponentRegistry.findByType(ComponentType.NavigationBarItem)?.createDefault()
                            if (newItem != null) {
                                viewModel.dispatch(WorkspaceIntent.InsertChild(
                                    parentId = selectedNode.id,
                                    node = newItem
                                ))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Navigation Item",
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                            Text(
                                text = "Add Navigation Item",
                                style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            is ComposableNode.BottomAppBarNode -> {
                InspectorField(label = "Actions (${selectedNode.actions.size})") {
                    DefaultButton(
                        onClick = {
                            val newItem = ComponentRegistry.findByType(ComponentType.IconButton)?.createDefault()
                            if (newItem != null) {
                                viewModel.dispatch(WorkspaceIntent.InsertChild(
                                    parentId = selectedNode.id,
                                    node = newItem
                                ))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Action Button",
                                modifier = Modifier.size(StudioSizes.IconSmall)
                            )
                            Text(
                                text = "Add Action Button",
                                style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
                InspectorField(label = "Floating Action Button Slot") {
                    SlotStatusRow(
                        slotName = "FAB",
                        childNode = selectedNode.floatingActionButton,
                        onAdd = {
                            val newFab = ComposableNode.FloatingActionButtonNode(
                                id = NodeId.generate("fab"),
                                shape = ShapeDef.Token(ShapeToken.Large),
                                containerColor = ColorSource.Theme(ColorToken.PrimaryContainer),
                                content = listOf(ComposableNode.IconNode(iconName = "Add", contentDescription = "Add"))
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "floating_action_button", newFab))
                        },
                        onSelect = { selectedNode.floatingActionButton?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "floating_action_button", null)) }
                    )
                }

                val currentColor = (selectedNode.containerColor as? ColorSource.Theme)?.token
                InspectorField(label = "Container Background") {
                    ColorTokenSelector(
                        selectedToken = currentColor,
                        onSelect = { tok ->
                            val src = tok?.let { ColorSource.Theme(it) }
                            viewModel.dispatch(WorkspaceIntent.UpdateNode(selectedNode.copy(containerColor = src)))
                        }
                    )
                }
            }

            is ComposableNode.NavigationRailNode -> {
                InspectorField(label = "Header Slot") {
                    SlotStatusRow(
                        slotName = "Header",
                        childNode = selectedNode.header,
                        onAdd = {
                            val newHeader = ComposableNode.FloatingActionButtonNode(
                                id = NodeId.generate("rail_fab"),
                                shape = ShapeDef.Token(ShapeToken.Large),
                                containerColor = ColorSource.Theme(ColorToken.PrimaryContainer),
                                content = listOf(ComposableNode.IconNode(iconName = "Edit", contentDescription = "Edit"))
                            )
                            viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "header", newHeader))
                        },
                        onSelect = { selectedNode.header?.let { viewModel.dispatch(WorkspaceIntent.SelectNode(it.id)) } },
                        onRemove = { viewModel.dispatch(WorkspaceIntent.SetSlot(selectedNode.id, "header", null)) }
                    )
                }

                InspectorField(label = "Navigation Items (${selectedNode.items.size})") {
                    DefaultButton(
                        onClick = {
                            val newItem = ComposableNode.NavigationRailItemNode(
                                id = NodeId.generate("rail_item"),
                                icon = ComposableNode.IconNode(iconName = "Bookmark", contentDescription = "Bookmark"),
                                label = ComposableNode.TextNode(text = "Item"),
                                selected = false
                            )
                            viewModel.dispatch(WorkspaceIntent.InsertChild(parentId = selectedNode.id, node = newItem))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add Rail Item")
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
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth()
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
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
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
    JewelTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth()
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

@Composable
fun ColorTokenSelector(
    selectedToken: ColorToken?,
    onSelect: (ColorToken?) -> Unit
) {
    val tokens = listOf(
        null to "Default",
        ColorToken.Surface to "Surface",
        ColorToken.SurfaceContainer to "Container",
        ColorToken.SurfaceContainerLow to "Cont. Low",
        ColorToken.SurfaceContainerHigh to "Cont. High",
        ColorToken.PrimaryContainer to "Primary",
        ColorToken.SecondaryContainer to "Secondary",
        ColorToken.TertiaryContainer to "Tertiary",
        ColorToken.ErrorContainer to "Error"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        tokens.chunked(3).forEach { rowTokens ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowTokens.forEach { (tok, label) ->
                    val isChosen = tok == selectedToken
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isChosen) StudioColors.ActiveSurface else StudioColors.CardSurface)
                            .border(
                                width = 1.dp,
                                color = if (isChosen) StudioColors.Primary else StudioColors.BorderSubtle,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onSelect(tok) }
                            .padding(vertical = 5.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = StudioTypography.Caption.copy(
                                color = if (isChosen) StudioColors.Primary else StudioColors.TextPrimary,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotStatusRow(
    slotName: String,
    childNode: ComposableNode?,
    onAdd: () -> Unit,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StudioColors.CardSurface)
            .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = slotName,
                style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = if (childNode != null) "Assigned (${childNode::class.simpleName?.replace("Node", "")})" else "Not set",
                style = StudioTypography.Badge.copy(
                    color = if (childNode != null) StudioColors.Success else StudioColors.TextMuted,
                    fontSize = 9.sp
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (childNode != null) {
                OutlinedSlimButton(onClick = onSelect) {
                    Text("Select", style = StudioTypography.Caption.copy(fontSize = 10.sp))
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        style = StudioTypography.Caption.copy(color = StudioColors.TextMuted, fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                DefaultButton(onClick = onAdd) {
                    Text("+ Add", style = StudioTypography.Caption.copy(fontSize = 10.sp))
                }
            }
        }
    }
}

@Composable
private fun CardTemplateActions(
    cardId: NodeId,
    viewModel: StudioViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Card Slot Templates",
            style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val headerRow = ComposableNode.RowNode(
                        id = NodeId.generate("card_header"),
                        horizontalArrangement = ArrangementHorizontalDef.Start,
                        verticalAlignment = AlignmentVerticalDef.CenterVertically,
                        modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                        children = listOf(
                            ComposableNode.IconNode(
                                id = NodeId.generate("icon"),
                                iconName = "Star",
                                contentDescription = "Header Icon"
                            ),
                            ComposableNode.SpacerNode(
                                id = NodeId.generate("spc"),
                                modifiers = listOf(ModifierDef.Width(DpVal(8f)))
                            ),
                            ComposableNode.TextNode(
                                id = NodeId.generate("hdr_title"),
                                text = "Card Title",
                                typography = TypographyToken.TitleMedium
                            )
                        )
                    )
                    viewModel.dispatch(WorkspaceIntent.InsertChild(parentId = cardId, node = headerRow, index = 0))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Header", style = StudioTypography.Caption.copy(fontSize = 10.sp))
            }

            OutlinedButton(
                onClick = {
                    val actionsRow = ComposableNode.RowNode(
                        id = NodeId.generate("card_actions"),
                        horizontalArrangement = ArrangementHorizontalDef.End,
                        verticalAlignment = AlignmentVerticalDef.CenterVertically,
                        modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                        children = listOf(
                            ComposableNode.TextButtonNode(
                                id = NodeId.generate("btn_cancel"),
                                content = listOf(ComposableNode.TextNode(text = "Dismiss"))
                            ),
                            ComposableNode.ButtonNode(
                                id = NodeId.generate("btn_action"),
                                content = listOf(ComposableNode.TextNode(text = "Action"))
                            )
                        )
                    )
                    viewModel.dispatch(WorkspaceIntent.InsertChild(parentId = cardId, node = actionsRow))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Actions Row", style = StudioTypography.Caption.copy(fontSize = 10.sp))
            }
        }
    }
}


