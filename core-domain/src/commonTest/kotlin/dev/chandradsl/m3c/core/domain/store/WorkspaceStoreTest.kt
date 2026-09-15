package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.AlignmentDef
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
import kotlin.test.assertTrue

class WorkspaceStoreTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun testInsertChildAndUndoRedo() {
        val rootId = NodeId("root_1")
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
        val txtId = NodeId("txt_1")
        val textNode = ComposableNode.TextNode(id = txtId, text = "Hello")
        val root = ComposableNode.BoxNode(
            id = NodeId("box_1"),
            contentAlignment = AlignmentDef.Center,
            children = listOf(textNode)
        )
        val store = WorkspaceStore(root)

        val newModifiers = listOf(
            ModifierDef.Padding(start = DpVal(16f), top = DpVal(16f)),
            ModifierDef.Background(
                color = ColorSource.Theme(ColorToken.Surface),
                shape = ShapeDef.Token(ShapeToken.Medium)
            )
        )
        store.dispatch(WorkspaceIntent.UpdateModifiers(targetId = txtId, modifiers = newModifiers))

        val updatedBox = store.state.rootNode as ComposableNode.BoxNode
        val updatedText = updatedBox.children.first() as ComposableNode.TextNode
        assertEquals(2, updatedText.modifiers.size)
    }

    @Test
    fun testJsonSerializationRoundTrip() {
        val originalTree: ComposableNode = ComposableNode.ColumnNode(
            id = NodeId("root_col"),
            modifiers = listOf(ModifierDef.FillMaxSize()),
            children = listOf(
                ComposableNode.BoxNode(
                    id = NodeId("box_1"),
                    contentAlignment = AlignmentDef.Center,
                    modifiers = listOf(
                        ModifierDef.Padding.all(DpVal(16f)),
                        ModifierDef.Background(
                            color = ColorSource.Theme(ColorToken.PrimaryContainer),
                            shape = ShapeDef.Token(ShapeToken.Large)
                        )
                    ),
                    children = listOf(
                        ComposableNode.TextNode(
                            id = NodeId("txt_title"),
                            text = "Material 3 Studio",
                            typography = TypographyToken.HeadlineMedium,
                            color = ColorSource.Theme(ColorToken.OnPrimaryContainer)
                        )
                    )
                )
            )
        )

        // Serialize to JSON
        val encodedJson = json.encodeToString(originalTree)
        println("Generated JSON:\n$encodedJson")

        // Verify concise discriminator tags are present
        assertTrue(encodedJson.contains("\"type\": \"column\""))
        assertTrue(encodedJson.contains("\"type\": \"box\""))
        assertTrue(encodedJson.contains("\"type\": \"text\""))
        assertTrue(encodedJson.contains("\"type\": \"fill_max_size\""))
        assertTrue(encodedJson.contains("\"type\": \"background\""))

        // Deserialize back
        val decodedTree = json.decodeFromString<ComposableNode>(encodedJson)
        assertEquals(originalTree, decodedTree)
    }
}