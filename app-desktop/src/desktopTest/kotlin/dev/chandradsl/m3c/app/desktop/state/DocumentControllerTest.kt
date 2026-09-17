package dev.chandradsl.m3c.app.desktop.state

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentControllerTest {

    private fun TestScope.createController(root: ComposableNode): DocumentController {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val controllerScope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob(backgroundScope.coroutineContext[kotlinx.coroutines.Job]))
        return DocumentController(
            initialRoot = root,
            scope = controllerScope,
            computationDispatcher = testDispatcher
        )
    }

    @Test
    fun testIsContainerNodeMatchesSchema() = runTest {
        val root = ComposableNode.ColumnNode()
        val controller = createController(root)

        assertTrue(controller.isContainerNode(ComposableNode.ColumnNode()))
        assertTrue(controller.isContainerNode(ComposableNode.RowNode()))
        assertTrue(controller.isContainerNode(ComposableNode.BoxNode()))
        assertTrue(controller.isContainerNode(ComposableNode.SurfaceNode()))
        assertTrue(controller.isContainerNode(ComposableNode.CardNode()))
        assertTrue(controller.isContainerNode(ComposableNode.ButtonNode()))
        assertTrue(controller.isContainerNode(ComposableNode.ScaffoldNode()))
        assertTrue(controller.isContainerNode(ComposableNode.NavigationBarNode()))
        assertTrue(controller.isContainerNode(ComposableNode.BottomAppBarNode()))
        assertTrue(controller.isContainerNode(ComposableNode.NavigationRailNode()))
        assertTrue(controller.isContainerNode(ComposableNode.BadgedBoxNode()))
        assertTrue(controller.isContainerNode(ComposableNode.TopAppBarNode(title = ComposableNode.TextNode(text = "Title"))))
        assertTrue(controller.isContainerNode(ComposableNode.AlertDialogNode()))

        assertFalse(controller.isContainerNode(ComposableNode.TextNode(text = "Sample")))
        assertFalse(controller.isContainerNode(ComposableNode.CheckboxNode()))
        assertFalse(controller.isContainerNode(ComposableNode.SpacerNode()))
        assertFalse(controller.isContainerNode(ComposableNode.HorizontalDividerNode()))
        assertFalse(controller.isContainerNode(ComposableNode.SliderNode()))
    }

    @Test
    fun testDuplicateNodeInsertsSiblingWithUniqueIds() = runTest {
        val text1 = ComposableNode.TextNode(text = "First")
        val text2 = ComposableNode.TextNode(text = "Second")
        val initialRoot = ComposableNode.ColumnNode(children = listOf(text1, text2))

        val controller = createController(initialRoot)
        assertEquals(2, (controller.workspaceState.rootNode as ComposableNode.ColumnNode).children.size)

        controller.duplicateNode(text1.id)
        testScheduler.advanceUntilIdle()

        val updatedRoot = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(3, updatedRoot.children.size)

        val duplicated = updatedRoot.children[1] as ComposableNode.TextNode
        assertEquals("First", duplicated.text)
        assertNotEquals(text1.id, duplicated.id)
        assertEquals(duplicated.id, controller.workspaceState.selectedNodeId)
    }

    @Test
    fun testDuplicateContainerClonesChildrenDeeply() = runTest {
        val innerText = ComposableNode.TextNode(text = "Inside Card")
        val card = ComposableNode.CardNode(content = listOf(innerText))
        val initialRoot = ComposableNode.ColumnNode(children = listOf(card))

        val controller = createController(initialRoot)
        controller.duplicateNode(card.id)
        testScheduler.advanceUntilIdle()

        val updatedRoot = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(2, updatedRoot.children.size)

        val duplicatedCard = updatedRoot.children[1] as ComposableNode.CardNode
        assertNotEquals(card.id, duplicatedCard.id)
        assertEquals(1, duplicatedCard.content.size)
        assertNotEquals(innerText.id, duplicatedCard.content[0].id)
        assertEquals("Inside Card", (duplicatedCard.content[0] as ComposableNode.TextNode).text)
    }

    @Test
    fun testWrapInColumnAndWrapInRow() = runTest {
        val text = ComposableNode.TextNode(text = "Wrap Me")
        val initialRoot = ComposableNode.ColumnNode(children = listOf(text))

        val controller = createController(initialRoot)

        // Wrap in Row
        controller.wrapInRow(text.id)
        testScheduler.advanceUntilIdle()
        var updatedRoot = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, updatedRoot.children.size)
        assertTrue(updatedRoot.children[0] is ComposableNode.RowNode)

        val row = updatedRoot.children[0] as ComposableNode.RowNode
        assertEquals(controller.workspaceState.selectedNodeId, row.id)
        assertEquals(1, row.children.size)
        assertEquals(text.id, row.children[0].id)

        // Wrap the Row in Column
        controller.wrapInColumn(row.id)
        testScheduler.advanceUntilIdle()
        updatedRoot = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, updatedRoot.children.size)
        assertTrue(updatedRoot.children[0] is ComposableNode.ColumnNode)

        val innerCol = updatedRoot.children[0] as ComposableNode.ColumnNode
        assertEquals(1, innerCol.children.size)
        assertEquals(row.id, innerCol.children[0].id)
    }

    @Test
    fun testMoveNodeUpAndDown() = runTest {
        val itemA = ComposableNode.TextNode(text = "A")
        val itemB = ComposableNode.TextNode(text = "B")
        val itemC = ComposableNode.TextNode(text = "C")
        val initialRoot = ComposableNode.ColumnNode(children = listOf(itemA, itemB, itemC))

        val controller = createController(initialRoot)

        assertFalse(controller.canMoveUp(itemA.id))
        assertTrue(controller.canMoveDown(itemA.id))
        assertTrue(controller.canMoveUp(itemB.id))
        assertTrue(controller.canMoveDown(itemB.id))
        assertTrue(controller.canMoveUp(itemC.id))
        assertFalse(controller.canMoveDown(itemC.id))

        // Move A down -> order becomes B, A, C
        controller.moveNodeDown(itemA.id)
        testScheduler.advanceUntilIdle()
        var root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(listOf(itemB.id, itemA.id, itemC.id), root.children.map { it.id })

        // Move C up -> order becomes B, C, A
        controller.moveNodeUp(itemC.id)
        testScheduler.advanceUntilIdle()
        root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(listOf(itemB.id, itemC.id, itemA.id), root.children.map { it.id })
    }

    @Test
    fun testMoveNodeIntoAndOut() = runTest {
        val child = ComposableNode.TextNode(text = "Child")
        val box = ComposableNode.BoxNode(children = emptyList())
        val initialRoot = ComposableNode.ColumnNode(children = listOf(box, child))

        val controller = createController(initialRoot)

        assertTrue(controller.canMoveIn(child.id))
        controller.moveNodeIn(child.id)
        testScheduler.advanceUntilIdle()

        var root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, root.children.size)
        val updatedBox = root.children[0] as ComposableNode.BoxNode
        assertEquals(1, updatedBox.children.size)
        assertEquals(child.id, updatedBox.children[0].id)

        assertTrue(controller.canMoveOut(child.id))
        controller.moveNodeOut(child.id)
        testScheduler.advanceUntilIdle()

        root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(2, root.children.size)
        assertEquals(box.id, root.children[0].id)
        assertEquals(child.id, root.children[1].id)
    }

    @Test
    fun testModifierOperationsAndScope() = runTest {
        val text = ComposableNode.TextNode(text = "Hello")
        val initialRoot = ComposableNode.ColumnNode(children = listOf(text))

        val controller = createController(initialRoot)
        assertEquals(ContainerScope.Column, controller.getParentScope(text.id))

        val pad = ModifierDef.Padding.all(DpVal(8f))
        controller.addModifier(text.id, pad)
        testScheduler.advanceUntilIdle()

        var root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, root.children[0].modifiers.size)
        assertEquals(pad, root.children[0].modifiers[0])

        controller.removeModifier(text.id, 0)
        testScheduler.advanceUntilIdle()
        root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertTrue(root.children[0].modifiers.isEmpty())
    }

    @Test
    fun testUndoAndRedo() = runTest {
        val text1 = ComposableNode.TextNode(text = "1")
        val text2 = ComposableNode.TextNode(text = "2")
        val initialRoot = ComposableNode.ColumnNode(children = listOf(text1))

        val controller = createController(initialRoot)
        assertFalse(controller.workspaceState.canUndo)
        assertFalse(controller.workspaceState.canRedo)

        controller.dispatch(
            dev.chandradsl.m3c.core.domain.store.WorkspaceIntent.InsertChild(
                parentId = initialRoot.id,
                node = text2,
                index = -1
            )
        )
        testScheduler.advanceUntilIdle()

        var root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(2, root.children.size)
        assertTrue(controller.workspaceState.canUndo)

        controller.undo()
        testScheduler.advanceUntilIdle()
        root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, root.children.size)
        assertTrue(controller.workspaceState.canRedo)

        controller.redo()
        testScheduler.advanceUntilIdle()
        root = controller.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(2, root.children.size)
    }
}
