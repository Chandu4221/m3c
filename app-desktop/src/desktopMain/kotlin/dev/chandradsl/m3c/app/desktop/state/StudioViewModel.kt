package dev.chandradsl.m3c.app.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chandradsl.m3c.app.desktop.io.DesktopFilePicker
import dev.chandradsl.m3c.core.codegen.ComposeCodeGenerator
import dev.chandradsl.m3c.core.domain.model.M3cProject
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import dev.chandradsl.m3c.core.domain.storage.M3cProjectSerializer
import java.awt.Frame
import java.io.File
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.model.hasDescendant
import dev.chandradsl.m3c.core.domain.schema.ComponentRegistry
import dev.chandradsl.m3c.core.domain.schema.ComponentType
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import dev.chandradsl.m3c.core.domain.store.TreeMutator
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

sealed interface StudioNotification {
    val message: String

    data class Info(override val message: String) : StudioNotification
    data class Success(override val message: String) : StudioNotification
    data class Warning(override val message: String) : StudioNotification
    data class Error(override val message: String) : StudioNotification
}

data class DraggedPaletteItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val factory: () -> ComposableNode
)

data class DraggedTreeNode(
    val nodeId: NodeId,
    val label: String
)

data class DraggedModifier(
    val nodeId: NodeId,
    val fromIndex: Int,
    val name: String,
    val summary: String
)

enum class TreeDropPosition {
    INSIDE, ABOVE, BELOW
}

enum class LeftDrawerTab {
    Palette,
    Hierarchy
}

enum class DevicePreset(val label: String, val width: Dp, val height: Dp) {
    PhonePortrait("Phone Portrait", 390.dp, 844.dp),
    PhoneLandscape("Phone Landscape", 844.dp, 390.dp),
    Tablet("Tablet", 800.dp, 1280.dp),
    Desktop("Desktop", 1024.dp, 768.dp)
}

/**
 * Main Studio ViewModel composing DocumentController, CanvasController, and DragController.
 * Provides derived, lazily-memoized Kotlin code generation.
 */
class StudioViewModel {

    // 0. Project File & Dirty State Management
    var projectName: String by mutableStateOf("Untitled")
    var packageName: String by mutableStateOf("com.example.app")
    var currentProjectFile: File? by mutableStateOf(null)
    var isDirty: Boolean by mutableStateOf(false)

