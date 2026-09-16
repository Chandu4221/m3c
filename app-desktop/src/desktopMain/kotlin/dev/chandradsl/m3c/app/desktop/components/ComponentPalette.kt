package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerticalDistribute
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken

data class PaletteItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val factory: () -> ComposableNode
)

@Composable
fun ComponentPalette(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color(0xFF181825))
            .border(width = 1.dp, color = Color(0xFF313244))
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PaletteSection(title = "Layout Containers", items = layoutItems, onSelect = viewModel::insertComponent)
        PaletteSection(title = "Surfaces & Cards", items = surfaceItems, onSelect = viewModel::insertComponent)
        PaletteSection(title = "Buttons & Actions", items = buttonItems, onSelect = viewModel::insertComponent)
        PaletteSection(title = "Text & Inputs", items = textItems, onSelect = viewModel::insertComponent)
        PaletteSection(title = "Selection & Feedback", items = selectionItems, onSelect = viewModel::insertComponent)
        PaletteSection(title = "Dividers & Utilities", items = utilityItems, onSelect = viewModel::insertComponent)
    }
}

@Composable
private fun PaletteSection(
    title: String,
    items: List<PaletteItem>,
    onSelect: (ComposableNode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            color = Color(0xFF6C7086),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        items.forEach { item ->
            PaletteItemCard(item = item, onClick = { onSelect(item.factory()) })
        }
    }
}

@Composable
private fun PaletteItemCard(
    item: PaletteItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E1E2E))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF313244))
                .padding(6.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = Color(0xFFCBA6F7),
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = Color(0xFFCDD6F4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.description,
                color = Color(0xFF6C7086),
                fontSize = 10.sp
            )
        }
    }
}

// ============================================================================
// Palette Component Definitions with Vector Icons
// ============================================================================

private val layoutItems = listOf(
    PaletteItem("Column", "Vertical layout container", Icons.Default.ViewColumn) {
        ComposableNode.ColumnNode(
            modifiers = listOf(ModifierDef.Padding.all(DpVal(8f)))
        )
    },
    PaletteItem("Row", "Horizontal layout container", Icons.Default.ViewStream) {
        ComposableNode.RowNode(
            modifiers = listOf(ModifierDef.Padding.all(DpVal(8f)))
        )
    },
    PaletteItem("Box", "Freeform stacking layout", Icons.Default.Layers) {
        ComposableNode.BoxNode()
    },
    PaletteItem("Spacer", "Flexible layout gap", Icons.Default.SpaceBar) {
        ComposableNode.SpacerNode(
            modifiers = listOf(ModifierDef.Height(DpVal(16f)))
        )
    }
)

private val surfaceItems = listOf(
    PaletteItem("Surface", "Material elevation canvas", Icons.Default.WebAsset) {
        ComposableNode.SurfaceNode(
            shape = ShapeDef.Token(ShapeToken.Medium),
            tonalElevation = DpVal(1f)
        )
    },
    PaletteItem("Card", "Filled container card", Icons.Default.CropPortrait) {
        ComposableNode.CardNode(
            content = listOf(ComposableNode.TextNode(text = "Card Content"))
        )
    },
    PaletteItem("Elevated Card", "Card with shadow elevation", Icons.AutoMirrored.Filled.FeaturedPlayList) {
        ComposableNode.ElevatedCardNode(
            content = listOf(ComposableNode.TextNode(text = "Elevated Card Content"))
        )
    },
    PaletteItem("Outlined Card", "Card with subtle border", Icons.Default.CheckBoxOutlineBlank) {
        ComposableNode.OutlinedCardNode(
            content = listOf(ComposableNode.TextNode(text = "Outlined Card Content"))
        )
    }
)

private val buttonItems = listOf(
    PaletteItem("Button", "Filled primary action", Icons.Default.SmartButton) {
        ComposableNode.ButtonNode(
            content = listOf(ComposableNode.TextNode(text = "Button"))
        )
    },
    PaletteItem("Elevated Button", "Button with shadow elevation", Icons.Default.AdsClick) {
        ComposableNode.ElevatedButtonNode(
            content = listOf(ComposableNode.TextNode(text = "Elevated Button"))
        )
    },
    PaletteItem("Filled Tonal Button", "Medium emphasis tonal button", Icons.Default.Highlight) {
        ComposableNode.FilledTonalButtonNode(
            content = listOf(ComposableNode.TextNode(text = "Tonal Button"))
        )
    },
    PaletteItem("Outlined Button", "Bordered action button", Icons.Default.CropLandscape) {
        ComposableNode.OutlinedButtonNode(
            content = listOf(ComposableNode.TextNode(text = "Outlined Button"))
        )
    },
    PaletteItem("Text Button", "Low emphasis text action", Icons.Default.TextFormat) {
        ComposableNode.TextButtonNode(
            content = listOf(ComposableNode.TextNode(text = "Text Button"))
        )
    },
    PaletteItem("Icon Button", "Standard icon action", Icons.Default.TouchApp) {
        ComposableNode.IconButtonNode(
            content = listOf(ComposableNode.TextNode(text = "★"))
        )
    },
    PaletteItem("FAB", "Floating action button", Icons.Default.AddCircle) {
        ComposableNode.FloatingActionButtonNode(
            shape = ShapeDef.Token(ShapeToken.Large),
            content = listOf(ComposableNode.TextNode(text = "+"))
        )
    }
)

private val textItems = listOf(
    PaletteItem("Text", "Label or heading text", Icons.Default.TextFields) {
        ComposableNode.TextNode(
            text = "Text Label",
            typography = TypographyToken.BodyMedium
        )
    },
    PaletteItem("Outlined Text Field", "User input box with border", Icons.Default.EditNote) {
        ComposableNode.OutlinedTextFieldNode(
            value = "",
            label = "Enter Text",
            placeholder = "Type here..."
        )
    },
    PaletteItem("Text Field", "Filled user input box", Icons.Default.EditNote) {
        ComposableNode.TextFieldNode(
            value = "",
            label = "Enter Text",
            placeholder = "Type here..."
        )
    }
)

private val selectionItems = listOf(
    PaletteItem("Checkbox", "Binary selection toggle", Icons.Default.CheckBox) {
        ComposableNode.CheckboxNode(checked = true)
    },
    PaletteItem("Switch", "On/Off slider toggle", Icons.Default.ToggleOn) {
        ComposableNode.SwitchNode(checked = true)
    },
    PaletteItem("Radio Button", "Single selection option", Icons.Default.RadioButtonChecked) {
        ComposableNode.RadioButtonNode(selected = true)
    },
    PaletteItem("Slider", "Continuous range input", Icons.Default.LinearScale) {
        ComposableNode.SliderNode(value = 0.5f)
    },
    PaletteItem("Circular Progress", "Radial loading spinner", Icons.Default.Autorenew) {
        ComposableNode.CircularProgressIndicatorNode(progress = null)
    },
    PaletteItem("Linear Progress", "Horizontal progress bar", Icons.Default.HorizontalRule) {
        ComposableNode.LinearProgressIndicatorNode(progress = 0.6f)
    }
)

private val utilityItems = listOf(
    PaletteItem("Horizontal Divider", "Subtle horizontal separator", Icons.Default.HorizontalDistribute) {
        ComposableNode.HorizontalDividerNode(thickness = DpVal(1f))
    },
    PaletteItem("Vertical Divider", "Subtle vertical separator", Icons.Default.VerticalDistribute) {
        ComposableNode.VerticalDividerNode(thickness = DpVal(1f))
    }
)