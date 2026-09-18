package dev.chandradsl.m3c.core.domain.storage

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.M3cProject
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class M3cProjectSerializerTest {

    @Test
    fun testProjectSerializationAndDeserialization() {
        val screen1 = M3cScreen(
            id = "screen_home",
            name = "HomeScreen",
            route = "home",
            rootNode = ComposableNode.ScaffoldNode(
                id = NodeId("scaffold_1"),
                topBar = ComposableNode.TopAppBarNode(
                    id = NodeId("top_bar_1"),
                    title = ComposableNode.TextNode(
                        id = NodeId("txt_title"),
                        text = "Home Screen",
                        typography = TypographyToken.TitleMedium
                    )
                ),
                content = ComposableNode.ColumnNode(
                    id = NodeId("col_content"),
                    children = listOf(
                        ComposableNode.ButtonNode(
                            id = NodeId("btn_navigate"),
                            content = listOf(ComposableNode.TextNode(text = "Go to Profile"))
                        )
                    )
                )
            )
        )

        val project = M3cProject(
            schemaVersion = 1,
            name = "TestShopApp",
            packageName = "com.test.shop",
            screens = listOf(screen1),
            activeScreenId = "screen_home"
        )

        val encoded = M3cProjectSerializer.encode(project)
        assertTrue(encoded.contains("\"name\": \"TestShopApp\""))
        assertTrue(encoded.contains("\"packageName\": \"com.test.shop\""))
        assertTrue(encoded.contains("\"type\": \"scaffold\""))

        val decoded = M3cProjectSerializer.decode(encoded)
        assertEquals(project, decoded)
    }

    @Test
    fun testMultiScreenSerialization() {
        val screen1 = M3cScreen(
            id = "screen_1",
            name = "HomeScreen",
            route = "home",
            rootNode = ComposableNode.ColumnNode(id = NodeId("home_root")),
            isStartDestination = true
        )
        val screen2 = M3cScreen(
            id = "screen_2",
            name = "ProfileScreen",
            route = "profile",
            rootNode = ComposableNode.RowNode(id = NodeId("profile_root")),
            isStartDestination = false
        )
        val project = M3cProject(
            name = "MultiScreenApp",
            packageName = "com.example.multi",
            screens = listOf(screen1, screen2),
            activeScreenId = "screen_2"
        )

        val json = M3cProjectSerializer.encode(project)
        val restored = M3cProjectSerializer.decode(json)

        assertEquals(2, restored.screens.size)
        assertEquals("HomeScreen", restored.screens[0].name)
        assertTrue(restored.screens[0].isStartDestination)
        assertEquals("ProfileScreen", restored.screens[1].name)
        assertFalse(restored.screens[1].isStartDestination)
        assertEquals("screen_2", restored.activeScreenId)
    }

    @Test
    fun testWorkspaceStoreLoadDocumentResetsHistory() = runTest(UnconfinedTestDispatcher()) {
        val root = ComposableNode.ColumnNode(id = NodeId("root_initial"))
        val store = WorkspaceStore(root, UnconfinedTestDispatcher(testScheduler))
        val job = store.start(this)

        // Make an edit so undo is available
        val child = ComposableNode.TextNode(id = NodeId("child_1"), text = "Initial Child")
        store.dispatch(WorkspaceIntent.InsertChild(parentId = root.id, node = child))
        assertTrue(store.state.canUndo)

        // Load new document
        val newRoot = ComposableNode.RowNode(id = NodeId("new_loaded_root"))
        store.dispatch(WorkspaceIntent.LoadDocument(newRoot))

        assertEquals(newRoot, store.state.rootNode)
        assertNull(store.state.selectedNodeId)
        assertFalse(store.state.canUndo, "Undo stack must be empty after loading document")
        assertFalse(store.state.canRedo, "Redo stack must be empty after loading document")

        job.cancel()
    }
}
