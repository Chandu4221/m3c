package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.IconCategory
import dev.chandradsl.m3c.core.domain.model.MaterialIconCatalog
import dev.chandradsl.m3c.core.domain.model.MaterialIconEntry
import dev.chandradsl.m3c.runtime.renderer.renderers.resolveMaterialIcon

/**
 * Categorized & Searchable Material Icon Picker for the Property Inspector.
 * Allows searching by name or keyword, filtering by Material categories,
 * and selecting icons with real-time visual preview.
 */
@Composable
fun MaterialIconPicker(
    selectedIconName: String,
    onSelectIcon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<IconCategory>(IconCategory.All) }
    var isExpanded by remember { mutableStateOf(true) }

    val filteredIcons = remember(searchQuery, selectedCategory) {
        MaterialIconCatalog.search(searchQuery, selectedCategory)
    }

    val currentVector = remember(selectedIconName) {
        resolveMaterialIcon(selectedIconName)
    }

    val currentEntry = remember(selectedIconName) {
        MaterialIconCatalog.find(selectedIconName)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Active Icon Banner Preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StudioColors.CardSurface)
                .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioColors.ActiveSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentVector,
                    contentDescription = selectedIconName,
                    tint = StudioColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedIconName,
                    style = StudioTypography.UIBody.copy(fontWeight = FontWeight.Bold),
                    color = StudioColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentEntry?.let { entry ->
                        Text(
                            text = entry.category.displayName,
                            style = StudioTypography.Caption.copy(
                                fontSize = 10.sp,
                                color = StudioColors.TextMuted
                            )
                        )
                        if (entry.isAutoMirrored) {
                            Text(
                                text = "• AutoMirrored",
                                style = StudioTypography.Caption.copy(
                                    fontSize = 10.sp,
                                    color = StudioColors.Info
                                )
                            )
                        }
                    } ?: Text(
                        text = "Custom / Extended",
                        style = StudioTypography.Caption.copy(
                            fontSize = 10.sp,
                            color = StudioColors.TextMuted
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioColors.ActiveSurface)
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isExpanded) "Collapse" else "Browse",
                    style = StudioTypography.Caption.copy(
                        color = StudioColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 2. Search Input (Clean, vertically centered, no text cutoff)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioColors.CardSurface)
                        .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = StudioColors.TextMuted,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search icons (e.g. arrow, cart)...",
                                style = StudioTypography.Caption.copy(
                                    fontSize = 12.sp,
                                    color = StudioColors.TextMuted
                                )
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = StudioTypography.UIBody.copy(
                                fontSize = 12.sp,
                                color = StudioColors.TextPrimary
                            ),
                            cursorBrush = SolidColor(StudioColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(StudioColors.ActiveSurface)
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = StudioColors.TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // 3. Category Filter Chips (Wrapping FlowRow: all categories fully visible, no scroll-wheel fighting)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconCategory.values().forEach { category ->
                        val isSelected = selectedCategory == category
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) StudioColors.Primary else StudioColors.CardSurface,
                            animationSpec = tween(120)
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) StudioColors.TextInverse else StudioColors.TextSecondary,
                            animationSpec = tween(120)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(bgColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) StudioColors.Primary else StudioColors.BorderSubtle,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 7.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.displayName,
                                style = StudioTypography.Caption.copy(
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Count summary
                Text(
                    text = "Showing ${filteredIcons.size} icons",
                    style = StudioTypography.Caption.copy(
                        fontSize = 10.sp,
                        color = StudioColors.TextMuted
                    )
                )

                // 4. Visual Icon Grid (Scrollable)
                if (filteredIcons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioColors.CardSurface)
                            .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "No icons found",
                                style = StudioTypography.Caption.copy(fontWeight = FontWeight.Medium),
                                color = StudioColors.TextSecondary
                            )
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "Clear search",
                                    style = StudioTypography.Caption.copy(
                                        color = StudioColors.Primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                } else {
                    val columns = 4
                    val chunkedIcons = remember(filteredIcons) {
                        filteredIcons.chunked(columns)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioColors.PanelSurface)
                            .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(6.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chunkedIcons.forEach { rowEntries ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowEntries.forEach { entry ->
                                    IconGridTile(
                                        entry = entry,
                                        isChosen = selectedIconName.equals(entry.name, ignoreCase = true),
                                        onSelect = { onSelectIcon(entry.name) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Fill remaining space if row is incomplete
                                val emptySlots = columns - rowEntries.size
                                repeat(emptySlots) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconGridTile(
    entry: MaterialIconEntry,
    isChosen: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vector = remember(entry.name) {
        resolveMaterialIcon(entry.name)
    }

    val tileBg by animateColorAsState(
        targetValue = if (isChosen) StudioColors.Primary else StudioColors.CardSurface,
        animationSpec = tween(120)
    )
    val tileBorder by animateColorAsState(
        targetValue = if (isChosen) StudioColors.Primary else StudioColors.BorderSubtle,
        animationSpec = tween(120)
    )
    val tileContentColor by animateColorAsState(
        targetValue = if (isChosen) StudioColors.TextInverse else StudioColors.TextPrimary,
        animationSpec = tween(120)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tileBg)
            .border(1.dp, tileBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = vector,
            contentDescription = entry.name,
            tint = tileContentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.name,
            style = StudioTypography.Caption.copy(
                color = tileContentColor,
                fontSize = 10.sp,
                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
