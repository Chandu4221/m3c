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
import dev.chandradsl.m3c.core.codegen.project.ProjectScaffoldGenerator
import dev.chandradsl.m3c.core.domain.model.M3cProject
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import dev.chandradsl.m3c.core.domain.storage.M3cProjectSerializer
import dev.chandradsl.m3c.core.domain.template.ScreenTemplate
import dev.chandradsl.m3c.core.domain.template.ScreenTemplates
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
import dev.chandradsl.m3c.core.domain.model.TreeDropPosition
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import dev.chandradsl.m3c.core.domain.model.allDirectChildren
import dev.chandradsl.m3c.core.domain.model.hasDescendant
import dev.chandradsl.m3c.core.domain.schema.ComponentCategory
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

enum class LeftDrawerTab {
    Palette,
    Hierarchy
}

enum class WindowSizeClass(val label: String, val badgeText: String, val rangeDescription: String) {
    Compact("Compact", "COMPACT", "< 600 dp"),
    Medium("Medium", "MEDIUM", "600–839 dp"),
    Expanded("Expanded", "EXPANDED", "≥ 840 dp");

    companion object {
        fun fromWidth(width: Dp): WindowSizeClass = when {
            width < 600.dp -> Compact
            width < 840.dp -> Medium
            else -> Expanded
        }
    }
}

enum class DevicePreset(
    val label: String,
    val baseWidth: Dp,
    val baseHeight: Dp,
    val isDefaultLandscape: Boolean = false
) {
    Phone("Phone", 390.dp, 844.dp, isDefaultLandscape = false),
    Foldable("Foldable", 673.dp, 841.dp, isDefaultLandscape = false),
    Tablet("Tablet", 800.dp, 1280.dp, isDefaultLandscape = false),
    Desktop("Desktop", 1200.dp, 800.dp, isDefaultLandscape = true);

    val width: Dp get() = baseWidth
    val height: Dp get() = baseHeight

    companion object {
        val PhonePortrait get() = Phone
        val PhoneLandscape get() = Phone
    }
}

enum class CodePreviewMode {
    ActiveScreen,
    NavGraph
}

/**
 * Main Studio ViewModel composing DocumentController, CanvasController, and DragController.
 * Provides derived, lazily-memoized Kotlin code generation.
 */
class StudioViewModel {

    // 0. Project File, Multi-Screen & Dirty State Management
    var projectName: String by mutableStateOf("Untitled")
    var packageName: String by mutableStateOf("com.example.app")
    var currentProjectFile: File? by mutableStateOf(null)
    var isDirty: Boolean by mutableStateOf(false)
    var isExportDialogOpen: Boolean by mutableStateOf(false)
    var codePreviewMode: CodePreviewMode by mutableStateOf(CodePreviewMode.ActiveScreen)

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

    var screens: List<M3cScreen> by mutableStateOf(
        listOf(
            M3cScreen(
                id = "screen_main",
                name = "MainScreen",
                route = "main",
                rootNode = initialRoot,
                isStartDestination = true
            )
        )
    )
    var activeScreenId: String by mutableStateOf("screen_main")

    val activeScreen: M3cScreen?
        get() = screens.firstOrNull { it.id == activeScreenId } ?: screens.firstOrNull()

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

    // 3. Multi-Screen Operations
    fun syncCurrentScreenRoot() {
        val curId = activeScreenId
        val curRoot = workspaceState.rootNode
        screens = screens.map { screen ->
            if (screen.id == curId) {
                screen.copy(rootNode = curRoot)
            } else screen
        }
    }

    fun selectScreen(screenId: String) {
        if (screenId == activeScreenId) return
        syncCurrentScreenRoot()
        val target = screens.firstOrNull { it.id == screenId } ?: return
        activeScreenId = target.id
        dispatch(WorkspaceIntent.LoadDocument(target.rootNode))
        notify("Switched to ${target.name}")
    }

