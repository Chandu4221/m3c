package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DragControllerTest {

    private val dummyIcon = ImageVector.Builder(
        name = "dummy",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).build()

    private val dummyItem = DraggedPaletteItem(
        name = "Button",
        description = "A standard button",
        icon = dummyIcon,
        factory = { ComposableNode.ButtonNode() }
    )

    private val rootId = NodeId.generate("root")

    @Test
    fun testCanvasBoundsAndHoveredCheck() {
        val controller = DragController()
        controller.updateCanvasBounds(Rect(100f, 100f, 800f, 600f))

        // Start drag outside canvas bounds
        controller.startPaletteDrag(dummyItem, Offset(50f, 50f), rootId)
        assertFalse(controller.isCanvasDropHovered)
        assertNull(controller.hoveredCanvasParentId)

        // Update drag to inside canvas bounds
        controller.updatePaletteDrag(Offset(100f, 100f), rootId) // offset now (150, 150)
        assertTrue(controller.isCanvasDropHovered)
        assertEquals(rootId, controller.hoveredCanvasParentId)
    }

    @Test
    fun testContainingContainersPicksSmallestArea() {
        val controller = DragController()
        controller.updateCanvasBounds(Rect(0f, 0f, 1000f, 1000f))

        val outerId = NodeId.generate("col")
        val innerId = NodeId.generate("card")

        // Outer: 0,0 to 500,500 (area = 250,000)
        controller.registerCanvasContainerBounds(outerId, "Column", Rect(0f, 0f, 500f, 500f), rootId)
        // Inner: 50,50 to 200,200 (area = 22,500)
        controller.registerCanvasContainerBounds(innerId, "Card", Rect(50f, 50f, 200f, 200f), rootId)

        controller.startPaletteDrag(dummyItem, Offset(100f, 100f), rootId)
        assertTrue(controller.isCanvasDropHovered)
        assertEquals(innerId, controller.hoveredCanvasParentId)
        assertEquals("Card", controller.hoveredCanvasParentName)

        // Move to (300, 300) - inside outer only
        controller.updatePaletteDrag(Offset(200f, 200f), rootId)
        assertEquals(outerId, controller.hoveredCanvasParentId)
        assertEquals("Column", controller.hoveredCanvasParentName)
    }

    @Test
    fun testOutsideAllContainersPicksNearestCenter() {
        val controller = DragController()
        controller.updateCanvasBounds(Rect(0f, 0f, 1000f, 1000f))

        val containerAId = NodeId.generate("a")
        val containerBId = NodeId.generate("b")

        // A center at (50, 50)
        controller.registerCanvasContainerBounds(containerAId, "ContainerA", Rect(0f, 0f, 100f, 100f), rootId)
        // B center at (350, 50)
        controller.registerCanvasContainerBounds(containerBId, "ContainerB", Rect(300f, 0f, 400f, 100f), rootId)

        // Drag at (280, 50) - outside both, closer to B (distance 70) than A (distance 230)
        controller.startPaletteDrag(dummyItem, Offset(280f, 50f), rootId)
        assertTrue(controller.isCanvasDropHovered)
        assertEquals(containerBId, controller.hoveredCanvasParentId)
        assertEquals("ContainerB", controller.hoveredCanvasParentName)
    }

    @Test
    fun testEmptyContainersFallbackToRoot() {
        val controller = DragController()
        controller.updateCanvasBounds(Rect(0f, 0f, 1000f, 1000f))

        controller.startPaletteDrag(dummyItem, Offset(100f, 100f), rootId)
        assertTrue(controller.isCanvasDropHovered)
        assertEquals(rootId, controller.hoveredCanvasParentId)
        assertEquals("Screen", controller.hoveredCanvasParentName)
    }

    @Test
    fun testUnregisterBoundsRecalculatesAndRemovesGhostTarget() {
        val controller = DragController()
        controller.updateCanvasBounds(Rect(0f, 0f, 1000f, 1000f))

        val containerAId = NodeId.generate("a")
        val containerBId = NodeId.generate("b")

        controller.registerCanvasContainerBounds(containerAId, "ContainerA", Rect(0f, 0f, 200f, 200f), rootId)
        controller.registerCanvasContainerBounds(containerBId, "ContainerB", Rect(250f, 0f, 450f, 200f), rootId)

        controller.startPaletteDrag(dummyItem, Offset(100f, 100f), rootId)
        assertEquals(containerAId, controller.hoveredCanvasParentId)

        // Delete container A (unregister it)
        controller.unregisterCanvasContainerBounds(containerAId, rootId)

        // Should no longer point to deleted container A
        assertNotEquals(containerAId, controller.hoveredCanvasParentId)
        // Recalculates to container B (nearest fallback)
        assertEquals(containerBId, controller.hoveredCanvasParentId)

        // Delete container B as well
        controller.unregisterCanvasContainerBounds(containerBId, rootId)
        // Recalculates to root fallback
        assertEquals(rootId, controller.hoveredCanvasParentId)
    }

    @Test
    fun testEndAndCancelPaletteDrag() {
        val controller = DragController()
        controller.updateCanvasBounds(Rect(0f, 0f, 1000f, 1000f))

        controller.startPaletteDrag(dummyItem, Offset(50f, 50f), rootId)
        val endedItem = controller.endPaletteDrag()
        assertEquals(dummyItem, endedItem)
        assertNull(controller.activeDragItem)
        assertFalse(controller.isCanvasDropHovered)
        assertNull(controller.hoveredCanvasParentId)

        controller.startPaletteDrag(dummyItem, Offset(50f, 50f), rootId)
        controller.cancelPaletteDrag()
        assertNull(controller.activeDragItem)
        assertFalse(controller.isCanvasDropHovered)
        assertNull(controller.hoveredCanvasParentId)
    }

    @Test
    fun testTreeDragAndDrop() {
        val controller = DragController()
        val draggedId = NodeId.generate("node")
        val targetId = NodeId.generate("target")

        controller.startTreeDrag(draggedId, "MyNode", Offset(10f, 10f))
        assertEquals(draggedId, controller.activeTreeDragNode?.nodeId)
        assertEquals("MyNode", controller.activeTreeDragNode?.label)
        assertEquals(Offset(10f, 10f), controller.dragPointerOffset)

        controller.updateTreeDrag(Offset(5f, 15f))
        assertEquals(Offset(15f, 25f), controller.dragPointerOffset)

        controller.updateTreeDropTarget(targetId, TreeDropPosition.INSIDE, isValid = true)
        assertEquals(targetId, controller.treeDropTargetId)
        assertEquals(TreeDropPosition.INSIDE, controller.treeDropPosition)

        // Invalid target clears drop target
        controller.updateTreeDropTarget(targetId, TreeDropPosition.INSIDE, isValid = false)
        assertNull(controller.treeDropTargetId)
        assertNull(controller.treeDropPosition)

        controller.cancelTreeDrag()
        assertNull(controller.activeTreeDragNode)
    }
}
