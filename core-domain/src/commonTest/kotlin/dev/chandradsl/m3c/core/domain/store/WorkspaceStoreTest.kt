package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkspaceStoreTest {

    @Test
    fun testInsertChildAndUndoRedo() {
        val root = ComposableNode.ColumnNode(id = "root_1")
        val store = WorkspaceStore(root)

        val textNode = ComposableNode.TextNode(id = "txt_1", text = "Hello M3C")
        store.dispatch(WorkspaceIntent.InsertChild(parentId = "root_1", node = textNode))

        val updatedRoot = store.state.rootNode as ComposableNode.ColumnNode
        assertEquals(1, updatedRoot.children.size)
        assertEquals("txt_1", updatedRoot.children.first().id)
        assertTrue(store.state.canUndo)
        assertFalse(store.state.canRedo)

        // Undo
        store.dispatch(WorkspaceIntent.Undo)
        val revertedRoot = store.state.rootNode as ComposableNode.ColumnNode
        assertEquals(0, revertedRoot.children.size)
        assertFalse(store.state.canUndo)
        assertTrue(store.state.canRedo)

        // Redo
        store.dispatch(WorkspaceIntent.Redo)
        val redoneRoot = store.state.rootNode as ComposableNode.ColumnNode
        assertEquals(1, redoneRoot.children.size)
    }

    @Test
    fun testUpdateModifier() {
        val textNode = ComposableNode.TextNode(id = "txt_1", text = "Hello")
        val root = ComposableNode.BoxNode(id = "box_1", children = listOf(textNode))
        val store = WorkspaceStore(root)

        val newModifiers = listOf(ModifierDef.Padding(start = 16f, top = 16f))
        store.dispatch(WorkspaceIntent.UpdateModifiers(targetId = "txt_1", modifiers = newModifiers))

        val updatedBox = store.state.rootNode as ComposableNode.BoxNode
        val updatedText = updatedBox.children.first() as ComposableNode.TextNode
        assertEquals(1, updatedText.modifiers.size)
    }
}