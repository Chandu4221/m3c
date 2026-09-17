package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.BorderDef
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.schema.childrenWithSlots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceStoreTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun testInsertChildAndUndoRedo() = runTest(UnconfinedTestDispatcher()) {
        val rootId = NodeId("root_col")
        val txtId = NodeId("txt_1")
        val root = ComposableNode.ColumnNode(id = rootId)
        val store = WorkspaceStore(root, UnconfinedTestDispatcher(testScheduler))
        val job = store.start(this)

        val textNode = ComposableNode.TextNode(
            id = txtId,
            text = "Hello M3C",
            typography = TypographyToken.TitleMedium,
            color = ColorSource.Theme(ColorToken.Primary)
        )
        store.dispatch(WorkspaceIntent.InsertChild(parentId = rootId, node = textNode))

        val updatedRoot = store.state.rootNode as ComposableNode.ColumnNode
        assertEquals(1, updatedRoot.children.size)
        assertEquals(txtId, updatedRoot.children.first().id)
        assertTrue(store.state.canUndo)

        // Undo
        store.dispatch(WorkspaceIntent.Undo)
        val revertedRoot = store.state.rootNode as ComposableNode.ColumnNode
        assertEquals(0, revertedRoot.children.size)
        assertTrue(store.state.canRedo)

        // Redo
        store.dispatch(WorkspaceIntent.Redo)
        val redoneRoot = store.state.rootNode as ComposableNode.ColumnNode
        assertEquals(1, redoneRoot.children.size)
        job.cancel()
    }

    @Test
    fun testNamedSlotsOnScaffold() = runTest(UnconfinedTestDispatcher()) {
        val scaffoldId = NodeId("scaffold_root")
        val topBarId = NodeId("top_app_bar")
        val fabId = NodeId("fab_add")

        val scaffold = ComposableNode.ScaffoldNode(id = scaffoldId)
        val store = WorkspaceStore(scaffold, UnconfinedTestDispatcher(testScheduler))
        val job = store.start(this)

        val topBar = ComposableNode.TopAppBarNode(
            id = topBarId,
            title = ComposableNode.TextNode(text = "Dashboard", typography = TypographyToken.TitleLarge)
        )
        val fab = ComposableNode.FloatingActionButtonNode(
            id = fabId,
            shape = ShapeDef.Token(ShapeToken.Large),
            content = listOf(ComposableNode.TextNode(text = "+"))
        )

        // Set TopBar slot
        store.dispatch(WorkspaceIntent.SetSlot(parentId = scaffoldId, slotName = "topBar", node = topBar))
        var currentScaffold = store.state.rootNode as ComposableNode.ScaffoldNode
        val topBarNode = currentScaffold.topBar
        assertNotNull(topBarNode)
        assertEquals(topBarId, topBarNode.id)

        // Set FAB slot
        store.dispatch(WorkspaceIntent.SetSlot(parentId = scaffoldId, slotName = "fab", node = fab))
        currentScaffold = store.state.rootNode as ComposableNode.ScaffoldNode
        val fabNode = currentScaffold.floatingActionButton
        assertNotNull(fabNode)
        assertEquals(fabId, fabNode.id)

        // Remove topBar by target ID
        store.dispatch(WorkspaceIntent.RemoveNode(targetId = topBarId))
        currentScaffold = store.state.rootNode as ComposableNode.ScaffoldNode
        assertNull(currentScaffold.topBar)
        assertNotNull(currentScaffold.floatingActionButton) // FAB remains intact!

        // Undo should bring topBar back
        store.dispatch(WorkspaceIntent.Undo)
        currentScaffold = store.state.rootNode as ComposableNode.ScaffoldNode
        assertNotNull(currentScaffold.topBar)
        job.cancel()
    }

    @Test
    fun testUpdateNodeProperty() = runTest(UnconfinedTestDispatcher()) {
        val txtId = NodeId("txt_1")
        val originalText = ComposableNode.TextNode(id = txtId, text = "Original Text")
        val root = ComposableNode.BoxNode(id = NodeId("box_root"), children = listOf(originalText))
        val store = WorkspaceStore(root, UnconfinedTestDispatcher(testScheduler))
        val job = store.start(this)

        // Inspector edits the text
        val updatedText = originalText.copy(text = "Modified by Inspector")
        store.dispatch(WorkspaceIntent.UpdateNode(updatedText))

        val currentBox = store.state.rootNode as ComposableNode.BoxNode
        val nodeInTree = currentBox.children.first() as ComposableNode.TextNode
        assertEquals("Modified by Inspector", nodeInTree.text)

        // Undo
        store.dispatch(WorkspaceIntent.Undo)
        val revertedBox = store.state.rootNode as ComposableNode.BoxNode
        val revertedNode = revertedBox.children.first() as ComposableNode.TextNode
        assertEquals("Original Text", revertedNode.text)
        job.cancel()
    }

    @Test
    fun testMaterial3CatalogJsonSerializationRoundTrip() {
        val m3Screen: ComposableNode = ComposableNode.ScaffoldNode(
            id = NodeId("screen_scaffold"),
            topBar = ComposableNode.TopAppBarNode(
                id = NodeId("top_bar"),
                title = ComposableNode.TextNode(
                    id = NodeId("top_bar_title"),
                    text = "M3 Component Studio",
                    typography = TypographyToken.TitleMedium
                ),
                containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
            ),
            floatingActionButton = ComposableNode.FloatingActionButtonNode(
                id = NodeId("main_fab"),
                shape = ShapeDef.Token(ShapeToken.Large),
                containerColor = ColorSource.Theme(ColorToken.PrimaryContainer),
                content = listOf(ComposableNode.TextNode(text = "Add"))
            ),
            content = ComposableNode.ColumnNode(
                id = NodeId("main_col"),
                modifiers = listOf(
                    ModifierDef.FillMaxSize(),
                    ModifierDef.Padding.all(DpVal(16f))
                ),
                children = listOf(
                    ComposableNode.OutlinedCardNode(
                        id = NodeId("card_input"),
                        shape = ShapeDef.Token(ShapeToken.Medium),
                        border = BorderDef(width = DpVal(1f), color = ColorSource.Theme(ColorToken.OutlineVariant)),
                        content = listOf(
                            ComposableNode.OutlinedTextFieldNode(
                                id = NodeId("tf_name"),
                                value = "Antigravity",
                                label = "User Name"
                            ),
                            ComposableNode.SwitchNode(
                                id = NodeId("sw_active"),
                                checked = true
                            ),
                            ComposableNode.ButtonNode(
                                id = NodeId("btn_submit"),
                                shape = ShapeDef.Token(ShapeToken.Full),
                                containerColor = ColorSource.Theme(ColorToken.Primary),
                                content = listOf(ComposableNode.TextNode(text = "Save Profile"))
                            )
                        )
                    ),
                    ComposableNode.SpacerNode(
                        id = NodeId("spacer_1"),
                        modifiers = listOf(ModifierDef.Height(DpVal(24f)))
                    ),
                    ComposableNode.LinearProgressIndicatorNode(
                        id = NodeId("prog_1"),
                        progress = 0.75f,
                        color = ColorSource.Theme(ColorToken.Tertiary)
                    )
                )
            )
        )

        // Serialize
        val encodedJson = json.encodeToString(m3Screen)
        println("Material 3 Screen JSON:\n$encodedJson")

        // Verify serial names
        assertTrue(encodedJson.contains("\"type\": \"scaffold\""))
        assertTrue(encodedJson.contains("\"type\": \"top_app_bar\""))
        assertTrue(encodedJson.contains("\"type\": \"floating_action_button\""))
        assertTrue(encodedJson.contains("\"type\": \"outlined_card\""))
        assertTrue(encodedJson.contains("\"type\": \"outlined_text_field\""))
        assertTrue(encodedJson.contains("\"type\": \"switch\""))
        assertTrue(encodedJson.contains("\"type\": \"linear_progress_indicator\""))

        // Deserialize back
        val decodedScreen = json.decodeFromString<ComposableNode>(encodedJson)
        assertEquals(m3Screen, decodedScreen)
    }

    @Test
    fun testWorkspaceStoreStateFlowEmitsUpdates() = runTest(UnconfinedTestDispatcher()) {
        val root = ComposableNode.ColumnNode(id = NodeId("root"))
        val store = WorkspaceStore(root, UnconfinedTestDispatcher(testScheduler))
        val job = store.start(this)

        assertEquals(root.id, store.stateFlow.value.rootNode.id)

        val child = ComposableNode.TextNode(text = "Reactive Text")
        store.dispatch(WorkspaceIntent.InsertChild(parentId = root.id, node = child))

        val emittedState = store.stateFlow.value
        val updatedColumn = emittedState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, updatedColumn.children.size)
        assertEquals(child.id, updatedColumn.children.first().id)
        assertTrue(emittedState.canUndo)
        job.cancel()
    }

    @Test
    fun testRemoveContainerClearsDescendantSelection() = runTest(UnconfinedTestDispatcher()) {
        val textNode = ComposableNode.TextNode(id = NodeId("descendant_text"), text = "Hello")
        val columnNode = ComposableNode.ColumnNode(id = NodeId("inner_col"), children = listOf(textNode))
        val root = ComposableNode.BoxNode(id = NodeId("root_box"), children = listOf(columnNode))

        val store = WorkspaceStore(root, UnconfinedTestDispatcher(testScheduler))
        val job = store.start(this)

        // Select the descendant TextNode
        store.dispatch(WorkspaceIntent.SelectNode(textNode.id))
        assertEquals(textNode.id, store.state.selectedNodeId)

        // Remove the ancestor container (inner_col)
        store.dispatch(WorkspaceIntent.RemoveNode(columnNode.id))

        // Assert selectedNodeId is cleared
        assertNull(store.state.selectedNodeId)
        job.cancel()
    }

    @Test
    fun testInitialRootChildInsertion() = runTest(UnconfinedTestDispatcher()) {
        val initialRoot: ComposableNode = ComposableNode.ScaffoldNode(
            id = NodeId("scaffold_root"),
            topBar = ComposableNode.TopAppBarNode(
                id = NodeId("top_bar"),
                title = ComposableNode.TextNode(
                    id = NodeId("title_txt"),
                    text = "My M3 Screen"
                )
            ),
            floatingActionButton = ComposableNode.FloatingActionButtonNode(
                id = NodeId("main_fab"),
                content = listOf(ComposableNode.TextNode(text = "+"))
            ),
            content = ComposableNode.ColumnNode(
                id = NodeId("main_content_col"),
                children = listOf(
                    ComposableNode.TextNode(
                        id = NodeId("welcome_txt"),
                        text = "Welcome to Material 3 Studio!"
                    )
                )
            )
        )

        val store = WorkspaceStore(initialRoot, UnconfinedTestDispatcher(testScheduler))
        val job = store.start(this)
        val button = ComposableNode.ButtonNode(id = NodeId("btn_new"))

        // Try inserting into scaffold_root
        store.dispatch(WorkspaceIntent.InsertChild(parentId = NodeId("scaffold_root"), node = button))
        val scaffoldAfterInsert = store.state.rootNode as ComposableNode.ScaffoldNode
        val col = scaffoldAfterInsert.content as ComposableNode.ColumnNode
        assertEquals(2, col.children.size)
        assertEquals(button.id, col.children.last().id)

        // Try inserting into main_content_col directly
        val card = ComposableNode.CardNode(id = NodeId("card_new"))
        store.dispatch(WorkspaceIntent.InsertChild(parentId = NodeId("main_content_col"), node = card))
        val scaffoldAfterCard = store.state.rootNode as ComposableNode.ScaffoldNode
        val col2 = scaffoldAfterCard.content as ComposableNode.ColumnNode
        assertEquals(3, col2.children.size)
        assertEquals(card.id, col2.children.last().id)
        job.cancel()
    }

    @Test
    fun testChildrenWithSlotsAndContainers() {
        val initialRoot: ComposableNode = ComposableNode.ScaffoldNode(
            id = NodeId("scaffold_root"),
            topBar = ComposableNode.TopAppBarNode(
                id = NodeId("top_bar"),
                title = ComposableNode.TextNode(
                    id = NodeId("title_txt"),
                    text = "My M3 Screen"
                )
            ),
            floatingActionButton = ComposableNode.FloatingActionButtonNode(
                id = NodeId("main_fab"),
                content = listOf(ComposableNode.TextNode(text = "+"))
            ),
            content = ComposableNode.ColumnNode(
                id = NodeId("main_content_col"),
                children = listOf(
                    ComposableNode.TextNode(
                        id = NodeId("welcome_txt"),
                        text = "Welcome to Material 3 Studio!"
                    )
                )
            )
        )

        fun collectContainers(node: ComposableNode): Set<String> {
            val ids = mutableSetOf<String>()
            fun walk(n: ComposableNode) {
                val children = n.childrenWithSlots()
                if (children.isNotEmpty()) {
                    ids.add(n.id.value)
                    children.forEach { walk(it.first) }
                }
            }
            walk(node)
            return ids
        }

        val containers = collectContainers(initialRoot)
        println("Containers in initialRoot: $containers")
        assertTrue(containers.contains("scaffold_root"))
        assertTrue(containers.contains("top_bar"))
        assertTrue(containers.contains("main_fab"))
        assertTrue(containers.contains("main_content_col"))
    }
}