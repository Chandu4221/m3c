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
import androidx.compose.material3.IconButton
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
                color = Color(0xFF6C7086),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Informational Note on Evaluation Order
        if (node.modifiers.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1E2E))
                    .border(width = 1.dp, color = Color(0xFF313244), shape = RoundedCornerShape(4.dp))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF89B4FA),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Order matters: Evaluated top-to-bottom",
                    color = Color(0xFFA6ADC8),
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }

        // Active Modifiers List with Sorting & Deletion
        if (node.modifiers.isEmpty()) {
            Text(
                text = "No modifiers attached",
                color = Color(0xFF585B70),
                fontSize = 11.sp,
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
            color = Color(0xFF6C7086),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddModifierChip(label = "Padding 16dp", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Padding.all(DpVal(16f)))
                }
                AddModifierChip(label = "Fill Width", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.FillMaxWidth())
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddModifierChip(label = "Fill Max Size", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.FillMaxSize())
                }
                AddModifierChip(label = "Height 48dp", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Height(DpVal(48f)))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddModifierChip(label = "Bg Primary", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Background(ColorSource.Theme(ColorToken.PrimaryContainer)))
                }
                AddModifierChip(label = "Clip Medium", modifier = Modifier.weight(1f)) {
                    viewModel.addModifier(node.id, ModifierDef.Clip(ShapeDef.Token(ShapeToken.Medium)))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
            .background(Color(0xFF1E1E2E))
            .border(width = 1.dp, color = Color(0xFF313244), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag Handle / Index Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = Color(0xFF585B70),
                modifier = Modifier.size(14.dp)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF313244))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = Color(0xFFCDD6F4),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Modifier Name & Parameters
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = modifierDef::class.simpleName ?: "Modifier",
                color = Color(0xFFCDD6F4),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatModifierDetails(modifierDef),
                color = Color(0xFF6C7086),
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        // Action Controls: Move Up, Move Down, Delete
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Move Up Button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index > 0) Color(0xFF313244) else Color.Transparent)
                    .clickable(enabled = index > 0, onClick = onMoveUp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Move Up",
                    tint = if (index > 0) Color(0xFFCDD6F4) else Color(0xFF45475A),
                    modifier = Modifier.size(12.dp)
                )
            }

            // Move Down Button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index < totalCount - 1) Color(0xFF313244) else Color.Transparent)
                    .clickable(enabled = index < totalCount - 1, onClick = onMoveDown),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move Down",
                    tint = if (index < totalCount - 1) Color(0xFFCDD6F4) else Color(0xFF45475A),
                    modifier = Modifier.size(12.dp)
                )
            }

            // Remove Button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF313244))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color(0xFFF38BA8),
                    modifier = Modifier.size(12.dp)
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
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E2E))
            .border(width = 1.dp, color = Color(0xFF313244), shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFFA6ADC8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
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
