package dev.chandradsl.m3c.runtime.renderer.decorator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.core.domain.model.NodeId

@Composable
fun SelectionDecorator(
    nodeId: NodeId,
    nodeTag: String,
    isSelected: Boolean,
    isInteractiveMode: Boolean = false,
    onSelect: (NodeId) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isInteractiveMode) {
        // Pure preview mode: render raw content with zero editor overhead
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null // No ripple so it feels like an editor selection
            ) {
                onSelect(nodeId)
            }
    ) {
        content()

        // Selection Tag pill in the top-start corner when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(bottomEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = nodeTag,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 9.sp,
                    lineHeight = 10.sp
                )
            }
        }
    }
}