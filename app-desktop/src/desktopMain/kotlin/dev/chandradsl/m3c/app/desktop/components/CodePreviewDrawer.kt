package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import kotlinx.coroutines.delay
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun CodePreviewDrawer(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    var copied by remember { mutableStateOf(false) }
    val generatedCode by viewModel.generatedCodeFlow.collectAsState()

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(viewModel.codeDrawerHeight)
            .background(StudioColors.CardSurface)
            .border(width = 1.dp, color = StudioColors.BorderSubtle)
    ) {
        // Drawer Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(StudioColors.PanelSurface)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GENERATED KOTLIN SOURCE (COMPOSE MULTIPLATFORM)",
                style = StudioTypography.SectionHeader.copy(color = StudioColors.Primary)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy to Clipboard Button
                val copyBtnBg by animateColorAsState(
                    targetValue = if (copied) StudioColors.Success else StudioColors.ActiveSurface,
                    animationSpec = tween(150)
                )
                val copyBtnBorder by animateColorAsState(
                    targetValue = if (copied) StudioColors.Success else StudioColors.BorderSubtle,
                    animationSpec = tween(150)
                )
                val copyBtnFg by animateColorAsState(
                    targetValue = if (copied) StudioColors.TextInverse else StudioColors.TextPrimary,
                    animationSpec = tween(150)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(copyBtnBg)
                        .border(width = 1.dp, color = copyBtnBorder, shape = RoundedCornerShape(4.dp))
                        .clickable {
                            val selection = StringSelection(generatedCode)
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
                            copied = true
                            viewModel.notifySuccess("Generated Kotlin Compose code copied to clipboard!")
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = copyBtnFg,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                    Text(
                        text = if (copied) "Copied!" else "Copy Code",
                        style = StudioTypography.Caption.copy(
                            color = copyBtnFg,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Close Drawer Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .clickable { viewModel.isCodeDrawerOpen = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = StudioColors.TextSecondary,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                }
            }
        }

        // Code Viewer Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(14.dp)
                .verticalScroll(vScroll)
                .horizontalScroll(hScroll)
        ) {
            Text(
                text = generatedCode,
                style = StudioTypography.CodeMonospace
            )
        }
    }
}
