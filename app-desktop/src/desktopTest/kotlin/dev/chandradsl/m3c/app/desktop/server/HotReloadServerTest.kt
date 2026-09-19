package dev.chandradsl.m3c.app.desktop.server

import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HotReloadServerTest {

    private lateinit var viewModel: StudioViewModel
    private lateinit var server: HotReloadServer
    private val testPort = 18989

    @BeforeTest
    fun setup() {
        viewModel = StudioViewModel()
        server = HotReloadServer(viewModel, initialPort = testPort)
    }

    @AfterTest
    fun tearDown() {
        server.stop()
    }

    @Test
    fun testServerLifecycle() {
        assertFalse(server.isRunning.value)
        assertTrue(server.start())
        assertTrue(server.isRunning.value)
        assertEquals(testPort, server.port)

        server.stop()
        assertFalse(server.isRunning.value)
    }

    @Test
    fun testStatusEndpoint() {
        assertTrue(server.start())

        val url = URI("http://localhost:$testPort/api/status").toURL()
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        assertEquals(200, conn.responseCode)

        val responseBody = conn.inputStream.bufferedReader().readText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("online", json["status"]?.jsonPrimitive?.content)
        assertEquals(testPort, json["port"]?.jsonPrimitive?.content?.toInt())
        assertNotNull(json["activeScreenId"]?.jsonPrimitive?.content)
        assertEquals("MainScreen", json["activeScreenName"]?.jsonPrimitive?.content)
    }

    @Test
    fun testScreensAndActiveEndpoints() {
        assertTrue(server.start())

        // Test /api/screens
        val screensUrl = URI("http://localhost:$testPort/api/screens").toURL()
        val screensConn = screensUrl.openConnection() as HttpURLConnection
        assertEquals(200, screensConn.responseCode)
        val screensBody = screensConn.inputStream.bufferedReader().readText()
        assertTrue(screensBody.contains("MainScreen"))

        // Test /api/screens/active
        val activeUrl = URI("http://localhost:$testPort/api/screens/active").toURL()
        val activeConn = activeUrl.openConnection() as HttpURLConnection
        assertEquals(200, activeConn.responseCode)
        val activeBody = activeConn.inputStream.bufferedReader().readText()
        assertTrue(activeBody.contains("MainScreen"))
        assertTrue(activeBody.contains("scaffold_root"))
    }

    @Test
    fun testCodeEndpoint() {
        assertTrue(server.start())

        val codeUrl = URI("http://localhost:$testPort/api/code").toURL()
        val codeConn = codeUrl.openConnection() as HttpURLConnection
        assertEquals(200, codeConn.responseCode)
        val codeBody = codeConn.inputStream.bufferedReader().readText()
        assertTrue(codeBody.contains("MainScreen"))
        assertTrue(codeBody.contains("code"))
    }

    @Test
    fun testLiveSseStreamingAndBroadcast() = runTest {
        assertTrue(server.start())
        assertEquals(0, server.clientCount.value)

        val liveUrl = URI("http://localhost:$testPort/api/live").toURL()
        val sseConn = liveUrl.openConnection() as HttpURLConnection
        sseConn.requestMethod = "GET"
        sseConn.setRequestProperty("Accept", "text/event-stream")

        val reader = BufferedReader(InputStreamReader(sseConn.inputStream))

        // Read initial handshake event
        val initialLine = reader.readLine()
        assertTrue(initialLine.startsWith("data:"))
        assertTrue(initialLine.contains("connected"))
        assertEquals(1, server.clientCount.value)

        // Broadcast screen update
        server.broadcastUpdate()

        // Read broadcast event
        var updateLine: String? = null
        while (updateLine == null || updateLine.isBlank()) {
            updateLine = reader.readLine()
        }
        assertTrue(updateLine.startsWith("data:"))
        assertTrue(updateLine.contains("screen_update"))

        sseConn.disconnect()
    }

    @Test
    fun testServerRestart() {
        assertTrue(server.start())
        assertEquals(testPort, server.port)

        val newPort = 18990
        assertTrue(server.restart(newPort))
        assertEquals(newPort, server.port)
        assertTrue(server.isRunning.value)

        val statusUrl = URI("http://localhost:$newPort/api/status").toURL()
        val conn = statusUrl.openConnection() as HttpURLConnection
        assertEquals(200, conn.responseCode)
    }
}
