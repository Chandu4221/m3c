package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ============================================================================
// 1. Identifiers & Dimension Primitives
// ============================================================================

@Serializable
@JvmInline
value class NodeId(val value: String) {
    init {
        require(value.isNotBlank()) { "NodeId cannot be blank" }
        require(value.matches(VALID_ID_REGEX)) {
            "Invalid NodeId '$value'. Must contain only alphanumeric characters, underscores, or hyphens."
        }
    }

    override fun toString(): String = value

    companion object {
        private val VALID_ID_REGEX = Regex("^[a-zA-Z0-9_-]+$")

        @OptIn(ExperimentalUuidApi::class)
        fun generate(prefix: String): NodeId {
            require(prefix.isNotBlank()) { "NodeId prefix cannot be blank" }
            require(prefix.matches(VALID_ID_REGEX)) { "Invalid prefix '$prefix'" }
            val shortUuid = Uuid.random().toHexString().take(8)
            return NodeId("${prefix}_$shortUuid")
        }
    }
}

@Serializable
@JvmInline
value class DpVal(val value: Float) {
    init {
        require(!value.isNaN()) { "Dp value cannot be NaN" }
        require(value >= 0f) { "Dp value cannot be negative, was $value" }
    }

    override fun toString(): String = "${value}.dp"

    companion object {
        val Zero = DpVal(0f)
    }
}

@Serializable
@JvmInline
value class SpVal(val value: Float) {
    init {
        require(!value.isNaN()) { "Sp value cannot be NaN" }
        require(value > 0f) { "Text size (Sp) must be strictly positive (> 0), was $value" }
    }

    override fun toString(): String = "${value}.sp"
}

@Serializable
@JvmInline
value class ColorHex(val value: Long) {
    init {
        // ARGB 32-bit integer boundary (0x00000000 to 0xFFFFFFFF)
        require(value in 0x00000000L..0xFFFFFFFFL) {
            "ColorHex value must be a valid 32-bit ARGB hex (0x00000000..0xFFFFFFFF), was 0x${value.toString(16).uppercase()}"
        }
    }

    override fun toString(): String = "0x${value.toString(16).padStart(8, '0').uppercase()}"

    companion object {
        val Black = ColorHex(0xFF000000L)
        val White = ColorHex(0xFFFFFFFFL)
        val Transparent = ColorHex(0x00000000L)
    }
}

// ============================================================================
// 2. Material 3 Color Roles & Source (Theme Token vs Custom Hex)
// ============================================================================

@Serializable
enum class ColorToken {
    Primary,
    OnPrimary,
    PrimaryContainer,
    OnPrimaryContainer,
    Secondary,
    OnSecondary,
    SecondaryContainer,
    OnSecondaryContainer,
    Tertiary,
    OnTertiary,
    TertiaryContainer,
    OnTertiaryContainer,
    Background,
    OnBackground,
    Surface,
    OnSurface,
    SurfaceVariant,
    OnSurfaceVariant,
    SurfaceContainerLowest,
    SurfaceContainerLow,
    SurfaceContainer,
    SurfaceContainerHigh,
    SurfaceContainerHighest,
    InverseSurface,
    InverseOnSurface,
    InversePrimary,
    Error,
    OnError,
    ErrorContainer,
    OnErrorContainer,
    Outline,
    OutlineVariant,
    Scrim
}

@Serializable
sealed interface ColorSource {
    @Serializable
    data class Theme(val token: ColorToken) : ColorSource

    @Serializable
    data class Custom(val hex: ColorHex) : ColorSource
}

// ============================================================================
// 3. Material 3 Typography Scale
// ============================================================================

@Serializable
enum class TypographyToken {
    DisplayLarge,
    DisplayMedium,
    DisplaySmall,
    HeadlineLarge,
    HeadlineMedium,
    HeadlineSmall,
    TitleLarge,
    TitleMedium,
    TitleSmall,
    BodyLarge,
    BodyMedium,
    BodySmall,
    LabelLarge,
    LabelMedium,
    LabelSmall
}

// ============================================================================
// 4. Material 3 Shapes & Borders
// ============================================================================

@Serializable
enum class ShapeToken {
    None,
    ExtraSmall,
    Small,
    Medium,
    Large,
    ExtraLarge,
    Full
}

@Serializable
sealed interface ShapeDef {
    @Serializable
    data class Token(val token: ShapeToken) : ShapeDef

    @Serializable
    data class Rounded(val cornerRadius: DpVal) : ShapeDef

    @Serializable
    data object Rectangle : ShapeDef
}

@Serializable
data class BorderDef(
    val width: DpVal,
    val color: ColorSource
)