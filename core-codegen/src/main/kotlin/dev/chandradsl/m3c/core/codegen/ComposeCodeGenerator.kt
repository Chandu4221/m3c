package dev.chandradsl.m3c.core.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementHorizontalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementVerticalDef
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.scope.ContainerScope

object ComposeCodeGenerator {

    val ComposableClass = ClassName("androidx.compose.runtime", "Composable")
    val ModifierClass = ClassName("androidx.compose.ui", "Modifier")

    // Component ClassNames
    private val ColumnClass = ClassName("androidx.compose.foundation.layout", "Column")
    private val RowClass = ClassName("androidx.compose.foundation.layout", "Row")
    private val BoxClass = ClassName("androidx.compose.foundation.layout", "Box")
    private val SpacerClass = ClassName("androidx.compose.foundation.layout", "Spacer")
    private val SurfaceClass = ClassName("androidx.compose.material3", "Surface")
    private val CardClass = ClassName("androidx.compose.material3", "Card")
    private val ElevatedCardClass = ClassName("androidx.compose.material3", "ElevatedCard")
    private val OutlinedCardClass = ClassName("androidx.compose.material3", "OutlinedCard")
    private val ButtonClass = ClassName("androidx.compose.material3", "Button")
    private val ElevatedButtonClass = ClassName("androidx.compose.material3", "ElevatedButton")
    private val FilledTonalButtonClass = ClassName("androidx.compose.material3", "FilledTonalButton")
    private val OutlinedButtonClass = ClassName("androidx.compose.material3", "OutlinedButton")
    private val TextButtonClass = ClassName("androidx.compose.material3", "TextButton")
    private val IconButtonClass = ClassName("androidx.compose.material3", "IconButton")
    private val FabClass = ClassName("androidx.compose.material3", "FloatingActionButton")
    private val TextClass = ClassName("androidx.compose.material3", "Text")
    private val TextFieldClass = ClassName("androidx.compose.material3", "TextField")
    private val OutlinedTextFieldClass = ClassName("androidx.compose.material3", "OutlinedTextField")
    private val CheckboxClass = ClassName("androidx.compose.material3", "Checkbox")
    private val SwitchClass = ClassName("androidx.compose.material3", "Switch")
    private val RadioButtonClass = ClassName("androidx.compose.material3", "RadioButton")
    private val SliderClass = ClassName("androidx.compose.material3", "Slider")
    private val CircularProgressClass = ClassName("androidx.compose.material3", "CircularProgressIndicator")
    private val LinearProgressClass = ClassName("androidx.compose.material3", "LinearProgressIndicator")
    private val TopAppBarDefaultsClass = ClassName("androidx.compose.material3", "TopAppBarDefaults")
    private val ScaffoldClass = ClassName("androidx.compose.material3", "Scaffold")
    private val ButtonDefaultsClass = ClassName("androidx.compose.material3", "ButtonDefaults")
    private val CardDefaultsClass = ClassName("androidx.compose.material3", "CardDefaults")
    private val TopAppBarClass = ClassName("androidx.compose.material3", "TopAppBar")
    private val NavigationBarClass = ClassName("androidx.compose.material3", "NavigationBar")
    private val NavigationBarItemClass = ClassName("androidx.compose.material3", "NavigationBarItem")
    private val HorizontalDividerClass = ClassName("androidx.compose.material3", "HorizontalDivider")
    private val VerticalDividerClass = ClassName("androidx.compose.material3", "VerticalDivider")
    private val AssistChipClass = ClassName("androidx.compose.material3", "AssistChip")
    private val FilterChipClass = ClassName("androidx.compose.material3", "FilterChip")
    private val InputChipClass = ClassName("androidx.compose.material3", "InputChip")
    private val SuggestionChipClass = ClassName("androidx.compose.material3", "SuggestionChip")
    private val BadgeClass = ClassName("androidx.compose.material3", "Badge")
    private val BadgedBoxClass = ClassName("androidx.compose.material3", "BadgedBox")
    private val BottomAppBarClass = ClassName("androidx.compose.material3", "BottomAppBar")
    private val NavigationRailClass = ClassName("androidx.compose.material3", "NavigationRail")
    private val NavigationRailItemClass = ClassName("androidx.compose.material3", "NavigationRailItem")
    private val RangeSliderClass = ClassName("androidx.compose.material3", "RangeSlider")
    private val AlertDialogClass = ClassName("androidx.compose.material3", "AlertDialog")

