package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinSyntaxHighlighterTest {

    @Test
    fun testKeywordsHighlighted() {
        val code = "package dev.test\nimport androidx.compose.runtime.*\nfun MyScreen() {\n    val count = 0\n}"
        val annotated = KotlinSyntaxHighlighter.highlight(code, isDark = true)
        
        // Ensure AnnotatedString preserves full text
        assertEquals(code, annotated.text)

        // Find styles covering keyword "package"
        val packageStyles = annotated.spanStyles.filter { it.start == 0 && it.end == 7 }
        assertEquals(1, packageStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.keyword, packageStyles[0].item.color)
        assertEquals(FontWeight.Bold, packageStyles[0].item.fontWeight)

        // Find styles covering keyword "val"
        val valIdx = code.indexOf("val ")
        val valStyles = annotated.spanStyles.filter { it.start == valIdx && it.end == valIdx + 3 }
        assertEquals(1, valStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.keyword, valStyles[0].item.color)
    }

    @Test
    fun testAnnotationsHighlighted() {
        val code = "@Composable\nfun Test() {}"
        val annotated = KotlinSyntaxHighlighter.highlight(code, isDark = true)

        val annotStyles = annotated.spanStyles.filter { it.start == 0 && it.end == 11 }
        assertEquals(1, annotStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.annotation, annotStyles[0].item.color)
    }

    @Test
    fun testStringsHighlighted() {
        val code = "val text = \"Hello World!\"\nval multi = \"\"\"Line 1\nLine 2\"\"\""
        val annotated = KotlinSyntaxHighlighter.highlight(code, isDark = false)

        val strIdx = code.indexOf("\"Hello World!\"")
        val strStyles = annotated.spanStyles.filter { it.start == strIdx && it.end == strIdx + 14 }
        assertEquals(1, strStyles.size)
        assertEquals(KotlinSyntaxHighlighter.LightPalette.string, strStyles[0].item.color)

        val multiIdx = code.indexOf("\"\"\"Line 1")
        val multiStyles = annotated.spanStyles.filter { it.start == multiIdx }
        assertEquals(1, multiStyles.size)
        assertEquals(KotlinSyntaxHighlighter.LightPalette.string, multiStyles[0].item.color)
    }

    @Test
    fun testNumbersAndDpHighlighted() {
        val code = "Modifier.padding(16.dp).size(24)"
        val annotated = KotlinSyntaxHighlighter.highlight(code, isDark = true)

        val numIdx = code.indexOf("16.dp")
        val numStyles = annotated.spanStyles.filter { it.start == numIdx }
        assertEquals(1, numStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.number, numStyles[0].item.color)
    }

    @Test
    fun testTypesAndComponentsHighlighted() {
        val code = "Column {\n    Button {}\n    Text(text = \"Hi\")\n}"
        val annotated = KotlinSyntaxHighlighter.highlight(code, isDark = true)

        val colIdx = code.indexOf("Column")
        val colStyles = annotated.spanStyles.filter { it.start == colIdx && it.end == colIdx + 6 }
        assertEquals(1, colStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.type, colStyles[0].item.color)

        val btnIdx = code.indexOf("Button")
        val btnStyles = annotated.spanStyles.filter { it.start == btnIdx && it.end == btnIdx + 6 }
        assertEquals(1, btnStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.type, btnStyles[0].item.color)
    }

    @Test
    fun testCommentsHighlighted() {
        val code = "// This is a single line comment\nval x = 1\n/* Multi\nline */"
        val annotated = KotlinSyntaxHighlighter.highlight(code, isDark = true)

        val singleCommentStyles = annotated.spanStyles.filter { it.start == 0 }
        assertEquals(1, singleCommentStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.comment, singleCommentStyles[0].item.color)
        assertEquals(FontStyle.Italic, singleCommentStyles[0].item.fontStyle)

        val multiIdx = code.indexOf("/* Multi")
        val multiCommentStyles = annotated.spanStyles.filter { it.start == multiIdx }
        assertEquals(1, multiCommentStyles.size)
        assertEquals(KotlinSyntaxHighlighter.DarkPalette.comment, multiCommentStyles[0].item.color)
        assertEquals(FontStyle.Italic, multiCommentStyles[0].item.fontStyle)
    }

    @Test
    fun testLightAndDarkPalettesDistinct() {
        val dark = KotlinSyntaxHighlighter.highlight("fun test()", isDark = true)
        val light = KotlinSyntaxHighlighter.highlight("fun test()", isDark = false)

        val darkKw = dark.spanStyles.first { it.start == 0 }.item.color
        val lightKw = light.spanStyles.first { it.start == 0 }.item.color

        assertEquals(KotlinSyntaxHighlighter.DarkPalette.keyword, darkKw)
        assertEquals(KotlinSyntaxHighlighter.LightPalette.keyword, lightKw)
        assertTrue(darkKw != lightKw)
    }
}
