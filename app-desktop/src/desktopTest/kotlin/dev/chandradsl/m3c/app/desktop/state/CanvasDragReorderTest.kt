package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.TreeDropPosition
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CanvasDragReorderTest {

    private fun waitFor(timeoutMs: Long = 3000, condition: () -> Boolean) = runBlocking {
        withTimeout(timeoutMs) {
            while (!condition()) {
                delay(15)
            }
        }
    }

    @Test
    fun testDragControllerCanvasDragTracking() {
        val controller = DragController()
        val nodeId = NodeId.generate("btn")
        val targetId = NodeId.generate("target")

        controller.registerCanvasNodeBounds(
            nodeId = targetId,
            tag = "Button",
            bounds = Rect(100f, 100f, 200f, 150f),
            parentLayout = "Column"
        )
        assertEquals(1, controller.allCanvasNodes.size)

        controller.startCanvasDrag(nodeId, Offset(50f, 50f))
        assertEquals(nodeId, controller.activeCanvasDragNodeId)
        assertEquals(Offset(50f, 50f), controller.dragPointerOffset)

        controller.updateCanvasDrag(Offset(10f, 20f))
        assertEquals(Offset(60f, 70f), controller.dragPointerOffset)

        controller.setCanvasDropTarget(targetId, TreeDropPosition.BELOW)
        assertEquals(targetId, controller.canvasDropTargetId)
        assertEquals(TreeDropPosition.BELOW, controller.canvasDropPosition)

        controller.cancelCanvasDrag()
        assertNull(controller.activeCanvasDragNodeId)
        assertNull(controller.canvasDropTargetId)
        assertNull(controller.canvasDropPosition)

        controller.unregisterCanvasNodeBounds(targetId)
        assertEquals(0, controller.allCanvasNodes.size)
    }

    @Test
    fun testCanvasSiblingReorderingInColumn() {
        val btnA = ComposableNode.ButtonNode(content = listOf(ComposableNode.TextNode(text = "Button A")))
        val btnB = ComposableNode.ButtonNode(content = listOf(ComposableNode.TextNode(text = "Button B")))
        val rootColumn = ComposableNode.ColumnNode(children = listOf(btnA, btnB))

        val viewModel = StudioViewModel()
        viewModel.dispatch(WorkspaceIntent.LoadDocument(rootColumn))
        waitFor { viewModel.workspaceState.rootNode.id == rootColumn.id }

        // Set canvas bounds
        viewModel.updateCanvasBounds(Rect(0f, 0f, 800f, 1000f))

        // Register bounds for nodes
        val boundsA = Rect(100f, 100f, 300f, 150f) // height = 50, midY = 125
        val boundsB = Rect(100f, 160f, 300f, 210f) // height = 50, midY = 185
        viewModel.registerCanvasNodeBounds(btnA.id, "Button", boundsA, "Column")
        viewModel.registerCanvasNodeBounds(btnB.id, "Button", boundsB, "Column")

        // 1. Drag Button A down to lower half of Button B (y = 195 > midY 185) -> BELOW
        viewModel.startCanvasDrag(btnA.id, Offset(150f, 100f))
        viewModel.updateCanvasDrag(Offset(0f, 95f)) // pointer becomes (150f, 195f)

        assertEquals(btnB.id, viewModel.canvasDropTargetId)
        assertEquals(TreeDropPosition.BELOW, viewModel.canvasDropPosition)

        // End drag to execute reordering
        viewModel.endCanvasDrag()

        // Wait for reorder to complete
        waitFor {
            val root = viewModel.workspaceState.rootNode as? ComposableNode.ColumnNode
            root?.children?.size == 2 && root.children[0].id == btnB.id
        }

        // Verify children order is now [btnB, btnA]
        val updatedChildren = (viewModel.workspaceState.rootNode as ComposableNode.ColumnNode).children
        assertEquals(2, updatedChildren.size)
        assertEquals(btnB.id, updatedChildren[0].id)
        assertEquals(btnA.id, updatedChildren[1].id)

        // 2. Drag Button A up to upper half of Button B (y = 170 < midY 185) -> ABOVE
        viewModel.startCanvasDrag(btnA.id, Offset(150f, 195f))
        viewModel.updateCanvasDrag(Offset(0f, -25f)) // pointer becomes (150f, 170f)

        assertEquals(btnB.id, viewModel.canvasDropTargetId)
        assertEquals(TreeDropPosition.ABOVE, viewModel.canvasDropPosition)

        viewModel.endCanvasDrag()

        // Wait for reorder to complete
        waitFor {
            val root = viewModel.workspaceState.rootNode as? ComposableNode.ColumnNode
            root?.children?.size == 2 && root.children[0].id == btnA.id
        }

        // Verify children order is restored to [btnA, btnB]
        val reorderedChildren = (viewModel.workspaceState.rootNode as ComposableNode.ColumnNode).children
        assertEquals(2, reorderedChildren.size)
        assertEquals(btnA.id, reorderedChildren[0].id)
        assertEquals(btnB.id, reorderedChildren[1].id)
    }

    @Test
    fun testCanvasSiblingReorderingInRow() {
        val textA = ComposableNode.TextNode(text = "Left")
        val textB = ComposableNode.TextNode(text = "Right")
        val rootRow = ComposableNode.RowNode(children = listOf(textA, textB))

        val viewModel = StudioViewModel()
        viewModel.dispatch(WorkspaceIntent.LoadDocument(rootRow))
        waitFor { viewModel.workspaceState.rootNode.id == rootRow.id }

        viewModel.updateCanvasBounds(Rect(0f, 0f, 800f, 1000f))

        val boundsA = Rect(100f, 100f, 150f, 140f) // width = 50, midX = 125
        val boundsB = Rect(160f, 100f, 210f, 140f) // width = 50, midX = 185
        viewModel.registerCanvasNodeBounds(textA.id, "Text", boundsA, "Row")
        viewModel.registerCanvasNodeBounds(textB.id, "Text", boundsB, "Row")

        // Drag Text A into right half of Text B (x = 195 > midX 185) -> BELOW (after)
        viewModel.startCanvasDrag(textA.id, Offset(100f, 120f))
        viewModel.updateCanvasDrag(Offset(95f, 0f)) // pointer becomes (195f, 120f)

        assertEquals(textB.id, viewModel.canvasDropTargetId)
        assertEquals(TreeDropPosition.BELOW, viewModel.canvasDropPosition)

        viewModel.endCanvasDrag()

        waitFor {
            val root = viewModel.workspaceState.rootNode as? ComposableNode.RowNode
            root?.children?.size == 2 && root.children[0].id == textB.id
        }

        val children = (viewModel.workspaceState.rootNode as ComposableNode.RowNode).children
        assertEquals(2, children.size)
        assertEquals(textB.id, children[0].id)
        assertEquals(textA.id, children[1].id)
    }

    @Test
    fun testCanvasContainerReparenting() {
        val btn = ComposableNode.ButtonNode(content = listOf(ComposableNode.TextNode(text = "Button")))
        val targetBox = ComposableNode.BoxNode(children = emptyList())
        val rootColumn = ComposableNode.ColumnNode(children = listOf(btn, targetBox))

        val viewModel = StudioViewModel()
        viewModel.dispatch(WorkspaceIntent.LoadDocument(rootColumn))
        waitFor { viewModel.workspaceState.rootNode.id == rootColumn.id }

        viewModel.updateCanvasBounds(Rect(0f, 0f, 800f, 1000f))

        val boxBounds = Rect(100f, 200f, 300f, 400f)
        viewModel.registerCanvasNodeBounds(targetBox.id, "Box", boxBounds, "Column")

        // Drag button inside box (pointer at 150, 300 is 50% height of box -> INSIDE)
        viewModel.startCanvasDrag(btn.id, Offset(100f, 100f))
        viewModel.updateCanvasDrag(Offset(50f, 200f)) // pointer becomes (150f, 300f), inside Box

        assertEquals(targetBox.id, viewModel.canvasDropTargetId)
        assertEquals(TreeDropPosition.INSIDE, viewModel.canvasDropPosition)

        viewModel.endCanvasDrag()

        waitFor {
            val root = viewModel.workspaceState.rootNode as? ComposableNode.ColumnNode
            val box = root?.children?.getOrNull(0) as? ComposableNode.BoxNode
            box?.children?.any { it.id == btn.id } == true
        }

        val root = viewModel.workspaceState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, root.children.size)
        val updatedBox = root.children[0] as ComposableNode.BoxNode
        assertEquals(1, updatedBox.children.size)
        assertEquals(btn.id, updatedBox.children[0].id)
    }
}
