package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.io.DesktopFilePicker
import dev.chandradsl.m3c.app.desktop.state.CodePreviewMode
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.app.desktop.theme.StudioColors
import dev.chandradsl.m3c.app.desktop.theme.StudioSizes
import dev.chandradsl.m3c.app.desktop.theme.StudioTypography
import kotlinx.coroutines.delay
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun CodePreviewDrawer(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    var copied by remember { mutableStateOf(false) }
    val generatedCode by viewModel.generatedCodeFlow.collectAsState()

    // Search in code state
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentMatchIndex by remember { mutableStateOf(0) }

    val lines = remember(generatedCode) { generatedCode.lines() }
    val matches = remember(generatedCode, searchQuery, isSearchOpen) {
        if (isSearchOpen && searchQuery.isNotBlank()) {
            KotlinSyntaxHighlighter.findSearchMatches(generatedCode, searchQuery)
        } else {
            emptyList()
        }
    }

    // Adjust active match index when match list changes
    LaunchedEffect(matches.size) {
        if (matches.isNotEmpty() && currentMatchIndex >= matches.size) {
            currentMatchIndex = matches.size - 1
        }
    }

    // Auto-scroll to active search match line
    LaunchedEffect(currentMatchIndex, matches) {
        if (matches.isNotEmpty() && currentMatchIndex in matches.indices) {
            val targetLine = matches[currentMatchIndex].lineIndex
            val lineHeightPx = with(density) { 18.sp.toPx() }
            val targetScroll = (targetLine * lineHeightPx).toInt()
            vScroll.animateScrollTo(targetScroll)
        }
    }

    val highlightedCode = remember(generatedCode, viewModel.isDarkMode, isSearchOpen, searchQuery, currentMatchIndex) {
        KotlinSyntaxHighlighter.highlight(
            code = generatedCode,
            isDark = viewModel.isDarkMode,
            searchQuery = if (isSearchOpen) searchQuery else null,
            activeMatchIndex = if (isSearchOpen && matches.isNotEmpty()) currentMatchIndex else -1
        )
    }

    // Identify line of currently selected canvas node
    val selectedNode = viewModel.selectedNode
    val selectedNodeLine = remember(generatedCode, selectedNode) {
        if (selectedNode == null) -1
        else {
            val typeName = selectedNode::class.simpleName?.replace("Node", "") ?: ""
            if (typeName.isEmpty()) -1
            else {
                lines.indexOfFirst { line ->
                    val trimmed = line.trimStart()
                    trimmed.startsWith("$typeName(") || trimmed.startsWith("$typeName {") || trimmed.startsWith("$typeName\n")
                }
            }
        }
    }

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
        // 1. Drawer Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(StudioColors.PanelSurface)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GENERATED SOURCE",
                    style = StudioTypography.SectionHeader.copy(color = StudioColors.Primary)
                )

                // Code Preview Mode Switcher Tabs
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.CardSurface)
                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val activeScreenName = viewModel.activeScreen?.name ?: "Screen"
                    CodeModeTab(
                        text = "$activeScreenName.kt",
                        isSelected = viewModel.codePreviewMode == CodePreviewMode.ActiveScreen,
                        onClick = { viewModel.codePreviewMode = CodePreviewMode.ActiveScreen }
                    )
                    CodeModeTab(
                        text = "AppNavHost.kt",
                        isSelected = viewModel.codePreviewMode == CodePreviewMode.NavGraph,
                        onClick = { viewModel.codePreviewMode = CodePreviewMode.NavGraph }
                    )
                }

                // Line count stats
                Text(
                    text = "${lines.size} lines",
                    style = StudioTypography.Caption.copy(
                        color = StudioColors.TextMuted,
                        fontSize = 11.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle Search Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSearchOpen) StudioColors.Primary.copy(alpha = 0.2f) else StudioColors.ActiveSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSearchOpen) StudioColors.Primary else StudioColors.BorderSubtle,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            isSearchOpen = !isSearchOpen
                            if (!isSearchOpen) {
                                searchQuery = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search in Code",
                        tint = if (isSearchOpen) StudioColors.Primary else StudioColors.TextSecondary,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                }

                // Save File Button (.kt export)
                val activeScreenName = viewModel.activeScreen?.name ?: "Screen"
                val defaultFileName = if (viewModel.codePreviewMode == CodePreviewMode.ActiveScreen) {
                    "$activeScreenName.kt"
                } else {
                    "AppNavHost.kt"
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioColors.ActiveSurface)
                        .border(width = 1.dp, color = StudioColors.BorderSubtle, shape = RoundedCornerShape(4.dp))
                        .clickable {
                            val file = DesktopFilePicker.chooseSaveKotlinFile(defaultName = defaultFileName)
                            if (file != null) {
                                try {
                                    file.writeText(generatedCode)
                                    viewModel.notifySuccess("Saved ${file.name} successfully!")
                                } catch (e: Exception) {
                                    viewModel.notifyError("Failed to save file: ${e.message}")
                                }
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Save File",
                        tint = StudioColors.TextPrimary,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                    Text(
                        text = "Save File",
                        style = StudioTypography.Caption.copy(
                            color = StudioColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

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

        // 2. Expandable Search Toolbar
        AnimatedVisibility(
            visible = isSearchOpen,
            enter = expandVertically(tween(150)) + fadeIn(tween(150)),
            exit = shrinkVertically(tween(120)) + fadeOut(tween(120))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioColors.PanelSurface)
                    .border(width = 1.dp, color = StudioColors.BorderSubtle)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input Field
                JewelTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        currentMatchIndex = 0
                    },
                    modifier = Modifier.width(260.dp).height(28.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = StudioColors.TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            Box(
                                modifier = Modifier.clickable {
                                    searchQuery = ""
                                    currentMatchIndex = 0
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = StudioColors.TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else null,
                    placeholder = {
                        Text(
                            text = "Find in code...",
                            style = StudioTypography.Caption.copy(color = StudioColors.TextMuted)
                        )
                    }
                )

                // Match count indicator badge
                if (searchQuery.isNotEmpty()) {
                    val matchText = if (matches.isEmpty()) "No matches" else "${currentMatchIndex + 1} of ${matches.size}"
                    val matchColor = if (matches.isEmpty()) StudioColors.Error else StudioColors.TextSecondary
                    Text(
                        text = matchText,
                        style = StudioTypography.Caption.copy(color = matchColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                }

                // Previous match navigation button
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioColors.ActiveSurface)
                        .clickable(enabled = matches.isNotEmpty()) {
                            if (matches.isNotEmpty()) {
                                currentMatchIndex = (currentMatchIndex - 1 + matches.size) % matches.size
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Previous Match",
                        tint = if (matches.isNotEmpty()) StudioColors.TextPrimary else StudioColors.TextMuted,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                }

                // Next match navigation button
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioColors.ActiveSurface)
                        .clickable(enabled = matches.isNotEmpty()) {
                            if (matches.isNotEmpty()) {
                                currentMatchIndex = (currentMatchIndex + 1) % matches.size
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Next Match",
                        tint = if (matches.isNotEmpty()) StudioColors.TextPrimary else StudioColors.TextMuted,
                        modifier = Modifier.size(StudioSizes.IconSmall)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Close Search Bar
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .clickable {
                            isSearchOpen = false
                            searchQuery = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search",
                        tint = StudioColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 3. Code Viewer Area wrapped in SelectionContainer
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioColors.CardSurface)
                    .verticalScroll(vScroll)
            ) {
                // Line Numbers Gutter (Disabled from selection to prevent accidental line number copy)
                DisableSelection {
                    val maxDigits = remember(lines.size) { lines.size.toString().length.coerceAtLeast(2) }
                    val activeSearchLine = if (isSearchOpen && matches.isNotEmpty() && currentMatchIndex in matches.indices) {
                        matches[currentMatchIndex].lineIndex
                    } else -1

                    Column(
                        modifier = Modifier
                            .background(StudioColors.PanelSurface)
                            .border(width = 1.dp, color = StudioColors.BorderSubtle)
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.indices.forEach { idx ->
                            val isSearchActive = idx == activeSearchLine
                            val isNodeSelected = idx == selectedNodeLine
                            val lineBg = when {
                                isSearchActive -> StudioColors.Warning.copy(alpha = 0.25f)
                                isNodeSelected -> StudioColors.Primary.copy(alpha = 0.2f)
                                else -> Color.Transparent
                            }
                            val lineFg = when {
                                isSearchActive -> StudioColors.Warning
                                isNodeSelected -> StudioColors.Primary
                                else -> StudioColors.TextMuted
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .background(lineBg)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = (idx + 1).toString().padStart(maxDigits, ' '),
                                    style = StudioTypography.CodeMonospace.copy(
                                        color = lineFg,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        fontWeight = if (isSearchActive || isNodeSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                // Syntax Highlighted Code with Horizontal Scroll
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(hScroll)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = highlightedCode,
                        style = StudioTypography.CodeMonospace.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeModeTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) StudioColors.ActiveSurface else Color.Transparent
    val textCol = if (isSelected) StudioColors.Primary else StudioColors.TextSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = StudioTypography.Caption.copy(
                color = textCol,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            )
        )
    }
}
