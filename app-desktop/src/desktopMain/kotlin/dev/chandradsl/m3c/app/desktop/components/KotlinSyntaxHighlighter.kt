package dev.chandradsl.m3c.app.desktop.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * High-performance, single-pass syntax highlighter for Kotlin Jetpack Compose source code.
 * Tokenizes keywords, annotations (@Composable), types/components, function calls,
 * strings, numbers, and comments with IntelliJ New UI-grade palettes for Light and Dark themes.
 */
object KotlinSyntaxHighlighter {

    data class SyntaxPalette(
        val keyword: Color,
        val annotation: Color,
        val string: Color,
        val number: Color,
        val type: Color,
        val functionCall: Color,
        val comment: Color,
        val punctuation: Color,
        val defaultText: Color
    )

    val DarkPalette = SyntaxPalette(
        keyword = Color(0xFFCC7832),       // IntelliJ Darcula / New UI Orange
        annotation = Color(0xFFBBB529),    // Gold/Yellow
        string = Color(0xFF6A8759),        // Emerald / Forest Green
        number = Color(0xFF6897BB),        // Cyan / Slate Blue
        type = Color(0xFF4EC9B0),          // Bright Teal / Cyan
        functionCall = Color(0xFFFFC66D),  // Golden Peach
        comment = Color(0xFF808080),       // Slate Grey
        punctuation = Color(0xFFA9B7C6),   // Off-white
        defaultText = Color(0xFFA9B7C6)    // Light Foreground
    )

    val LightPalette = SyntaxPalette(
        keyword = Color(0xFF0033B3),       // Classic IntelliJ Blue
        annotation = Color(0xFF9E880D),    // Dark Gold
        string = Color(0xFF067D17),        // Rich Green
        number = Color(0xFF1750EB),        // Blue
        type = Color(0xFF00627A),          // Deep Cyan
        functionCall = Color(0xFF7A3E9D),  // Deep Purple
        comment = Color(0xFF8C8C8C),       // Muted Grey
        punctuation = Color(0xFF000000),   // Dark Text
        defaultText = Color(0xFF000000)
    )

    private val KEYWORDS = setOf(
        "package", "import", "fun", "val", "var", "when", "if", "else", "return",
        "class", "data", "object", "interface", "sealed", "companion", "override",
        "private", "public", "internal", "protected", "true", "false", "null",
        "by", "in", "is", "as", "for", "while", "do", "try", "catch", "finally",
        "throw", "this", "super", "typealias", "const", "lateinit", "suspend",
        "inline", "crossinline", "noinline", "operator", "infix", "tailrec",
        "external", "enum", "open", "abstract", "final", "inner", "vararg",
        "constructor", "init"
    )

    fun highlight(code: String, isDark: Boolean): AnnotatedString {
        val palette = if (isDark) DarkPalette else LightPalette
        return highlight(code, palette)
    }

    fun highlight(code: String, palette: SyntaxPalette): AnnotatedString {
        val builder = AnnotatedString.Builder(code)
        val len = code.length
        var i = 0

        while (i < len) {
            val c = code[i]

            // 1. Single-line comment
            if (c == '/' && i + 1 < len && code[i + 1] == '/') {
                val start = i
                while (i < len && code[i] != '\n') {
                    i++
                }
                builder.addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    start,
                    i
                )
                continue
            }

            // 2. Multi-line comment
            if (c == '/' && i + 1 < len && code[i + 1] == '*') {
                val start = i
                i += 2
                while (i + 1 < len && !(code[i] == '*' && code[i + 1] == '/')) {
                    i++
                }
                if (i + 1 < len) i += 2 else i = len
                builder.addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    start,
                    i
                )
                continue
            }

            // 3. Triple-quoted multiline string
            if (c == '"' && i + 2 < len && code[i + 1] == '"' && code[i + 2] == '"') {
                val start = i
                i += 3
                while (i + 2 < len && !(code[i] == '"' && code[i + 1] == '"' && code[i + 2] == '"')) {
                    i++
                }
                if (i + 2 < len) i += 3 else i = len
                builder.addStyle(SpanStyle(color = palette.string), start, i)
                continue
            }

            // 4. Regular double-quoted string
            if (c == '"') {
                val start = i
                i++
                while (i < len && code[i] != '"' && code[i] != '\n') {
                    if (code[i] == '\\' && i + 1 < len) {
                        i += 2
                    } else {
                        i++
                    }
                }
                if (i < len && code[i] == '"') {
                    i++
                }
                builder.addStyle(SpanStyle(color = palette.string), start, i)
                continue
            }

            // 5. Annotation (@Composable, @Preview, etc.)
            if (c == '@' && i + 1 < len && (code[i + 1].isLetter() || code[i + 1] == '_')) {
                val start = i
                i++
                while (i < len && (code[i].isLetterOrDigit() || code[i] == '_' || code[i] == '.')) {
                    i++
                }
                builder.addStyle(
                    SpanStyle(color = palette.annotation, fontWeight = FontWeight.SemiBold),
                    start,
                    i
                )
                continue
            }

            // 6. Number literal
            if (c.isDigit() || (c == '.' && i + 1 < len && code[i + 1].isDigit() && (i == 0 || !code[i - 1].isLetterOrDigit()))) {
                val start = i
                var hasDot = false
                while (i < len && (code[i].isLetterOrDigit() || code[i] == '.' || code[i] == '_')) {
                    if (code[i] == '.') {
                        if (i + 2 <= len && (code.substring(i + 1).startsWith("dp") || code.substring(i + 1).startsWith("sp"))) {
                            i += 3
                            break
                        }
                        if (hasDot) break
                        hasDot = true
                    }
                    i++
                }
                builder.addStyle(SpanStyle(color = palette.number), start, i)
                continue
            }

            // 7. Word: Keyword, Type / Composable, Function call, or Identifier
            if (c.isLetter() || c == '_') {
                val start = i
                while (i < len && (code[i].isLetterOrDigit() || code[i] == '_')) {
                    i++
                }
                val word = code.substring(start, i)
                when {
                    KEYWORDS.contains(word) -> {
                        builder.addStyle(
                            SpanStyle(color = palette.keyword, fontWeight = FontWeight.Bold),
                            start,
                            i
                        )
                    }
                    word[0].isUpperCase() -> {
                        builder.addStyle(
                            SpanStyle(color = palette.type, fontWeight = FontWeight.SemiBold),
                            start,
                            i
                        )
                    }
                    else -> {
                        var peek = i
                        while (peek < len && (code[peek] == ' ' || code[peek] == '\t')) {
                            peek++
                        }
                        if (peek < len && (code[peek] == '(' || code[peek] == '{')) {
                            builder.addStyle(SpanStyle(color = palette.functionCall), start, i)
                        } else {
                            builder.addStyle(SpanStyle(color = palette.defaultText), start, i)
                        }
                    }
                }
                continue
            }

            // 8. Other characters
            i++
        }

        return builder.toAnnotatedString()
    }
}
