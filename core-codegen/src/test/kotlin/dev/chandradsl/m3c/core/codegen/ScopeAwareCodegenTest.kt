package dev.chandradsl.m3c.core.codegen

import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScopeAwareCodegenTest {

    @Test
    fun testRowScopeEmitsWeightAndAlign() {
        val rowNode = ComposableNode.RowNode(
            children = listOf(
                ComposableNode.TextNode(
                    text = "Weighted Row Item",
                    modifiers = listOf(
                        ModifierDef.RowScopeModifier.Weight(1.0f),
                        ModifierDef.RowScopeModifier.Align(AlignmentVerticalDef.CenterVertically)
                    )
                )
            )
        )

        val code = ComposeCodeGenerator.generateCodeString(rootNode = rowNode)
        assertTrue(code.contains(".weight(1.0f)"), "Expected .weight(1.0f) inside Row")
        assertTrue(code.contains(".align(Alignment.CenterVertically)"), "Expected .align inside Row")
    }

    @Test
    fun testColumnScopeEmitsWeightAndAlign() {
        val colNode = ComposableNode.ColumnNode(
            children = listOf(
                ComposableNode.TextNode(
                    text = "Weighted Column Item",
                    modifiers = listOf(
                        ModifierDef.ColumnScopeModifier.Weight(2.0f),
                        ModifierDef.ColumnScopeModifier.Align(AlignmentHorizontalDef.CenterHorizontally)
                    )
                )
            )
        )

        val code = ComposeCodeGenerator.generateCodeString(rootNode = colNode)
        assertTrue(code.contains(".weight(2.0f)"), "Expected .weight(2.0f) inside Column")
        assertTrue(code.contains(".align(Alignment.CenterHorizontally)"), "Expected .align inside Column")
    }

    @Test
    fun testBoxScopeEmitsBoxAlign() {
        val boxNode = ComposableNode.BoxNode(
            children = listOf(
                ComposableNode.TextNode(
                    text = "Box Aligned Item",
                    modifiers = listOf(
                        ModifierDef.BoxScopeModifier.Align(AlignmentDef.BottomEnd)
                    )
                )
            )
        )

        val code = ComposeCodeGenerator.generateCodeString(rootNode = boxNode)
        assertTrue(code.contains(".align(Alignment.BottomEnd)"), "Expected .align(Alignment.BottomEnd) inside Box")
    }

    @Test
    fun testIllegalScopedModifiersFilteredOutDuringCodegen() {
        // A standalone text node with RowScopeModifier.Weight should NOT emit .weight()
        val standaloneText = ComposableNode.TextNode(
            text = "Standalone",
            modifiers = listOf(
                ModifierDef.FillMaxWidth(),
                ModifierDef.RowScopeModifier.Weight(1.0f) // Illegal outside Row!
            )
        )

        val code = ComposeCodeGenerator.generateCodeString(rootNode = standaloneText)
        assertTrue(code.contains(".fillMaxWidth()"), "Expected universal modifier .fillMaxWidth()")
        assertFalse(code.contains(".weight("), "Illegal .weight() should not be emitted outside RowScope")
    }

    @Test
    fun testModifierCodeGeneratorFiltersByScopeDirectly() {
        val mixedModifiers = listOf(
            ModifierDef.FillMaxWidth(),
            ModifierDef.RowScopeModifier.Weight(1.0f),
            ModifierDef.ColumnScopeModifier.Weight(2.0f),
            ModifierDef.BoxScopeModifier.Align(AlignmentDef.Center)
        )

        // When generating with ContainerScope.Row:
        val rowChain = ModifierCodeGenerator.generateModifierChain(
            modifiers = mixedModifiers,
            parentScope = ContainerScope.Row
        )?.toString() ?: ""
        assertTrue(rowChain.contains(".fillMaxWidth()"))
        assertTrue(rowChain.contains(".weight(1.0f)"))
        assertFalse(rowChain.contains(".weight(2.0f)"))
        assertFalse(rowChain.contains(".align("))

        // When generating with ContainerScope.None:
        val noneChain = ModifierCodeGenerator.generateModifierChain(
            modifiers = mixedModifiers,
            parentScope = ContainerScope.None
        )?.toString() ?: ""
        assertTrue(noneChain.contains(".fillMaxWidth()"))
        assertFalse(noneChain.contains(".weight("))
        assertFalse(noneChain.contains(".align("))
    }
}