    private fun createDefaultScaffold(): ComposableNode = ComposableNode.ScaffoldNode(
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

    // 1. Initial starter screen
    private val initialRoot: ComposableNode = createDefaultScaffold()

    // 2. Coroutine Scope & Notifications
    val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _notificationChannel = Channel<StudioNotification>(Channel.BUFFERED)
    val notificationFlow: Flow<StudioNotification> = _notificationChannel.receiveAsFlow()

    fun notify(message: String) {
        viewModelScope.launch {
            _notificationChannel.send(StudioNotification.Info(message))
        }
    }

    fun notifySuccess(message: String) {
        viewModelScope.launch {
            _notificationChannel.send(StudioNotification.Success(message))
        }
    }

    fun notifyWarning(message: String) {
        viewModelScope.launch {
            _notificationChannel.send(StudioNotification.Warning(message))
        }
    }

    fun notifyError(message: String) {
        viewModelScope.launch {
            _notificationChannel.send(StudioNotification.Error(message))
        }
    }

    // 3. Project File Operations
    fun newProject(parentFrame: Frame? = null) {
        val freshRoot = createDefaultScaffold()
        dispatch(WorkspaceIntent.LoadDocument(freshRoot))
        currentProjectFile = null
        projectName = "Untitled"
        isDirty = false
        notifySuccess("Created new project")
    }

    fun openProject(parentFrame: Frame? = null) {
        val file = DesktopFilePicker.chooseOpenProjectFile(parentFrame) ?: return
        openProjectFile(file)
    }

    fun openProjectFile(file: File) {
        try {
            val content = file.readText()
            val project = M3cProjectSerializer.decode(content)
            val screen = project.screens.firstOrNull { it.id == project.activeScreenId }
                ?: project.screens.firstOrNull()
                ?: error("Project contains no screens")

            dispatch(WorkspaceIntent.LoadDocument(screen.rootNode))
            currentProjectFile = file
            projectName = project.name.ifBlank { file.nameWithoutExtension }
            packageName = project.packageName
            isDirty = false
            notifySuccess("Opened ${file.name}")
        } catch (e: Exception) {
            notifyError("Failed to open project: ${e.message}")
        }
    }

    fun saveProject(parentFrame: Frame? = null) {
        val target = currentProjectFile
        if (target != null) {
            saveToProjectFile(target)
        } else {
            saveProjectAs(parentFrame)
        }
    }

    fun saveProjectAs(parentFrame: Frame? = null) {
        val file = DesktopFilePicker.chooseSaveProjectFile(defaultName = projectName, parentFrame = parentFrame) ?: return
        saveToProjectFile(file)
    }

    private fun saveToProjectFile(file: File) {
        try {
            val screen = M3cScreen(
                id = "main_screen",
                name = "MainScreen",
                route = "main",
                rootNode = workspaceState.rootNode
            )
            val project = M3cProject(
                schemaVersion = 1,
                name = projectName.ifBlank { file.nameWithoutExtension },
                packageName = packageName,
                screens = listOf(screen),
                activeScreenId = screen.id
            )
            val jsonText = M3cProjectSerializer.encode(project)
            file.writeText(jsonText)
            currentProjectFile = file
            projectName = file.nameWithoutExtension
            isDirty = false
            notifySuccess("Saved to ${file.name}")
        } catch (e: Exception) {
            notifyError("Failed to save project: ${e.message}")
        }
    }

    // 4. Focused Sub-Controllers
    val documentController = DocumentController(initialRoot, viewModelScope)
    val canvasController = CanvasController()
    val dragController = DragController()

    // 5. Document State & Intent Delegation
    val workspaceState: WorkspaceState get() = documentController.workspaceState
    val selectedNode: ComposableNode? get() = documentController.selectedNode
    val targetedSlot: Pair<NodeId, String>? get() = documentController.targetedSlot

    fun dispatch(intent: WorkspaceIntent) {
        documentController.dispatch(intent)
        if (intent is WorkspaceIntent.LoadDocument) {
            isDirty = false
        } else if (intent !is WorkspaceIntent.SelectNode) {
            isDirty = true
        }
    }

    fun setTargetSlot(parentId: NodeId, slotName: String) =
        documentController.setTargetSlot(parentId, slotName)

    fun clearTargetSlot() = documentController.clearTargetSlot()

    fun insertComponent(newNode: ComposableNode) {
        if (isInteractiveMode) return
        documentController.insertComponent(newNode)
    }

    fun deleteSelectedNode() = documentController.deleteSelectedNode()
    fun duplicateSelectedNode() = documentController.duplicateSelectedNode()
    fun duplicateNode(nodeId: NodeId) = documentController.duplicateNode(nodeId)
    fun wrapInColumn(nodeId: NodeId) = documentController.wrapInColumn(nodeId)
    fun wrapInRow(nodeId: NodeId) = documentController.wrapInRow(nodeId)
    fun wrapInContainer(nodeId: NodeId, containerType: String) = documentController.wrapInContainer(nodeId, containerType)
    fun isContainerNode(node: ComposableNode): Boolean = documentController.isContainerNode(node)

    fun moveNodeUp(nodeId: NodeId) = documentController.moveNodeUp(nodeId)
    fun moveNodeDown(nodeId: NodeId) = documentController.moveNodeDown(nodeId)
    fun moveNodeOut(nodeId: NodeId) = documentController.moveNodeOut(nodeId)
    fun moveNodeIn(nodeId: NodeId) = documentController.moveNodeIn(nodeId)

    fun canMoveUp(nodeId: NodeId): Boolean = documentController.canMoveUp(nodeId)
    fun canMoveDown(nodeId: NodeId): Boolean = documentController.canMoveDown(nodeId)
    fun canMoveOut(nodeId: NodeId): Boolean = documentController.canMoveOut(nodeId)
    fun canMoveIn(nodeId: NodeId): Boolean = documentController.canMoveIn(nodeId)

    fun moveNodeInto(sourceId: NodeId, targetContainerId: NodeId) =
        documentController.moveNodeInto(sourceId, targetContainerId)

    fun moveNodeRelative(sourceId: NodeId, targetNodeId: NodeId, placeAfter: Boolean) =
        documentController.moveNodeRelative(sourceId, targetNodeId, placeAfter)

    fun reorderModifier(targetId: NodeId, fromIndex: Int, toIndex: Int) =
        documentController.reorderModifier(targetId, fromIndex, toIndex)

    fun removeModifier(targetId: NodeId, index: Int) =
        documentController.removeModifier(targetId, index)

    fun addModifier(targetId: NodeId, modifierDef: ModifierDef) =
        documentController.addModifier(targetId, modifierDef)

    fun getParentScope(nodeId: NodeId): ContainerScope =
        documentController.getParentScope(nodeId)

    fun undo() {
        documentController.undo()
        isDirty = true
    }

    fun redo() {
        documentController.redo()
        isDirty = true
    }

    // 4. Canvas & Panel Delegation
    var isDarkMode: Boolean
        get() = canvasController.isDarkMode
        set(value) { canvasController.isDarkMode = value }

    var isInteractiveMode: Boolean
        get() = canvasController.isInteractiveMode
        private set(value) { canvasController.isInteractiveMode = value }

    fun updateInteractiveMode(enabled: Boolean) {
        canvasController.isInteractiveMode = enabled
        if (enabled) {
            dispatch(WorkspaceIntent.SelectNode(null))
        }
    }

    var isCodeDrawerOpen: Boolean
        get() = canvasController.isCodeDrawerOpen
        set(value) { canvasController.isCodeDrawerOpen = value }

    var leftDrawerTab: LeftDrawerTab
        get() = canvasController.leftDrawerTab
        set(value) { canvasController.leftDrawerTab = value }

    var leftPanelWidth: Dp
        get() = canvasController.leftPanelWidth
        set(value) { canvasController.leftPanelWidth = value }

    var rightPanelWidth: Dp
        get() = canvasController.rightPanelWidth
        set(value) { canvasController.rightPanelWidth = value }

    var codeDrawerHeight: Dp
        get() = canvasController.codeDrawerHeight
        set(value) { canvasController.codeDrawerHeight = value }

    fun resizeLeftPanel(deltaDp: Float) = canvasController.resizeLeftPanel(deltaDp)
    fun resizeRightPanel(deltaDp: Float) = canvasController.resizeRightPanel(deltaDp)
    fun resizeCodeDrawer(deltaDp: Float) = canvasController.resizeCodeDrawer(deltaDp)

    fun resetLeftPanelWidth() = canvasController.resetLeftPanelWidth()
    fun resetRightPanelWidth() = canvasController.resetRightPanelWidth()
    fun resetCodeDrawerHeight() = canvasController.resetCodeDrawerHeight()

    var currentDevicePreset: DevicePreset
        get() = canvasController.currentDevicePreset
        set(value) { canvasController.currentDevicePreset = value }

    var canvasZoom: Float
        get() = canvasController.canvasZoom
        set(value) { canvasController.canvasZoom = value }

    fun zoomIn() = canvasController.zoomIn()
    fun zoomOut() = canvasController.zoomOut()
    fun resetZoom() = canvasController.resetZoom()
    fun fitToViewport() = canvasController.fitToViewport()
    fun setDevicePreset(preset: DevicePreset) = canvasController.setDevicePreset(preset)

    // 5. Drag & Drop Delegation (Palette -> Canvas)
    val activeDragItem: DraggedPaletteItem? get() = dragController.activeDragItem
    val dragPointerOffset: Offset get() = dragController.dragPointerOffset
    val isCanvasDropHovered: Boolean get() = dragController.isCanvasDropHovered
    val canvasBoundsInWindow: Rect get() = dragController.canvasBoundsInWindow
    val hoveredCanvasParentId: NodeId? get() = dragController.hoveredCanvasParentId
    val hoveredCanvasParentName: String? get() = dragController.hoveredCanvasParentName

    fun registerCanvasContainerBounds(nodeId: NodeId, name: String, bounds: Rect) =
        dragController.registerCanvasContainerBounds(nodeId, name, bounds, workspaceState.rootNode.id)

    fun unregisterCanvasContainerBounds(nodeId: NodeId) =
        dragController.unregisterCanvasContainerBounds(nodeId, workspaceState.rootNode.id)

    fun isContainerTag(tag: String): Boolean {
        val normalized = tag.lowercase()
        if (normalized == "fab" || normalized == "floatingactionbutton") {
            return ComponentRegistry.findByType(ComponentType.FloatingActionButton)?.acceptsChildren ?: true
        }
        val def = ComponentRegistry.all.firstOrNull {
            it.type.name.equals(tag, ignoreCase = true) ||
            it.displayName.replace(" ", "").equals(tag, ignoreCase = true)
        }
        return def?.acceptsChildren ?: false
    }

    fun startPaletteDrag(item: DraggedPaletteItem, initialOffset: Offset) {
        if (isInteractiveMode) return
        dragController.startPaletteDrag(item, initialOffset, workspaceState.rootNode.id)
    }

    fun updatePaletteDrag(delta: Offset) =
        dragController.updatePaletteDrag(delta, workspaceState.rootNode.id)

    fun updateCanvasBounds(bounds: Rect) =
        dragController.updateCanvasBounds(bounds)

    fun endPaletteDrag() {
        val wasHovered = isCanvasDropHovered
        val targetParentId = hoveredCanvasParentId ?: run {
            val selected = selectedNode
            if (selected != null && documentController.isContainerNode(selected)) {
                selected.id
            } else if (selected != null) {
                TreeMutator.findParent(workspaceState.rootNode, selected.id)?.id ?: workspaceState.rootNode.id
            } else {
                workspaceState.rootNode.id
            }
        }
        val item = dragController.endPaletteDrag()
        if (item != null && wasHovered) {
            val newNode = item.factory()
            dispatch(WorkspaceIntent.InsertChild(parentId = targetParentId, node = newNode))
            dispatch(WorkspaceIntent.SelectNode(newNode.id))
        }
    }

    fun cancelPaletteDrag() = dragController.cancelPaletteDrag()

    // 6. Tree Hierarchy Drag & Drop
    val activeTreeDragNode: DraggedTreeNode? get() = dragController.activeTreeDragNode
    val treeDropTargetId: NodeId? get() = dragController.treeDropTargetId
    val treeDropPosition: TreeDropPosition? get() = dragController.treeDropPosition

    fun startTreeDrag(node: ComposableNode, initialOffset: Offset, label: String) {
        if (isInteractiveMode) return
        if (node.id == workspaceState.rootNode.id) return
        dragController.startTreeDrag(node.id, label, initialOffset)
    }

    fun updateTreeDrag(delta: Offset) = dragController.updateTreeDrag(delta)

    fun updateTreeDropTarget(targetId: NodeId?, position: TreeDropPosition?) {
        val dragged = activeTreeDragNode ?: return
        val valid = targetId != null && position != null && canDropTreeNode(dragged.nodeId, targetId, position)
        dragController.updateTreeDropTarget(targetId, position, valid)
    }

    fun endTreeDrag() {
        val dragged = activeTreeDragNode
        val targetId = treeDropTargetId
        val position = treeDropPosition
        if (dragged != null && targetId != null && position != null) {
            when (position) {
                TreeDropPosition.INSIDE -> moveNodeInto(dragged.nodeId, targetId)
                TreeDropPosition.ABOVE -> moveNodeRelative(dragged.nodeId, targetId, placeAfter = false)
                TreeDropPosition.BELOW -> moveNodeRelative(dragged.nodeId, targetId, placeAfter = true)
            }
        }
        dragController.cancelTreeDrag()
    }

    fun cancelTreeDrag() = dragController.cancelTreeDrag()

    fun canDropTreeNode(sourceId: NodeId, targetId: NodeId, position: TreeDropPosition): Boolean {
        if (sourceId == targetId) return false
        if (sourceId == workspaceState.rootNode.id) return false
        val sourceNode = TreeMutator.findNode(workspaceState.rootNode, sourceId) ?: return false
        if (sourceNode.hasDescendant(targetId)) return false

        return when (position) {
            TreeDropPosition.INSIDE -> {
                val targetNode = TreeMutator.findNode(workspaceState.rootNode, targetId) ?: return false
                documentController.isContainerNode(targetNode)
            }
            TreeDropPosition.ABOVE, TreeDropPosition.BELOW -> {
                if (targetId == workspaceState.rootNode.id) return false
                val targetParent = TreeMutator.findParent(workspaceState.rootNode, targetId) ?: return false
                targetParent.id != sourceId && !sourceNode.hasDescendant(targetParent.id)
            }
        }
    }

    // 7. Modifier Drag & Drop
    val activeModifierDrag: DraggedModifier? get() = dragController.activeModifierDrag
    val modifierDropTargetIndex: Int? get() = dragController.modifierDropTargetIndex

    fun startModifierDrag(nodeId: NodeId, fromIndex: Int, initialOffset: Offset, name: String, summary: String) {
        if (isInteractiveMode) return
        dragController.startModifierDrag(DraggedModifier(nodeId, fromIndex, name, summary), initialOffset)
    }

    fun updateModifierDrag(delta: Offset) =
        dragController.updateModifierDrag(delta, dragController.modifierDropTargetIndex)

    fun updateModifierDropTarget(targetIndex: Int) {
        if (activeModifierDrag != null && modifierDropTargetIndex != targetIndex) {
            dragController.modifierDropTargetIndex = targetIndex
        }
    }

    fun endModifierDrag() {
        val drag = activeModifierDrag
        val to = modifierDropTargetIndex
        if (drag != null && to != null && drag.fromIndex != to) {
            reorderModifier(drag.nodeId, drag.fromIndex, to)
        }
        dragController.cancelModifierDrag()
    }

    fun cancelModifierDrag() = dragController.cancelModifierDrag()

    // 8. Reactive, Off-Thread & Debounced Code Generation
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val generatedCodeFlow: StateFlow<String> = snapshotFlow { workspaceState.rootNode }
        .debounce(150.milliseconds)
        .distinctUntilChanged()
        .mapLatest { rootNode ->
            withContext(Dispatchers.Default) {
                ComposeCodeGenerator.generateCodeString(
                    packageName = "dev.chandradsl.m3c.preview",
                    componentName = "MyScreen",
                    rootNode = rootNode
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ComposeCodeGenerator.generateCodeString(
                packageName = "dev.chandradsl.m3c.preview",
                componentName = "MyScreen",
                rootNode = initialRoot
            )
        )

    val generatedCode: String
        get() = generatedCodeFlow.value
}
