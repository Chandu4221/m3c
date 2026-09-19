package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.EasingCurve
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.state.TransitionEffect
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Badge
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun AnimationControlBar(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.animationPreview

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(StudioColors.PanelSurface.copy(alpha = 0.96f))
                .border(1.dp, StudioColors.BorderActive.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .width(760.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Status badge, Playback controls, Scrubber, Time, Loop, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Badge {
                    Text(
                        text = "MOTION PREVIEW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = StudioColors.Primary
                    )
                }

                // Play / Pause Button
                AnimationIconButton(
                    icon = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    tooltip = if (state.isPlaying) "Pause" else "Play",
                    isActive = state.isPlaying,
                    onClick = {
                        if (state.isPlaying) viewModel.pauseAnimation() else viewModel.playAnimation()
                    }
                )

                // Replay Button
                AnimationIconButton(
                    icon = Icons.Default.Refresh,
                    tooltip = "Replay from start",
                    onClick = { viewModel.replayAnimation() }
                )

                // Scrubber Slider
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(
                        value = state.progress,
                        onValueChange = { viewModel.seekAnimation(it) },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Millisecond readout
                Text(
                    text = "${state.elapsedMs} ms / ${state.durationMs} ms",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = StudioColors.TextPrimary
                )

                // Loop Toggle Button
                AnimationIconButton(
                    icon = Icons.Default.Repeat,
                    tooltip = if (state.isLooping) "Loop: ON" else "Loop: OFF",
                    isActive = state.isLooping,
                    onClick = { viewModel.toggleAnimationLoop() }
                )

                // Close Button
                AnimationIconButton(
                    icon = Icons.Default.Close,
                    tooltip = "Exit Preview (F8)",
                    onClick = { viewModel.toggleAnimationPreview(false) }
                )
            }

            // Row 2: Effect pills, Easing curve pills, Speed pills, Duration pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Effects
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Effect:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioColors.TextSecondary
                    )
                    TransitionEffect.entries.forEach { effect ->
                        AnimationPill(
                            label = effect.displayName,
                            isSelected = state.effect == effect,
                            onClick = { viewModel.setAnimationEffect(effect) }
                        )
                    }
                }

                // Speed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Speed:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioColors.TextSecondary
                    )
                    listOf(0.25f to "0.25x", 0.5f to "0.5x", 1.0f to "1x", 2.0f to "2x").forEach { (speed, label) ->
                        AnimationPill(
                            label = label,
                            isSelected = state.speedMultiplier == speed,
                            onClick = { viewModel.setAnimationSpeed(speed) }
                        )
                    }
                }
            }

            // Row 3: Easing curve selection & Duration options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Easing curves
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Curve:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioColors.TextSecondary
                    )
                    EasingCurve.entries.forEach { curve ->
                        val shortName = when (curve) {
                            EasingCurve.FastOutSlowIn -> "M3 Standard"
                            EasingCurve.Linear -> "Linear"
                            EasingCurve.FastOutLinearIn -> "Accelerate"
                            EasingCurve.LinearOutSlowIn -> "Decelerate"
                            EasingCurve.Bounce -> "Bounce"
                            EasingCurve.Overshoot -> "Overshoot"
                        }
                        AnimationPill(
                            label = shortName,
                            isSelected = state.easing == curve,
                            onClick = { viewModel.setAnimationEasing(curve) }
                        )
                    }
                }

                // Duration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Duration:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioColors.TextSecondary
                    )
                    listOf(150, 300, 500, 800, 1200).forEach { dur ->
                        AnimationPill(
                            label = "${dur}ms",
                            isSelected = state.durationMs == dur,
                            onClick = { viewModel.setAnimationDuration(dur) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
private fun AnimationIconButton(
    icon: ImageVector,
    tooltip: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (isActive) StudioColors.Primary.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(150)
    )
    val tint by animateColorAsState(
        if (isActive) StudioColors.Primary else StudioColors.TextPrimary,
        animationSpec = tween(150)
    )

    Tooltip(tooltip = { Text(tooltip) }) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AnimationPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (isSelected) StudioColors.Primary else StudioColors.CardSurface,
        animationSpec = tween(150)
    )
    val textCol by animateColorAsState(
        if (isSelected) StudioColors.TextInverse else StudioColors.TextPrimary,
        animationSpec = tween(150)
    )
    val borderCol by animateColorAsState(
        if (isSelected) StudioColors.Primary else StudioColors.BorderSubtle,
        animationSpec = tween(150)
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textCol
        )
    }
}
