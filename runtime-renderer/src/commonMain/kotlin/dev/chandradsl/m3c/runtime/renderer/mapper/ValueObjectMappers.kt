package dev.chandradsl.m3c.runtime.renderer.mapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementHorizontalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementVerticalDef
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken

@Composable
fun ColorSource.toComposeColor(): Color = when (this) {
    is ColorSource.Custom -> Color(hex.value)
    is ColorSource.Theme -> token.toComposeColor()
}

@Composable
fun ColorToken.toComposeColor(): Color = when (this) {
    ColorToken.Primary -> MaterialTheme.colorScheme.primary
    ColorToken.OnPrimary -> MaterialTheme.colorScheme.onPrimary
    ColorToken.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
    ColorToken.OnPrimaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
    ColorToken.Secondary -> MaterialTheme.colorScheme.secondary
    ColorToken.OnSecondary -> MaterialTheme.colorScheme.onSecondary
    ColorToken.SecondaryContainer -> MaterialTheme.colorScheme.secondaryContainer
    ColorToken.OnSecondaryContainer -> MaterialTheme.colorScheme.onSecondaryContainer
    ColorToken.Tertiary -> MaterialTheme.colorScheme.tertiary
    ColorToken.OnTertiary -> MaterialTheme.colorScheme.onTertiary
    ColorToken.TertiaryContainer -> MaterialTheme.colorScheme.tertiaryContainer
    ColorToken.OnTertiaryContainer -> MaterialTheme.colorScheme.onTertiaryContainer
    ColorToken.Background -> MaterialTheme.colorScheme.background
    ColorToken.OnBackground -> MaterialTheme.colorScheme.onBackground
    ColorToken.Surface -> MaterialTheme.colorScheme.surface
    ColorToken.OnSurface -> MaterialTheme.colorScheme.onSurface
    ColorToken.SurfaceVariant -> MaterialTheme.colorScheme.surfaceVariant
    ColorToken.OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
    ColorToken.SurfaceContainerLowest -> MaterialTheme.colorScheme.surfaceContainerLowest
    ColorToken.SurfaceContainerLow -> MaterialTheme.colorScheme.surfaceContainerLow
    ColorToken.SurfaceContainer -> MaterialTheme.colorScheme.surfaceContainer
    ColorToken.SurfaceContainerHigh -> MaterialTheme.colorScheme.surfaceContainerHigh
    ColorToken.SurfaceContainerHighest -> MaterialTheme.colorScheme.surfaceContainerHighest
    ColorToken.InverseSurface -> MaterialTheme.colorScheme.inverseSurface
    ColorToken.InverseOnSurface -> MaterialTheme.colorScheme.inverseOnSurface
    ColorToken.InversePrimary -> MaterialTheme.colorScheme.inversePrimary
    ColorToken.Error -> MaterialTheme.colorScheme.error
    ColorToken.OnError -> MaterialTheme.colorScheme.onError
    ColorToken.ErrorContainer -> MaterialTheme.colorScheme.errorContainer
    ColorToken.OnErrorContainer -> MaterialTheme.colorScheme.onErrorContainer
    ColorToken.Outline -> MaterialTheme.colorScheme.outline
    ColorToken.OutlineVariant -> MaterialTheme.colorScheme.outlineVariant
    ColorToken.Scrim -> MaterialTheme.colorScheme.scrim
}

@Composable
fun ShapeDef.toComposeShape(): Shape = when (this) {
    is ShapeDef.Rectangle -> RectangleShape
    is ShapeDef.Rounded -> RoundedCornerShape(cornerRadius.value.dp)
    is ShapeDef.Token -> token.toComposeShape()
}

@Composable
fun ShapeToken.toComposeShape(): Shape = when (this) {
    ShapeToken.None -> RectangleShape
    ShapeToken.ExtraSmall -> MaterialTheme.shapes.extraSmall
    ShapeToken.Small -> MaterialTheme.shapes.small
    ShapeToken.Medium -> MaterialTheme.shapes.medium
    ShapeToken.Large -> MaterialTheme.shapes.large
    ShapeToken.ExtraLarge -> MaterialTheme.shapes.extraLarge
    ShapeToken.Full -> CircleShape
}

