package dev.chandradsl.m3c.core.domain.scope

import dev.chandradsl.m3c.core.domain.model.ComposableNode

/**
 * Represents the Compose layout scope provided to child composables.
 *
 * In Jetpack Compose / Material 3:
 * - `Row` provides [RowScope] (supporting `.weight()`, `.align()`).
 * - `Column` provides [ColumnScope] (supporting `.weight()`, `.align()`).
 * - `Box` provides [BoxScope] (supporting `.align()`).
 * - Standalone nodes or containers with unadorned content lambdas have [None].
 */
enum class ContainerScope {
    None,
    Row,
    Column,
    Box;

    val isRow: Boolean get() = this == Row
    val isColumn: Boolean get() = this == Column
    val isBox: Boolean get() = this == Box
}

/**
 * Returns the layout receiver scope that this container provides to its immediate children.
 */
val ComposableNode.childScope: ContainerScope
    get() = when (this) {
        is ComposableNode.RowNode -> ContainerScope.Row
        is ComposableNode.ButtonNode,
        is ComposableNode.ElevatedButtonNode,
        is ComposableNode.FilledTonalButtonNode,
        is ComposableNode.OutlinedButtonNode,
        is ComposableNode.TextButtonNode -> ContainerScope.Row

        is ComposableNode.ColumnNode -> ContainerScope.Column
        is ComposableNode.CardNode,
        is ComposableNode.ElevatedCardNode,
        is ComposableNode.OutlinedCardNode -> ContainerScope.Column

        is ComposableNode.BoxNode -> ContainerScope.Box

        else -> ContainerScope.None
    }
