package dev.chandradsl.m3c.core.domain.command

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.allDirectChildren
import dev.chandradsl.m3c.core.domain.schema.childrenWithSlots
import dev.chandradsl.m3c.core.domain.store.HistoryTimelineItem
import dev.chandradsl.m3c.core.domain.store.TreeMutator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents a reversible semantic editing command performed on the UI AST document.
 */
sealed interface EditorCommand {
    val description: String

    /**
     * Executes the command against [root] and returns the mutated root AST.
     */
    fun execute(root: ComposableNode): ComposableNode

    /**
     * Creates the exact inverse command that reverses this command's mutations.
     */
    fun inverse(root: ComposableNode): EditorCommand

    /**
     * Optional coalescing/merging of consecutive commands (e.g. rapid keystrokes or modifier changes).
     */
    fun mergeWith(next: EditorCommand): EditorCommand? = null
}

/**
 * Inserts [node] into [parentId] at [index].
 */
data class InsertNodeCommand(
    val parentId: NodeId,
    val node: ComposableNode,
    val index: Int = -1
) : EditorCommand {
    override val description: String
        get() = "Insert ${node::class.simpleName?.replace("Node", "")}"

    override fun execute(root: ComposableNode): ComposableNode =
        TreeMutator.insertChild(root, parentId, node, index)

    override fun inverse(root: ComposableNode): EditorCommand =
        DeleteNodeCommand(
            targetId = node.id,
            savedParentId = parentId,
            savedNode = node,
            savedIndex = index
        )
}

/**
 * Deletes node [targetId] from the tree, saving its state and parent context for full restoration upon inverse.
 */
data class DeleteNodeCommand(
    val targetId: NodeId,
    val savedParentId: NodeId? = null,
    val savedNode: ComposableNode? = null,
    val savedIndex: Int = -1,
    val savedSlotName: String? = null
) : EditorCommand {
    override val description: String
        get() = "Delete ${savedNode?.let { it::class.simpleName?.replace("Node", "") } ?: "Node"}"

    override fun execute(root: ComposableNode): ComposableNode =
        TreeMutator.removeNode(root, targetId) ?: root

    override fun inverse(root: ComposableNode): EditorCommand {
        val parentId = savedParentId
        val node = savedNode
        if (parentId != null && node != null) {
            if (savedSlotName != null) {
                return SetSlotCommand(
                    parentId = parentId,
                    slotName = savedSlotName,
                    node = node,
                    previousNode = null
                )
            }
            return InsertNodeCommand(
                parentId = parentId,
                node = node,
                index = savedIndex
            )
        }
        return this
    }

    companion object {
        /**
         * Factory that inspects [root] before deletion to capture parent ID, index, and slot context.
         */
        fun create(root: ComposableNode, targetId: NodeId): DeleteNodeCommand {
            val parent = TreeMutator.findParent(root, targetId)
            val target = TreeMutator.findNode(root, targetId)
            var slotName: String? = null
            var index = -1

            if (parent != null && target != null) {
                val slotted = parent.childrenWithSlots().find { it.first.id == targetId }
                if (slotted?.second != null) {
                    slotName = slotted.second?.id
                } else {
                    index = parent.allDirectChildren.indexOfFirst { it.id == targetId }
                }
            }

            return DeleteNodeCommand(
                targetId = targetId,
                savedParentId = parent?.id,
                savedNode = target,
                savedIndex = index,
                savedSlotName = slotName
            )
        }
    }
}

/**
 * Replaces a node's properties while preserving its ID and tree structure.
 */
data class UpdateNodeCommand(
    val node: ComposableNode,
    val previousNode: ComposableNode? = null
) : EditorCommand {
    override val description: String
        get() = "Update ${node::class.simpleName?.replace("Node", "")}"

    override fun execute(root: ComposableNode): ComposableNode =
        TreeMutator.updateNode(root, node)

    override fun inverse(root: ComposableNode): EditorCommand {
        val old = previousNode ?: TreeMutator.findNode(root, node.id)
        return UpdateNodeCommand(node = old ?: node, previousNode = node)
    }

    override fun mergeWith(next: EditorCommand): EditorCommand? {
        if (next is UpdateNodeCommand && next.node.id == node.id) {
            // Retain original previousNode from first edit, and newest node from next edit
            return UpdateNodeCommand(node = next.node, previousNode = this.previousNode)
        }
        return null
    }

    companion object {
        fun create(root: ComposableNode, newNode: ComposableNode): UpdateNodeCommand {
            val oldNode = TreeMutator.findNode(root, newNode.id)
            return UpdateNodeCommand(node = newNode, previousNode = oldNode)
        }
    }
}

