package dev.chandradsl.m3c.core.domain.schema

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import dev.chandradsl.m3c.core.domain.store.TreeMutator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SlotArchitectureTest {

    @Test
    fun testSlotDeclarationsOnScaffold() {
        val scaffoldDef = ComponentRegistry.findByType(ComponentType.Scaffold)
        assertNotNull(scaffoldDef)
        assertEquals(4, scaffoldDef.slots.size)

        val topBarSlot = scaffoldDef.slots.find { it.id == StandardSlots.TOP_BAR }
        assertNotNull(topBarSlot)
        assertEquals("Top App Bar", topBarSlot.displayName)
        assertEquals(SlotCardinality.Single, topBarSlot.cardinality)
        assertTrue(topBarSlot.accepts(ComponentType.TopAppBar))
        assertFalse(topBarSlot.accepts(ComponentType.Button))

        val fabSlot = scaffoldDef.slots.find { it.id == StandardSlots.FLOATING_ACTION_BUTTON }
        assertNotNull(fabSlot)
        assertTrue(fabSlot.accepts(ComponentType.FloatingActionButton))
        assertFalse(fabSlot.accepts(ComponentType.Column))

        val contentSlot = scaffoldDef.slots.find { it.id == StandardSlots.CONTENT }
        assertNotNull(contentSlot)
        // Accepts any type
        assertTrue(contentSlot.accepts(ComponentType.Column))
        assertTrue(contentSlot.accepts(ComponentType.Box))
    }

    @Test
    fun testSlotDeclarationsOnTopAppBar() {
        val topBarDef = ComponentRegistry.findByType(ComponentType.TopAppBar)
        assertNotNull(topBarDef)
        assertEquals(3, topBarDef.slots.size)

        val actionsSlot = topBarDef.slots.find { it.id == StandardSlots.ACTIONS }
        assertNotNull(actionsSlot)
        assertEquals(SlotCardinality.List, actionsSlot.cardinality)
        assertEquals(ContainerScope.Row, actionsSlot.providedScope)
        assertTrue(actionsSlot.accepts(ComponentType.IconButton))
        assertFalse(actionsSlot.accepts(ComponentType.Scaffold))
    }

    @Test
    fun testChildrenWithSlotsAndGetSlotChildren() {
        val titleNode = ComposableNode.TextNode(text = "App Title")
        val navIcon = ComposableNode.IconButtonNode()
        val action1 = ComposableNode.IconButtonNode()
        val topBar = ComposableNode.TopAppBarNode(
            title = titleNode,
            navigationIcon = navIcon,
            actions = listOf(action1)
        )

        val scaffold = ComposableNode.ScaffoldNode(
            topBar = topBar,
            content = ComposableNode.ColumnNode()
        )

        val scaffoldSlots = scaffold.childrenWithSlots()
        assertEquals(2, scaffoldSlots.size)
        assertEquals(StandardSlots.TOP_BAR, scaffoldSlots[0].second?.id)
        assertEquals(StandardSlots.CONTENT, scaffoldSlots[1].second?.id)

        val topBarSlots = topBar.childrenWithSlots()
        assertEquals(3, topBarSlots.size)
        assertEquals(StandardSlots.TITLE, topBarSlots[0].second?.id)
        assertEquals(StandardSlots.NAVIGATION_ICON, topBarSlots[1].second?.id)
        assertEquals(StandardSlots.ACTIONS, topBarSlots[2].second?.id)

        assertEquals(listOf(navIcon), topBar.getSlotChildren(StandardSlots.NAVIGATION_ICON))
        assertEquals(listOf(action1), topBar.getSlotChildren(StandardSlots.ACTIONS))
    }

    @Test
    fun testTreeMutatorSetAndClearSlot() {
        val scaffold = ComposableNode.ScaffoldNode()
        val topBar = ComposableNode.TopAppBarNode(title = ComposableNode.TextNode(text = "My Screen"))

        // Set topBar slot
        val updated = TreeMutator.setSlot(scaffold, scaffold.id, StandardSlots.TOP_BAR, topBar) as ComposableNode.ScaffoldNode
        assertNotNull(updated.topBar)
        assertEquals(topBar.id, updated.topBar?.id)

        // Clear topBar slot
        val cleared = TreeMutator.setSlot(updated, scaffold.id, StandardSlots.TOP_BAR, null) as ComposableNode.ScaffoldNode
        assertNull(cleared.topBar)
    }

    @Test
    fun testTreeMutatorSlotScopeSanitization() {
        val topBar = ComposableNode.TopAppBarNode(title = ComposableNode.TextNode(text = "Title"))

        // An action inside TopAppBar receives ContainerScope.Row!
        // So a Row-scoped modifier (e.g. Weight) is allowed, but a Column-scoped modifier is sanitized away.
        val actionWithRowModifier = ComposableNode.IconButtonNode(
            modifiers = listOf(
                ModifierDef.Padding.all(DpVal(4f)),
                ModifierDef.RowScopeModifier.Weight(1f) // Valid in Row
            )
        )

        val withAction = TreeMutator.setSlot(topBar, topBar.id, StandardSlots.ACTIONS, actionWithRowModifier) as ComposableNode.TopAppBarNode
        assertEquals(1, withAction.actions.size)
        assertEquals(2, withAction.actions[0].modifiers.size)
        assertTrue(withAction.actions[0].modifiers.any { it is ModifierDef.RowScopeModifier.Weight })
    }

    @Test
    fun testScaffoldBottomBarSlotAcceptsNavigationAndBottomBar() {
        val scaffoldDef = ComponentRegistry.findByType(ComponentType.Scaffold)
        assertNotNull(scaffoldDef)
        val bottomBarSlot = scaffoldDef.slots.find { it.id == StandardSlots.BOTTOM_BAR }
        assertNotNull(bottomBarSlot)
        assertTrue(bottomBarSlot.accepts(ComponentType.NavigationBar))
        assertTrue(bottomBarSlot.accepts(ComponentType.BottomAppBar))
        assertFalse(bottomBarSlot.accepts(ComponentType.Button))
    }

    @Test
    fun testEmptyScaffoldTreeMutatorRouting() {
        val emptyScaffold = ComposableNode.ScaffoldNode(
            topBar = null,
            bottomBar = null,
            floatingActionButton = null,
            content = null
        )

        // 1. Add NavigationBar -> Should route to bottomBar
        val navBar = ComposableNode.NavigationBarNode()
        val withNavBar = TreeMutator.insertChild(emptyScaffold, emptyScaffold.id, navBar) as ComposableNode.ScaffoldNode
        assertNotNull(withNavBar.bottomBar)
        assertEquals(navBar.id, withNavBar.bottomBar?.id)
        assertNull(withNavBar.content)

        // 2. Add TopAppBar -> Should route to topBar
        val topBar = ComposableNode.TopAppBarNode(title = ComposableNode.TextNode(text = "Title"))
        val withTopBar = TreeMutator.insertChild(emptyScaffold, emptyScaffold.id, topBar) as ComposableNode.ScaffoldNode
        assertNotNull(withTopBar.topBar)
        assertEquals(topBar.id, withTopBar.topBar?.id)
        assertNull(withTopBar.content)

        // 3. Add FAB -> Should route to floatingActionButton
        val fab = ComposableNode.FloatingActionButtonNode()
        val withFab = TreeMutator.insertChild(emptyScaffold, emptyScaffold.id, fab) as ComposableNode.ScaffoldNode
        assertNotNull(withFab.floatingActionButton)
        assertEquals(fab.id, withFab.floatingActionButton?.id)
        assertNull(withFab.content)

        // 4. Add regular content (e.g. Column) -> Should route to content
        val column = ComposableNode.ColumnNode()
        val withColumn = TreeMutator.insertChild(emptyScaffold, emptyScaffold.id, column) as ComposableNode.ScaffoldNode
        assertNotNull(withColumn.content)
        assertEquals(column.id, withColumn.content?.id)

        // 5. Add BottomAppBar -> Should route to bottomBar
        val bottomAppBar = ComposableNode.BottomAppBarNode()
        val withBottomAppBar = TreeMutator.insertChild(emptyScaffold, emptyScaffold.id, bottomAppBar) as ComposableNode.ScaffoldNode
        assertNotNull(withBottomAppBar.bottomBar)
        assertEquals(bottomAppBar.id, withBottomAppBar.bottomBar?.id)
    }

    @Test
    fun testNavigationBarDefaultFactoryHasItems() {
        val navBarDef = ComponentRegistry.findByType(ComponentType.NavigationBar)
        assertNotNull(navBarDef)
        val defaultNode = navBarDef.createDefault() as ComposableNode.NavigationBarNode
        assertTrue(defaultNode.items.isNotEmpty(), "Default NavigationBar must have items")
        assertEquals(3, defaultNode.items.size)
    }

    @Test
    fun testContainersWithAddedChild() {
        // BottomAppBar with added child should add to actions
        val bottomBar = ComposableNode.BottomAppBarNode()
        val iconBtn = ComposableNode.IconButtonNode()
        val updatedBottomBar = TreeMutator.insertChild(bottomBar, bottomBar.id, iconBtn) as ComposableNode.BottomAppBarNode
        assertEquals(1, updatedBottomBar.actions.size)
        assertEquals(iconBtn.id, updatedBottomBar.actions[0].id)

        // NavigationRail with added child should add to items
        val navRail = ComposableNode.NavigationRailNode()
        val railItem = ComposableNode.NavigationRailItemNode(icon = ComposableNode.TextNode(text = "Home"))
        val updatedRail = TreeMutator.insertChild(navRail, navRail.id, railItem) as ComposableNode.NavigationRailNode
        assertEquals(1, updatedRail.items.size)
        assertEquals(railItem.id, updatedRail.items[0].id)
    }

    @Test
    fun testAlertDialogSlotMutations() {
        val dialog = ComposableNode.AlertDialogNode(
            title = null,
            text = null,
            icon = null,
            confirmButton = null,
            dismissButton = null
        )

        val titleNode = ComposableNode.TextNode(text = "Warning")
        val textNode = ComposableNode.TextNode(text = "Are you sure?")
        val iconNode = ComposableNode.IconNode(iconName = "Warning")
        val confirmNode = ComposableNode.TextButtonNode(content = listOf(ComposableNode.TextNode(text = "Yes")))
        val dismissNode = ComposableNode.TextButtonNode(content = listOf(ComposableNode.TextNode(text = "No")))

        var updated = TreeMutator.setSlot(dialog, dialog.id, StandardSlots.TITLE, titleNode) as ComposableNode.AlertDialogNode
        updated = TreeMutator.setSlot(updated, dialog.id, StandardSlots.TEXT, textNode) as ComposableNode.AlertDialogNode
        updated = TreeMutator.setSlot(updated, dialog.id, StandardSlots.ICON, iconNode) as ComposableNode.AlertDialogNode
        updated = TreeMutator.setSlot(updated, dialog.id, StandardSlots.CONFIRM_BUTTON, confirmNode) as ComposableNode.AlertDialogNode
        updated = TreeMutator.setSlot(updated, dialog.id, StandardSlots.DISMISS_BUTTON, dismissNode) as ComposableNode.AlertDialogNode

        assertEquals(titleNode.id, updated.title?.id)
        assertEquals(textNode.id, updated.text?.id)
        assertEquals(iconNode.id, updated.icon?.id)
        assertEquals(confirmNode.id, updated.confirmButton?.id)
        assertEquals(dismissNode.id, updated.dismissButton?.id)

        // Clear dismiss button slot
        val cleared = TreeMutator.setSlot(updated, dialog.id, StandardSlots.DISMISS_BUTTON, null) as ComposableNode.AlertDialogNode
        assertNull(cleared.dismissButton)
        assertNotNull(cleared.confirmButton)
    }

    @Test
    fun testTextFieldSlotMutations() {
        val textField = ComposableNode.TextFieldNode(value = "Search query")
        val leadingIcon = ComposableNode.IconNode(iconName = "Search")
        val trailingIcon = ComposableNode.IconNode(iconName = "Clear")

        val withIcons = TreeMutator.setSlot(textField, textField.id, StandardSlots.LEADING_ICON, leadingIcon) as ComposableNode.TextFieldNode
        val withBoth = TreeMutator.setSlot(withIcons, textField.id, StandardSlots.TRAILING_ICON, trailingIcon) as ComposableNode.TextFieldNode

        assertEquals(leadingIcon.id, withBoth.leadingIcon?.id)
        assertEquals(trailingIcon.id, withBoth.trailingIcon?.id)

        val clearedTrailing = TreeMutator.setSlot(withBoth, textField.id, StandardSlots.TRAILING_ICON, null) as ComposableNode.TextFieldNode
        assertNull(clearedTrailing.trailingIcon)
        assertNotNull(clearedTrailing.leadingIcon)
    }
}
