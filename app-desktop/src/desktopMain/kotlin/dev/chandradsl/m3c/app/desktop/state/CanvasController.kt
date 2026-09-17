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

    var leftPanelWidth: Dp by mutableStateOf(260.dp)
    var rightPanelWidth: Dp by mutableStateOf(300.dp)
    var codeDrawerHeight: Dp by mutableStateOf(260.dp)

    var currentDevicePreset: DevicePreset by mutableStateOf(DevicePreset.PhonePortrait)
    var canvasZoom: Float by mutableStateOf(1.0f)

    fun resizeLeftPanel(deltaDp: Float) {
        val newWidth = (leftPanelWidth.value + deltaDp).coerceIn(180f, 500f)
        leftPanelWidth = newWidth.dp
    }

    fun resizeRightPanel(deltaDp: Float) {
        val newWidth = (rightPanelWidth.value + deltaDp).coerceIn(240f, 550f)
        rightPanelWidth = newWidth.dp
    }

    fun resizeCodeDrawer(deltaDp: Float) {
        val newHeight = (codeDrawerHeight.value + deltaDp).coerceIn(120f, 600f)
        codeDrawerHeight = newHeight.dp
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

    fun setDevicePreset(preset: DevicePreset) {
        currentDevicePreset = preset
    }
}
