package dev.chandradsl.m3c.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Root serializable model for a .m3c project file.
 * Designed to hold multi-screen specifications, package configuration, and metadata.
 */
@Serializable
data class M3cProject(
    val schemaVersion: Int = 1,
    val name: String = "Untitled",
    val packageName: String = "com.example.app",
    val screens: List<M3cScreen> = emptyList(),
    val activeScreenId: String = ""
)

/**
 * Represents a single distinct screen in an m3c project.
 */
@Serializable
data class M3cScreen(
    val id: String,
    val name: String,
    val route: String,
    val rootNode: ComposableNode,
    val isStartDestination: Boolean = false
)
