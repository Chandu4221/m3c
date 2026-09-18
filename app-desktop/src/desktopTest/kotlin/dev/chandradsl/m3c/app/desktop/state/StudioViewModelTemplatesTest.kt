package dev.chandradsl.m3c.app.desktop.state

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.template.ScreenTemplates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StudioViewModelTemplatesTest {

    @Test
    fun testAddScreenFromTemplate() {
        val vm = StudioViewModel()
        assertEquals(1, vm.screens.size)

        // Add Login Screen from Template
        vm.addScreenFromTemplate(ScreenTemplates.Login)
        assertEquals(2, vm.screens.size)
        val loginScreen = vm.screens[1]
        assertEquals("Login", loginScreen.name)
        assertEquals("login", loginScreen.route)
        assertEquals(loginScreen.id, vm.activeScreenId)
        assertTrue(vm.isDirty)
        assertTrue(loginScreen.rootNode is ComposableNode.ScaffoldNode)

        // Add Settings Screen from Template
        vm.addScreenFromTemplate(ScreenTemplates.Settings)
        assertEquals(3, vm.screens.size)
        val settingsScreen = vm.screens[2]
        assertEquals("Settings", settingsScreen.name)
        assertEquals("settings", settingsScreen.route)
        assertEquals(settingsScreen.id, vm.activeScreenId)

        // Add duplicate template (name collision handling)
        vm.addScreenFromTemplate(ScreenTemplates.Login)
        assertEquals(4, vm.screens.size)
        val login2Screen = vm.screens[3]
        assertEquals("Login2", login2Screen.name)
        assertEquals("login2", login2Screen.route)
    }

    @Test
    fun testAddAllTemplatesToProject() {
        val vm = StudioViewModel()
        val initialId = vm.activeScreenId

        for (template in ScreenTemplates.all) {
            vm.addScreenFromTemplate(template)
        }

        // 1 initial screen + 5 templates = 6 screens
        assertEquals(6, vm.screens.size)

        val names = vm.screens.map { it.name }
        assertTrue(names.contains("Login"))
        assertTrue(names.contains("Profile"))
        assertTrue(names.contains("Settings"))
        assertTrue(names.contains("Feed"))
        assertTrue(names.contains("Checkout"))

        // Check start destination preserved on initial screen
        val initialScreen = vm.screens.first { it.id == initialId }
        assertTrue(initialScreen.isStartDestination)
    }
}
