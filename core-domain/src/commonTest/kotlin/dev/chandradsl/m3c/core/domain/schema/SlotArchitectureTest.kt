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
}
