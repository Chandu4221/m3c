package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Defines the relative or container position where a dragged node or palette item will be dropped.
 */
@Serializable
enum class TreeDropPosition {
    /** Drop inside a container node (e.g. Column, Row, Box, Scaffold). */
    INSIDE,
    /** Insert immediately before the target sibling node. */
    ABOVE,
    /** Insert immediately after the target sibling node. */
    BELOW;

    val isRelative: Boolean get() = this == ABOVE || this == BELOW
}