    /**
     * Generates a complete Kotlin source file containing the @Composable function.
     */
    fun generateFile(
        packageName: String = "dev.chandradsl.m3c.generated",
        componentName: String = "GeneratedScreen",
        rootNode: ComposableNode
    ): FileSpec {
        val funSpec = generateFunction(componentName, rootNode)
        return FileSpec.builder(packageName, componentName)
            .addImport("androidx.compose.ui.unit", "dp", "sp")
            .addImport("androidx.compose.foundation.layout", "padding")
            .addFunction(funSpec)
            .build()
    }

    /**
     * Generates raw formatted Kotlin code as a String (ready for live preview or clipboard).
     */
    fun generateCodeString(
        packageName: String = "dev.chandradsl.m3c.generated",
        componentName: String = "GeneratedScreen",
        rootNode: ComposableNode
    ): String = generateFile(packageName, componentName, rootNode).toString()

    /**
     * Generates the @Composable FunSpec.
     */
    fun generateFunction(
        functionName: String,
        rootNode: ComposableNode
    ): FunSpec {
        return FunSpec.builder(functionName)
            .addAnnotation(ComposableClass)
            .addParameter(
                ParameterSpec.builder("modifier", ModifierClass)
                    .defaultValue("%T", ModifierClass)
                    .build()
            )
            .addCode(generateNodeCode(rootNode, isRoot = true))
            .build()
    }

    /**
     * Recursively emits Compose code blocks for any ComposableNode.
     */
    fun generateNodeCode(
        node: ComposableNode,
        isRoot: Boolean = false,
        parentScope: ContainerScope = ContainerScope.None
    ): CodeBlock {
        val b = CodeBlock.builder()
        val modifierCode = if (isRoot) {
            ModifierCodeGenerator.generateModifierChain(node.modifiers, baseModifierName = "modifier", parentScope = parentScope)
        } else {
            ModifierCodeGenerator.generateModifierChain(node.modifiers, parentScope = parentScope)
        }

        when (node) {
            is ComposableNode.ColumnNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                if (node.verticalArrangement != ArrangementVerticalDef.Top) {
                    args.add(CodeBlock.of("verticalArrangement = %L", ValueCodeGenerator.generateVerticalArrangement(node.verticalArrangement)))
                }
                if (node.horizontalAlignment != AlignmentHorizontalDef.Start) {
                    args.add(CodeBlock.of("horizontalAlignment = %L", ValueCodeGenerator.generateHorizontalAlignment(node.horizontalAlignment)))
                }

                b.add("%T", ColumnClass)
                emitArgumentsAndChildren(b, args, node.children, childScope = ContainerScope.Column)
            }

            is ComposableNode.RowNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                if (node.horizontalArrangement != ArrangementHorizontalDef.Start) {
                    args.add(CodeBlock.of("horizontalArrangement = %L", ValueCodeGenerator.generateHorizontalArrangement(node.horizontalArrangement)))
                }
                if (node.verticalAlignment != AlignmentVerticalDef.Top) {
                    args.add(CodeBlock.of("verticalAlignment = %L", ValueCodeGenerator.generateVerticalAlignment(node.verticalAlignment)))
                }

                b.add("%T", RowClass)
                emitArgumentsAndChildren(b, args, node.children, childScope = ContainerScope.Row)
            }

            is ComposableNode.BoxNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                if (node.contentAlignment != AlignmentDef.TopStart) {
                    args.add(CodeBlock.of("contentAlignment = %L", ValueCodeGenerator.generateAlignment(node.contentAlignment)))
                }

