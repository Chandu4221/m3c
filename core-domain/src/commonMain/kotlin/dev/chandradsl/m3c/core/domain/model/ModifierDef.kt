package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface ModifierDef {
    @Serializable
    data class Padding(
        val start: Float = 0f,
        val top: Float = 0f,
        val end: Float = 0f,
        val bottom: Float = 0f
    ) : ModifierDef

    @Serializable
    data class FillMaxWidth(val fraction: Float = 1.0f) : ModifierDef

    @Serializable
    data class FillMaxHeight(val fraction: Float = 1.0f) : ModifierDef

    @Serializable
    data class FillMaxSize(val fraction: Float = 1.0f) : ModifierDef

    @Serializable
    data class Size(val width: Float, val height: Float) : ModifierDef

    @Serializable
    data class Width(val width: Float) : ModifierDef

    @Serializable
    data class Height(val height: Float) : ModifierDef

    @Serializable
    data class Background(val colorHex: Long) : ModifierDef

    @Serializable
    data class Clip(val cornerRadius: Float) : ModifierDef

    @Serializable
    data class Weight(val weight: Float, val fill: Boolean = true) : ModifierDef
}