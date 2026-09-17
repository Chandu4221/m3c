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
    TextField("text_field"),
    OutlinedTextField("outlined_text_field"),

    // 5. Selection & Feedback
    Checkbox("checkbox"),
    Switch("switch"),
    RadioButton("radio_button"),
    Slider("slider"),
    CircularProgressIndicator("circular_progress_indicator"),
    LinearProgressIndicator("linear_progress_indicator"),

    // 6. Dividers
    HorizontalDivider("horizontal_divider"),
    VerticalDivider("vertical_divider"),

    // 7. Scaffolding & Navigation
    Scaffold("scaffold"),
    TopAppBar("top_app_bar"),
    NavigationBar("navigation_bar"),
    NavigationBarItem("navigation_bar_item")
}
