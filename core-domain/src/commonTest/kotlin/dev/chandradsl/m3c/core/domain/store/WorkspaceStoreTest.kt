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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkspaceStoreTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun testInsertChildAndUndoRedo() {
        val rootId = NodeId("root_col")
        val txtId = NodeId("txt_1")
        val root = ComposableNode.ColumnNode(id = rootId)
        val store = WorkspaceStore(root)

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
    }

    @Test
    fun testNamedSlotsOnScaffold() {
        val scaffoldId = NodeId("scaffold_root")
        val topBarId = NodeId("top_app_bar")
        val fabId = NodeId("fab_add")

        val scaffold = ComposableNode.ScaffoldNode(id = scaffoldId)
        val store = WorkspaceStore(scaffold)

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
    }

    @Test
    fun testUpdateNodeProperty() {
        val txtId = NodeId("txt_1")
        val originalText = ComposableNode.TextNode(id = txtId, text = "Original Text")
        val root = ComposableNode.BoxNode(id = NodeId("box_root"), children = listOf(originalText))
        val store = WorkspaceStore(root)

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
    fun testWorkspaceStoreStateFlowEmitsUpdates() = kotlinx.coroutines.test.runTest {
        val root = ComposableNode.ColumnNode(id = NodeId("root"))
        val store = WorkspaceStore(root)

        assertEquals(root.id, store.stateFlow.value.rootNode.id)

        val child = ComposableNode.TextNode(text = "Reactive Text")
        store.dispatch(WorkspaceIntent.InsertChild(parentId = root.id, node = child))

        val emittedState = store.stateFlow.value
        val updatedColumn = emittedState.rootNode as ComposableNode.ColumnNode
        assertEquals(1, updatedColumn.children.size)
        assertEquals(child.id, updatedColumn.children.first().id)
        assertTrue(emittedState.canUndo)
    }
}