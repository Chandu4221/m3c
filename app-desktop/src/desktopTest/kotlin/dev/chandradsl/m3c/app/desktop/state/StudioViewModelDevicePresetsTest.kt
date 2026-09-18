package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StudioViewModelDevicePresetsTest {

    @Test
    fun testDefaultDevicePresetAndWindowSizeClass() {
        val vm = StudioViewModel()
        assertEquals(DevicePreset.Phone, vm.currentDevicePreset)
        assertFalse(vm.isLandscape)
        assertEquals(390.dp, vm.effectiveViewportWidth)
        assertEquals(844.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Compact, vm.currentWindowSizeClass)
    }

    @Test
    fun testPhoneOrientationToggle() {
        val vm = StudioViewModel()
        vm.setDevicePreset(DevicePreset.Phone)
        assertEquals(WindowSizeClass.Compact, vm.currentWindowSizeClass)

        // Rotate to landscape
        vm.toggleOrientation()
        assertTrue(vm.isLandscape)
        assertEquals(844.dp, vm.effectiveViewportWidth)
        assertEquals(390.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Expanded, vm.currentWindowSizeClass)

        // Rotate back to portrait
        vm.toggleOrientation()
        assertFalse(vm.isLandscape)
        assertEquals(390.dp, vm.effectiveViewportWidth)
        assertEquals(844.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Compact, vm.currentWindowSizeClass)
    }

    @Test
    fun testFoldablePresetAndWindowSizeClass() {
        val vm = StudioViewModel()
        vm.setDevicePreset(DevicePreset.Foldable)
        assertFalse(vm.isLandscape)
        assertEquals(673.dp, vm.effectiveViewportWidth)
        assertEquals(841.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Medium, vm.currentWindowSizeClass)

        // Foldable in landscape
        vm.toggleOrientation()
        assertTrue(vm.isLandscape)
        assertEquals(841.dp, vm.effectiveViewportWidth)
        assertEquals(673.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Expanded, vm.currentWindowSizeClass)
    }

    @Test
    fun testTabletPresetAndWindowSizeClass() {
        val vm = StudioViewModel()
        vm.setDevicePreset(DevicePreset.Tablet)
        assertFalse(vm.isLandscape)
        assertEquals(800.dp, vm.effectiveViewportWidth)
        assertEquals(1280.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Medium, vm.currentWindowSizeClass)

        // Tablet in landscape
        vm.toggleOrientation()
        assertTrue(vm.isLandscape)
        assertEquals(1280.dp, vm.effectiveViewportWidth)
        assertEquals(800.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Expanded, vm.currentWindowSizeClass)
    }

    @Test
    fun testDesktopPresetAndWindowSizeClass() {
        val vm = StudioViewModel()
        vm.setDevicePreset(DevicePreset.Desktop)
        assertTrue(vm.isLandscape, "Desktop preset defaults to landscape")
        assertEquals(1200.dp, vm.effectiveViewportWidth)
        assertEquals(800.dp, vm.effectiveViewportHeight)
        assertEquals(WindowSizeClass.Expanded, vm.currentWindowSizeClass)
    }

    @Test
    fun testWindowSizeClassBreakpointsCalculation() {
        assertEquals(WindowSizeClass.Compact, WindowSizeClass.fromWidth(360.dp))
        assertEquals(WindowSizeClass.Compact, WindowSizeClass.fromWidth(599.dp))
        assertEquals(WindowSizeClass.Medium, WindowSizeClass.fromWidth(600.dp))
        assertEquals(WindowSizeClass.Medium, WindowSizeClass.fromWidth(700.dp))
        assertEquals(WindowSizeClass.Medium, WindowSizeClass.fromWidth(839.dp))
        assertEquals(WindowSizeClass.Expanded, WindowSizeClass.fromWidth(840.dp))
        assertEquals(WindowSizeClass.Expanded, WindowSizeClass.fromWidth(1200.dp))
    }
}
