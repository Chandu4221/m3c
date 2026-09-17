package dev.chandradsl.m3c.core.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.scope.ContainerScope

object ModifierCodeGenerator {

    val ModifierClass = ClassName("androidx.compose.ui", "Modifier")

    /**
     * Generates a chained Modifier CodeBlock (e.g. `Modifier.fillMaxWidth().padding(16.dp)`)
     * If baseModifierName is provided (e.g. "modifier"), it chains onto that parameter instead.
     * Scoped modifiers are validated against [parentScope] so illegal scoped extensions are never emitted.
     */
    fun generateModifierChain(
        modifiers: List<ModifierDef>,
        baseModifierName: String? = null,
        parentScope: ContainerScope = ContainerScope.None
    ): CodeBlock? {
        val validModifiers = modifiers.filter { def ->
            def.requiredScope == ContainerScope.None || def.requiredScope == parentScope
        }
        if (validModifiers.isEmpty() && baseModifierName == null) return null

        val builder = CodeBlock.builder()
        if (baseModifierName != null) {
            builder.add("%L", baseModifierName)
        } else {
            builder.add("%T", ModifierClass)
        }

        validModifiers.forEach { def ->
            builder.add("\n·") // Indented dot chaining
            builder.add(generateSingleModifier(def))
        }

        return builder.build()
    }

    private fun generateSingleModifier(def: ModifierDef): CodeBlock = when (def) {
        is ModifierDef.FillMaxSize -> {
            if (def.fraction == 1.0f) CodeBlock.of(".fillMaxSize()")
            else CodeBlock.of(".fillMaxSize(%Lf)", def.fraction)
        }
        is ModifierDef.FillMaxWidth -> {
            if (def.fraction == 1.0f) CodeBlock.of(".fillMaxWidth()")
            else CodeBlock.of(".fillMaxWidth(%Lf)", def.fraction)
        }
        is ModifierDef.FillMaxHeight -> {
            if (def.fraction == 1.0f) CodeBlock.of(".fillMaxHeight()")
            else CodeBlock.of(".fillMaxHeight(%Lf)", def.fraction)
        }
        is ModifierDef.Size -> {
            if (def.width == def.height) CodeBlock.of(".size(%L)", def.width.toString())
            else CodeBlock.of(".size(width = %L, height = %L)", def.width.toString(), def.height.toString())
        }
        is ModifierDef.Width -> CodeBlock.of(".width(%L)", def.width.toString())
        is ModifierDef.Height -> CodeBlock.of(".height(%L)", def.height.toString())
        is ModifierDef.DefaultMinSize -> CodeBlock.of(
            ".defaultMinSize(minWidth = %L, minHeight = %L)",
            def.minWidth.toString(),
            def.minHeight.toString()
        )
        is ModifierDef.Padding -> generatePadding(def)
        is ModifierDef.Offset -> CodeBlock.of(".offset(x = %L, y = %L)", def.x.toString(), def.y.toString())
        is ModifierDef.Background -> {
            val colorBlock = ValueCodeGenerator.generateColor(def.color)
            val shape = def.shape
            if (shape != null) {
                CodeBlock.of(".background(color = %L, shape = %L)", colorBlock, ValueCodeGenerator.generateShape(shape))
            } else {
                CodeBlock.of(".background(color = %L)", colorBlock)
            }
        }
        is ModifierDef.Border -> {
            val borderBlock = ValueCodeGenerator.generateBorder(def.border)
            val shape = def.shape
            if (shape != null) {
                CodeBlock.of(".border(border = %L, shape = %L)", borderBlock, ValueCodeGenerator.generateShape(shape))
            } else {
                CodeBlock.of(".border(border = %L)", borderBlock)
            }
        }
        is ModifierDef.Clip -> CodeBlock.of(".clip(%L)", ValueCodeGenerator.generateShape(def.shape))
        is ModifierDef.Shadow -> {
            val shape = def.shape
            if (shape != null) {
                CodeBlock.of(
                    ".shadow(elevation = %L, shape = %L, clip = %L)",
                    def.elevation.toString(),
                    ValueCodeGenerator.generateShape(shape),
                    def.clip
                )
            } else {
                CodeBlock.of(".shadow(elevation = %L, clip = %L)", def.elevation.toString(), def.clip)
            }
        }
        is ModifierDef.Alpha -> CodeBlock.of(".alpha(%Lf)", def.alpha)
        is ModifierDef.Clickable -> {
            val label = def.onClickLabel
            if (label != null) {
                CodeBlock.of(".clickable(enabled = %L, onClickLabel = %S) { /* TODO */ }", def.enabled, label)
            } else {
                CodeBlock.of(".clickable(enabled = %L) { /* TODO */ }", def.enabled)
            }
        }
        is ModifierDef.RowScopeModifier.Weight -> {
            if (def.fill) CodeBlock.of(".weight(%Lf)", def.weight)
            else CodeBlock.of(".weight(%Lf, fill = false)", def.weight)
        }
        is ModifierDef.RowScopeModifier.Align -> CodeBlock.of(
            ".align(%L)",
            ValueCodeGenerator.generateVerticalAlignment(def.alignment)
        )
        is ModifierDef.ColumnScopeModifier.Weight -> {
            if (def.fill) CodeBlock.of(".weight(%Lf)", def.weight)
            else CodeBlock.of(".weight(%Lf, fill = false)", def.weight)
        }
        is ModifierDef.ColumnScopeModifier.Align -> CodeBlock.of(
            ".align(%L)",
            ValueCodeGenerator.generateHorizontalAlignment(def.alignment)
        )
        is ModifierDef.BoxScopeModifier.Align -> CodeBlock.of(
            ".align(%L)",
            ValueCodeGenerator.generateAlignment(def.alignment)
        )
    }

    private fun generatePadding(padding: ModifierDef.Padding): CodeBlock {
        val s = padding.start
        val t = padding.top
        val e = padding.end
        val b = padding.bottom

        // 1. All sides equal
        if (s == t && t == e && e == b) {
            return CodeBlock.of(".padding(%L)", s.toString())
        }
        // 2. Symmetric (Horizontal & Vertical)
        if (s == e && t == b) {
            return CodeBlock.of(".padding(horizontal = %L, vertical = %L)", s.toString(), t.toString())
        }
        // 3. Individual non-zero values
        val parts = mutableListOf<String>()
        if (s != DpVal.Zero) parts.add("start = ${s}")
        if (t != DpVal.Zero) parts.add("top = ${t}")
        if (e != DpVal.Zero) parts.add("end = ${e}")
        if (b != DpVal.Zero) parts.add("bottom = ${b}")

        return if (parts.isEmpty()) {
            CodeBlock.of(".padding(0.dp)")
        } else {
            CodeBlock.of(".padding(%L)", parts.joinToString(", "))
        }
    }
}