                b.add("%T", BoxClass)
                emitArgumentsAndChildren(b, args, node.children, childScope = ContainerScope.Box)
            }

            is ComposableNode.SurfaceNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.shape?.let { args.add(CodeBlock.of("shape = %L", ValueCodeGenerator.generateShape(it))) }
                node.color?.let { args.add(CodeBlock.of("color = %L", ValueCodeGenerator.generateColor(it))) }

                b.add("%T", SurfaceClass)
                emitArgumentsAndChildren(b, args, node.children, childScope = ContainerScope.None)
            }

            is ComposableNode.CardNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.shape?.let { args.add(CodeBlock.of("shape = %L", ValueCodeGenerator.generateShape(it))) }

                b.add("%T", CardClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Column)
            }

            is ComposableNode.ElevatedCardNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T", ElevatedCardClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Column)
            }

            is ComposableNode.OutlinedCardNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T", OutlinedCardClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Column)
            }

            is ComposableNode.ButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                if (!node.enabled) args.add(CodeBlock.of("enabled = false"))
                node.shape?.let { args.add(CodeBlock.of("shape = %L", ValueCodeGenerator.generateShape(it))) }

                if (node.containerColor != null || node.contentColor != null) {
                    val colorArgs = mutableListOf<CodeBlock>()
                    node.containerColor?.let { colorArgs.add(CodeBlock.of("containerColor = %L", ValueCodeGenerator.generateColor(it))) }
                    node.contentColor?.let { colorArgs.add(CodeBlock.of("contentColor = %L", ValueCodeGenerator.generateColor(it))) }
                    args.add(CodeBlock.of("colors = %T.buttonColors(%L)", ButtonDefaultsClass, colorArgs.joinToCodeBlock()))
                }

                b.add("%T", ButtonClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Row)
            }

            is ComposableNode.ElevatedButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T", ElevatedButtonClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Row)
            }

            is ComposableNode.FilledTonalButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T", FilledTonalButtonClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Row)
            }

            is ComposableNode.OutlinedButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T", OutlinedButtonClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Row)
            }

            is ComposableNode.TextButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T", TextButtonClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Row)
            }

            is ComposableNode.IconButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T", IconButtonClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Row)
            }

            is ComposableNode.FloatingActionButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.shape?.let { args.add(CodeBlock.of("shape = %L", ValueCodeGenerator.generateShape(it))) }
                node.containerColor?.let { args.add(CodeBlock.of("containerColor = %L", ValueCodeGenerator.generateColor(it))) }
                node.contentColor?.let { args.add(CodeBlock.of("contentColor = %L", ValueCodeGenerator.generateColor(it))) }

                b.add("%T", FabClass)
                emitArgumentsAndChildren(b, args, node.content, childScope = ContainerScope.Row)
            }

            is ComposableNode.TextNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("text = %S", node.text))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.typography?.let { args.add(CodeBlock.of("style = %L", ValueCodeGenerator.generateTypography(it))) }
                node.color?.let { args.add(CodeBlock.of("color = %L", ValueCodeGenerator.generateColor(it))) }
                node.fontSize?.let { args.add(CodeBlock.of("fontSize = %L", it.toString())) }

                b.add("%T(%L)\n", TextClass, args.joinToCodeBlock())
            }

            is ComposableNode.TextFieldNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("value = %S", node.value))
                args.add(CodeBlock.of("onValueChange = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.label?.let { args.add(CodeBlock.of("label = { %T(%S) }", TextClass, it)) }
                node.placeholder?.let { args.add(CodeBlock.of("placeholder = { %T(%S) }", TextClass, it)) }

                b.add("%T(%L)\n", TextFieldClass, args.joinToCodeBlock())
            }

            is ComposableNode.OutlinedTextFieldNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("value = %S", node.value))
                args.add(CodeBlock.of("onValueChange = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.label?.let { args.add(CodeBlock.of("label = { %T(%S) }", TextClass, it)) }
                node.placeholder?.let { args.add(CodeBlock.of("placeholder = { %T(%S) }", TextClass, it)) }

                b.add("%T(%L)\n", OutlinedTextFieldClass, args.joinToCodeBlock())
            }

            is ComposableNode.CheckboxNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("checked = %L", node.checked))
                args.add(CodeBlock.of("onCheckedChange = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T(%L)\n", CheckboxClass, args.joinToCodeBlock())
            }

            is ComposableNode.SwitchNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("checked = %L", node.checked))
                args.add(CodeBlock.of("onCheckedChange = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T(%L)\n", SwitchClass, args.joinToCodeBlock())
            }

            is ComposableNode.RadioButtonNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("selected = %L", node.selected))
                args.add(CodeBlock.of("onClick = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T(%L)\n", RadioButtonClass, args.joinToCodeBlock())
            }

            is ComposableNode.SliderNode -> {
                val args = mutableListOf<CodeBlock>()
                args.add(CodeBlock.of("value = %Lf", node.value))
                args.add(CodeBlock.of("onValueChange = { /* TODO */ }"))
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T(%L)\n", SliderClass, args.joinToCodeBlock())
            }

            is ComposableNode.CircularProgressIndicatorNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.progress?.let { args.add(CodeBlock.of("progress = { %Lf }", it)) }
                b.add("%T(%L)\n", CircularProgressClass, args.joinToCodeBlock())
            }

            is ComposableNode.LinearProgressIndicatorNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                node.progress?.let { args.add(CodeBlock.of("progress = { %Lf }", it)) }
                b.add("%T(%L)\n", LinearProgressClass, args.joinToCodeBlock())
            }

            is ComposableNode.SpacerNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T(%L)\n", SpacerClass, args.joinToCodeBlock())
            }

            is ComposableNode.HorizontalDividerNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T(%L)\n", HorizontalDividerClass, args.joinToCodeBlock())
            }

            is ComposableNode.VerticalDividerNode -> {
                val args = mutableListOf<CodeBlock>()
                modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                b.add("%T(%L)\n", VerticalDividerClass, args.joinToCodeBlock())
            }

            is ComposableNode.ScaffoldNode -> {
                b.add("%T(\n", ScaffoldClass)
                b.indent()
                modifierCode?.let { b.add("modifier = %L,\n", it) }

                node.topBar?.let { topBar ->
                    b.add("topBar = {\n")
                    b.indent()
                    b.add(generateNodeCode(topBar))
                    b.unindent()
                    b.add("},\n")
                }

                node.bottomBar?.let { bottomBar ->
                    b.add("bottomBar = {\n")
                    b.indent()
                    b.add(generateNodeCode(bottomBar))
                    b.unindent()
                    b.add("},\n")
                }

                node.floatingActionButton?.let { fab ->
                    b.add("floatingActionButton = {\n")
                    b.indent()
                    b.add(generateNodeCode(fab))
                    b.unindent()
                    b.add("},\n")
                }

                b.unindent()
                b.add(") { innerPadding ->\n")
                b.indent()
                node.content?.let { content ->
                    b.add(generateNodeCode(content))
                }
                b.unindent()
                b.add("}\n")
            }

            is ComposableNode.TopAppBarNode -> {
                b.add("%T(\n", TopAppBarClass)
                b.indent()
                b.add("title = {\n")
                b.indent()
                b.add(generateNodeCode(node.title))
                b.unindent()
                b.add("},\n")

                node.navigationIcon?.let { navIcon ->
                    b.add("navigationIcon = {\n")
                    b.indent()
                    b.add(generateNodeCode(navIcon))
                    b.unindent()
                    b.add("},\n")
                }

                if (node.actions.isNotEmpty()) {
                    b.add("actions = {\n")
                    b.indent()
                    node.actions.forEach { action -> b.add(generateNodeCode(action, parentScope = ContainerScope.Row)) }
                    b.unindent()
                    b.add("},\n")
                }

                if (node.containerColor != null || node.titleContentColor != null) {
                    val colorArgs = mutableListOf<CodeBlock>()
                    node.containerColor?.let { colorArgs.add(CodeBlock.of("containerColor = %L", ValueCodeGenerator.generateColor(it))) }
                    node.titleContentColor?.let { colorArgs.add(CodeBlock.of("titleContentColor = %L", ValueCodeGenerator.generateColor(it))) }
                    b.add("colors = %T.topAppBarColors(%L),\n", TopAppBarDefaultsClass, colorArgs.joinToCodeBlock())
                }

                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.NavigationBarNode -> {
                b.add("%T(\n", NavigationBarClass)
                b.indent()
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(") {\n")
                b.indent()
                node.items.forEach { item -> b.add(generateNodeCode(item, parentScope = ContainerScope.Row)) }
                b.unindent()
                b.add("}\n")
            }

            is ComposableNode.NavigationBarItemNode -> {
                b.add("%T(\n", NavigationBarItemClass)
                b.indent()
                b.add("selected = %L,\n", node.selected)
                b.add("onClick = { /* TODO */ },\n")
                b.add("icon = {\n")
                b.indent()
                b.add(generateNodeCode(node.icon))
                b.unindent()
                b.add("},\n")
                node.label?.let { lbl ->
                    b.add("label = {\n")
                    b.indent()
                    b.add(generateNodeCode(lbl))
                    b.unindent()
                    b.add("},\n")
                }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.AssistChipNode -> {
                b.add("%T(\n", AssistChipClass)
                b.indent()
                b.add("onClick = { /* TODO */ },\n")
                b.add("label = { %T(%S) },\n", TextClass, node.label)
                node.leadingIcon?.let { icon ->
                    b.add("leadingIcon = {\n")
                    b.indent()
                    b.add(generateNodeCode(icon))
                    b.unindent()
                    b.add("},\n")
                }
                if (!node.enabled) b.add("enabled = false,\n")
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.FilterChipNode -> {
                b.add("%T(\n", FilterChipClass)
                b.indent()
                b.add("selected = %L,\n", node.selected)
                b.add("onClick = { /* TODO */ },\n")
                b.add("label = { %T(%S) },\n", TextClass, node.label)
                node.leadingIcon?.let { icon ->
                    b.add("leadingIcon = {\n")
                    b.indent()
                    b.add(generateNodeCode(icon))
                    b.unindent()
                    b.add("},\n")
                }
                if (!node.enabled) b.add("enabled = false,\n")
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.InputChipNode -> {
                b.add("%T(\n", InputChipClass)
                b.indent()
                b.add("selected = %L,\n", node.selected)
                b.add("onClick = { /* TODO */ },\n")
                b.add("label = { %T(%S) },\n", TextClass, node.label)
                node.leadingIcon?.let { icon ->
                    b.add("leadingIcon = {\n")
                    b.indent()
                    b.add(generateNodeCode(icon))
                    b.unindent()
                    b.add("},\n")
                }
                node.trailingIcon?.let { icon ->
                    b.add("trailingIcon = {\n")
                    b.indent()
                    b.add(generateNodeCode(icon))
                    b.unindent()
                    b.add("},\n")
                }
                if (!node.enabled) b.add("enabled = false,\n")
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.SuggestionChipNode -> {
                b.add("%T(\n", SuggestionChipClass)
                b.indent()
                b.add("onClick = { /* TODO */ },\n")
                b.add("label = { %T(%S) },\n", TextClass, node.label)
                node.icon?.let { icon ->
                    b.add("icon = {\n")
                    b.indent()
                    b.add(generateNodeCode(icon))
                    b.unindent()
                    b.add("},\n")
                }
                if (!node.enabled) b.add("enabled = false,\n")
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.BadgeNode -> {
                if (node.text != null) {
                    b.add("%T(\n", BadgeClass)
                    b.indent()
                    modifierCode?.let { b.add("modifier = %L,\n", it) }
                    b.unindent()
                    b.add(") {\n")
                    b.indent()
                    b.add("%T(%S)\n", TextClass, node.text)
                    b.unindent()
                    b.add("}\n")
                } else {
                    val args = mutableListOf<CodeBlock>()
                    modifierCode?.let { args.add(CodeBlock.of("modifier = %L", it)) }
                    b.add("%T", BadgeClass)
                    emitArgumentsAndChildren(b, args, emptyList())
                }
            }

            is ComposableNode.BadgedBoxNode -> {
                b.add("%T(\n", BadgedBoxClass)
                b.indent()
                b.add("badge = {\n")
                b.indent()
                node.badge?.let { b.add(generateNodeCode(it)) } ?: b.add("%T()\n", BadgeClass)
                b.unindent()
                b.add("},\n")
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(") {\n")
                b.indent()
                node.content?.let { b.add(generateNodeCode(it)) }
                b.unindent()
                b.add("}\n")
            }

            is ComposableNode.BottomAppBarNode -> {
                b.add("%T(\n", BottomAppBarClass)
                b.indent()
                b.add("actions = {\n")
                b.indent()
                node.actions.forEach { action -> b.add(generateNodeCode(action, parentScope = ContainerScope.Row)) }
                b.unindent()
                b.add("},\n")
                node.floatingActionButton?.let { fab ->
                    b.add("floatingActionButton = {\n")
                    b.indent()
                    b.add(generateNodeCode(fab))
                    b.unindent()
                    b.add("},\n")
                }
                node.containerColor?.let { b.add("containerColor = %L,\n", ValueCodeGenerator.generateColor(it)) }
                node.contentColor?.let { b.add("contentColor = %L,\n", ValueCodeGenerator.generateColor(it)) }
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.NavigationRailNode -> {
                b.add("%T(\n", NavigationRailClass)
                b.indent()
                node.header?.let { hdr ->
                    b.add("header = {\n")
                    b.indent()
                    b.add(generateNodeCode(hdr))
                    b.unindent()
                    b.add("},\n")
                }
                node.containerColor?.let { b.add("containerColor = %L,\n", ValueCodeGenerator.generateColor(it)) }
                node.contentColor?.let { b.add("contentColor = %L,\n", ValueCodeGenerator.generateColor(it)) }
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(") {\n")
                b.indent()
                node.items.forEach { item -> b.add(generateNodeCode(item, parentScope = ContainerScope.Column)) }
                b.unindent()
                b.add("}\n")
            }

            is ComposableNode.NavigationRailItemNode -> {
                b.add("%T(\n", NavigationRailItemClass)
                b.indent()
                b.add("selected = %L,\n", node.selected)
                b.add("onClick = { /* TODO */ },\n")
                b.add("icon = {\n")
                b.indent()
                b.add(generateNodeCode(node.icon))
                b.unindent()
                b.add("},\n")
                node.label?.let { lbl ->
                    b.add("label = {\n")
                    b.indent()
                    b.add(generateNodeCode(lbl))
                    b.unindent()
                    b.add("},\n")
                }
                if (!node.alwaysShowLabel) b.add("alwaysShowLabel = false,\n")
                if (!node.enabled) b.add("enabled = false,\n")
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.RangeSliderNode -> {
                b.add("%T(\n", RangeSliderClass)
                b.indent()
                b.add("value = %Lf..%Lf,\n", node.startValue, node.endValue)
                b.add("onValueChange = { /* TODO */ },\n")
                if (node.steps > 0) b.add("steps = %L,\n", node.steps)
                if (!node.enabled) b.add("enabled = false,\n")
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }

            is ComposableNode.AlertDialogNode -> {
                b.add("%T(\n", AlertDialogClass)
                b.indent()
                b.add("onDismissRequest = { /* TODO */ },\n")
                node.confirmButton?.let { confirm ->
                    b.add("confirmButton = {\n")
                    b.indent()
                    b.add(generateNodeCode(confirm))
                    b.unindent()
                    b.add("},\n")
                }
                node.dismissButton?.let { dismiss ->
                    b.add("dismissButton = {\n")
                    b.indent()
                    b.add(generateNodeCode(dismiss))
                    b.unindent()
                    b.add("},\n")
                }
                node.icon?.let { icon ->
                    b.add("icon = {\n")
                    b.indent()
                    b.add(generateNodeCode(icon))
                    b.unindent()
                    b.add("},\n")
                }
                node.title?.let { title ->
                    b.add("title = {\n")
                    b.indent()
                    b.add(generateNodeCode(title))
                    b.unindent()
                    b.add("},\n")
                }
                node.text?.let { text ->
                    b.add("text = {\n")
                    b.indent()
                    b.add(generateNodeCode(text))
                    b.unindent()
                    b.add("},\n")
                }
                node.containerColor?.let { b.add("containerColor = %L,\n", ValueCodeGenerator.generateColor(it)) }
                modifierCode?.let { b.add("modifier = %L,\n", it) }
                b.unindent()
                b.add(")\n")
            }
        }

        return b.build()
    }

    private fun emitArgumentsAndChildren(
        builder: CodeBlock.Builder,
        arguments: List<CodeBlock>,
        children: List<ComposableNode>,
        childScope: ContainerScope = ContainerScope.None
    ) {
        if (arguments.isNotEmpty()) {
            builder.add("(\n")
            builder.indent()
            arguments.forEachIndexed { index, arg ->
                builder.add("%L", arg)
                if (index < arguments.size - 1) builder.add(",\n") else builder.add("\n")
            }
            builder.unindent()
            builder.add(")")
        }

        if (children.isNotEmpty()) {
            builder.add(" {\n")
            builder.indent()
            children.forEach { child ->
                builder.add(generateNodeCode(child, parentScope = childScope))
            }
            builder.unindent()
            builder.add("}\n")
        } else if (arguments.isEmpty()) {
            builder.add("()\n")
        } else {
            builder.add("\n")
        }
    }

    private fun List<CodeBlock>.joinToCodeBlock(): CodeBlock {
        val b = CodeBlock.builder()
        forEachIndexed { index, item ->
            b.add("%L", item)
            if (index < size - 1) b.add(", ")
        }
        return b.build()
    }
}