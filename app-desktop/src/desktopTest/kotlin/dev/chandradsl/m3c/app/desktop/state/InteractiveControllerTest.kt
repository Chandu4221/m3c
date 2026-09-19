package dev.chandradsl.m3c.app.desktop.state

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.InteractiveEvent
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import dev.chandradsl.m3c.core.domain.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InteractiveControllerTest {

    private fun testScreen(id: String, name: String, route: String) = M3cScreen(
        id = id,
        name = name,
        route = route,
        rootNode = ComposableNode.BoxNode(id = NodeId("root_$id"), children = emptyList())
    )

    @Test
    fun testRecordClickEvent() {
        val controller = InteractiveController()
        val event = InteractiveEvent.Click(
            nodeId = NodeId("btn_1"),
            componentTag = "Button",
            label = "Submit Form"
        )

        controller.handleEvent(event, emptyList())

        assertEquals(1, controller.eventLogs.size)
        assertEquals("Clicked 'Submit Form'", controller.eventLogs.first().title)
        assertEquals("Clicked 'Submit Form'", controller.lastFeedbackMessage)
    }

    @Test
    fun testNavigationForwardFromButtonLabel() {
        var switchedScreenId: String? = null
        val controller = InteractiveController(
            onNavigateToScreen = { switchedScreenId = it }
        )
        val screen1 = testScreen("screen_1", "LoginScreen", "login")
        val screen2 = testScreen("screen_2", "FeedScreen", "feed")
        val screens = listOf(screen1, screen2)

        val clickEvent = InteractiveEvent.Click(
            nodeId = NodeId("btn_login"),
            componentTag = "Button",
            label = "Sign In"
        )

        controller.handleEvent(clickEvent, screens)

        assertEquals("screen_2", switchedScreenId)
        assertEquals(1, controller.eventLogs.size)
        val log = controller.eventLogs.first()
        assertTrue(log.isNavigation)
        assertEquals("Clicked 'Sign In' → Navigated to FeedScreen", log.title)
        assertEquals("Navigated to FeedScreen", controller.lastFeedbackMessage)
    }

    @Test
    fun testBackNavigation() {
        var switchedScreenId: String? = null
        val controller = InteractiveController(
            onNavigateToScreen = { switchedScreenId = it }
        )
        val screen1 = testScreen("screen_1", "FeedScreen", "feed")
        val screen2 = testScreen("screen_2", "DetailsScreen", "details")
        val screens = listOf(screen1, screen2)

        val backClickEvent = InteractiveEvent.Click(
            nodeId = NodeId("btn_back"),
            componentTag = "IconButton",
            label = "arrow_back"
        )

        controller.handleEvent(backClickEvent, screens)

        assertEquals("screen_1", switchedScreenId)
        assertEquals(1, controller.eventLogs.size)
        val log = controller.eventLogs.first()
        assertTrue(log.isNavigation)
        assertEquals("Back action clicked → Returned to FeedScreen", log.title)
        assertEquals("Returned to FeedScreen", controller.lastFeedbackMessage)
    }

    @Test
    fun testValueChangeMutatesDynamicVariables() {
        val controller = InteractiveController()
        val event = InteractiveEvent.ValueChange(
            nodeId = NodeId("txt_email"),
            componentTag = "TextField",
            value = "alice@example.com"
        )

        controller.handleEvent(event, emptyList())

        assertEquals(1, controller.eventLogs.size)
        assertEquals("alice@example.com", controller.dynamicVariables["TextField_txt_email"])
    }

    @Test
    fun testToggleMutatesDynamicVariables() {
        val controller = InteractiveController()
        val eventToggleOn = InteractiveEvent.Toggle(
            nodeId = NodeId("sw_dark"),
            componentTag = "Switch",
            checked = true
        )

        controller.handleEvent(eventToggleOn, emptyList())

        assertEquals("true", controller.dynamicVariables["Switch_sw_dark"])

        val eventToggleOff = InteractiveEvent.Toggle(
            nodeId = NodeId("sw_dark"),
            componentTag = "Switch",
            checked = false
        )

        controller.handleEvent(eventToggleOff, emptyList())

        assertEquals("false", controller.dynamicVariables["Switch_sw_dark"])
        assertEquals(2, controller.eventLogs.size)
    }

    @Test
    fun testResetStateClearsEverything() {
        val controller = InteractiveController()
        controller.handleEvent(
            InteractiveEvent.Click(NodeId("b1"), "Button", "Go"),
            emptyList()
        )
        controller.handleEvent(
            InteractiveEvent.ValueChange(NodeId("t1"), "TextField", "Hello"),
            emptyList()
        )

        assertTrue(controller.eventLogs.isNotEmpty())
        assertTrue(controller.dynamicVariables.isNotEmpty())

        controller.resetState()

        assertTrue(controller.eventLogs.isEmpty())
        assertTrue(controller.dynamicVariables.isEmpty())
        assertEquals("Interactive state reset", controller.lastFeedbackMessage)
    }
}