    private fun createStarterScreenNode(title: String, hasBackButton: Boolean = true): ComposableNode {
        val timeId = System.currentTimeMillis()
        val topBarNode = if (hasBackButton) {
            ComposableNode.TopAppBarNode(
                id = NodeId("top_bar_$timeId"),
                title = ComposableNode.TextNode(
                    id = NodeId("title_$timeId"),
                    text = title,
                    typography = TypographyToken.TitleLarge
                ),
                navigationIcon = ComposableNode.IconButtonNode(
                    id = NodeId("nav_back_$timeId"),
                    content = listOf(
                        ComposableNode.IconNode(
                            id = NodeId("icon_back_$timeId"),
                            iconName = "ArrowBack",
                            contentDescription = "Back"
                        )
                    )
                ),
                containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
            )
        } else {
            ComposableNode.TopAppBarNode(
                id = NodeId("top_bar_$timeId"),
                title = ComposableNode.TextNode(
                    id = NodeId("title_$timeId"),
                    text = title,
                    typography = TypographyToken.TitleLarge
                ),
                containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
            )
        }

        return ComposableNode.ScaffoldNode(
            id = NodeId("scaffold_$timeId"),
            topBar = topBarNode,
            content = ComposableNode.ColumnNode(
                id = NodeId("content_col_$timeId"),
                modifiers = listOf(
                    ModifierDef.FillMaxSize(),
                    ModifierDef.Padding.all(DpVal(16f))
                ),
                children = listOf(
                    ComposableNode.TextNode(
                        id = NodeId("text_header_$timeId"),
                        text = "$title Screen",
                        typography = TypographyToken.HeadlineSmall
                    )
                )
            )
        )
    }

    fun addScreen(
        name: String,
        route: String,
        isStartDestination: Boolean = false
    ) {
        syncCurrentScreenRoot()
        val sanitizedName = name.trim().replace(Regex("[^a-zA-Z0-9_]"), "").let {
            if (it.isBlank()) "Screen${screens.size + 1}" else it.replaceFirstChar { c -> c.uppercase() }
        }
        val sanitizedRoute = route.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").let {
            if (it.isBlank()) sanitizedName.lowercase() else it
        }
        val uniqueId = "screen_${System.currentTimeMillis()}"
        val starterRoot = createStarterScreenNode(sanitizedName, hasBackButton = screens.isNotEmpty())

        val newScreen = M3cScreen(
            id = uniqueId,
            name = sanitizedName,
            route = sanitizedRoute,
            rootNode = starterRoot,
            isStartDestination = isStartDestination || screens.isEmpty()
        )

        screens = if (newScreen.isStartDestination) {
            screens.map { it.copy(isStartDestination = false) } + newScreen
        } else {
            screens + newScreen
        }

