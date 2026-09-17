package dev.chandradsl.m3c.core.domain.store

import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import dev.chandradsl.m3c.core.domain.scope.childScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScopedModifierInvariantTest {

    @Test
    fun testContainerScopesAreAccurate() {
        assertEquals(ContainerScope.Row, ComposableNode.RowNode().childScope)
        assertEquals(ContainerScope.Row, ComposableNode.ButtonNode().childScope)
        assertEquals(ContainerScope.Row, ComposableNode.ElevatedButtonNode().childScope)
        assertEquals(ContainerScope.Row, ComposableNode.OutlinedButtonNode().childScope)

        assertEquals(ContainerScope.Column, ComposableNode.ColumnNode().childScope)
        assertEquals(ContainerScope.Column, ComposableNode.CardNode().childScope)
        assertEquals(ContainerScope.Column, ComposableNode.ElevatedCardNode().childScope)
        assertEquals(ContainerScope.Column, ComposableNode.OutlinedCardNode().childScope)

        assertEquals(ContainerScope.Box, ComposableNode.BoxNode().childScope)
        assertEquals(ContainerScope.None, ComposableNode.TextNode(text = "Hello").childScope)
    }

    @Test
    fun testFindParentAndFindNode() {
        val rootId = NodeId("root_col")
        val rowId = NodeId("child_row")
        val btnId = NodeId("btn_inside_row")
        val txtId = NodeId("txt_inside_btn")

        val txt = ComposableNode.TextNode(id = txtId, text = "Click")
        val btn = ComposableNode.ButtonNode(id = btnId, content = listOf(txt))
        val row = ComposableNode.RowNode(id = rowId, children = listOf(btn))
        val root = ComposableNode.ColumnNode(id = rootId, children = listOf(row))

        assertEquals(rootId, TreeMutator.findParent(root, rowId)?.id)
        assertEquals(rowId, TreeMutator.findParent(root, btnId)?.id)
        assertEquals(btnId, TreeMutator.findParent(root, txtId)?.id)
        assertNull(TreeMutator.findParent(root, rootId))

        assertNotNull(TreeMutator.findNode(root, txtId))
        assertNotNull(TreeMutator.findNode(root, btnId))
        assertNotNull(TreeMutator.findNode(root, rowId))
        assertNotNull(TreeMutator.findNode(root, rootId))
        assertNull(TreeMutator.findNode(root, NodeId("non_existent")))
    }

    @Test
    fun testRowScopeModifiersAllowedInRowAndRejectedInBox() {
        val rowId = NodeId("row")
        val textInsideRowId = NodeId("txt_row")
        val boxId = NodeId("box")
        val textInsideBoxId = NodeId("txt_box")

        val textInRow = ComposableNode.TextNode(id = textInsideRowId, text = "Row Item")
        val textInBox = ComposableNode.TextNode(id = textInsideBoxId, text = "Box Item")
        val row = ComposableNode.RowNode(id = rowId, children = listOf(textInRow))
        val box = ComposableNode.BoxNode(id = boxId, children = listOf(textInBox))
        val root = ComposableNode.ColumnNode(id = NodeId("root"), children = listOf(row, box))

        // 1. Updating text inside Row with RowScopeModifier.Weight -> Allowed
        val updatedRoot1 = TreeMutator.updateModifiers(
            root = root,
            targetId = textInsideRowId,
            newModifiers = listOf(
                ModifierDef.FillMaxWidth(),
                ModifierDef.RowScopeModifier.Weight(1f),
                ModifierDef.RowScopeModifier.Align(AlignmentVerticalDef.CenterVertically)
            )
        )
        val rowChild = TreeMutator.findNode(updatedRoot1, textInsideRowId) as ComposableNode.TextNode
        assertEquals(3, rowChild.modifiers.size)

        // 2. Updating text inside Box with RowScopeModifier.Weight -> Stripped (Invariant Protected!)
        val updatedRoot2 = TreeMutator.updateModifiers(
            root = root,
            targetId = textInsideBoxId,
            newModifiers = listOf(
                ModifierDef.FillMaxWidth(),
                ModifierDef.RowScopeModifier.Weight(1f), // Illegal in Box!
                ModifierDef.BoxScopeModifier.Align(AlignmentDef.Center) // Legal in Box!
            )
        )
        val boxChild = TreeMutator.findNode(updatedRoot2, textInsideBoxId) as ComposableNode.TextNode
        assertEquals(2, boxChild.modifiers.size)
        assertTrue(boxChild.modifiers.any { it is ModifierDef.FillMaxWidth })
        assertTrue(boxChild.modifiers.any { it is ModifierDef.BoxScopeModifier.Align })
        assertTrue(boxChild.modifiers.none { it is ModifierDef.RowScopeModifier.Weight })
    }

    @Test
    fun testInsertChildSanitizesIncompatibleModifiers() {
        val colId = NodeId("col")
        val col = ComposableNode.ColumnNode(id = colId)

        // Child prepared with RowScopeModifier.Weight
        val childWithRowMod = ComposableNode.TextNode(
            id = NodeId("txt"),
            text = "Test",
            modifiers = listOf(
                ModifierDef.FillMaxWidth(),
                ModifierDef.RowScopeModifier.Weight(1f) // Incompatible with Column!
            )
        )

        val updatedCol = TreeMutator.insertChild(col, colId, childWithRowMod) as ComposableNode.ColumnNode
        assertEquals(1, updatedCol.children.size)
        val insertedChild = updatedCol.children.first()
        // The incompatible Row modifier was automatically stripped, maintaining domain invariants
        assertEquals(1, insertedChild.modifiers.size)
        assertTrue(insertedChild.modifiers.first() is ModifierDef.FillMaxWidth)
    }

    @Test
    fun testColumnScopeModifiersAllowedInColumnAndCards() {
        val cardId = NodeId("card")
        val txtId = NodeId("card_text")
        val txt = ComposableNode.TextNode(id = txtId, text = "Card Text")
        val card = ComposableNode.CardNode(id = cardId, content = listOf(txt))

        val updatedCard = TreeMutator.updateModifiers(
            root = card,
            targetId = txtId,
            newModifiers = listOf(
                ModifierDef.ColumnScopeModifier.Weight(1f),
                ModifierDef.ColumnScopeModifier.Align(AlignmentHorizontalDef.CenterHorizontally)
            )
        ) as ComposableNode.CardNode

        val updatedTxt = updatedCard.content.first()
        assertEquals(2, updatedTxt.modifiers.size)
        assertTrue(updatedTxt.modifiers.any { it is ModifierDef.ColumnScopeModifier.Weight })
    }
}
