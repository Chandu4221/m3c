package dev.chandradsl.m3c.core.domain.model

import dev.chandradsl.m3c.core.domain.scope.ContainerScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ModifierDef {
    val requiredScope: ContainerScope get() = ContainerScope.None

    // ========================================================================
    // 1. Sizing & Dimensions
    // ========================================================================

    @Serializable
    @SerialName("fill_max_size")
    data class FillMaxSize(val fraction: Float = 1.0f) : ModifierDef

    @Serializable
    @SerialName("fill_max_width")
    data class FillMaxWidth(val fraction: Float = 1.0f) : ModifierDef

    @Serializable
    @SerialName("fill_max_height")
    data class FillMaxHeight(val fraction: Float = 1.0f) : ModifierDef

    @Serializable
    @SerialName("size")
    data class Size(val width: DpVal, val height: DpVal) : ModifierDef

    @Serializable
    @SerialName("width")
    data class Width(val width: DpVal) : ModifierDef

    @Serializable
    @SerialName("height")
    data class Height(val height: DpVal) : ModifierDef

    @Serializable
    @SerialName("default_min_size")
    data class DefaultMinSize(
        val minWidth: DpVal = DpVal.Zero,
        val minHeight: DpVal = DpVal.Zero
    ) : ModifierDef

    // ========================================================================
    // 2. Spacing & Offsets
    // ========================================================================

    @Serializable
    @SerialName("padding")
    data class Padding(
        val start: DpVal = DpVal.Zero,
        val top: DpVal = DpVal.Zero,
        val end: DpVal = DpVal.Zero,
        val bottom: DpVal = DpVal.Zero
    ) : ModifierDef {
        companion object {
            fun all(all: DpVal) = Padding(start = all, top = all, end = all, bottom = all)
            fun symmetric(horizontal: DpVal = DpVal.Zero, vertical: DpVal = DpVal.Zero) =
                Padding(start = horizontal, top = vertical, end = horizontal, bottom = vertical)
        }
    }

    @Serializable
    @SerialName("offset")
    data class Offset(val x: DpVal = DpVal.Zero, val y: DpVal = DpVal.Zero) : ModifierDef

    // ========================================================================
    // 3. Drawing, Appearance & Shapes
    // ========================================================================

    @Serializable
    @SerialName("background")
    data class Background(
        val color: ColorSource,
        val shape: ShapeDef? = null
    ) : ModifierDef

    @Serializable
    @SerialName("border")
    data class Border(
        val border: BorderDef,
        val shape: ShapeDef? = null
    ) : ModifierDef

    @Serializable
    @SerialName("clip")
    data class Clip(val shape: ShapeDef) : ModifierDef

    @Serializable
    @SerialName("shadow")
    data class Shadow(
        val elevation: DpVal,
        val shape: ShapeDef? = null,
        val clip: Boolean = false
    ) : ModifierDef

    @Serializable
    @SerialName("alpha")
    data class Alpha(val alpha: Float) : ModifierDef {
        init {
            require(alpha in 0f..1f) { "Alpha must be between 0.0 and 1.0, was $alpha" }
        }
    }

    // ========================================================================
    // 4. Interaction
    // ========================================================================

    @Serializable
    @SerialName("clickable")
    data class Clickable(
        val enabled: Boolean = true,
        val onClickLabel: String? = null
    ) : ModifierDef

    // ========================================================================
    // 5. Scoped Modifiers
    // ========================================================================

    @Serializable
    sealed interface RowScopeModifier : ModifierDef {
        override val requiredScope: ContainerScope get() = ContainerScope.Row

        @Serializable
        @SerialName("row_weight")
        data class Weight(val weight: Float, val fill: Boolean = true) : RowScopeModifier

        @Serializable
        @SerialName("row_align")
        data class Align(val alignment: AlignmentVerticalDef) : RowScopeModifier
    }

    @Serializable
    sealed interface ColumnScopeModifier : ModifierDef {
        override val requiredScope: ContainerScope get() = ContainerScope.Column

        @Serializable
        @SerialName("column_weight")
        data class Weight(val weight: Float, val fill: Boolean = true) : ColumnScopeModifier

        @Serializable
        @SerialName("column_align")
        data class Align(val alignment: AlignmentHorizontalDef) : ColumnScopeModifier
    }

    @Serializable
    sealed interface BoxScopeModifier : ModifierDef {
        override val requiredScope: ContainerScope get() = ContainerScope.Box

        @Serializable
        @SerialName("box_align")
        data class Align(val alignment: AlignmentDef) : BoxScopeModifier
    }
}