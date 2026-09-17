package dev.chandradsl.m3c.core.domain.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComponentRegistryTest {

    @Test
    fun testAllComponentTypesAreRegistered() {
        val registeredTypes = ComponentRegistry.all.map { it.type }.toSet()
        for (type in ComponentType.entries) {
            assertTrue(registeredTypes.contains(type), "Missing registration for ComponentType.$type in ComponentRegistry")
        }
        assertEquals(ComponentType.entries.size, ComponentRegistry.all.size)
    }

    @Test
    fun testEveryDefinitionCreatesValidMatchingNode() {
        for (def in ComponentRegistry.all) {
            val node = def.createDefault()
            assertNotNull(node, "createDefault() should not be null for ${def.displayName}")
            assertEquals(def.type, node.componentType, "Default node componentType must match definition for ${def.displayName}")
        }
    }

    @Test
    fun testCategoriesContainComponents() {
        for (category in ComponentCategory.entries) {
            val items = ComponentRegistry.byCategory(category)
            assertTrue(items.isNotEmpty(), "Category $category must contain at least one registered component")
        }
    }

    @Test
    fun testFindByNodeResolvesCorrectDefinition() {
        for (def in ComponentRegistry.all) {
            val node = def.createDefault()
            val resolvedDef = ComponentRegistry.findByNode(node)
            assertNotNull(resolvedDef)
            assertEquals(def.type, resolvedDef.type)
            assertEquals(def.displayName, resolvedDef.displayName)
        }
    }
}