/**
 * Replaces the modifier list on a specific node.
 */
data class UpdateModifiersCommand(
    val targetId: NodeId,
    val modifiers: List<ModifierDef>,
    val previousModifiers: List<ModifierDef>? = null
) : EditorCommand {
    override val description: String
        get() = "Update Modifiers"

    override fun execute(root: ComposableNode): ComposableNode =
        TreeMutator.updateModifiers(root, targetId, modifiers)

    override fun inverse(root: ComposableNode): EditorCommand {
        val oldModifiers = previousModifiers ?: TreeMutator.findNode(root, targetId)?.modifiers ?: emptyList()
        return UpdateModifiersCommand(
            targetId = targetId,
            modifiers = oldModifiers,
            previousModifiers = modifiers
        )
    }

    override fun mergeWith(next: EditorCommand): EditorCommand? {
        if (next is UpdateModifiersCommand && next.targetId == targetId) {
            return UpdateModifiersCommand(
                targetId = targetId,
                modifiers = next.modifiers,
                previousModifiers = this.previousModifiers
            )
        }
        return null
    }

    companion object {
        fun create(root: ComposableNode, targetId: NodeId, newModifiers: List<ModifierDef>): UpdateModifiersCommand {
            val oldNode = TreeMutator.findNode(root, targetId)
            return UpdateModifiersCommand(
                targetId = targetId,
                modifiers = newModifiers,
                previousModifiers = oldNode?.modifiers
            )
        }
    }
}

/**
 * Sets or clears a named slot on [parentId].
 */
data class SetSlotCommand(
    val parentId: NodeId,
    val slotName: String,
    val node: ComposableNode?,
    val previousNode: ComposableNode? = null
) : EditorCommand {
    override val description: String
        get() = if (node != null) "Set Slot $slotName" else "Clear Slot $slotName"

    override fun execute(root: ComposableNode): ComposableNode =
        TreeMutator.setSlot(root, parentId, slotName, node)

    override fun inverse(root: ComposableNode): EditorCommand =
        SetSlotCommand(
            parentId = parentId,
            slotName = slotName,
            node = previousNode,
            previousNode = node
        )

    companion object {
        fun create(root: ComposableNode, parentId: NodeId, slotName: String, newNode: ComposableNode?): SetSlotCommand {
            val parent = TreeMutator.findNode(root, parentId)
            val currentSlotChild = parent?.childrenWithSlots()
                ?.find { it.second?.id.equals(slotName, ignoreCase = true) }
                ?.first
            return SetSlotCommand(
                parentId = parentId,
                slotName = slotName,
                node = newNode,
                previousNode = currentSlotChild
            )
        }
    }
}

/**
 * A sequence of commands executed as a single undoable/redoable atomic batch.
 */
data class CompositeCommand(
    override val description: String,
    val commands: List<EditorCommand>
) : EditorCommand {
    override fun execute(root: ComposableNode): ComposableNode =
        commands.fold(root) { acc, cmd -> cmd.execute(acc) }

    override fun inverse(root: ComposableNode): EditorCommand =
        CompositeCommand(
            description = "Undo $description",
            commands = commands.asReversed().map { it.inverse(root) }
        )
}

/**
 * Manages the undo and redo history for an editor document using semantic [EditorCommand]s.
 * Replaces full-tree cloning with inverse command execution.
 */
