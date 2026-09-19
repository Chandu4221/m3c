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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.chandradsl.m3c.core.domain.store.HistoryTimelineItem
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

/**
 * Visual revision history timeline panel providing time-travel debugging and step rollback/rollforward.
 * 100% JetBrains Jewel UI.
 */
@Composable
fun HistoryTimelinePanel(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val timeline = viewModel.historyTimeline
    val listState = rememberLazyListState()

    // Auto-scroll to current active step whenever timeline changes
    val currentIndex = timeline.indexOfFirst { it.isCurrent }
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(StudioColors.PanelSurface)
    ) {
        // 1. Header with Title, Count Badge, and Clear Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(StudioColors.PanelSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = StudioColors.Primary,
                    modifier = Modifier.size(StudioSizes.IconMedium)
                )
                Text(
                    text = "Timeline",
                    style = StudioTypography.SectionHeader.copy(fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${timeline.size} steps",
                        style = StudioTypography.Caption.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioColors.TextSecondary
                        )
                    )
                }
            }

            if (timeline.size > 1) {
                OutlinedButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(StudioSizes.IconSmall)
                        )
                        Text(
                            text = "Clear",
                            style = StudioTypography.Caption.copy(fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // 2. Quick Action Bar (Undo / Redo / Active Screen indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioColors.CardSurface)
                .border(width = 1.dp, color = StudioColors.BorderSubtle)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Screen: ${viewModel.activeScreen?.name ?: "Active"}",
                style = StudioTypography.Caption.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = StudioColors.TextPrimary
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DefaultButton(
                    onClick = { viewModel.undo() },
                    enabled = viewModel.workspaceState.canUndo,
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = null,
                            modifier = Modifier.size(StudioSizes.IconSmall)
                        )
                        Text("Undo", style = StudioTypography.Caption.copy(fontSize = 11.sp))
                    }
                }

                DefaultButton(
                    onClick = { viewModel.redo() },
                    enabled = viewModel.workspaceState.canRedo,
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = null,
                            modifier = Modifier.size(StudioSizes.IconSmall)
                        )
                        Text("Redo", style = StudioTypography.Caption.copy(fontSize = 11.sp))
                    }
                }
            }
        }

        // 3. Informational Banner when starting fresh
        if (timeline.size <= 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioColors.CardSurface)
                    .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = StudioColors.TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "History Baseline Ready",
                        style = StudioTypography.UIBody.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Every component insertion, modifier update, or reordering action on the canvas will be logged here for seamless time-travel rewind.",
                        style = StudioTypography.Caption.copy(color = StudioColors.TextSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 4. Stepped Timeline List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(timeline, key = { it.stepIndex }) { item ->
                    TimelineItemRow(
                        item = item,
                        onClick = { viewModel.jumpToHistoryStep(item.stepIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: HistoryTimelineItem,
    onClick: () -> Unit
) {
    val rowBg = when {
        item.isCurrent -> StudioColors.ActiveSurface
        else -> StudioColors.PanelSurface
    }

    val borderColor = when {
        item.isCurrent -> StudioColors.Primary
        else -> StudioColors.BorderSubtle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(rowBg)
            .border(
                width = if (item.isCurrent) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Step Indicator Dot & Connector Guide
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                item.isCurrent -> {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(StudioColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StudioColors.TextInverse)
                        )
                    }
                }
                item.isFuture -> {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, StudioColors.TextMuted, CircleShape)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StudioColors.Success.copy(alpha = 0.8f))
                    )
                }
            }
        }

        // Step Details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "#${item.stepIndex}",
                    style = StudioTypography.CodeMonospace.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isCurrent) StudioColors.Primary else StudioColors.TextSecondary
                    )
                )
                Text(
                    text = item.description,
                    style = StudioTypography.UIBody.copy(
                        fontSize = 13.sp,
                        fontWeight = if (item.isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            item.isCurrent -> StudioColors.TextPrimary
                            item.isFuture -> StudioColors.TextMuted
                            else -> StudioColors.TextPrimary
                        }
                    ),
                    maxLines = 1
                )
            }
        }

        // State Pill
        val (badgeText, badgeBg, badgeTextColor) = when {
            item.isCurrent -> Triple("CURRENT", StudioColors.Primary, StudioColors.TextInverse)
            item.isFuture -> Triple("REDO", StudioColors.CardSurface, StudioColors.TextMuted)
            else -> Triple("PAST", StudioColors.Success.copy(alpha = 0.15f), StudioColors.Success)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(badgeBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = badgeText,
                style = StudioTypography.Caption.copy(
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            )
        }
    }
}
