package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chandradsl.m3c.core.codegen.ComposeCodeGenerator
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.core.domain.store.WorkspaceStore

enum class LeftDrawerTab {
    Palette,
    Hierarchy
}

class StudioViewModel {

    // 1. Initial starter screen (A clean Material 3 Scaffold with TopAppBar, FAB & Column)
    private val initialRoot: ComposableNode = ComposableNode.ScaffoldNode(
        id = NodeId("scaffold_root"),
        topBar = ComposableNode.TopAppBarNode(
            id = NodeId("top_bar"),
            title = ComposableNode.TextNode(
                id = NodeId("title_txt"),
                text = "My M3 Screen",
                typography = TypographyToken.TitleLarge
            ),
            containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
        ),
        floatingActionButton = ComposableNode.FloatingActionButtonNode(
            id = NodeId("main_fab"),
            shape = ShapeDef.Token(ShapeToken.Large),
            containerColor = ColorSource.Theme(ColorToken.PrimaryContainer),
            content = listOf(ComposableNode.TextNode(text = "+"))
        ),
        content = ComposableNode.ColumnNode(
            id = NodeId("main_content_col"),
            modifiers = listOf(
                ModifierDef.FillMaxSize(),
                ModifierDef.Padding.all(DpVal(16f))
            ),
            children = listOf(
                ComposableNode.TextNode(
                    id = NodeId("welcome_txt"),
                    text = "Welcome to Material 3 Studio!",
                    typography = TypographyToken.HeadlineSmall
                )
            )
        )
    )

    private val store = WorkspaceStore(initialRoot)

    // 2. Reactive Workspace State
    var workspaceState: WorkspaceState by mutableStateOf(store.state)
        private set

    // 3. Studio UI Settings
    var isDarkMode: Boolean by mutableStateOf(false)
    var isInteractiveMode: Boolean by mutableStateOf(false)
    var isCodeDrawerOpen: Boolean by mutableStateOf(false)
    var leftDrawerTab: LeftDrawerTab by mutableStateOf(LeftDrawerTab.Palette)

    // 4. Live Generated Code
    var generatedCode: String by mutableStateOf(generateCode())
        private set

    // 5. Selected Node in the AST
    val selectedNode: ComposableNode?
        get() {
            val id = workspaceState.selectedNodeId ?: return null
            return findNodeRecursive(workspaceState.rootNode, id)
        }

    fun dispatch(intent: WorkspaceIntent) {
        store.dispatch(intent)
        workspaceState = store.state
        generatedCode = generateCode()
    }

    fun insertComponent(newNode: ComposableNode) {
        val targetParentId = workspaceState.selectedNodeId ?: workspaceState.rootNode.id
        dispatch(WorkspaceIntent.InsertChild(parentId = targetParentId, node = newNode))
        dispatch(WorkspaceIntent.SelectNode(newNode.id))
    }

    fun deleteSelectedNode() {
        val id = workspaceState.selectedNodeId ?: return
        if (id == workspaceState.rootNode.id) return // Don't delete root
        dispatch(WorkspaceIntent.RemoveNode(id))
    }

    fun undo() = dispatch(WorkspaceIntent.Undo)
    fun redo() = dispatch(WorkspaceIntent.Redo)

    private fun generateCode(): String {
        return ComposeCodeGenerator.generateCodeString(
            packageName = "dev.chandradsl.m3c.preview",
            componentName = "MyScreen",
            rootNode = store.state.rootNode
        )
    }

    private fun findNodeRecursive(current: ComposableNode, targetId: NodeId): ComposableNode? {
        if (current.id == targetId) return current

        when (current) {
            is ComposableNode.ColumnNode -> {
                current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.RowNode -> {
                current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.BoxNode -> {
                current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.SurfaceNode -> {
                current.children.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.NavigationBarNode -> {
                current.items.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.CardNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.ElevatedCardNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.OutlinedCardNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.ButtonNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.ElevatedButtonNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.FilledTonalButtonNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.OutlinedButtonNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.TextButtonNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.IconButtonNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.FloatingActionButtonNode -> {
                current.content.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.ScaffoldNode -> {
                current.topBar?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.bottomBar?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.floatingActionButton?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.content?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.TopAppBarNode -> {
                findNodeRecursive(current.title, targetId)?.let { return it }
                current.navigationIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.actions.forEach { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.TextFieldNode -> {
                current.leadingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.trailingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.OutlinedTextFieldNode -> {
                current.leadingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
                current.trailingIcon?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            is ComposableNode.NavigationBarItemNode -> {
                findNodeRecursive(current.icon, targetId)?.let { return it }
                current.label?.let { findNodeRecursive(it, targetId)?.let { return it } }
            }
            else -> Unit
        }
        return null
    }
}