package dev.chandradsl.m3c.core.domain.storage

import dev.chandradsl.m3c.core.domain.model.M3cProject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Platform-independent serializer and deserializer for .m3c project files.
 */
object M3cProjectSerializer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Serializes an [M3cProject] to a formatted JSON string.
     */
    fun encode(project: M3cProject): String {
        return json.encodeToString(project)
    }

    /**
     * Deserializes an [M3cProject] from a JSON string.
     */
    fun decode(jsonString: String): M3cProject {
        return json.decodeFromString(jsonString)
    }
}
