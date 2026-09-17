package dev.chandradsl.m3c.core.domain.schema

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.scope.ContainerScope

/**
 * Central registry of all supported Material 3 composable component definitions.
 *
 * Serves as the single source of truth for Palette, Inspector, Hierarchy, Codegen, and AST generation.
 */
object ComponentRegistry {

    private val definitions = LinkedHashMap<ComponentType, ComponentDefinition>()

    init {
        // ====================================================================
        // 1. Layout Containers
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.Column,
            displayName = "Column",
            description = "Vertical layout container",
            category = ComponentCategory.LayoutContainers,
            iconName = "view_column",
            acceptsChildren = true,
            factory = { ComposableNode.ColumnNode(modifiers = listOf(ModifierDef.Padding.all(DpVal(8f)))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.Row,
            displayName = "Row",
            description = "Horizontal layout container",
            category = ComponentCategory.LayoutContainers,
            iconName = "view_stream",
            acceptsChildren = true,
            factory = { ComposableNode.RowNode(modifiers = listOf(ModifierDef.Padding.all(DpVal(8f)))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.Box,
            displayName = "Box",
            description = "Freeform stacking layout",
            category = ComponentCategory.LayoutContainers,
            iconName = "layers",
            acceptsChildren = true,
            factory = { ComposableNode.BoxNode() }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.Spacer,
            displayName = "Spacer",
            description = "Flexible layout gap",
            category = ComponentCategory.LayoutContainers,
            iconName = "space_bar",
            acceptsChildren = false,
            factory = { ComposableNode.SpacerNode(modifiers = listOf(ModifierDef.Height(DpVal(16f)))) }
        ))

        // ====================================================================
        // 2. Surfaces & Cards
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.Surface,
            displayName = "Surface",
            description = "Material elevation canvas",
            category = ComponentCategory.SurfacesAndCards,
            iconName = "web_asset",
            acceptsChildren = true,
            factory = { ComposableNode.SurfaceNode() }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.Card,
            displayName = "Card",
            description = "Filled container card",
            category = ComponentCategory.SurfacesAndCards,
            iconName = "crop_portrait",
            acceptsChildren = true,
            factory = {
                ComposableNode.CardNode(
                    modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                    content = listOf(ComposableNode.TextNode(text = "Card Content"))
                )
            }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.ElevatedCard,
            displayName = "Elevated Card",
            description = "Card with shadow elevation",
            category = ComponentCategory.SurfacesAndCards,
            iconName = "featured_play_list",
            acceptsChildren = true,
            factory = {
                ComposableNode.ElevatedCardNode(
                    modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                    content = listOf(ComposableNode.TextNode(text = "Elevated Card"))
                )
            }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.OutlinedCard,
            displayName = "Outlined Card",
            description = "Card with subtle border",
            category = ComponentCategory.SurfacesAndCards,
            iconName = "check_box_outline_blank",
            acceptsChildren = true,
            factory = {
                ComposableNode.OutlinedCardNode(
                    modifiers = listOf(ModifierDef.Padding.all(DpVal(8f))),
                    content = listOf(ComposableNode.TextNode(text = "Outlined Card"))
                )
            }
        ))

        // ====================================================================
        // 3. Buttons & Actions
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.Button,
            displayName = "Button",
            description = "Filled primary action",
            category = ComponentCategory.ButtonsAndActions,
            iconName = "smart_button",
            acceptsChildren = true,
            factory = { ComposableNode.ButtonNode(content = listOf(ComposableNode.TextNode(text = "Button"))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.ElevatedButton,
            displayName = "Elevated Button",
            description = "Button with shadow elevation",
            category = ComponentCategory.ButtonsAndActions,
            iconName = "ads_click",
            acceptsChildren = true,
            factory = { ComposableNode.ElevatedButtonNode(content = listOf(ComposableNode.TextNode(text = "Elevated"))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.FilledTonalButton,
            displayName = "Filled Tonal Button",
            description = "Medium emphasis tonal button",
            category = ComponentCategory.ButtonsAndActions,
            iconName = "highlight",
            acceptsChildren = true,
            factory = { ComposableNode.FilledTonalButtonNode(content = listOf(ComposableNode.TextNode(text = "Tonal Button"))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.OutlinedButton,
            displayName = "Outlined Button",
            description = "Bordered action button",
            category = ComponentCategory.ButtonsAndActions,
            iconName = "crop_landscape",
            acceptsChildren = true,
            factory = { ComposableNode.OutlinedButtonNode(content = listOf(ComposableNode.TextNode(text = "Outlined"))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.TextButton,
            displayName = "Text Button",
            description = "Low emphasis text button",
            category = ComponentCategory.ButtonsAndActions,
            iconName = "text_format",
            acceptsChildren = true,
            factory = { ComposableNode.TextButtonNode(content = listOf(ComposableNode.TextNode(text = "Text Button"))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.IconButton,
            displayName = "Icon Button",
            description = "Compact icon trigger",
            category = ComponentCategory.ButtonsAndActions,
            iconName = "touch_app",
            acceptsChildren = true,
            factory = { ComposableNode.IconButtonNode(content = listOf(ComposableNode.TextNode(text = "★"))) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.FloatingActionButton,
            displayName = "FAB",
            description = "Floating action button",
            category = ComponentCategory.ButtonsAndActions,
            iconName = "add_circle",
            acceptsChildren = true,
            factory = {
                ComposableNode.FloatingActionButtonNode(
                    shape = ShapeDef.Token(ShapeToken.Large),
                    content = listOf(ComposableNode.TextNode(text = "+"))
                )
            }
        ))

        // ====================================================================
        // 4. Text & Inputs
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.Text,
            displayName = "Text",
            description = "Typography display label",
            category = ComponentCategory.TextAndInputs,
            iconName = "text_fields",
            acceptsChildren = false,
            factory = { ComposableNode.TextNode(text = "Label Text", typography = TypographyToken.BodyMedium) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.TextField,
            displayName = "TextField",
            description = "Filled input text field",
            category = ComponentCategory.TextAndInputs,
            iconName = "edit_note",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.LEADING_ICON,
                    displayName = "Leading Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.IconButton, ComponentType.Text)
                ),
                SlotDefinition(
                    id = StandardSlots.TRAILING_ICON,
                    displayName = "Trailing Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.IconButton, ComponentType.Text)
                )
            ),
            factory = { ComposableNode.TextFieldNode(label = "Input Label") }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.OutlinedTextField,
            displayName = "Outlined TextField",
            description = "Bordered input text field",
            category = ComponentCategory.TextAndInputs,
            iconName = "edit_note",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.LEADING_ICON,
                    displayName = "Leading Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.IconButton, ComponentType.Text)
                ),
                SlotDefinition(
                    id = StandardSlots.TRAILING_ICON,
                    displayName = "Trailing Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.IconButton, ComponentType.Text)
                )
            ),
            factory = { ComposableNode.OutlinedTextFieldNode(label = "Input Label") }
        ))

        // ====================================================================
        // 5. Selection & Feedback
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.Checkbox,
            displayName = "Checkbox",
            description = "Binary multi-select control",
            category = ComponentCategory.SelectionAndFeedback,
            iconName = "check_box",
            acceptsChildren = false,
            factory = { ComposableNode.CheckboxNode(checked = true) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.Switch,
            displayName = "Switch",
            description = "Toggle state switch",
            category = ComponentCategory.SelectionAndFeedback,
            iconName = "toggle_on",
            acceptsChildren = false,
            factory = { ComposableNode.SwitchNode(checked = true) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.RadioButton,
            displayName = "RadioButton",
            description = "Single selection option",
            category = ComponentCategory.SelectionAndFeedback,
            iconName = "radio_button_checked",
            acceptsChildren = false,
            factory = { ComposableNode.RadioButtonNode(selected = true) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.Slider,
            displayName = "Slider",
            description = "Continuous range slider",
            category = ComponentCategory.SelectionAndFeedback,
            iconName = "linear_scale",
            acceptsChildren = false,
            factory = { ComposableNode.SliderNode(value = 0.5f) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.CircularProgressIndicator,
            displayName = "Circular Progress",
            description = "Radial loading spinner",
            category = ComponentCategory.SelectionAndFeedback,
            iconName = "autorenew",
            acceptsChildren = false,
            factory = { ComposableNode.CircularProgressIndicatorNode() }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.LinearProgressIndicator,
            displayName = "Linear Progress",
            description = "Horizontal loading bar",
            category = ComponentCategory.SelectionAndFeedback,
            iconName = "horizontal_rule",
            acceptsChildren = false,
            factory = { ComposableNode.LinearProgressIndicatorNode(progress = 0.6f) }
        ))

        // ====================================================================
        // 6. Dividers
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.HorizontalDivider,
            displayName = "Horizontal Divider",
            description = "Horizontal separating line",
            category = ComponentCategory.Dividers,
            iconName = "horizontal_distribute",
            acceptsChildren = false,
            factory = { ComposableNode.HorizontalDividerNode() }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.VerticalDivider,
            displayName = "Vertical Divider",
            description = "Vertical separating line",
            category = ComponentCategory.Dividers,
            iconName = "vertical_distribute",
            acceptsChildren = false,
            factory = { ComposableNode.VerticalDividerNode(modifiers = listOf(ModifierDef.Height(DpVal(24f)))) }
        ))

        // ====================================================================
        // 7. Scaffolding & Navigation
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.Scaffold,
            displayName = "Scaffold",
            description = "Top-level screen layout structure",
            category = ComponentCategory.Navigation,
            iconName = "web_asset",
            acceptsChildren = true,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.TOP_BAR,
                    displayName = "Top App Bar",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.TopAppBar)
                ),
                SlotDefinition(
                    id = StandardSlots.BOTTOM_BAR,
                    displayName = "Bottom Bar",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.NavigationBar, ComponentType.BottomAppBar)
                ),
                SlotDefinition(
                    id = StandardSlots.FLOATING_ACTION_BUTTON,
                    displayName = "Floating Action Button",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.FloatingActionButton)
                ),
                SlotDefinition(
                    id = StandardSlots.CONTENT,
                    displayName = "Main Content",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = emptySet()
                )
            ),
            factory = {
                ComposableNode.ScaffoldNode(
                    topBar = ComposableNode.TopAppBarNode(title = ComposableNode.TextNode(text = "Top App Bar")),
                    content = ComposableNode.ColumnNode(children = listOf(ComposableNode.TextNode(text = "Scaffold Content")))
                )
            }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.TopAppBar,
            displayName = "TopAppBar",
            description = "Screen header top app bar",
            category = ComponentCategory.Navigation,
            iconName = "view_agenda",
            acceptsChildren = true,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.TITLE,
                    displayName = "Title",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text)
                ),
                SlotDefinition(
                    id = StandardSlots.NAVIGATION_ICON,
                    displayName = "Navigation Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.IconButton)
                ),
                SlotDefinition(
                    id = StandardSlots.ACTIONS,
                    displayName = "Actions",
                    cardinality = SlotCardinality.List,
                    acceptedTypes = setOf(ComponentType.IconButton),
                    providedScope = ContainerScope.Row
                )
            ),
            factory = { ComposableNode.TopAppBarNode(title = ComposableNode.TextNode(text = "Title")) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.NavigationBar,
            displayName = "NavigationBar",
            description = "Bottom navigation bar",
            category = ComponentCategory.Navigation,
            iconName = "view_stream",
            acceptsChildren = true,
            factory = {
                ComposableNode.NavigationBarNode(
                    items = listOf(
                        ComposableNode.NavigationBarItemNode(
                            selected = true,
                            icon = ComposableNode.TextNode(text = "★"),
                            label = ComposableNode.TextNode(text = "Home")
                        ),
                        ComposableNode.NavigationBarItemNode(
                            selected = false,
                            icon = ComposableNode.TextNode(text = "⌕"),
                            label = ComposableNode.TextNode(text = "Search")
                        ),
                        ComposableNode.NavigationBarItemNode(
                            selected = false,
                            icon = ComposableNode.TextNode(text = "●"),
                            label = ComposableNode.TextNode(text = "Profile")
                        )
                    )
                )
            }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.NavigationBarItem,
            displayName = "Navigation Item",
            description = "Navigation bar destination item",
            category = ComponentCategory.Navigation,
            iconName = "touch_app",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.ICON,
                    displayName = "Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.IconButton, ComponentType.Text)
                ),
                SlotDefinition(
                    id = StandardSlots.LABEL,
                    displayName = "Label",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text)
                )
            ),
            factory = {
                ComposableNode.NavigationBarItemNode(
                    icon = ComposableNode.TextNode(text = "★"),
                    label = ComposableNode.TextNode(text = "Tab")
                )
            }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.BottomAppBar,
            displayName = "BottomAppBar",
            description = "Bottom application toolbar",
            category = ComponentCategory.Navigation,
            iconName = "view_agenda",
            acceptsChildren = true,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.ACTIONS,
                    displayName = "Actions",
                    cardinality = SlotCardinality.List,
                    acceptedTypes = emptySet(),
                    providedScope = ContainerScope.Row
                ),
                SlotDefinition(
                    id = StandardSlots.FLOATING_ACTION_BUTTON,
                    displayName = "FAB",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.FloatingActionButton)
                )
            ),
            factory = {
                ComposableNode.BottomAppBarNode(
                    actions = listOf(
                        ComposableNode.IconButtonNode(
                            content = listOf(ComposableNode.TextNode(text = "☰"))
                        ),
                        ComposableNode.IconButtonNode(
                            content = listOf(ComposableNode.TextNode(text = "🔍"))
                        )
                    ),
                    floatingActionButton = ComposableNode.FloatingActionButtonNode(
                        content = listOf(ComposableNode.TextNode(text = "+"))
                    )
                )
            }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.NavigationRail,
            displayName = "NavigationRail",
            description = "Vertical side navigation rail",
            category = ComponentCategory.Navigation,
            iconName = "view_sidebar",
            acceptsChildren = true,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.HEADER,
                    displayName = "Header",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = emptySet()
                )
            ),
            factory = {
                ComposableNode.NavigationRailNode(
                    header = ComposableNode.TextNode(text = "M3"),
                    items = listOf(
                        ComposableNode.NavigationRailItemNode(
                            selected = true,
                            icon = ComposableNode.TextNode(text = "★"),
                            label = ComposableNode.TextNode(text = "Home")
                        ),
                        ComposableNode.NavigationRailItemNode(
                            selected = false,
                            icon = ComposableNode.TextNode(text = "⌕"),
                            label = ComposableNode.TextNode(text = "Search")
                        ),
                        ComposableNode.NavigationRailItemNode(
                            selected = false,
                            icon = ComposableNode.TextNode(text = "●"),
                            label = ComposableNode.TextNode(text = "Profile")
                        )
                    )
                )
            }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.NavigationRailItem,
            displayName = "Navigation Rail Item",
            description = "Destination item in navigation rail",
            category = ComponentCategory.Navigation,
            iconName = "touch_app",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.ICON,
                    displayName = "Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.IconButton, ComponentType.Text)
                ),
                SlotDefinition(
                    id = StandardSlots.LABEL,
                    displayName = "Label",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text)
                )
            ),
            factory = {
                ComposableNode.NavigationRailItemNode(
                    icon = ComposableNode.TextNode(text = "★"),
                    label = ComposableNode.TextNode(text = "Rail")
                )
            }
        ))

        // ====================================================================
        // 8. Chips & Badges
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.AssistChip,
            displayName = "AssistChip",
            description = "Smart suggestion or action chip",
            category = ComponentCategory.ChipsAndBadges,
            iconName = "label",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.LEADING_ICON,
                    displayName = "Leading Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text, ComponentType.IconButton)
                )
            ),
            properties = listOf(
                PropertyDefinition.StringProperty("label", "Label", "Assist"),
                PropertyDefinition.BooleanProperty("enabled", "Enabled", true)
            ),
            factory = { ComposableNode.AssistChipNode(label = "Assist") }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.FilterChip,
            displayName = "FilterChip",
            description = "Filter selection chip",
            category = ComponentCategory.ChipsAndBadges,
            iconName = "label",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.LEADING_ICON,
                    displayName = "Leading Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text, ComponentType.IconButton)
                )
            ),
            properties = listOf(
                PropertyDefinition.StringProperty("label", "Label", "Filter"),
                PropertyDefinition.BooleanProperty("selected", "Selected", false),
                PropertyDefinition.BooleanProperty("enabled", "Enabled", true)
            ),
            factory = { ComposableNode.FilterChipNode(label = "Filter") }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.InputChip,
            displayName = "InputChip",
            description = "Input tag or recipient chip",
            category = ComponentCategory.ChipsAndBadges,
            iconName = "label",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.LEADING_ICON,
                    displayName = "Leading Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text, ComponentType.IconButton)
                ),
                SlotDefinition(
                    id = StandardSlots.TRAILING_ICON,
                    displayName = "Trailing Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text, ComponentType.IconButton)
                )
            ),
            properties = listOf(
                PropertyDefinition.StringProperty("label", "Label", "Input"),
                PropertyDefinition.BooleanProperty("selected", "Selected", false),
                PropertyDefinition.BooleanProperty("enabled", "Enabled", true)
            ),
            factory = { ComposableNode.InputChipNode(label = "Input") }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.SuggestionChip,
            displayName = "SuggestionChip",
            description = "Dynamic suggestion chip",
            category = ComponentCategory.ChipsAndBadges,
            iconName = "label",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.ICON,
                    displayName = "Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text, ComponentType.IconButton)
                )
            ),
            properties = listOf(
                PropertyDefinition.StringProperty("label", "Label", "Suggestion"),
                PropertyDefinition.BooleanProperty("enabled", "Enabled", true)
            ),
            factory = { ComposableNode.SuggestionChipNode(label = "Suggestion") }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.Badge,
            displayName = "Badge",
            description = "Notification count or indicator badge",
            category = ComponentCategory.ChipsAndBadges,
            iconName = "notifications",
            acceptsChildren = false,
            properties = listOf(
                PropertyDefinition.StringProperty("text", "Text", "8")
            ),
            factory = { ComposableNode.BadgeNode(text = "8") }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.BadgedBox,
            displayName = "BadgedBox",
            description = "Container linking a badge to content",
            category = ComponentCategory.ChipsAndBadges,
            iconName = "notifications",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.BADGE,
                    displayName = "Badge",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Badge)
                ),
                SlotDefinition(
                    id = StandardSlots.CONTENT,
                    displayName = "Content",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = emptySet()
                )
            ),
            factory = {
                ComposableNode.BadgedBoxNode(
                    badge = ComposableNode.BadgeNode(text = "3"),
                    content = ComposableNode.TextNode(text = "Mail")
                )
            }
        ))

        // ====================================================================
        // 9. Additional Controls & Dialogs
        // ====================================================================
        register(StandardComponentDefinition(
            type = ComponentType.RangeSlider,
            displayName = "RangeSlider",
            description = "Dual-thumb range selector",
            category = ComponentCategory.SelectionAndFeedback,
            iconName = "linear_scale",
            acceptsChildren = false,
            properties = listOf(
                PropertyDefinition.FloatProperty("startValue", "Start Value", 0f, 1f, 0.2f),
                PropertyDefinition.FloatProperty("endValue", "End Value", 0f, 1f, 0.8f),
                PropertyDefinition.BooleanProperty("enabled", "Enabled", true)
            ),
            factory = { ComposableNode.RangeSliderNode(startValue = 0.2f, endValue = 0.8f) }
        ))

        register(StandardComponentDefinition(
            type = ComponentType.AlertDialog,
            displayName = "AlertDialog",
            description = "Material 3 modal alert dialog",
            category = ComponentCategory.Dialogs,
            iconName = "feedback",
            acceptsChildren = false,
            slots = listOf(
                SlotDefinition(
                    id = StandardSlots.ICON,
                    displayName = "Icon",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text, ComponentType.IconButton)
                ),
                SlotDefinition(
                    id = StandardSlots.TITLE,
                    displayName = "Title",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text)
                ),
                SlotDefinition(
                    id = StandardSlots.TEXT,
                    displayName = "Body Text",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Text)
                ),
                SlotDefinition(
                    id = StandardSlots.CONFIRM_BUTTON,
                    displayName = "Confirm Button",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Button, ComponentType.TextButton)
                ),
                SlotDefinition(
                    id = StandardSlots.DISMISS_BUTTON,
                    displayName = "Dismiss Button",
                    cardinality = SlotCardinality.Single,
                    acceptedTypes = setOf(ComponentType.Button, ComponentType.TextButton)
                )
            ),
            factory = {
                ComposableNode.AlertDialogNode(
                    title = ComposableNode.TextNode(text = "Dialog Title"),
                    text = ComposableNode.TextNode(text = "A dialog is a type of modal window that appears in front of app content."),
                    confirmButton = ComposableNode.TextButtonNode(content = listOf(ComposableNode.TextNode(text = "Confirm")))
                )
            }
        ))
    }

    fun register(definition: ComponentDefinition) {
        definitions[definition.type] = definition
    }

    val all: List<ComponentDefinition>
        get() = definitions.values.toList()

    val allCategories: List<ComponentCategory>
        get() = ComponentCategory.entries

    fun findByType(type: ComponentType): ComponentDefinition? =
        definitions[type]

    fun findByNode(node: ComposableNode): ComponentDefinition? =
        definitions[node.componentType]

    fun byCategory(category: ComponentCategory): List<ComponentDefinition> =
        definitions.values.filter { it.category == category }
}

