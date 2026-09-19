package dev.chandradsl.m3c.core.domain.model

/**
 * Domain event emitted during canvas interactive simulation mode.
 * Captures user interactions like button clicks, input value changes, toggles, and navigation actions.
 */
sealed interface InteractiveEvent {

    /**
     * User clicked on a button, icon button, card, or actionable component.
     */
    data class Click(
        val nodeId: NodeId,
        val componentTag: String,
        val label: String? = null
    ) : InteractiveEvent

    /**
     * User edited the value of a text input or dragged a slider.
     */
    data class ValueChange(
        val nodeId: NodeId,
        val componentTag: String,
        val value: String
    ) : InteractiveEvent

    /**
     * User toggled a switch, checkbox, or radio button.
     */
    data class Toggle(
        val nodeId: NodeId,
        val componentTag: String,
        val checked: Boolean
    ) : InteractiveEvent

    /**
     * User triggered an action that requests navigating to a target screen.
     */
    data class Navigation(
        val targetScreenName: String
    ) : InteractiveEvent
}
