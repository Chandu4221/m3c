package dev.chandradsl.m3c.runtime.renderer.mapper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ModifierDef

@Composable
fun List<ModifierDef>.toComposeModifier(): Modifier =
    fold(Modifier as Modifier) { currentModifier, def ->
        currentModifier.then(def.toUniversalModifier())
    }

@Composable
private fun ModifierDef.toUniversalModifier(): Modifier = when (this) {
    is ModifierDef.FillMaxSize -> Modifier.fillMaxSize(fraction)
    is ModifierDef.FillMaxWidth -> Modifier.fillMaxWidth(fraction)
    is ModifierDef.FillMaxHeight -> Modifier.fillMaxHeight(fraction)
    is ModifierDef.Size -> Modifier.size(width.value.dp, height.value.dp)
    is ModifierDef.Width -> Modifier.width(width.value.dp)
    is ModifierDef.Height -> Modifier.height(height.value.dp)
    is ModifierDef.DefaultMinSize -> Modifier.defaultMinSize(minWidth.value.dp, minHeight.value.dp)
    is ModifierDef.Padding -> Modifier.padding(
        start = start.value.dp,
        top = top.value.dp,
        end = end.value.dp,
        bottom = bottom.value.dp
    )
    is ModifierDef.Offset -> Modifier.offset(x = x.value.dp, y = y.value.dp)
    is ModifierDef.Background -> {
        val composeColor = color.toComposeColor()
        val composeShape = shape?.toComposeShape() ?: RectangleShape
        Modifier.background(color = composeColor, shape = composeShape)
    }
    is ModifierDef.Border -> {
        val composeColor = border.color.toComposeColor()
        val composeShape = shape?.toComposeShape() ?: RectangleShape
        Modifier.border(width = border.width.value.dp, color = composeColor, shape = composeShape)
    }
    is ModifierDef.Clip -> Modifier.clip(shape.toComposeShape())
    is ModifierDef.Shadow -> {
        val composeShape = shape?.toComposeShape() ?: RectangleShape
        Modifier.shadow(elevation = elevation.value.dp, shape = composeShape, clip = clip)
    }
    is ModifierDef.Alpha -> Modifier.alpha(alpha)
    is ModifierDef.Clickable -> Modifier.clickable(enabled = enabled, onClickLabel = onClickLabel) { /* Preview Click */ }
    else -> Modifier
}

@Composable
fun RowScope.toComposeRowModifier(modifiers: List<ModifierDef>): Modifier =
    modifiers.fold(Modifier as Modifier) { current, def ->
        when (def) {
            is ModifierDef.RowScopeModifier.Weight -> current.then(Modifier.weight(def.weight, def.fill))
            is ModifierDef.RowScopeModifier.Align -> current.then(Modifier.align(def.alignment.toComposeAlignment()))
            else -> current.then(def.toUniversalModifier())
        }
    }

@Composable
fun ColumnScope.toComposeColumnModifier(modifiers: List<ModifierDef>): Modifier =
    modifiers.fold(Modifier as Modifier) { current, def ->
        when (def) {
            is ModifierDef.ColumnScopeModifier.Weight -> current.then(Modifier.weight(def.weight, def.fill))
            is ModifierDef.ColumnScopeModifier.Align -> current.then(Modifier.align(def.alignment.toComposeAlignment()))
            else -> current.then(def.toUniversalModifier())
        }
    }

@Composable
fun BoxScope.toComposeBoxModifier(modifiers: List<ModifierDef>): Modifier =
    modifiers.fold(Modifier as Modifier) { current, def ->
        when (def) {
            is ModifierDef.BoxScopeModifier.Align -> current.then(Modifier.align(def.alignment.toComposeAlignment()))
            else -> current.then(def.toUniversalModifier())
        }
    }