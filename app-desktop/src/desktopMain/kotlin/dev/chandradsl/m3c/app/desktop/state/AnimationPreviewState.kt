package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

enum class TransitionEffect(val displayName: String, val description: String) {
    Fade("Fade", "Smooth opacity crossfade"),
    SlideUp("Slide Up", "Enter from bottom with vertical translate"),
    SlideDown("Slide Down", "Enter from top with vertical translate"),
    SlideHorizontal("Slide Right", "Enter from left with horizontal translate"),
    Scale("Scale & Fade", "Zoom in with scale and opacity"),
    Expand("Expand Vertically", "Reveal via vertical expand")
}

val BounceEasing = Easing { fraction ->
    val t = fraction.coerceIn(0f, 1f)
    if (t < 0.3636f) {
        7.5625f * t * t
    } else if (t < 0.7272f) {
        val post = t - 0.5454f
        7.5625f * post * post + 0.75f
    } else if (t < 0.9090f) {
        val post = t - 0.8181f
        7.5625f * post * post + 0.9375f
    } else {
        val post = t - 0.9545f
        7.5625f * post * post + 0.984375f
    }
}

val AnticipateOvershootEasing = Easing { fraction ->
    val t = fraction.coerceIn(0f, 1f)
    val s = 1.70158f * 1.525f
    if (t < 0.5f) {
        0.5f * (2f * t).let { it * it * ((s + 1f) * it - s) }
    } else {
        val p = 2f * t - 2f
        0.5f * (p * p * ((s + 1f) * p + s) + 2f)
    }
}

enum class EasingCurve(val displayName: String, val easing: Easing) {
    FastOutSlowIn("M3 Standard (FastOutSlowIn)", FastOutSlowInEasing),
    Linear("Linear", LinearEasing),
    FastOutLinearIn("Accelerate (FastOutLinearIn)", FastOutLinearInEasing),
    LinearOutSlowIn("Decelerate (LinearOutSlowIn)", LinearOutSlowInEasing),
    Bounce("Bounce", BounceEasing),
    Overshoot("Overshoot", AnticipateOvershootEasing);

    fun transform(progress: Float): Float {
        return easing.transform(progress.coerceIn(0f, 1f))
    }
}

data class AnimationPreviewState(
    val isActive: Boolean = false,
    val isPlaying: Boolean = false,
    val progress: Float = 1.0f,
    val durationMs: Int = 300,
    val speedMultiplier: Float = 1.0f,
    val isLooping: Boolean = false,
    val effect: TransitionEffect = TransitionEffect.Fade,
    val easing: EasingCurve = EasingCurve.FastOutSlowIn
) {
    val elapsedMs: Int
        get() = (progress.coerceIn(0f, 1f) * durationMs).toInt()

    val effectiveDurationMs: Int
        get() = (durationMs / speedMultiplier.coerceAtLeast(0.1f)).toInt()
}
