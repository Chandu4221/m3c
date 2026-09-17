package dev.chandradsl.m3c.core.domain.schema

/**
 * Unique identifier representing the semantic type of a Material 3 Composable.
 */
enum class ComponentType(val id: String) {
    // 1. Layout Containers
    Column("column"),
    Row("row"),
    Box("box"),
    Spacer("spacer"),

    // 2. Surfaces & Cards
    Surface("surface"),
    Card("card"),
    ElevatedCard("elevated_card"),
    OutlinedCard("outlined_card"),

    // 3. Buttons & Actions
    Button("button"),
    ElevatedButton("elevated_button"),
    FilledTonalButton("filled_tonal_button"),
    OutlinedButton("outlined_button"),
    TextButton("text_button"),
    IconButton("icon_button"),
    FloatingActionButton("floating_action_button"),

    // 4. Text & Inputs
    Text("text"),
    Icon("icon"),
    TextField("text_field"),
    OutlinedTextField("outlined_text_field"),

    // 5. Chips & Badges
    AssistChip("assist_chip"),
    FilterChip("filter_chip"),
    InputChip("input_chip"),
    SuggestionChip("suggestion_chip"),
    Badge("badge"),
    BadgedBox("badged_box"),

    // 6. Selection & Feedback
    Checkbox("checkbox"),
    Switch("switch"),
    RadioButton("radio_button"),
    Slider("slider"),
    RangeSlider("range_slider"),
    CircularProgressIndicator("circular_progress_indicator"),
    LinearProgressIndicator("linear_progress_indicator"),

    // 7. Dividers
    HorizontalDivider("horizontal_divider"),
    VerticalDivider("vertical_divider"),

    // 8. Scaffolding & Navigation
    Scaffold("scaffold"),
    TopAppBar("top_app_bar"),
    BottomAppBar("bottom_app_bar"),
    NavigationBar("navigation_bar"),
    NavigationBarItem("navigation_bar_item"),
    NavigationRail("navigation_rail"),
    NavigationRailItem("navigation_rail_item"),

    // 9. Dialogs
    AlertDialog("alert_dialog")
}
