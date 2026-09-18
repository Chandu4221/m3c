package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class CanvasController {
    var isDarkMode: Boolean by mutableStateOf(true)
    var isInteractiveMode: Boolean by mutableStateOf(false)
    var isCodeDrawerOpen: Boolean by mutableStateOf(false)
    var leftDrawerTab: LeftDrawerTab by mutableStateOf(LeftDrawerTab.Palette)

    val defaultLeftPanelWidth: Dp = 280.dp
    val minLeftPanelWidth: Dp = 240.dp
    val maxLeftPanelWidth: Dp = 550.dp

    val defaultRightPanelWidth: Dp = 320.dp
    val minRightPanelWidth: Dp = 280.dp
    val maxRightPanelWidth: Dp = 600.dp

    val defaultCodeDrawerHeight: Dp = 260.dp
    val minCodeDrawerHeight: Dp = 140.dp
    val maxCodeDrawerHeight: Dp = 600.dp

    var leftPanelWidth: Dp by mutableStateOf(defaultLeftPanelWidth)
    var rightPanelWidth: Dp by mutableStateOf(defaultRightPanelWidth)
    var codeDrawerHeight: Dp by mutableStateOf(defaultCodeDrawerHeight)

    var currentDevicePreset: DevicePreset by mutableStateOf(DevicePreset.Phone)
    var isLandscape: Boolean by mutableStateOf(false)
    var canvasZoom: Float by mutableStateOf(1.0f)

    val effectiveViewportWidth: Dp
        get() = if (isLandscape) {
            maxOf(currentDevicePreset.baseWidth, currentDevicePreset.baseHeight)
        } else {
            minOf(currentDevicePreset.baseWidth, currentDevicePreset.baseHeight)
        }

    val effectiveViewportHeight: Dp
        get() = if (isLandscape) {
            minOf(currentDevicePreset.baseWidth, currentDevicePreset.baseHeight)
        } else {
            maxOf(currentDevicePreset.baseWidth, currentDevicePreset.baseHeight)
        }

    val currentWindowSizeClass: WindowSizeClass
        get() = WindowSizeClass.fromWidth(effectiveViewportWidth)

    fun resizeLeftPanel(deltaDp: Float) {
        val newWidth = (leftPanelWidth.value + deltaDp).coerceIn(minLeftPanelWidth.value, maxLeftPanelWidth.value)
        leftPanelWidth = newWidth.dp
    }

    fun resizeRightPanel(deltaDp: Float) {
        val newWidth = (rightPanelWidth.value + deltaDp).coerceIn(minRightPanelWidth.value, maxRightPanelWidth.value)
        rightPanelWidth = newWidth.dp
    }

    fun resizeCodeDrawer(deltaDp: Float) {
        val newHeight = (codeDrawerHeight.value + deltaDp).coerceIn(minCodeDrawerHeight.value, maxCodeDrawerHeight.value)
        codeDrawerHeight = newHeight.dp
    }

    fun resetLeftPanelWidth() {
        leftPanelWidth = defaultLeftPanelWidth
    }

    fun resetRightPanelWidth() {
        rightPanelWidth = defaultRightPanelWidth
    }

    fun resetCodeDrawerHeight() {
        codeDrawerHeight = defaultCodeDrawerHeight
    }

    fun zoomIn() {
        val next = ((canvasZoom + 0.15f) * 100).toInt() / 100f
        canvasZoom = next.coerceIn(0.4f, 2.0f)
    }

    fun zoomOut() {
        val next = ((canvasZoom - 0.15f) * 100).toInt() / 100f
        canvasZoom = next.coerceIn(0.4f, 2.0f)
    }

    fun resetZoom() {
        canvasZoom = 1.0f
    }

    fun fitToViewport() {
        canvasZoom = 0.75f
    }

    fun toggleOrientation() {
        isLandscape = !isLandscape
    }

    fun setOrientation(landscape: Boolean) {
        isLandscape = landscape
    }

    fun setDevicePreset(preset: DevicePreset) {
        currentDevicePreset = preset
        isLandscape = preset.isDefaultLandscape
    }
}
