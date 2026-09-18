package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.M3cScreen

/**
 * Jewel-styled Screen Tab Bar rendered above the Canvas Viewport.
 * Enables rapid screen switching, creating new screens, renaming/editing routes,
 * duplicating, and designating the start destination.
 */
@Composable
fun ScreenTabBar(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showAddModal by remember { mutableStateOf(false) }
    var editingScreen by remember { mutableStateOf<M3cScreen?>(null) }
    var showManagerModal by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(StudioColors.PanelSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Scrollable Screen Tabs
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Section label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Screens",
                        tint = StudioColors.TextSecondary,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                    Text(
                        text = "SCREENS",
                        style = StudioTypography.Caption.copy(
                            color = StudioColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                // Screen Tab Pills
                for (screen in viewModel.screens) {
                    val isActive = screen.id == viewModel.activeScreenId
                    ScreenTabPill(
                        screen = screen,
                        isActive = isActive,
                        canDelete = viewModel.screens.size > 1,
                        onSelect = { viewModel.selectScreen(screen.id) },
                        onEdit = { editingScreen = screen },
                        onDelete = { viewModel.deleteScreen(screen.id) }
                    )
                }

                // Add Screen Quick Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.CardSurface)
                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                        .clickable { showAddModal = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Screen",
                            tint = StudioColors.Primary,
                            modifier = Modifier.size(StudioSizes.IconSmall)
                        )
                        Text(
                            text = "Add",
                            style = StudioTypography.Caption.copy(
                                color = StudioColors.Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Right: Screen Manager button & count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                        .clickable { showManagerModal = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Manage Screens",
                            tint = StudioColors.TextPrimary,
                            modifier = Modifier.size(StudioSizes.IconSmall)
                        )
                        Text(
                            text = "Manage (${viewModel.screens.size})",
                            style = StudioTypography.Caption.copy(
                                color = StudioColors.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Add Screen Modal Dialog Overlay
        if (showAddModal) {
            AddScreenDialog(
                screensCount = viewModel.screens.size,
                onDismiss = { showAddModal = false },
                onAdd = { name, route, isStart ->
                    viewModel.addScreen(name, route, isStart)
                    showAddModal = false
                }
            )
        }

        // Edit Screen Modal Dialog Overlay
        editingScreen?.let { screen ->
            EditScreenDialog(
                screen = screen,
                onDismiss = { editingScreen = null },
                onSave = { newName, newRoute, isStart ->
                    viewModel.updateScreen(screen.id, newName, newRoute, isStart)
                    editingScreen = null
                }
            )
        }

        // Full Screen Manager Modal Dialog Overlay
        if (showManagerModal) {
            ScreenManagerDialog(
                viewModel = viewModel,
                onDismiss = { showManagerModal = false },
                onAddScreen = {
                    showManagerModal = false
                    showAddModal = true
                },
                onEditScreen = { screen ->
                    showManagerModal = false
                    editingScreen = screen
                }
            )
        }
    }
}

@Composable
private fun ScreenTabPill(
    screen: M3cScreen,
    isActive: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val bg = if (isActive) StudioColors.ActiveSurface else StudioColors.CardSurface
    val borderCol = if (isActive) StudioColors.Primary else StudioColors.BorderSubtle
    val textCol = if (isActive) StudioColors.TextPrimary else StudioColors.TextSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = if (isActive) StudioColors.Primary else StudioColors.TextSecondary,
            modifier = Modifier.size(StudioSizes.IconSmall)
        )

        // Screen Name
        Text(
            text = screen.name,
            style = StudioTypography.Caption.copy(
                color = textCol,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Route badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(StudioColors.BorderSubtle.copy(alpha = 0.5f))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "/${screen.route}",
                style = StudioTypography.Badge.copy(
                    color = StudioColors.TextSecondary,
                    fontSize = 9.sp
                )
            )
        }

        // Start destination marker
        if (screen.isStartDestination) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Start Destination",
                tint = StudioColors.Warning,
                modifier = Modifier.size(12.dp)
            )
        }

        // Edit button
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Screen",
            tint = StudioColors.TextSecondary,
            modifier = Modifier
                .size(12.dp)
                .clickable(onClick = onEdit)
        )

        // Quick delete button
        if (canDelete) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Screen",
                tint = StudioColors.TextSecondary,
                modifier = Modifier
                    .size(12.dp)
                    .clickable(onClick = onDelete)
            )
        }
    }
}

