package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
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
                // Click on empty canvas deselects active node in design mode
                if (!viewModel.isInteractiveMode) {
                    viewModel.dispatch(WorkspaceIntent.SelectNode(null))
                }
            }
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 32.dp)
        ) {
            // Device Frame Header Tag & Mode Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Mobile Device • 390 × 844",
                    color = Color(0xFF6C7086),
                    fontSize = 11.sp
                )

                if (viewModel.isInteractiveMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E3A2F))
                            .border(width = 1.dp, color = Color(0xFFA6E3A1), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFA6E3A1))
                        )
                        Text(
                            text = "Interactive Mode",
                            color = Color(0xFFA6E3A1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Simulated Device Frame
            Box(
                modifier = Modifier
                    .width(390.dp)
                    .height(844.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 2.dp,
                        color = if (viewModel.isInteractiveMode) Color(0xFFA6E3A1).copy(alpha = 0.5f) else Color(0xFF313244),
                        shape = RoundedCornerShape(24.dp)
                    )
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
                            state = if (viewModel.isInteractiveMode) state.copy(selectedNodeId = null) else state,
                            onIntent = { intent ->
                                if (viewModel.isInteractiveMode && intent is WorkspaceIntent.SelectNode) {
                                    return@NodeRenderer
                                }
                                viewModel.dispatch(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}