        activeScreenId = newScreen.id
        dispatch(WorkspaceIntent.LoadDocument(newScreen.rootNode))
        isDirty = true
        notifySuccess("Added screen '$sanitizedName'")
    }

    fun addScreenFromTemplate(
        template: ScreenTemplate,
        isStartDestination: Boolean = false
    ) {
        syncCurrentScreenRoot()
        var count = 1
        var candidateName = template.name
        while (screens.any { it.name.equals(candidateName, ignoreCase = true) }) {
            count++
            candidateName = "${template.name}$count"
        }
        val sanitizedName = candidateName
        val sanitizedRoute = sanitizedName.lowercase().replace(Regex("[^a-z0-9_]"), "_")
        val uniqueId = "screen_${System.currentTimeMillis()}"
        val templateRoot = template.createRoot()

        val newScreen = M3cScreen(
            id = uniqueId,
            name = sanitizedName,
            route = sanitizedRoute,
            rootNode = templateRoot,
            isStartDestination = isStartDestination || screens.isEmpty()
        )

        screens = if (newScreen.isStartDestination) {
            screens.map { it.copy(isStartDestination = false) } + newScreen
        } else {
            screens + newScreen
        }

        activeScreenId = newScreen.id
        dispatch(WorkspaceIntent.LoadDocument(newScreen.rootNode))
        isDirty = true
        notifySuccess("Added '${template.name}' screen from template library")
    }

    fun updateScreen(
        screenId: String,
        name: String,
        route: String,
        isStartDestination: Boolean = false
    ) {
        val sanitizedName = name.trim().replace(Regex("[^a-zA-Z0-9_]"), "").let {
            if (it.isBlank()) "Screen" else it.replaceFirstChar { c -> c.uppercase() }
        }
        val sanitizedRoute = route.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").let {
            if (it.isBlank()) sanitizedName.lowercase() else it
        }

        screens = screens.map { screen ->
            if (screen.id == screenId) {
                screen.copy(
                    name = sanitizedName,
                    route = sanitizedRoute,
                    isStartDestination = isStartDestination
                )
            } else if (isStartDestination) {
                screen.copy(isStartDestination = false)
            } else screen
        }
        isDirty = true
        notifySuccess("Updated screen '$sanitizedName'")
    }

    private fun deepCloneWithNewIds(node: ComposableNode): ComposableNode {
        val freshId = NodeId("${node.id.value}_copy_${(1000..9999).random()}")
        return when (node) {
            is ComposableNode.ColumnNode -> node.copy(id = freshId, children = node.children.map { deepCloneWithNewIds(it) })
            is ComposableNode.RowNode -> node.copy(id = freshId, children = node.children.map { deepCloneWithNewIds(it) })
            is ComposableNode.BoxNode -> node.copy(id = freshId, children = node.children.map { deepCloneWithNewIds(it) })
            is ComposableNode.ScaffoldNode -> node.copy(
                id = freshId,
                topBar = node.topBar?.let { deepCloneWithNewIds(it) as? ComposableNode.TopAppBarNode },
                bottomBar = node.bottomBar?.let { deepCloneWithNewIds(it) as? ComposableNode.NavigationBarNode },
                floatingActionButton = node.floatingActionButton?.let { deepCloneWithNewIds(it) as? ComposableNode.FloatingActionButtonNode },
                content = node.content?.let { deepCloneWithNewIds(it) }
            )
            is ComposableNode.TopAppBarNode -> node.copy(
                id = freshId,
                title = deepCloneWithNewIds(node.title),
                navigationIcon = node.navigationIcon?.let { deepCloneWithNewIds(it) },
                actions = node.actions.map { deepCloneWithNewIds(it) }
            )
            is ComposableNode.ButtonNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.ElevatedButtonNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.FilledTonalButtonNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.OutlinedButtonNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.TextButtonNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.CardNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.OutlinedCardNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.ElevatedCardNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.TextNode -> node.copy(id = freshId)
            is ComposableNode.IconNode -> node.copy(id = freshId)
            is ComposableNode.IconButtonNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.FloatingActionButtonNode -> node.copy(id = freshId, content = node.content.map { deepCloneWithNewIds(it) })
            is ComposableNode.NavigationBarNode -> node.copy(id = freshId, items = node.items.map { deepCloneWithNewIds(it) })
            is ComposableNode.NavigationBarItemNode -> node.copy(
                id = freshId,
                icon = deepCloneWithNewIds(node.icon),
                label = node.label?.let { deepCloneWithNewIds(it) }
            )
            is ComposableNode.TextFieldNode -> node.copy(id = freshId)
            is ComposableNode.OutlinedTextFieldNode -> node.copy(id = freshId)
            is ComposableNode.CheckboxNode -> node.copy(id = freshId)
            is ComposableNode.SwitchNode -> node.copy(id = freshId)
            is ComposableNode.RadioButtonNode -> node.copy(id = freshId)
            is ComposableNode.SliderNode -> node.copy(id = freshId)
            is ComposableNode.RangeSliderNode -> node.copy(id = freshId)
            is ComposableNode.SpacerNode -> node.copy(id = freshId)
            is ComposableNode.SurfaceNode -> node.copy(id = freshId, children = node.children.map { deepCloneWithNewIds(it) })
            is ComposableNode.CircularProgressIndicatorNode -> node.copy(id = freshId)
            is ComposableNode.LinearProgressIndicatorNode -> node.copy(id = freshId)
            is ComposableNode.HorizontalDividerNode -> node.copy(id = freshId)
            is ComposableNode.VerticalDividerNode -> node.copy(id = freshId)
            is ComposableNode.AssistChipNode -> node.copy(id = freshId)
            is ComposableNode.FilterChipNode -> node.copy(id = freshId)
            is ComposableNode.InputChipNode -> node.copy(id = freshId)
            is ComposableNode.SuggestionChipNode -> node.copy(id = freshId)
            is ComposableNode.BadgeNode -> node.copy(id = freshId)
            is ComposableNode.BadgedBoxNode -> node.copy(
                id = freshId,
                badge = node.badge?.let { deepCloneWithNewIds(it) as? ComposableNode.BadgeNode },
                content = node.content?.let { deepCloneWithNewIds(it) }
            )
            is ComposableNode.BottomAppBarNode -> node.copy(
                id = freshId,
                actions = node.actions.map { deepCloneWithNewIds(it) },
                floatingActionButton = node.floatingActionButton?.let { deepCloneWithNewIds(it) }
            )
            is ComposableNode.NavigationRailNode -> node.copy(
                id = freshId,
                header = node.header?.let { deepCloneWithNewIds(it) },
                items = node.items.map { deepCloneWithNewIds(it) }
            )
            is ComposableNode.NavigationRailItemNode -> node.copy(id = freshId)
            is ComposableNode.AlertDialogNode -> node.copy(id = freshId)
        }
    }

    fun duplicateScreen(screenId: String) {
        syncCurrentScreenRoot()
        val source = screens.firstOrNull { it.id == screenId } ?: return
        val newId = "screen_${System.currentTimeMillis()}"
        val newName = "${source.name}Copy"
        val newRoute = "${source.route}_copy"
        val clonedRoot = deepCloneWithNewIds(source.rootNode)

        val clonedScreen = M3cScreen(
            id = newId,
            name = newName,
            route = newRoute,
            rootNode = clonedRoot,
            isStartDestination = false
        )

        screens = screens + clonedScreen
        activeScreenId = clonedScreen.id
        dispatch(WorkspaceIntent.LoadDocument(clonedScreen.rootNode))
        isDirty = true
        notifySuccess("Duplicated '${source.name}'")
    }

    fun deleteScreen(screenId: String) {
        if (screens.size <= 1) {
            notifyWarning("Cannot delete the only screen in the project")
            return
        }
        val target = screens.firstOrNull { it.id == screenId } ?: return
        val remaining = screens.filterNot { it.id == screenId }

        val updatedRemaining = if (target.isStartDestination && remaining.none { it.isStartDestination }) {
            remaining.mapIndexed { idx, s -> if (idx == 0) s.copy(isStartDestination = true) else s }
        } else {
            remaining
        }

        screens = updatedRemaining
        if (activeScreenId == screenId) {
            val nextScreen = updatedRemaining.first()
            activeScreenId = nextScreen.id
            dispatch(WorkspaceIntent.LoadDocument(nextScreen.rootNode))
        }
        isDirty = true
        notifySuccess("Deleted '${target.name}'")
    }

    fun setStartDestination(screenId: String) {
        screens = screens.map { screen ->
            screen.copy(isStartDestination = (screen.id == screenId))
        }
        isDirty = true
        val name = screens.firstOrNull { it.id == screenId }?.name ?: ""
        notifySuccess("Set '$name' as Start Destination")
    }

    // 4. Project File Operations
    fun newProject(parentFrame: Frame? = null) {
        val freshRoot = createDefaultScaffold()
        val defaultScreen = M3cScreen(
            id = "screen_main",
            name = "MainScreen",
            route = "main",
            rootNode = freshRoot,
            isStartDestination = true
        )
        screens = listOf(defaultScreen)
        activeScreenId = defaultScreen.id
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
            val loadedScreens = project.screens.ifEmpty {
                listOf(
                    M3cScreen(
                        id = "screen_main",
                        name = "MainScreen",
                        route = "main",
                        rootNode = createDefaultScaffold(),
                        isStartDestination = true
                    )
                )
            }
            val activeScreen = loadedScreens.firstOrNull { it.id == project.activeScreenId }
                ?: loadedScreens.first()

            screens = loadedScreens
            activeScreenId = activeScreen.id
            dispatch(WorkspaceIntent.LoadDocument(activeScreen.rootNode))
            currentProjectFile = file
            projectName = project.name.ifBlank { file.nameWithoutExtension }
            packageName = project.packageName
            isDirty = false
            notifySuccess("Opened ${file.name} (${loadedScreens.size} screens)")
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
            syncCurrentScreenRoot()
            val project = M3cProject(
                schemaVersion = 1,
                name = projectName.ifBlank { file.nameWithoutExtension },
                packageName = packageName,
                screens = screens,
                activeScreenId = activeScreenId
            )
            val jsonText = M3cProjectSerializer.encode(project)
            file.writeText(jsonText)
            currentProjectFile = file
            projectName = file.nameWithoutExtension
            isDirty = false
            notifySuccess("Saved ${screens.size} screen(s) to ${file.name}")
        } catch (e: Exception) {
            notifyError("Failed to save project: ${e.message}")
        }
    }

    fun openExportDialog() {
        syncCurrentScreenRoot()
        isExportDialogOpen = true
    }

    fun closeExportDialog() {
        isExportDialogOpen = false
    }

    fun exportProjectToDirectory(
        targetDir: File,
        customName: String = projectName,
        customPackage: String = packageName
    ): Boolean {
        return try {
            syncCurrentScreenRoot()
            val project = M3cProject(
                schemaVersion = 1,
                name = customName.ifBlank { projectName },
                packageName = customPackage.ifBlank { packageName },
                screens = screens,
                activeScreenId = activeScreenId
            )
            val generated = ProjectScaffoldGenerator.exportToDirectory(project, targetDir)
            notifySuccess("Exported project (${generated.size} files) to ${targetDir.name}/")
            true
        } catch (e: Exception) {
            notifyError("Export failed: ${e.message}")
            false
        }
    }

    fun exportProjectToZip(
        zipFile: File,
        customName: String = projectName,
        customPackage: String = packageName
    ): Boolean {
        return try {
            syncCurrentScreenRoot()
            val project = M3cProject(
                schemaVersion = 1,
                name = customName.ifBlank { projectName },
                packageName = customPackage.ifBlank { packageName },
                screens = screens,
                activeScreenId = activeScreenId
            )
            val exported = ProjectScaffoldGenerator.exportToZip(project, zipFile)
            notifySuccess("Exported project ZIP to ${exported.name}")
            true
        } catch (e: Exception) {
            notifyError("Export ZIP failed: ${e.message}")
            false
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

    var isLandscape: Boolean
        get() = canvasController.isLandscape
        set(value) { canvasController.isLandscape = value }

    val effectiveViewportWidth: Dp
        get() = canvasController.effectiveViewportWidth

    val effectiveViewportHeight: Dp
        get() = canvasController.effectiveViewportHeight

    val currentWindowSizeClass: WindowSizeClass
        get() = canvasController.currentWindowSizeClass

    fun toggleOrientation() = canvasController.toggleOrientation()

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

    fun isStructuralContainer(tag: String): Boolean {
        val def = ComponentRegistry.all.firstOrNull {
            it.type.name.equals(tag, ignoreCase = true) ||
            it.displayName.replace(" ", "").equals(tag, ignoreCase = true)
        }
        return def?.category == ComponentCategory.LayoutContainers ||
               def?.category == ComponentCategory.SurfacesAndCards ||
               tag.equals("Box", ignoreCase = true) ||
               tag.equals("Column", ignoreCase = true) ||
               tag.equals("Row", ignoreCase = true)
    }

    fun startPaletteDrag(item: DraggedPaletteItem, initialOffset: Offset) {
        if (isInteractiveMode) return
        dragController.startPaletteDrag(item, initialOffset, workspaceState.rootNode.id)
        updateCanvasDropTarget()
    }

    fun updatePaletteDrag(delta: Offset) {
        dragController.updatePaletteDrag(delta, workspaceState.rootNode.id)
        updateCanvasDropTarget()
    }

    fun updateCanvasBounds(bounds: Rect) =
        dragController.updateCanvasBounds(bounds)

    fun endPaletteDrag() {
        val wasHovered = isCanvasDropHovered
        val canvasDropTarget = dragController.canvasDropTargetId
        val canvasDropPos = dragController.canvasDropPosition
        val item = dragController.endPaletteDrag()
        dragController.cancelCanvasDrag()

        if (item != null && wasHovered) {
            val newNode = item.factory()
            if (canvasDropTarget != null && canvasDropPos != null && canvasDropPos.isRelative) {
                val targetParent = TreeMutator.findParent(workspaceState.rootNode, canvasDropTarget)
                if (targetParent != null) {
                    val siblings = targetParent.allDirectChildren
                    val targetIdx = siblings.indexOfFirst { it.id == canvasDropTarget }
                    val insertIdx = if (canvasDropPos == TreeDropPosition.BELOW) {
                        if (targetIdx >= 0) targetIdx + 1 else -1
                    } else {
                        if (targetIdx >= 0) targetIdx else 0
                    }
                    dispatch(WorkspaceIntent.InsertChild(parentId = targetParent.id, node = newNode, index = insertIdx))
                } else {
                    dispatch(WorkspaceIntent.InsertChild(parentId = workspaceState.rootNode.id, node = newNode))
                }
            } else {
                val targetParentId = canvasDropTarget ?: hoveredCanvasParentId ?: run {
                    val selected = selectedNode
                    if (selected != null && documentController.isContainerNode(selected)) {
                        selected.id
                    } else if (selected != null) {
                        TreeMutator.findParent(workspaceState.rootNode, selected.id)?.id ?: workspaceState.rootNode.id
                    } else {
                        workspaceState.rootNode.id
                    }
                }
                dispatch(WorkspaceIntent.InsertChild(parentId = targetParentId, node = newNode))
            }
            dispatch(WorkspaceIntent.SelectNode(newNode.id))
        }
    }

    fun cancelPaletteDrag() {
        dragController.cancelPaletteDrag()
        dragController.cancelCanvasDrag()
    }

    // 5b. On-Canvas Drag & Drop Reordering
    val activeCanvasDragNodeId: NodeId? get() = dragController.activeCanvasDragNodeId
    val canvasDropTargetId: NodeId? get() = dragController.canvasDropTargetId
    val canvasDropPosition: TreeDropPosition? get() = dragController.canvasDropPosition

    fun registerCanvasNodeBounds(nodeId: NodeId, tag: String, bounds: Rect, parentLayout: String?) =
        dragController.registerCanvasNodeBounds(nodeId, tag, bounds, parentLayout)

    fun unregisterCanvasNodeBounds(nodeId: NodeId) =
        dragController.unregisterCanvasNodeBounds(nodeId)

    fun startCanvasDrag(nodeId: NodeId, initialOffset: Offset) {
        if (isInteractiveMode) return
        if (nodeId == workspaceState.rootNode.id) return
        dragController.startCanvasDrag(nodeId, initialOffset)
        updateCanvasDropTarget()
    }

    fun updateCanvasDrag(delta: Offset) {
        dragController.updateCanvasDrag(delta)
        updateCanvasDropTarget()
    }

    fun endCanvasDrag() {
        val draggedId = dragController.activeCanvasDragNodeId
        val targetId = dragController.canvasDropTargetId
        val position = dragController.canvasDropPosition
        if (draggedId != null && targetId != null && position != null) {
            when (position) {
                TreeDropPosition.INSIDE -> moveNodeInto(draggedId, targetId)
                TreeDropPosition.ABOVE -> moveNodeRelative(draggedId, targetId, placeAfter = false)
                TreeDropPosition.BELOW -> moveNodeRelative(draggedId, targetId, placeAfter = true)
            }
        }
        dragController.cancelCanvasDrag()
    }

    fun cancelCanvasDrag() {
        dragController.cancelCanvasDrag()
    }

    private fun updateCanvasDropTarget() {
        val draggedId = dragController.activeCanvasDragNodeId
        val pointer = dragController.dragPointerOffset
        val root = workspaceState.rootNode

        val candidates = dragController.allCanvasNodes.filter { (id, info) ->
            if (id == draggedId) return@filter false
            if (draggedId != null) {
                val draggedNode = TreeMutator.findNode(root, draggedId)
                if (draggedNode?.hasDescendant(id) == true) return@filter false
            }
            info.bounds.contains(pointer)
        }

        if (candidates.isEmpty()) {
            if (dragController.canvasBoundsInWindow.contains(pointer)) {
                dragController.setCanvasDropTarget(root.id, TreeDropPosition.INSIDE)
            } else {
                dragController.setCanvasDropTarget(null, null)
            }
            return
        }

        val best = candidates.minByOrNull { it.value.bounds.width * it.value.bounds.height }?.value
        if (best == null) {
            dragController.setCanvasDropTarget(null, null)
            return
        }

        val targetId = best.nodeId
        if (targetId == root.id) {
            dragController.setCanvasDropTarget(root.id, TreeDropPosition.INSIDE)
            return
        }

        when (best.parentLayout) {
            "Column" -> {
                if (isStructuralContainer(best.tag)) {
                    val h = best.bounds.height
                    val relY = pointer.y - best.bounds.top
                    val pos = when {
                        relY < h * 0.2f -> TreeDropPosition.ABOVE
                        relY > h * 0.8f -> TreeDropPosition.BELOW
                        else -> TreeDropPosition.INSIDE
                    }
                    dragController.setCanvasDropTarget(targetId, pos)
                } else {
                    val midY = best.bounds.top + best.bounds.height * 0.5f
                    val pos = if (pointer.y < midY) TreeDropPosition.ABOVE else TreeDropPosition.BELOW
                    dragController.setCanvasDropTarget(targetId, pos)
                }
            }
            "Row" -> {
                if (isStructuralContainer(best.tag)) {
                    val w = best.bounds.width
                    val relX = pointer.x - best.bounds.left
                    val pos = when {
                        relX < w * 0.2f -> TreeDropPosition.ABOVE
                        relX > w * 0.8f -> TreeDropPosition.BELOW
                        else -> TreeDropPosition.INSIDE
                    }
                    dragController.setCanvasDropTarget(targetId, pos)
                } else {
                    val midX = best.bounds.left + best.bounds.width * 0.5f
                    val pos = if (pointer.x < midX) TreeDropPosition.ABOVE else TreeDropPosition.BELOW
                    dragController.setCanvasDropTarget(targetId, pos)
                }
            }
            else -> {
                if (isContainerTag(best.tag)) {
                    dragController.setCanvasDropTarget(targetId, TreeDropPosition.INSIDE)
                } else {
                    val parent = TreeMutator.findParent(root, targetId)
                    if (parent != null) {
                        dragController.setCanvasDropTarget(parent.id, TreeDropPosition.INSIDE)
                    } else {
                        dragController.setCanvasDropTarget(root.id, TreeDropPosition.INSIDE)
                    }
                }
            }
        }
    }

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
    val generatedCodeFlow: StateFlow<String> = snapshotFlow {
        Triple(workspaceState.rootNode, codePreviewMode, activeScreenId)
    }
        .debounce(150.milliseconds)
        .distinctUntilChanged()
        .mapLatest { (rootNode, mode, currentScreenId) ->
            withContext(Dispatchers.Default) {
                when (mode) {
                    CodePreviewMode.ActiveScreen -> {
                        val currentScreenName = screens.firstOrNull { it.id == currentScreenId }?.name ?: "MainScreen"
                        ComposeCodeGenerator.generateCodeString(
                            packageName = packageName,
                            componentName = currentScreenName,
                            rootNode = rootNode
                        )
                    }
                    CodePreviewMode.NavGraph -> {
                        val updatedScreens = screens.map {
                            if (it.id == currentScreenId) it.copy(rootNode = rootNode) else it
                        }
                        ComposeCodeGenerator.generateNavGraphCodeString(
                            packageName = packageName,
                            screens = updatedScreens,
                            graphName = "AppNavHost"
                        )
                    }
                }
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ComposeCodeGenerator.generateCodeString(
                packageName = "com.example.app",
                componentName = "MainScreen",
                rootNode = initialRoot
            )
        )

    val generatedCode: String
        get() = generatedCodeFlow.value
}