class CommandHistory(
    private val maxHistorySize: Int = 100
) {
    private val mutex = Mutex()
    private val undoStack = ArrayDeque<EditorCommand>()
    private val redoStack = ArrayDeque<EditorCommand>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    val lastCommandDescription: String?
        get() = undoStack.lastOrNull()?.description

    val lastUndoDescription: String?
        get() = undoStack.lastOrNull()?.description

    val nextRedoDescription: String?
        get() = redoStack.lastOrNull()?.description

    /**
     * Executes [command] against [currentRoot], recording it in the undo stack.
     * Clears the redo stack upon new user actions.
     */
    suspend fun execute(command: EditorCommand, currentRoot: ComposableNode): ComposableNode = mutex.withLock {
        val newRoot = command.execute(currentRoot)
        if (newRoot != currentRoot) {
            // Try coalescing with previous command
            val previous = undoStack.lastOrNull()
            val merged = previous?.mergeWith(command)
            if (merged != null) {
                undoStack.removeLast()
                undoStack.addLast(merged)
            } else {
                if (undoStack.size >= maxHistorySize) {
                    undoStack.removeFirst()
                }
                undoStack.addLast(command)
            }
            redoStack.clear()
        }
        newRoot
    }

    /**
     * Undoes the last command by executing its inverse.
     */
    suspend fun undo(currentRoot: ComposableNode): ComposableNode = mutex.withLock {
        if (undoStack.isEmpty()) return@withLock currentRoot
        val command = undoStack.removeLast()
        val inverseCommand = command.inverse(currentRoot)
        val undoneRoot = inverseCommand.execute(currentRoot)
        redoStack.addLast(command)
        undoneRoot
    }

    /**
     * Redoes the previously undone command.
     */
    suspend fun redo(currentRoot: ComposableNode): ComposableNode = mutex.withLock {
        if (redoStack.isEmpty()) return@withLock currentRoot
        val command = redoStack.removeLast()
        val redoneRoot = command.execute(currentRoot)
        undoStack.addLast(command)
        redoneRoot
    }

    /**
     * Jumps directly to an arbitrary step index in the command history timeline.
     * Step 0 represents the baseline initial document.
     * Step N represents the state after applying the N-th command in the undo stack.
     */
    suspend fun jumpTo(targetStep: Int, currentRoot: ComposableNode): ComposableNode = mutex.withLock {
        var root = currentRoot
        val currentStep = undoStack.size
        if (targetStep < currentStep) {
            val stepsToUndo = currentStep - targetStep
            repeat(stepsToUndo) {
                if (undoStack.isNotEmpty()) {
                    val command = undoStack.removeLast()
                    val inverseCommand = command.inverse(root)
                    root = inverseCommand.execute(root)
                    redoStack.addLast(command)
                }
            }
        } else if (targetStep > currentStep) {
            val stepsToRedo = targetStep - currentStep
            repeat(stepsToRedo) {
                if (redoStack.isNotEmpty()) {
                    val command = redoStack.removeLast()
                    root = command.execute(root)
                    undoStack.addLast(command)
                }
            }
        }
        root
    }

    /**
     * Produces a snapshot of the chronological timeline (past, current, and future/redoable steps).
     */
    fun getTimelineSnapshot(): List<HistoryTimelineItem> {
        val items = mutableListOf<HistoryTimelineItem>()
        val currentStep = undoStack.size

        items.add(
            HistoryTimelineItem(
                stepIndex = 0,
                description = "Initial Screen",
                isCurrent = currentStep == 0,
                isFuture = false
            )
        )

        undoStack.forEachIndexed { index, cmd ->
            val step = index + 1
            items.add(
                HistoryTimelineItem(
                    stepIndex = step,
                    description = cmd.description,
                    isCurrent = step == currentStep,
                    isFuture = false
                )
            )
        }

        val futureList = redoStack.reversed()
        futureList.forEachIndexed { index, cmd ->
            val step = currentStep + index + 1
            items.add(
                HistoryTimelineItem(
                    stepIndex = step,
                    description = cmd.description,
                    isCurrent = false,
                    isFuture = true
                )
            )
        }

        return items
    }

    fun snapshotStacks(): Pair<List<EditorCommand>, List<EditorCommand>> {
        return undoStack.toList() to redoStack.toList()
    }

    fun restoreStacks(undo: List<EditorCommand>, redo: List<EditorCommand>) {
        undoStack.clear()
        undoStack.addAll(undo)
        redoStack.clear()
        redoStack.addAll(redo)
    }

    suspend fun clear() = mutex.withLock {
        undoStack.clear()
        redoStack.clear()
    }
}
