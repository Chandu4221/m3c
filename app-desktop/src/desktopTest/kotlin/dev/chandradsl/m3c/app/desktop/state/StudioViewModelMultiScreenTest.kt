package dev.chandradsl.m3c.app.desktop.state

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModelMultiScreenTest {

    private fun TestScope.createViewModel(): StudioViewModel {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val vmScope = CoroutineScope(testDispatcher + SupervisorJob(backgroundScope.coroutineContext[Job]))
        return StudioViewModel(coroutineScope = vmScope, computationDispatcher = testDispatcher)
    }

    @Test
    fun testInitialMultiScreenState() {
        val vm = StudioViewModel()
        assertEquals(1, vm.screens.size)
        val firstScreen = vm.screens.first()
        assertEquals("MainScreen", firstScreen.name)
        assertEquals("main", firstScreen.route)
        assertTrue(firstScreen.isStartDestination)
        assertEquals(firstScreen.id, vm.activeScreenId)
        assertFalse(vm.isDirty)
    }

    @Test
    fun testAddScreenAndSwitching() {
        val vm = StudioViewModel()
        val initialScreenId = vm.activeScreenId

        // Add a second screen
        vm.addScreen(name = "DetailsScreen", route = "details", isStartDestination = false)

        assertEquals(2, vm.screens.size)
        val secondScreen = vm.screens[1]
        assertEquals("DetailsScreen", secondScreen.name)
        assertEquals("details", secondScreen.route)
        assertFalse(secondScreen.isStartDestination)
        assertEquals(secondScreen.id, vm.activeScreenId)
        assertTrue(vm.isDirty)

        // Switch back to initial screen
        vm.selectScreen(initialScreenId)
        assertEquals(initialScreenId, vm.activeScreenId)
        assertEquals("MainScreen", vm.activeScreen?.name)
    }

    @Test
    fun testSyncRootNodeOnScreenSwitch() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val mainScreenId = vm.activeScreenId

        // Add a child node to main screen
        val newChild = ComposableNode.TextNode(id = NodeId("test_text"), text = "Edited Text")
        vm.dispatch(WorkspaceIntent.InsertChild(parentId = vm.workspaceState.rootNode.id, node = newChild))
        advanceUntilIdle()

        // Add second screen (which switches to it)
        vm.addScreen("ProfileScreen", "profile")
        advanceUntilIdle()
        assertNotEquals(mainScreenId, vm.activeScreenId)

        // Switch back to main screen
        vm.selectScreen(mainScreenId)
        advanceUntilIdle()

        // Verify the edits in main screen were preserved in its AST
        val mainScreen = vm.screens.first { it.id == mainScreenId }
        val foundNode = (mainScreen.rootNode as? ComposableNode.ScaffoldNode)?.content
            ?: mainScreen.rootNode
        assertTrue(vm.workspaceState.rootNode.id == mainScreen.rootNode.id)
    }

    @Test
    fun testUpdateScreenMetadata() {
        val vm = StudioViewModel()
        val screenId = vm.activeScreenId

        vm.updateScreen(screenId, "DashboardScreen", "dashboard", isStartDestination = true)

        val updated = vm.screens.first { it.id == screenId }
        assertEquals("DashboardScreen", updated.name)
        assertEquals("dashboard", updated.route)
        assertTrue(updated.isStartDestination)
        assertTrue(vm.isDirty)
    }

    @Test
    fun testDuplicateScreen() {
        val vm = StudioViewModel()
        val initialId = vm.activeScreenId

        vm.duplicateScreen(initialId)

        assertEquals(2, vm.screens.size)
        val duplicated = vm.screens[1]
        assertEquals("MainScreenCopy", duplicated.name)
        assertEquals("main_copy", duplicated.route)
        assertNotEquals(initialId, duplicated.id)
        assertNotEquals(vm.screens[0].rootNode.id, duplicated.rootNode.id)
        assertEquals(duplicated.id, vm.activeScreenId)
        assertTrue(vm.isDirty)
    }

    @Test
    fun testDeleteScreenRules() {
        val vm = StudioViewModel()

        // 1. Cannot delete only screen
        val onlyScreenId = vm.activeScreenId
        vm.deleteScreen(onlyScreenId)
        assertEquals(1, vm.screens.size)

        // 2. Add second screen, then delete first screen
        vm.addScreen("SettingsScreen", "settings", isStartDestination = false)
        assertEquals(2, vm.screens.size)

        val settingsId = vm.activeScreenId
        vm.deleteScreen(onlyScreenId)

        assertEquals(1, vm.screens.size)
        assertEquals(settingsId, vm.screens.first().id)
        // Since only screen remains, it should become start destination
        assertTrue(vm.screens.first().isStartDestination)
        assertEquals(settingsId, vm.activeScreenId)
        assertTrue(vm.isDirty)
    }

    @Test
    fun testSetStartDestination() {
        val vm = StudioViewModel()
        vm.addScreen("SecondScreen", "second", isStartDestination = false)
        val secondId = vm.activeScreenId

        vm.setStartDestination(secondId)

        val first = vm.screens[0]
        val second = vm.screens[1]
        assertFalse(first.isStartDestination)
        assertTrue(second.isStartDestination)
        assertTrue(vm.isDirty)
    }

    @Test
    fun testNewProjectResetsToSingleScreen() {
        val vm = StudioViewModel()
        vm.addScreen("TempScreen", "temp")
        assertEquals(2, vm.screens.size)

        vm.newProject()

        assertEquals(1, vm.screens.size)
        assertEquals("MainScreen", vm.screens.first().name)
        assertEquals("main", vm.screens.first().route)
        assertTrue(vm.screens.first().isStartDestination)
        assertFalse(vm.isDirty)
    }
}
