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
import androidx.compose.material.icons.filled.Close
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
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent

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

        // Active Modifiers List
        if (node.modifiers.isEmpty()) {
            Text(
                text = "No modifiers attached",
                color = Color(0xFF585B70),
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            node.modifiers.forEachIndexed { index, mod ->
                ModifierCard(
                    modifierDef = mod,
                    onDelete = {
                        val newModifiers = node.modifiers.filterIndexed { i, _ -> i != index }
                        viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = newModifiers))
                    }
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
                    val updated = node.modifiers + ModifierDef.Padding.all(DpVal(16f))
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
                AddModifierChip(label = "Fill Width", modifier = Modifier.weight(1f)) {
                    val updated = node.modifiers + ModifierDef.FillMaxWidth()
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddModifierChip(label = "Fill Max Size", modifier = Modifier.weight(1f)) {
                    val updated = node.modifiers + ModifierDef.FillMaxSize()
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
                AddModifierChip(label = "Height 48dp", modifier = Modifier.weight(1f)) {
                    val updated = node.modifiers + ModifierDef.Height(DpVal(48f))
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddModifierChip(label = "Bg Primary", modifier = Modifier.weight(1f)) {
                    val updated = node.modifiers + ModifierDef.Background(ColorSource.Theme(ColorToken.PrimaryContainer))
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
                AddModifierChip(label = "Clip Medium", modifier = Modifier.weight(1f)) {
                    val updated = node.modifiers + ModifierDef.Clip(ShapeDef.Token(ShapeToken.Medium))
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AddModifierChip(label = "Clickable", modifier = Modifier.weight(1f)) {
                    val updated = node.modifiers + ModifierDef.Clickable(enabled = true)
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
                AddModifierChip(label = "Shadow 4dp", modifier = Modifier.weight(1f)) {
                    val updated = node.modifiers + ModifierDef.Shadow(elevation = DpVal(4f))
                    viewModel.dispatch(WorkspaceIntent.UpdateModifiers(targetId = node.id, modifiers = updated))
                }
            }
        }
    }
}

@Composable
private fun ModifierCard(
    modifierDef: ModifierDef,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = modifierDef::class.simpleName ?: "Modifier",
                color = Color(0xFFCDD6F4),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatModifierDetails(modifierDef),
                color = Color(0xFF6C7086),
                fontSize = 10.sp
            )
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
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