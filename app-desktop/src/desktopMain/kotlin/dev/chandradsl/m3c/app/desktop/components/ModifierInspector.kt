package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken

@Composable
fun ModifierInspector(
    viewModel: StudioViewModel,
    node: ComposableNode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MODIFIERS (${node.modifiers.size})",
                style = StudioTypography.SectionHeader
            )
        }

        // Informational Note on Evaluation Order (adheres to 12px hard floor & 4.5:1 contrast)
        if (node.modifiers.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioColors.CardSurface)
                    .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = StudioColors.Info,
                    modifier = Modifier.size(StudioSizes.IconMedium)
                )
                Text(
                    text = "Order matters: Evaluated top-to-bottom",
                    style = StudioTypography.Caption.copy(color = StudioColors.TextPrimary)
                )
            }
        }

        // Active Modifiers List with Sorting & Deletion
        if (node.modifiers.isEmpty()) {
            Text(
                text = "No modifiers attached",
                style = StudioTypography.Caption.copy(color = StudioColors.TextMuted),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            node.modifiers.forEachIndexed { index, mod ->
                ModifierSortableCard(
                    index = index,
                    totalCount = node.modifiers.size,
                    modifierDef = mod,
                    onMoveUp = { viewModel.reorderModifier(node.id, index, index - 1) },
                    onMoveDown = { viewModel.reorderModifier(node.id, index, index + 1) },
                    onDelete = { viewModel.removeModifier(node.id, index) }
                )
            }
        }

        // Quick Add Modifiers Grid
        Text(
            text = "+ ADD MODIFIER",
            style = StudioTypography.SectionHeader,
            modifier = Modifier.padding(top = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AddModifierChip(label = "Padding 16dp", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Padding.all(DpVal(16f)))
                }
                AddModifierChip(label = "Fill Width", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.FillMaxWidth())
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AddModifierChip(label = "Fill Max Size", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.FillMaxSize())
                }
                AddModifierChip(label = "Height 48dp", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Height(DpVal(48f)))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AddModifierChip(label = "Bg Primary", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Background(ColorSource.Theme(ColorToken.PrimaryContainer)))
                }
                AddModifierChip(label = "Clip Medium", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Clip(ShapeDef.Token(ShapeToken.Medium)))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AddModifierChip(label = "Clickable", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Clickable(enabled = true))
                }
                AddModifierChip(label = "Shadow 4dp", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Shadow(elevation = DpVal(4f)))
                }
            }
        }
    }
}

@Composable
private fun ModifierSortableCard(
    index: Int,
    totalCount: Int,
    modifierDef: ModifierDef,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StudioColors.CardSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag Handle / Index Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = StudioColors.TextMuted,
                modifier = Modifier.size(StudioSizes.IconMedium)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioColors.ActiveSurface)
                    .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = StudioTypography.Badge.copy(color = StudioColors.TextPrimary)
                )
            }
        }

        // Modifier Name & Parameters
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = modifierDef::class.simpleName ?: "Modifier",
                style = StudioTypography.UIBody
            )
            Text(
                text = formatModifierDetails(modifierDef),
                style = StudioTypography.Caption,
                maxLines = 1
            )
        }

        // Action Controls: Move Up, Move Down, Delete
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Move Up Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index > 0) StudioColors.ActiveSurface else Color.Transparent)
                    .clickable(enabled = index > 0, onClick = onMoveUp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Move Up",
                    tint = if (index > 0) StudioColors.TextPrimary else StudioColors.TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(StudioSizes.IconSmall)
                )
            }

            // Move Down Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index < totalCount - 1) StudioColors.ActiveSurface else Color.Transparent)
                    .clickable(enabled = index < totalCount - 1, onClick = onMoveDown),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move Down",
                    tint = if (index < totalCount - 1) StudioColors.TextPrimary else StudioColors.TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(StudioSizes.IconSmall)
                )
            }

            // Remove Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioColors.ActiveSurface)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = StudioColors.Error,
                    modifier = Modifier.size(StudioSizes.IconSmall)
                )
            }
        }
    }
}

@Composable
private fun AddModifierChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StudioColors.CardSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = StudioTypography.Caption.copy(
                color = StudioColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun formatModifierDetails(def: ModifierDef): String = when (def) {
    is ModifierDef.Padding -> "${def.start}, ${def.top}, ${def.end}, ${def.bottom}"
    is ModifierDef.FillMaxSize -> "fraction = ${def.fraction}"
    is ModifierDef.FillMaxWidth -> "fraction = ${def.fraction}"
    is ModifierDef.FillMaxHeight -> "fraction = ${def.fraction}"
    is ModifierDef.Size -> "${def.width} × ${def.height}"
    is ModifierDef.Width -> "${def.width}"
    is ModifierDef.Height -> "${def.height}"
    is ModifierDef.Background -> "color = ${def.color}"
    is ModifierDef.Border -> "width = ${def.border.width}"
    is ModifierDef.Clip -> "shape = ${def.shape}"
    is ModifierDef.Shadow -> "elevation = ${def.elevation}"
    is ModifierDef.Alpha -> "alpha = ${def.alpha}"
    is ModifierDef.Clickable -> "enabled = ${def.enabled}"
    is ModifierDef.RowScopeModifier.Weight -> "weight = ${def.weight}"
    is ModifierDef.RowScopeModifier.Align -> "align = ${def.alignment}"
    is ModifierDef.ColumnScopeModifier.Weight -> "weight = ${def.weight}"
    is ModifierDef.ColumnScopeModifier.Align -> "align = ${def.alignment}"
    is ModifierDef.BoxScopeModifier.Align -> "align = ${def.alignment}"
    else -> ""
}
