package dev.chandradsl.m3c.core.domain.command

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.schema.StandardSlots
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommandHistoryTest {

    @Test
    fun testInsertAndUndoRedo() {
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
    fun testDeleteAndRestoreAtExactIndex() {
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
    fun testUpdateNodeMerging() {
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
    fun testSetSlotCommand() {
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

    @Test
    fun testWorkspaceStoreWithCommands() {
        val root = ComposableNode.ColumnNode()
        val store = WorkspaceStore(root)

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
    }
}
