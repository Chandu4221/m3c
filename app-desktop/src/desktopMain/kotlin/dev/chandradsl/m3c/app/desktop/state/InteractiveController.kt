package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chandradsl.m3c.core.domain.model.InteractiveEvent
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class InteractiveLogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
    val title: String,
    val detail: String? = null,
    val isNavigation: Boolean = false
)

/**
 * Manages live interactive prototype state, user action event history, and dynamic form inputs
 * while the studio canvas is in Interactive Mode.
 */
class InteractiveController(
    private val onNavigateToScreen: (String) -> Unit = {}
) {
    val eventLogs = mutableStateListOf<InteractiveLogEntry>()
    val dynamicVariables = mutableStateMapOf<String, String>()
    var lastFeedbackMessage: String? by mutableStateOf(null)

    fun handleEvent(event: InteractiveEvent, availableScreens: List<M3cScreen> = emptyList()) {
        when (event) {
            is InteractiveEvent.Click -> {
                val label = event.label ?: event.componentTag
                val trimmed = label.trim()

                // Smart simulation: detect screen navigation based on button label and available screens
                val matchingScreen = availableScreens.firstOrNull { screen ->
                    screen.name.equals(trimmed, ignoreCase = true) ||
                    screen.name.removeSuffix("Screen").equals(trimmed, ignoreCase = true) ||
                    ((trimmed.equals("Sign In", ignoreCase = true) || trimmed.equals("Log In", ignoreCase = true)) &&
                        (screen.name.contains("Feed", ignoreCase = true) || screen.name.contains("Home", ignoreCase = true) || screen.name.contains("Dashboard", ignoreCase = true))) ||
                    (trimmed.equals("Checkout", ignoreCase = true) && screen.name.contains("Checkout", ignoreCase = true)) ||
                    (trimmed.equals("Profile", ignoreCase = true) && screen.name.contains("Profile", ignoreCase = true)) ||
                    (trimmed.equals("Settings", ignoreCase = true) && screen.name.contains("Settings", ignoreCase = true))
                }

                if (matchingScreen != null) {
                    val log = InteractiveLogEntry(
                        title = "Clicked '$label' → Navigated to ${matchingScreen.name}",
                        detail = "Triggered navigation route for ${matchingScreen.name}",
                        isNavigation = true
                    )
                    eventLogs.add(0, log)
                    lastFeedbackMessage = "Navigated to ${matchingScreen.name}"
                    onNavigateToScreen(matchingScreen.id)
                } else if (trimmed.equals("arrow_back", ignoreCase = true) || trimmed.equals("Back", ignoreCase = true)) {
                    val firstScreen = availableScreens.firstOrNull()
                    if (firstScreen != null) {
                        val log = InteractiveLogEntry(
                            title = "Back action clicked → Returned to ${firstScreen.name}",
                            detail = "Simulated back stack pop",
                            isNavigation = true
                        )
                        eventLogs.add(0, log)
                        lastFeedbackMessage = "Returned to ${firstScreen.name}"
                        onNavigateToScreen(firstScreen.id)
                    }
                } else {
                    val log = InteractiveLogEntry(
                        title = "Clicked '$label'",
                        detail = "Component: ${event.componentTag} (${event.nodeId.value})"
                    )
                    eventLogs.add(0, log)
                    lastFeedbackMessage = "Clicked '$label'"
                }
            }

            is InteractiveEvent.ValueChange -> {
                val varKey = "${event.componentTag}_${event.nodeId.value}"
                dynamicVariables[varKey] = event.value
                val log = InteractiveLogEntry(
                    title = "${event.componentTag} value changed",
                    detail = "\"${event.value}\""
                )
                eventLogs.add(0, log)
                lastFeedbackMessage = "${event.componentTag} = \"${event.value}\""
            }

            is InteractiveEvent.Toggle -> {
                val varKey = "${event.componentTag}_${event.nodeId.value}"
                val stateText = if (event.checked) "ON (true)" else "OFF (false)"
                dynamicVariables[varKey] = event.checked.toString()
                val log = InteractiveLogEntry(
                    title = "${event.componentTag} toggled to $stateText",
                    detail = "Component: ${event.componentTag} (${event.nodeId.value})"
                )
                eventLogs.add(0, log)
                lastFeedbackMessage = "${event.componentTag}: $stateText"
            }

            is InteractiveEvent.Navigation -> {
                val log = InteractiveLogEntry(
                    title = "Navigated to ${event.targetScreenName}",
                    detail = "Programmatic route dispatch",
                    isNavigation = true
                )
                eventLogs.add(0, log)
                lastFeedbackMessage = "Navigated to ${event.targetScreenName}"
                val target = availableScreens.firstOrNull { it.name.equals(event.targetScreenName, ignoreCase = true) }
                if (target != null) {
                    onNavigateToScreen(target.id)
                }
            }
        }
    }

    fun resetState() {
        eventLogs.clear()
        dynamicVariables.clear()
        lastFeedbackMessage = "Interactive state reset"
    }
}
