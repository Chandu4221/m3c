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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryTimelineTest {

    private fun TestScope.createViewModel(): StudioViewModel {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val vmScope = CoroutineScope(testDispatcher + SupervisorJob(backgroundScope.coroutineContext[Job]))
        return StudioViewModel(coroutineScope = vmScope, computationDispatcher = testDispatcher)
    }

    @Test
    fun testInitialTimelineState() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertFalse(vm.workspaceState.canUndo)
        assertFalse(vm.workspaceState.canRedo)
        assertEquals(1, vm.historyTimeline.size)
        val initialStep = vm.historyTimeline.first()
        assertEquals(0, initialStep.stepIndex)
        assertTrue(initialStep.isCurrent)
        assertFalse(initialStep.isFuture)
    }

    @Test
    fun testTimelineRecordsOperations() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val rootId = vm.workspaceState.rootNode.id

        val button = ComposableNode.ButtonNode(
            id = NodeId("test_btn"),
            content = listOf(ComposableNode.TextNode(text = "Click Me"))
        )

        vm.dispatch(WorkspaceIntent.InsertChild(parentId = rootId, node = button))
        advanceUntilIdle()

        assertTrue(vm.workspaceState.canUndo)
        assertFalse(vm.workspaceState.canRedo)
        assertEquals(2, vm.historyTimeline.size)

        val currentStep = vm.historyTimeline.first { it.isCurrent }
        assertEquals(1, currentStep.stepIndex)
        assertNotNull(vm.lastUndoDescription)
    }

    @Test
    fun testTimeTravelJumpTo() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val rootId = vm.workspaceState.rootNode.id

        val text1 = ComposableNode.TextNode(id = NodeId("t1"), text = "First")
        val text2 = ComposableNode.TextNode(id = NodeId("t2"), text = "Second")
        val text3 = ComposableNode.TextNode(id = NodeId("t3"), text = "Third")

        vm.dispatch(WorkspaceIntent.InsertChild(parentId = rootId, node = text1))
        advanceUntilIdle()
        vm.dispatch(WorkspaceIntent.InsertChild(parentId = rootId, node = text2))
        advanceUntilIdle()
        vm.dispatch(WorkspaceIntent.InsertChild(parentId = rootId, node = text3))
        advanceUntilIdle()

        assertEquals(4, vm.historyTimeline.size) // step 0, 1, 2, 3
        assertEquals(3, vm.historyTimeline.first { it.isCurrent }.stepIndex)

        // Time-travel jump to Step 1
        vm.jumpToHistoryStep(1)
        advanceUntilIdle()
        val currentStepAfterJump = vm.historyTimeline.first { it.isCurrent }
        assertEquals(1, currentStepAfterJump.stepIndex)
        assertTrue(vm.workspaceState.canRedo)
        assertNotNull(vm.nextRedoDescription)

        // Fast-forward jump to Step 3
        vm.jumpToHistoryStep(3)
        advanceUntilIdle()
        assertEquals(3, vm.historyTimeline.first { it.isCurrent }.stepIndex)
        assertFalse(vm.workspaceState.canRedo)
        assertTrue(vm.workspaceState.canUndo)

        // Jump to Step 0 (Initial Baseline)
        vm.jumpToHistoryStep(0)
        advanceUntilIdle()
        assertEquals(0, vm.historyTimeline.first { it.isCurrent }.stepIndex)
        assertFalse(vm.workspaceState.canUndo)
        assertTrue(vm.workspaceState.canRedo)
    }

    @Test
    fun testMultiScreenHistoryIsolation() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val screen1Id = vm.activeScreenId
        val screen1RootId = vm.workspaceState.rootNode.id

        // Edit Screen 1
        val btn1 = ComposableNode.ButtonNode(id = NodeId("btn_s1"), content = emptyList())
        vm.dispatch(WorkspaceIntent.InsertChild(parentId = screen1RootId, node = btn1))
        advanceUntilIdle()
        assertTrue(vm.workspaceState.canUndo)

        // Add Screen 2 (which selects it)
        vm.addScreen(name = "DetailsScreen", route = "details")
        advanceUntilIdle()
        val screen2Id = vm.activeScreenId
        val screen2RootId = vm.workspaceState.rootNode.id

        // Screen 2 has fresh history
        assertFalse(vm.workspaceState.canUndo)

        // Edit Screen 2
        val textS2 = ComposableNode.TextNode(id = NodeId("txt_s2"), text = "Details Header")
        vm.dispatch(WorkspaceIntent.InsertChild(parentId = screen2RootId, node = textS2))
        advanceUntilIdle()
        assertTrue(vm.workspaceState.canUndo)

        // Switch back to Screen 1
        vm.selectScreen(screen1Id)
        advanceUntilIdle()
        assertEquals(screen1Id, vm.activeScreenId)

        // Screen 1 history should be restored and undoable!
        assertTrue(vm.workspaceState.canUndo)

        // Undo Screen 1 edit
        vm.undo()
        advanceUntilIdle()

        // Switch back to Screen 2
        vm.selectScreen(screen2Id)
        advanceUntilIdle()
        assertEquals(screen2Id, vm.activeScreenId)

        // Screen 2 history should still be intact and undoable!
        assertTrue(vm.workspaceState.canUndo)
    }
}