/**
 * Maps any AST node directly to its semantic [ComponentType].
 */
val ComposableNode.componentType: ComponentType
    get() = when (this) {
        is ComposableNode.ColumnNode -> ComponentType.Column
        is ComposableNode.RowNode -> ComponentType.Row
        is ComposableNode.BoxNode -> ComponentType.Box
        is ComposableNode.SpacerNode -> ComponentType.Spacer
        is ComposableNode.SurfaceNode -> ComponentType.Surface
        is ComposableNode.CardNode -> ComponentType.Card
        is ComposableNode.ElevatedCardNode -> ComponentType.ElevatedCard
        is ComposableNode.OutlinedCardNode -> ComponentType.OutlinedCard
        is ComposableNode.ButtonNode -> ComponentType.Button
        is ComposableNode.ElevatedButtonNode -> ComponentType.ElevatedButton
        is ComposableNode.FilledTonalButtonNode -> ComponentType.FilledTonalButton
        is ComposableNode.OutlinedButtonNode -> ComponentType.OutlinedButton
        is ComposableNode.TextButtonNode -> ComponentType.TextButton
        is ComposableNode.IconButtonNode -> ComponentType.IconButton
        is ComposableNode.FloatingActionButtonNode -> ComponentType.FloatingActionButton
        is ComposableNode.TextNode -> ComponentType.Text
        is ComposableNode.TextFieldNode -> ComponentType.TextField
        is ComposableNode.OutlinedTextFieldNode -> ComponentType.OutlinedTextField
        is ComposableNode.CheckboxNode -> ComponentType.Checkbox
        is ComposableNode.SwitchNode -> ComponentType.Switch
        is ComposableNode.RadioButtonNode -> ComponentType.RadioButton
        is ComposableNode.SliderNode -> ComponentType.Slider
        is ComposableNode.CircularProgressIndicatorNode -> ComponentType.CircularProgressIndicator
        is ComposableNode.LinearProgressIndicatorNode -> ComponentType.LinearProgressIndicator
        is ComposableNode.HorizontalDividerNode -> ComponentType.HorizontalDivider
        is ComposableNode.VerticalDividerNode -> ComponentType.VerticalDivider
        is ComposableNode.ScaffoldNode -> ComponentType.Scaffold
        is ComposableNode.TopAppBarNode -> ComponentType.TopAppBar
        is ComposableNode.NavigationBarNode -> ComponentType.NavigationBar
        is ComposableNode.NavigationBarItemNode -> ComponentType.NavigationBarItem
        is ComposableNode.AssistChipNode -> ComponentType.AssistChip
        is ComposableNode.FilterChipNode -> ComponentType.FilterChip
        is ComposableNode.InputChipNode -> ComponentType.InputChip
        is ComposableNode.SuggestionChipNode -> ComponentType.SuggestionChip
        is ComposableNode.BadgeNode -> ComponentType.Badge
        is ComposableNode.BadgedBoxNode -> ComponentType.BadgedBox
        is ComposableNode.BottomAppBarNode -> ComponentType.BottomAppBar
        is ComposableNode.NavigationRailNode -> ComponentType.NavigationRail
        is ComposableNode.NavigationRailItemNode -> ComponentType.NavigationRailItem
        is ComposableNode.RangeSliderNode -> ComponentType.RangeSlider
        is ComposableNode.AlertDialogNode -> ComponentType.AlertDialog
    }
