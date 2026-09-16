package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.runtime.renderer.renderers.NodeRenderer

@Composable
fun CanvasViewport(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.workspaceState
    val scrollState = rememberScrollState()
    val backdropInteraction = remember { MutableInteractionSource() }

    // Outer Canvas Stage
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF11111B)) // Deep dark editor backdrop
            .clickable(
                interactionSource = backdropInteraction,
                indication = null
            ) {
                // Click on empty canvas deselects active node
                viewModel.dispatch(WorkspaceIntent.SelectNode(null))
            }
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 32.dp)
        ) {
            // Device Frame Header Tag
            Text(
                text = "Mobile Device • 390 × 844",
                color = Color(0xFF6C7086),
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Simulated Device Frame
            Box(
                modifier = Modifier
                    .width(390.dp)
                    .height(844.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(width = 2.dp, color = Color(0xFF313244), shape = RoundedCornerShape(24.dp))
            ) {
                // Material 3 Live Theme Provider
                val colorScheme = if (viewModel.isDarkMode) darkColorScheme() else lightColorScheme()

                MaterialTheme(colorScheme = colorScheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NodeRenderer(
                            node = state.rootNode,
                            state = state,
                            onIntent = viewModel::dispatch
                        )
                    }
                }
            }
        }
    }
}