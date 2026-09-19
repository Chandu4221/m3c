package dev.chandradsl.m3c.app.desktop.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AnimationPreviewTest {

    @Test
    fun testInitialAnimationState() {
        val vm = StudioViewModel()
        val anim = vm.animationPreview
        assertFalse(anim.isActive)
        assertFalse(anim.isPlaying)
        assertEquals(1.0f, anim.progress)
        assertEquals(300, anim.durationMs)
        assertEquals(1.0f, anim.speedMultiplier)
        assertFalse(anim.isLooping)
        assertEquals(TransitionEffect.Fade, anim.effect)
        assertEquals(EasingCurve.FastOutSlowIn, anim.easing)
    }

    @Test
    fun testToggleAnimationPreview() {
        val vm = StudioViewModel()
        vm.toggleAnimationPreview(true)
        assertTrue(vm.animationPreview.isActive)
        assertTrue(vm.animationPreview.isPlaying)

        vm.toggleAnimationPreview(false)
        assertFalse(vm.animationPreview.isActive)
        assertFalse(vm.animationPreview.isPlaying)
        assertEquals(1.0f, vm.animationPreview.progress)
    }

    @Test
    fun testPlayPauseReplayAndSeek() {
        val vm = StudioViewModel()

        vm.toggleAnimationPreview(true)
        assertTrue(vm.animationPreview.isPlaying)

        vm.pauseAnimation()
        assertFalse(vm.animationPreview.isPlaying)

        vm.seekAnimation(0.45f)
        assertEquals(0.45f, vm.animationPreview.progress)
        assertFalse(vm.animationPreview.isPlaying)
        assertEquals(135, vm.animationPreview.elapsedMs) // 0.45 * 300

        vm.replayAnimation()
        assertTrue(vm.animationPreview.isPlaying)
        assertEquals(0.0f, vm.animationPreview.progress)
    }

    @Test
    fun testSpeedAndDurationSettings() {
        val vm = StudioViewModel()

        vm.setAnimationDuration(600)
        assertEquals(600, vm.animationPreview.durationMs)

        vm.setAnimationSpeed(2.0f)
        assertEquals(2.0f, vm.animationPreview.speedMultiplier)
        assertEquals(300, vm.animationPreview.effectiveDurationMs) // 600 / 2.0

        vm.setAnimationSpeed(0.5f)
        assertEquals(0.5f, vm.animationPreview.speedMultiplier)
        assertEquals(1200, vm.animationPreview.effectiveDurationMs) // 600 / 0.5
    }

    @Test
    fun testEffectAndEasingSelection() {
        val vm = StudioViewModel()

        vm.setAnimationEffect(TransitionEffect.SlideUp)
        assertEquals(TransitionEffect.SlideUp, vm.animationPreview.effect)

        vm.setAnimationEffect(TransitionEffect.Scale)
        assertEquals(TransitionEffect.Scale, vm.animationPreview.effect)

        vm.setAnimationEasing(EasingCurve.Bounce)
        assertEquals(EasingCurve.Bounce, vm.animationPreview.easing)

        vm.toggleAnimationLoop()
        assertTrue(vm.animationPreview.isLooping)
        vm.toggleAnimationLoop()
        assertFalse(vm.animationPreview.isLooping)
    }

    @Test
    fun testEasingCalculations() {
        assertEquals(0f, EasingCurve.Linear.transform(0f), 0.001f)
        assertEquals(0.5f, EasingCurve.Linear.transform(0.5f), 0.001f)
        assertEquals(1f, EasingCurve.Linear.transform(1f), 0.001f)

        assertEquals(0f, EasingCurve.FastOutSlowIn.transform(0f), 0.001f)
        assertEquals(1f, EasingCurve.FastOutSlowIn.transform(1f), 0.001f)

        assertEquals(0f, EasingCurve.FastOutLinearIn.transform(0f), 0.001f)
        assertEquals(1f, EasingCurve.FastOutLinearIn.transform(1f), 0.001f)

        assertEquals(0f, EasingCurve.LinearOutSlowIn.transform(0f), 0.001f)
        assertEquals(1f, EasingCurve.LinearOutSlowIn.transform(1f), 0.001f)

        assertEquals(0f, EasingCurve.Bounce.transform(0f), 0.001f)
        assertEquals(1f, EasingCurve.Bounce.transform(1f), 0.01f)
    }
}
