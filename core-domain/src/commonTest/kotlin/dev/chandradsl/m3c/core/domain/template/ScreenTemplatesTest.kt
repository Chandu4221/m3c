package dev.chandradsl.m3c.core.domain.template

import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.M3cProject
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import dev.chandradsl.m3c.core.domain.storage.M3cProjectSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScreenTemplatesTest {

    @Test
    fun testAllTemplatesDefinedAndDistinct() {
        val templates = ScreenTemplates.all
        assertEquals(5, templates.size)

        val ids = templates.map { it.id }.toSet()
        assertEquals(5, ids.size, "Template IDs must all be unique")

        val names = templates.map { it.name }.toSet()
        assertEquals(5, names.size, "Template names must all be unique")

        val routes = templates.map { it.defaultRoute }.toSet()
        assertEquals(5, routes.size, "Template routes must all be unique")

        for (template in templates) {
            assertTrue(template.description.isNotBlank(), "Description for ${template.name} should not be blank")
            assertNotNull(ScreenTemplates.findById(template.id), "Should find ${template.id} by findById")
        }
    }

    @Test
    fun testTemplateRootsAreScaffoldsAndIdsAreUniqueAcrossGenerations() {
        for (template in ScreenTemplates.all) {
            val root1 = template.createRoot()
            val root2 = template.createRoot()

            assertTrue(root1 is ComposableNode.ScaffoldNode, "Template ${template.name} should have ScaffoldNode root")
            assertTrue(root2 is ComposableNode.ScaffoldNode, "Template ${template.name} should have ScaffoldNode root")

            // Ensure unique IDs generated per call
            assertTrue(root1.id != root2.id, "Subsequent calls to createRoot should generate unique node IDs")
        }
    }

    @Test
    fun testTemplatesRoundTripSerialization() {
        val screens = ScreenTemplates.all.mapIndexed { index, template ->
            M3cScreen(
                id = "screen_${template.id}",
                name = template.name,
                route = template.defaultRoute,
                rootNode = template.createRoot(),
                isStartDestination = index == 0
            )
        }

        val project = M3cProject(
            name = "TemplateProject",
            packageName = "dev.chandradsl.templates",
            screens = screens,
            activeScreenId = screens.first().id
        )

        val json = M3cProjectSerializer.encode(project)
        assertTrue(json.isNotBlank())

        val decoded = M3cProjectSerializer.decode(json)
        assertEquals(5, decoded.screens.size)
        assertEquals("TemplateProject", decoded.name)
        assertEquals("Login", decoded.screens[0].name)
        assertEquals("Profile", decoded.screens[1].name)
        assertEquals("Settings", decoded.screens[2].name)
        assertEquals("Feed", decoded.screens[3].name)
        assertEquals("Checkout", decoded.screens[4].name)
    }
}
