package dev.chandradsl.m3c.app.desktop.components

import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import org.jetbrains.jewel.foundation.lazy.tree.TreeState
import org.jetbrains.jewel.foundation.lazy.SelectableLazyListState
import androidx.compose.foundation.lazy.LazyListState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HierarchyTreeTest {

    @Test
    fun testTreeStructureAndFlattening() {
        val viewModel = StudioViewModel()
        val root = viewModel.workspaceState.rootNode

        val tree = buildJewelTree(root)
        val containers = collectAllContainerIds(root)
        println("Container IDs: $containers")

        val treeState = TreeState(SelectableLazyListState(LazyListState()))
        println("Initial openNodes: ${treeState.openNodes}")

        // Test before openNodes
        val initialRoots = tree.roots
        assertEquals(1, initialRoots.size)
        println("Root id: ${initialRoots[0].id}")

        // Now open nodes
        treeState.openNodes(containers.toList())
        println("Open nodes after calling openNodes: ${treeState.openNodes}")

        // Walk depth first
        val nodes = tree.walkDepthFirst().toList()
        println("Walked nodes (${nodes.size}):")
        nodes.forEach {
            println(" - [${it.depth}] id=${it.id} data=${it.data.node::class.simpleName} slot=${it.data.slotLabel}")
        }
        assertTrue(nodes.size >= 4, "Expected at least 4 nodes in walk")
    }

    @Test
    fun testInsertComponentFromPalette() {
        val viewModel = StudioViewModel()
        val paletteItems = dev.chandradsl.m3c.core.domain.schema.ComponentRegistry.all
        println("Registered components count: ${paletteItems.size}")

        // Find Button
        val buttonDef = paletteItems.find { it.displayName == "Button" }!!
        val newButton = buttonDef.createDefault()

        println("Initial root children count: ${(viewModel.workspaceState.rootNode as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode).content?.let { (it as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode).children.size }}")

        viewModel.insertComponent(newButton)

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeout(3000) {
                while (((viewModel.workspaceState.rootNode as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode).content as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode).children.size < 2) {
                    kotlinx.coroutines.delay(20)
                }
            }
        }

        val updatedContent = (viewModel.workspaceState.rootNode as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode).content as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode
        println("Updated content column children count: ${updatedContent.children.size}")
        println("Children: ${updatedContent.children.map { it.id.value to it::class.simpleName }}")
        assertEquals(2, updatedContent.children.size)
        assertEquals(newButton.id, viewModel.workspaceState.selectedNodeId)

        val tree = buildJewelTree(viewModel.workspaceState.rootNode)
        val walked = tree.walkDepthFirst().toList()
        println("Walked after insert (${walked.size}):")
        walked.forEach {
            println(" - [${it.depth}] id=${it.id} data=${it.data.node::class.simpleName}")
        }
        assertTrue(walked.any { it.id == newButton.id.value })
    }

    @Test
    fun testJewelFlattenTreeLogic() {
        val viewModel = StudioViewModel()
        val root = viewModel.workspaceState.rootNode
        val tree = buildJewelTree(root)

        val treeState = TreeState(SelectableLazyListState(LazyListState()))

        // Exactly as in BasicLazyTree.kt:
        fun org.jetbrains.jewel.foundation.lazy.tree.Tree.Element<*>.flatten(state: TreeState): MutableList<org.jetbrains.jewel.foundation.lazy.tree.Tree.Element<*>> {
            val orderedChildren = mutableListOf<org.jetbrains.jewel.foundation.lazy.tree.Tree.Element<*>>()
            when (this) {
                is org.jetbrains.jewel.foundation.lazy.tree.Tree.Element.Node<*> -> {
                    orderedChildren.add(this)
                    if (id !in state.openNodes) {
                        return orderedChildren
                    }
                    open(true)
                    children?.forEach { child -> orderedChildren.addAll(child.flatten(state)) }
                }
                is org.jetbrains.jewel.foundation.lazy.tree.Tree.Element.Leaf<*> -> {
                    orderedChildren.add(this)
                }
            }
            return orderedChildren
        }

        // Pass 1: openNodes is empty
        val pass1 = tree.roots.flatMap { it.flatten(treeState) }
        println("Pass 1 flattened (${pass1.size}): ${pass1.map { it.id }}")

        // Pass 2: after openNodes is called
        val containers = collectAllContainerIds(root)
        treeState.openNodes(containers.toList())
        println("treeState.openNodes: ${treeState.openNodes}")
        val pass2 = tree.roots.flatMap { it.flatten(treeState) }
        println("Pass 2 flattened (${pass2.size}): ${pass2.map { it.id }}")
    }

    @Test
    fun testInsertAndCloneIconComponent() {
        val viewModel = StudioViewModel()
        val iconDef = dev.chandradsl.m3c.core.domain.schema.ComponentRegistry.findByType(
            dev.chandradsl.m3c.core.domain.schema.ComponentType.Icon
        )
        kotlin.test.assertNotNull(iconDef)
        val iconNode = iconDef.createDefault() as dev.chandradsl.m3c.core.domain.model.ComposableNode.IconNode
        assertEquals("Favorite", iconNode.iconName)

        viewModel.insertComponent(iconNode)

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeout(3000) {
                while (true) {
                    val root = viewModel.workspaceState.rootNode as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode
                    val col = root.content as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode
                    if (col.children.any { it is dev.chandradsl.m3c.core.domain.model.ComposableNode.IconNode }) {
                        break
                    }
                    kotlinx.coroutines.delay(20)
                }
            }
        }

        // Test duplicate via documentController
        val root = viewModel.workspaceState.rootNode as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode
        val col = root.content as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode
        val insertedIcon = col.children.filterIsInstance<dev.chandradsl.m3c.core.domain.model.ComposableNode.IconNode>().first()
        viewModel.dispatch(dev.chandradsl.m3c.core.domain.store.WorkspaceIntent.SelectNode(insertedIcon.id))
        viewModel.documentController.duplicateSelectedNode()

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeout(3000) {
                while (true) {
                    val currentRoot = viewModel.workspaceState.rootNode as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode
                    val currentCol = currentRoot.content as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode
                    val iconCount = currentCol.children.count { it is dev.chandradsl.m3c.core.domain.model.ComposableNode.IconNode }
                    if (iconCount >= 2) break
                    kotlinx.coroutines.delay(20)
                }
            }
        }
    }

    @Test
    fun testBuildContextMenuItemsForLeafAndContainerNodes() {
        val viewModel = StudioViewModel()
        val root = viewModel.workspaceState.rootNode
        val rootItems = buildTreeContextMenuItems(root, viewModel)

        // Root cannot be duplicated or deleted
        assertTrue(rootItems.none { it.label == "Duplicate" })
        assertTrue(rootItems.none { it.label == "Delete" })
        assertTrue(rootItems.any { it.label == "Select" })

        // Find child node inside content
        val scaffold = root as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode
        val column = scaffold.content as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode
        val child = column.children.first()

        val childItems = buildTreeContextMenuItems(child, viewModel)
        assertTrue(childItems.any { it.label == "Select" })
        assertTrue(childItems.any { it.label == "Duplicate" })
        assertTrue(childItems.any { it.label == "Wrap in Column" })
        assertTrue(childItems.any { it.label == "Wrap in Row" })
        assertTrue(childItems.any { it.label == "Delete" })

        // Container (Column) has Add items
        val columnItems = buildTreeContextMenuItems(column, viewModel)
        assertTrue(columnItems.any { it.label == "Add Text" })
        assertTrue(columnItems.any { it.label == "Add Button" })
        assertTrue(columnItems.any { it.label == "Add Icon" })

        // Test executing an action from the context menu items
        val duplicateItem = childItems.first { it.label == "Duplicate" }
        duplicateItem.onClick()

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeout(3000) {
                while (true) {
                    val currentRoot = viewModel.workspaceState.rootNode as dev.chandradsl.m3c.core.domain.model.ComposableNode.ScaffoldNode
                    val currentCol = currentRoot.content as dev.chandradsl.m3c.core.domain.model.ComposableNode.ColumnNode
                    if (currentCol.children.size >= 2) break
                    kotlinx.coroutines.delay(20)
                }
            }
        }
    }
}
