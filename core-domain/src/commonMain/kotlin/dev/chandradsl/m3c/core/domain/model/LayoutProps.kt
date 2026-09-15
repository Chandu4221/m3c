package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class VerticalArrangementDef {
    Top, Bottom, Center, SpaceBetween, SpaceAround, SpaceEvenly
}

@Serializable
enum class HorizontalArrangementDef {
    Start, End, Center, SpaceBetween, SpaceAround, SpaceEvenly
}

@Serializable
enum class AlignmentHorizontalDef {
    Start, CenterHorizontally, End
}

@Serializable
enum class AlignmentVerticalDef {
    Top, CenterVertically, Bottom
}