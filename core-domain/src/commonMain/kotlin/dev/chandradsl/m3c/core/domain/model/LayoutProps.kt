package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ArrangementVerticalDef {
    Top, Bottom, Center, SpaceBetween, SpaceAround, SpaceEvenly
}

@Serializable
enum class ArrangementHorizontalDef {
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

@Serializable
enum class AlignmentDef {
    TopStart,
    TopCenter,
    TopEnd,
    CenterStart,
    Center,
    CenterEnd,
    BottomStart,
    BottomCenter,
    BottomEnd
}