package dev.chandradsl.m3c.core.domain.schema

/**
 * Functional categories for grouping components in palettes, trees, and inspectors.
 */
enum class ComponentCategory(val displayName: String) {
    LayoutContainers("LAYOUT CONTAINERS"),
    SurfacesAndCards("SURFACES & CARDS"),
    ButtonsAndActions("BUTTONS & ACTIONS"),
    TextAndInputs("TEXT & INPUTS"),
    ChipsAndBadges("CHIPS & BADGES"),
    SelectionAndFeedback("SELECTION & FEEDBACK"),
    Dividers("DIVIDERS"),
    Navigation("NAVIGATION"),
    Dialogs("DIALOGS")
}