@Composable
private fun AddScreenDialog(
    screensCount: Int,
    onDismiss: () -> Unit,
    onAdd: (name: String, route: String, isStart: Boolean) -> Unit
) {
    var screenName by remember { mutableStateOf("Screen${screensCount + 1}") }
    var route by remember { mutableStateOf("screen_${screensCount + 1}") }
    var isStart by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clickable(enabled = false) {}
                .width(420.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(StudioColors.PanelSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Add New Screen",
                    style = StudioTypography.ModalTitle
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Screen Name (Composable)",
                        style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                    )
                    JewelTextField(
                        value = screenName,
                        onValueChange = { newName ->
                            screenName = newName
                            if (route.startsWith("screen_") || route.isBlank()) {
                                route = newName.lowercase().replace(Regex("[^a-z0-9_]"), "_")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Navigation Route",
                        style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                    )
                    JewelTextField(
                        value = route,
                        onValueChange = { route = it.lowercase().replace(Regex("[^a-z0-9_]"), "_") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isStart,
                        onCheckedChange = { isStart = it }
                    )
                    Text(
                        text = "Set as start destination",
                        style = StudioTypography.Caption
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    DefaultButton(
                        onClick = { onAdd(screenName, route, isStart) },
                        enabled = screenName.isNotBlank() && route.isNotBlank()
                    ) {
                        Text("Add Screen")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditScreenDialog(
    screen: M3cScreen,
    onDismiss: () -> Unit,
    onSave: (newName: String, newRoute: String, isStart: Boolean) -> Unit
) {
    var screenName by remember(screen) { mutableStateOf(screen.name) }
    var route by remember(screen) { mutableStateOf(screen.route) }
    var isStart by remember(screen) { mutableStateOf(screen.isStartDestination) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clickable(enabled = false) {}
                .width(420.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(StudioColors.PanelSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Edit Screen: ${screen.name}",
                    style = StudioTypography.ModalTitle
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Screen Name",
                        style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                    )
                    JewelTextField(
                        value = screenName,
                        onValueChange = { screenName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Navigation Route",
                        style = StudioTypography.Caption.copy(fontWeight = FontWeight.SemiBold)
                    )
                    JewelTextField(
                        value = route,
                        onValueChange = { route = it.lowercase().replace(Regex("[^a-z0-9_]"), "_") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isStart,
                        onCheckedChange = { isStart = it }
                    )
                    Text(
                        text = "Set as start destination",
                        style = StudioTypography.Caption
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    DefaultButton(
                        onClick = { onSave(screenName, route, isStart) },
                        enabled = screenName.isNotBlank() && route.isNotBlank()
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenManagerDialog(
    viewModel: StudioViewModel,
    onDismiss: () -> Unit,
    onAddScreen: () -> Unit,
    onEditScreen: (M3cScreen) -> Unit
) {
    val vScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clickable(enabled = false) {}
                .width(620.dp)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(StudioColors.PanelSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Project Screens Manager",
                        style = StudioTypography.ModalTitle
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = StudioColors.TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onDismiss)
                    )
                }

                Text(
                    text = "Configure screens, navigation routes, and initial launch destination for ${viewModel.projectName}.",
                    style = StudioTypography.Caption.copy(color = StudioColors.TextSecondary)
                )

                // Screens List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .verticalScroll(vScroll),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (screen in viewModel.screens) {
                        val isCurrentActive = screen.id == viewModel.activeScreenId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrentActive) StudioColors.ActiveSurface else StudioColors.CardSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrentActive) StudioColors.Primary else StudioColors.BorderSubtle,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (screen.isStartDestination) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Toggle Start Destination",
                                    tint = if (screen.isStartDestination) StudioColors.Warning else StudioColors.TextSecondary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { viewModel.setStartDestination(screen.id) }
                                )

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = screen.name,
                                            style = StudioTypography.UIBody.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (isCurrentActive) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(StudioColors.Primary.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "EDITING",
                                                    style = StudioTypography.Badge.copy(color = StudioColors.Primary, fontSize = 8.sp)
                                                )
                                            }
                                        }
                                        if (screen.isStartDestination) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(StudioColors.Warning.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "START",
                                                    style = StudioTypography.Badge.copy(color = StudioColors.Warning, fontSize = 8.sp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Route: /${screen.route}",
                                        style = StudioTypography.Caption.copy(color = StudioColors.TextSecondary)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Switch to button
                                if (!isCurrentActive) {
                                    OutlinedButton(onClick = { viewModel.selectScreen(screen.id) }) {
                                        Text("Open")
                                    }
                                }

                                // Duplicate
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StudioColors.CardSurface)
                                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                                        .clickable { viewModel.duplicateScreen(screen.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate",
                                        tint = StudioColors.TextSecondary,
                                        modifier = Modifier.size(StudioSizes.IconSmall)
                                    )
                                }

                                // Edit
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StudioColors.CardSurface)
                                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                                        .clickable { onEditScreen(screen) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = StudioColors.TextSecondary,
                                        modifier = Modifier.size(StudioSizes.IconSmall)
                                    )
                                }

                                // Delete (if > 1 screen)
                                if (viewModel.screens.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(StudioColors.CardSurface)
                                            .border(width = 1.dp, color = StudioColors.Error.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                                            .clickable { viewModel.deleteScreen(screen.id) },
                                        contentAlignment = Alignment.Center
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onAddScreen) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                            Text("New Screen")
                        }
                    }

                    DefaultButton(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
