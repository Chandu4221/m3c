package dev.chandradsl.m3c.core.codegen

import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken
import kotlin.test.Test
import kotlin.test.assertTrue

class ComposeCodeGeneratorTest {

    @Test
    fun testGenerateFullMaterial3Screen() {
        val screenTree = ComposableNode.ScaffoldNode(
            id = NodeId("scaffold_root"),
            topBar = ComposableNode.TopAppBarNode(
                title = ComposableNode.TextNode(
                    text = "M3 Component Studio",
                    typography = TypographyToken.TitleMedium
                ),
                containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
            ),
            floatingActionButton = ComposableNode.FloatingActionButtonNode(
                shape = ShapeDef.Token(ShapeToken.Large),
                containerColor = ColorSource.Theme(ColorToken.PrimaryContainer),
                content = listOf(ComposableNode.TextNode(text = "Add"))
            ),
            content = ComposableNode.ColumnNode(
                modifiers = listOf(
                    ModifierDef.FillMaxSize(),
                    ModifierDef.Padding.all(DpVal(16f))
                ),
                children = listOf(
                    ComposableNode.OutlinedCardNode(
                        content = listOf(
                            ComposableNode.OutlinedTextFieldNode(
                                value = "Antigravity",
                                label = "User Name"
                            ),
                            ComposableNode.ButtonNode(
                                shape = ShapeDef.Token(ShapeToken.Full),
                                containerColor = ColorSource.Theme(ColorToken.Primary),
                                content = listOf(ComposableNode.TextNode(text = "Save Profile"))
                            )
                        )
                    )
                )
            )
        )

        // Generate Kotlin code string
        val code = ComposeCodeGenerator.generateCodeString(
            packageName = "dev.chandradsl.m3c.preview",
            componentName = "DashboardScreen",
            rootNode = screenTree
        )

        println("=== Generated Kotlin Code ===\n$code\n=============================")

        // 1. Check Package & Imports
        assertTrue(code.contains("package dev.chandradsl.m3c.preview"))
        assertTrue(code.contains("import androidx.compose.runtime.Composable"))
        assertTrue(code.contains("import androidx.compose.ui.Modifier"))
        assertTrue(code.contains("import androidx.compose.ui.unit.dp"))
        assertTrue(code.contains("import androidx.compose.material3.MaterialTheme"))
        assertTrue(code.contains("import androidx.compose.material3.Scaffold"))
        assertTrue(code.contains("import androidx.compose.material3.TopAppBar"))
        assertTrue(code.contains("import androidx.compose.material3.OutlinedCard"))
        assertTrue(code.contains("import androidx.compose.material3.OutlinedTextField"))
        assertTrue(code.contains("import androidx.compose.material3.Button"))

        // 2. Check Composable Function Signature
        assertTrue(code.contains("@Composable"))
        assertTrue(code.contains("public fun DashboardScreen(modifier: Modifier = Modifier)"))

        // 3. Check Scaffold Slots & Trailing Lambdas
        assertTrue(code.contains("topBar = {"))
        assertTrue(code.contains("floatingActionButton = {"))
        assertTrue(code.contains(") { innerPadding ->"))

        // 4. Check M3 Theme Tokens
        assertTrue(code.contains("MaterialTheme.colorScheme.surfaceContainer"))
        assertTrue(code.contains("MaterialTheme.colorScheme.primaryContainer"))
        assertTrue(code.contains("MaterialTheme.typography.titleMedium"))
        assertTrue(code.contains("MaterialTheme.shapes.large"))

        // 5. Check Modifier Chaining
        assertTrue(code.contains(".fillMaxSize()"))
        assertTrue(code.contains(".padding(16.0.dp)"))
    }
}