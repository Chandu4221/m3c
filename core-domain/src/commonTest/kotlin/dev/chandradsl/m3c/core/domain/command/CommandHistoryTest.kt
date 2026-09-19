package dev.chandradsl.m3c.core.domain.command

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.schema.StandardSlots
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceStore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommandHistoryTest {

    @Test
    fun testInsertAndUndoRedo() = runTest {
        val root = ComposableNode.ColumnNode()
        val history = CommandHistory()

        val textNode = ComposableNode.TextNode(text = "Hello World")
        val insertCmd = InsertNodeCommand(parentId = root.id, node = textNode)

        val rootAfterInsert = history.execute(insertCmd, root) as ComposableNode.ColumnNode
        assertEquals(1, rootAfterInsert.children.size)
        assertEquals(textNode.id, rootAfterInsert.children[0].id)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)

        val rootAfterUndo = history.undo(rootAfterInsert) as ComposableNode.ColumnNode
        assertEquals(0, rootAfterUndo.children.size)
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)

        val rootAfterRedo = history.redo(rootAfterUndo) as ComposableNode.ColumnNode
        assertEquals(1, rootAfterRedo.children.size)
        assertEquals(textNode.id, rootAfterRedo.children[0].id)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun testDeleteAndRestoreAtExactIndex() = runTest {
        val child1 = ComposableNode.TextNode(text = "One")
        val child2 = ComposableNode.TextNode(text = "Two")
        val child3 = ComposableNode.TextNode(text = "Three")
        val root = ComposableNode.ColumnNode(children = listOf(child1, child2, child3))

        val history = CommandHistory()
        val deleteCmd = DeleteNodeCommand.create(root, child2.id)

        val rootAfterDelete = history.execute(deleteCmd, root) as ComposableNode.ColumnNode
        assertEquals(listOf(child1.id, child3.id), rootAfterDelete.children.map { it.id })

        val rootAfterUndo = history.undo(rootAfterDelete) as ComposableNode.ColumnNode
        assertEquals(3, rootAfterUndo.children.size)
        assertEquals(child2.id, rootAfterUndo.children[1].id)
        assertEquals("Two", (rootAfterUndo.children[1] as ComposableNode.TextNode).text)
    }

    @Test
    fun testUpdateNodeMerging() = runTest {
        val textNode = ComposableNode.TextNode(text = "Init")
        val root = ComposableNode.ColumnNode(children = listOf(textNode))

        val history = CommandHistory()

        // Typing "Init" -> "Initial" -> "Initial Value"
        val cmd1 = UpdateNodeCommand.create(root, textNode.copy(text = "Initial"))
        val root1 = history.execute(cmd1, root)

        val cmd2 = UpdateNodeCommand.create(root1, textNode.copy(text = "Initial Value"))
        val root2 = history.execute(cmd2, root1)

        assertEquals("Initial Value", (root2 as ComposableNode.ColumnNode).children[0].let { (it as ComposableNode.TextNode).text })

        // A single undo should revert back to the original "Init" due to command merging
        val rootUndone = history.undo(root2) as ComposableNode.ColumnNode
        assertEquals("Init", (rootUndone.children[0] as ComposableNode.TextNode).text)
    }

    @Test
    fun testSetSlotCommand() = runTest {
        val scaffold = ComposableNode.ScaffoldNode()
        val topBar = ComposableNode.TopAppBarNode(title = ComposableNode.TextNode(text = "Screen"))

        val history = CommandHistory()
        val setSlotCmd = SetSlotCommand.create(scaffold, scaffold.id, StandardSlots.TOP_BAR, topBar)

        val withTopBar = history.execute(setSlotCmd, scaffold) as ComposableNode.ScaffoldNode
        assertNotNull(withTopBar.topBar)
        assertEquals(topBar.id, withTopBar.topBar?.id)

        val undone = history.undo(withTopBar) as ComposableNode.ScaffoldNode
        assertNull(undone.topBar)

        val redone = history.redo(undone) as ComposableNode.ScaffoldNode
        assertNotNull(redone.topBar)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testWorkspaceStoreWithCommands() = runTest(kotlinx.coroutines.test.UnconfinedTestDispatcher()) {
        val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        val root = ComposableNode.ColumnNode()
        val store = WorkspaceStore(root, testDispatcher)
        val job = store.start(this)

        val btn = ComposableNode.ButtonNode()
        store.dispatch(WorkspaceIntent.InsertChild(parentId = root.id, node = btn))

        assertTrue(store.state.canUndo)
        assertFalse(store.state.canRedo)
        assertEquals(1, (store.state.rootNode as ComposableNode.ColumnNode).children.size)

        store.dispatch(WorkspaceIntent.UpdateModifiers(
            targetId = btn.id,
            modifiers = listOf(ModifierDef.Padding.all(DpVal(16f)))
        ))
        assertEquals(1, (store.state.rootNode as ComposableNode.ColumnNode).children[0].modifiers.size)

        // Undo modifier update
        store.dispatch(WorkspaceIntent.Undo)
        assertEquals(0, (store.state.rootNode as ComposableNode.ColumnNode).children[0].modifiers.size)

        // Undo button insert
        store.dispatch(WorkspaceIntent.Undo)
        assertEquals(0, (store.state.rootNode as ComposableNode.ColumnNode).children.size)
        assertFalse(store.state.canUndo)
        assertTrue(store.state.canRedo)

        // Redo button insert
        store.dispatch(WorkspaceIntent.Redo)
        assertEquals(1, (store.state.rootNode as ComposableNode.ColumnNode).children.size)
        job.cancel()
    }

    @Test
    fun testJumpToTimeline() = runTest {
        val root = ComposableNode.ColumnNode()
        val history = CommandHistory()

        val text1 = ComposableNode.TextNode(text = "First")
        val text2 = ComposableNode.TextNode(text = "Second")
        val text3 = ComposableNode.TextNode(text = "Third")

        var current = history.execute(InsertNodeCommand(root.id, text1), root)
        current = history.execute(InsertNodeCommand(root.id, text2), current)
        current = history.execute(InsertNodeCommand(root.id, text3), current)

        assertEquals(3, (current as ComposableNode.ColumnNode).children.size)
        val timeline = history.getTimelineSnapshot()
        assertEquals(4, timeline.size) // step 0, 1, 2, 3
        assertEquals(3, timeline.first { it.isCurrent }.stepIndex)

        // Jump to Step 1 (only First should remain)
        current = history.jumpTo(1, current)
        assertEquals(1, (current as ComposableNode.ColumnNode).children.size)
        assertEquals("First", (current.children[0] as ComposableNode.TextNode).text)

        val timelineAfterJump = history.getTimelineSnapshot()
        assertEquals(1, timelineAfterJump.first { it.isCurrent }.stepIndex)
        // Steps 2 and 3 should now be future/redoable
        assertTrue(timelineAfterJump[2].isFuture)
        assertTrue(timelineAfterJump[3].isFuture)

        // Jump to Step 3 (all three should be restored)
        current = history.jumpTo(3, current)
        assertEquals(3, (current as ComposableNode.ColumnNode).children.size)

        // Jump to Step 0 (initial document, 0 children)
        current = history.jumpTo(0, current)
        assertEquals(0, (current as ComposableNode.ColumnNode).children.size)
    }

    @Test
    fun testSnapshotAndRestoreStacks() = runTest {
        val root = ComposableNode.ColumnNode()
        val historyA = CommandHistory()

        val text1 = ComposableNode.TextNode(text = "A1")
        val text2 = ComposableNode.TextNode(text = "A2")

        val stateAfter2 = historyA.execute(InsertNodeCommand(root.id, text2), historyA.execute(InsertNodeCommand(root.id, text1), root))
        val (undoA, redoA) = historyA.snapshotStacks()
        assertEquals(2, undoA.size)

        // Simulate switching to Screen B
        val historyB = CommandHistory()
        assertEquals(0, historyB.snapshotStacks().first.size)

        // Restore Screen A's stacks into historyB
        historyB.restoreStacks(undoA, redoA)
        assertEquals(2, historyB.snapshotStacks().first.size)

        val undone = historyB.undo(stateAfter2) as ComposableNode.ColumnNode
        assertEquals(1, undone.children.size)
        assertEquals("A1", (undone.children[0] as ComposableNode.TextNode).text)
    }
}
