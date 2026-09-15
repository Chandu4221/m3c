package dev.chandradsl.m3c.core.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementHorizontalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementVerticalDef
import dev.chandradsl.m3c.core.domain.model.BorderDef
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken

object ValueCodeGenerator {

    // Common Compose Types
    val ColorClass = ClassName("androidx.compose.ui.graphics", "Color")
    val RectangleShapeClass = ClassName("androidx.compose.ui.graphics", "RectangleShape")
    val RoundedCornerShapeClass = ClassName("androidx.compose.foundation.shape", "RoundedCornerShape")
    val CircleShapeClass = ClassName("androidx.compose.foundation.shape", "CircleShape")
    val MaterialThemeClass = ClassName("androidx.compose.material3", "MaterialTheme")
    val AlignmentClass = ClassName("androidx.compose.ui", "Alignment")
    val ArrangementClass = ClassName("androidx.compose.foundation.layout", "Arrangement")
    val BorderStrokeClass = ClassName("androidx.compose.foundation", "BorderStroke")

    // ========================================================================
    // 1. Colors & Theming
    // ========================================================================

    fun generateColor(source: ColorSource): CodeBlock = when (source) {
        is ColorSource.Custom -> CodeBlock.of("%T(%L)", ColorClass, source.hex.toString())
        is ColorSource.Theme -> generateColorToken(source.token)
    }

    fun generateColorToken(token: ColorToken): CodeBlock {
        val propertyName = token.name.replaceFirstChar { it.lowercase() }
        return CodeBlock.of("%T.colorScheme.%L", MaterialThemeClass, propertyName)
    }

    // ========================================================================
    // 2. Shapes
    // ========================================================================

    fun generateShape(shape: ShapeDef): CodeBlock = when (shape) {
        is ShapeDef.Rectangle -> CodeBlock.of("%T", RectangleShapeClass)
        is ShapeDef.Rounded -> CodeBlock.of("%T(%L)", RoundedCornerShapeClass, shape.cornerRadius.toString())
        is ShapeDef.Token -> generateShapeToken(shape.token)
    }

    fun generateShapeToken(token: ShapeToken): CodeBlock = when (token) {
        ShapeToken.None -> CodeBlock.of("%T", RectangleShapeClass)
        ShapeToken.Full -> CodeBlock.of("%T", CircleShapeClass)
        else -> {
            val propertyName = token.name.replaceFirstChar { it.lowercase() }
            CodeBlock.of("%T.shapes.%L", MaterialThemeClass, propertyName)
        }
    }

    // ========================================================================
    // 3. Typography
    // ========================================================================

    fun generateTypography(token: TypographyToken): CodeBlock {
        val propertyName = token.name.replaceFirstChar { it.lowercase() }
        return CodeBlock.of("%T.typography.%L", MaterialThemeClass, propertyName)
    }

    // ========================================================================
    // 4. Alignments & Arrangements
    // ========================================================================

    fun generateAlignment(alignment: AlignmentDef): CodeBlock =
        CodeBlock.of("%T.%L", AlignmentClass, alignment.name)

    fun generateHorizontalAlignment(alignment: AlignmentHorizontalDef): CodeBlock =
        CodeBlock.of("%T.%L", AlignmentClass, alignment.name)

    fun generateVerticalAlignment(alignment: AlignmentVerticalDef): CodeBlock =
        CodeBlock.of("%T.%L", AlignmentClass, alignment.name)

    fun generateVerticalArrangement(arrangement: ArrangementVerticalDef): CodeBlock =
        CodeBlock.of("%T.%L", ArrangementClass, arrangement.name)

    fun generateHorizontalArrangement(arrangement: ArrangementHorizontalDef): CodeBlock =
        CodeBlock.of("%T.%L", ArrangementClass, arrangement.name)

    // ========================================================================
    // 5. Border
    // ========================================================================

    fun generateBorder(border: BorderDef): CodeBlock =
        CodeBlock.of("%T(%L, %L)", BorderStrokeClass, border.width.toString(), generateColor(border.color))
}