@Composable
fun TypographyToken.toComposeTextStyle(): TextStyle = when (this) {
    TypographyToken.DisplayLarge -> MaterialTheme.typography.displayLarge
    TypographyToken.DisplayMedium -> MaterialTheme.typography.displayMedium
    TypographyToken.DisplaySmall -> MaterialTheme.typography.displaySmall
    TypographyToken.HeadlineLarge -> MaterialTheme.typography.headlineLarge
    TypographyToken.HeadlineMedium -> MaterialTheme.typography.headlineMedium
    TypographyToken.HeadlineSmall -> MaterialTheme.typography.headlineSmall
    TypographyToken.TitleLarge -> MaterialTheme.typography.titleLarge
    TypographyToken.TitleMedium -> MaterialTheme.typography.titleMedium
    TypographyToken.TitleSmall -> MaterialTheme.typography.titleSmall
    TypographyToken.BodyLarge -> MaterialTheme.typography.bodyLarge
    TypographyToken.BodyMedium -> MaterialTheme.typography.bodyMedium
    TypographyToken.BodySmall -> MaterialTheme.typography.bodySmall
    TypographyToken.LabelLarge -> MaterialTheme.typography.labelLarge
    TypographyToken.LabelMedium -> MaterialTheme.typography.labelMedium
    TypographyToken.LabelSmall -> MaterialTheme.typography.labelSmall
}

fun AlignmentDef.toComposeAlignment(): Alignment = when (this) {
    AlignmentDef.TopStart -> Alignment.TopStart
    AlignmentDef.TopCenter -> Alignment.TopCenter
    AlignmentDef.TopEnd -> Alignment.TopEnd
    AlignmentDef.CenterStart -> Alignment.CenterStart
    AlignmentDef.Center -> Alignment.Center
    AlignmentDef.CenterEnd -> Alignment.CenterEnd
    AlignmentDef.BottomStart -> Alignment.BottomStart
    AlignmentDef.BottomCenter -> Alignment.BottomCenter
    AlignmentDef.BottomEnd -> Alignment.BottomEnd
}

fun AlignmentHorizontalDef.toComposeAlignment(): Alignment.Horizontal = when (this) {
    AlignmentHorizontalDef.Start -> Alignment.Start
    AlignmentHorizontalDef.CenterHorizontally -> Alignment.CenterHorizontally
    AlignmentHorizontalDef.End -> Alignment.End
}

fun AlignmentVerticalDef.toComposeAlignment(): Alignment.Vertical = when (this) {
    AlignmentVerticalDef.Top -> Alignment.Top
    AlignmentVerticalDef.CenterVertically -> Alignment.CenterVertically
    AlignmentVerticalDef.Bottom -> Alignment.Bottom
}

fun ArrangementVerticalDef.toComposeArrangement(): Arrangement.Vertical = when (this) {
    ArrangementVerticalDef.Top -> Arrangement.Top
    ArrangementVerticalDef.Bottom -> Arrangement.Bottom
    ArrangementVerticalDef.Center -> Arrangement.Center
    ArrangementVerticalDef.SpaceBetween -> Arrangement.SpaceBetween
    ArrangementVerticalDef.SpaceAround -> Arrangement.SpaceAround
    ArrangementVerticalDef.SpaceEvenly -> Arrangement.SpaceEvenly
}

fun ArrangementHorizontalDef.toComposeArrangement(): Arrangement.Horizontal = when (this) {
    ArrangementHorizontalDef.Start -> Arrangement.Start
    ArrangementHorizontalDef.End -> Arrangement.End
    ArrangementHorizontalDef.Center -> Arrangement.Center
    ArrangementHorizontalDef.SpaceBetween -> Arrangement.SpaceBetween
    ArrangementHorizontalDef.SpaceAround -> Arrangement.SpaceAround
    ArrangementHorizontalDef.SpaceEvenly -> Arrangement.SpaceEvenly
}