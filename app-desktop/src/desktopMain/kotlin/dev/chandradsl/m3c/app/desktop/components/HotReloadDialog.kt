package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import org.jetbrains.jewel.ui.component.Badge
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Tooltip
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun HotReloadDialog(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.isHotReloadRunning.collectAsState()
    val clientCount by viewModel.hotReloadClientCount.collectAsState()
    val logs by viewModel.hotReloadLogs.collectAsState()
    val localIp = viewModel.localNetworkIp
    var portText by remember { mutableStateOf(viewModel.hotReloadPort.toString()) }

    // Dialog Scrim Backdrop
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = { viewModel.isHotReloadDialogOpen = false }),
        contentAlignment = Alignment.Center
    ) {
        // Modal Window Card
        Column(
            modifier = Modifier
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(StudioColors.PanelSurface)
                .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(12.dp))
                .clickable(enabled = false) {}
                .width(620.dp)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (isRunning) StudioColors.Success else StudioColors.TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Hot-Reload Companion Bridge",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = StudioColors.TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge
                    Badge {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isRunning) StudioColors.Success else StudioColors.TextMuted)
                            )
                            Text(
                                text = if (isRunning) "ONLINE • $clientCount CLIENT(S)" else "STOPPED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) StudioColors.Success else StudioColors.TextMuted
                            )
                        }
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.isHotReloadDialogOpen = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = StudioColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Server Controls & Address Details Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioColors.CardSurface)
                    .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Network URLs
                UrlCopyRow(
                    label = "Localhost URL",
                    url = "http://localhost:${viewModel.hotReloadPort}"
                )
                UrlCopyRow(
                    label = "Companion Network URL",
                    url = "http://$localIp:${viewModel.hotReloadPort}"
                )

                // Controls Row: Port & Start/Stop/Sync
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Port:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioColors.TextSecondary
                        )
                        JewelTextField(
                            value = portText,
                            onValueChange = { portText = it.filter { char -> char.isDigit() }.take(5) },
                            modifier = Modifier.width(90.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRunning) {
                            OutlinedButton(
                                onClick = { viewModel.pushHotReloadSync() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Push Sync", fontSize = 12.sp)
                                }
                            }

                            DefaultButton(
                                onClick = { viewModel.stopHotReloadServer() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Stop Server", fontSize = 12.sp)
                                }
                            }
                        } else {
                            DefaultButton(
                                onClick = {
                                    val portInt = portText.toIntOrNull() ?: 8989
                                    viewModel.startHotReloadServer(portInt)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Start Server", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Endpoints Quick Reference
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "AVAILABLE COMPANION ENDPOINTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioColors.TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EndpointPill(method = "GET", path = "/api/status", desc = "Server health")
                    EndpointPill(method = "GET", path = "/api/screens/active", desc = "Active screen AST")
                    EndpointPill(method = "GET", path = "/api/code", desc = "Kotlin Code")
                    EndpointPill(method = "GET", path = "/api/live", desc = "Live SSE Stream")
                }
            }

            // Live Activity Logs Box
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ACTIVITY LOG (${logs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioColors.TextMuted
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioColors.DarkCanvasBackdrop)
                        .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    if (logs.isEmpty()) {
                        Text(
                            text = "Server idle. Start server or connect a companion device to see events.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StudioColors.TextMuted,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(logs) { logEntry ->
                                Text(
                                    text = logEntry,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = StudioColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlCopyRow(label: String, url: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = StudioColors.TextMuted)
            Text(
                text = url,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StudioColors.Primary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(StudioColors.ActiveSurface)
                .clickable {
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(url), null)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp), tint = StudioColors.TextPrimary)
                Text("Copy", fontSize = 11.sp, color = StudioColors.TextPrimary)
            }
        }
    }
}

@Composable
private fun EndpointPill(method: String, path: String, desc: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioColors.CardSurface)
            .border(1.dp, StudioColors.BorderSubtle, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = method, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioColors.Success)
            Text(text = path, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = StudioColors.TextPrimary)
        }
    }
}
