package dev.chandradsl.m3c.app.desktop.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import dev.chandradsl.m3c.app.desktop.state.StudioViewModel
import dev.chandradsl.m3c.core.domain.model.M3cProject
import dev.chandradsl.m3c.core.domain.model.M3cScreen
import dev.chandradsl.m3c.core.domain.storage.M3cProjectSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/**
 * Embedded Zero-Dependency Hot-Reload Server.
 * Exposes live REST endpoints and Server-Sent Events (SSE) streaming for companion
 * Android preview devices, emulators, and external preview clients.
 */
class HotReloadServer(
    private val viewModel: StudioViewModel,
    initialPort: Int = 8989
) {
    private var server: HttpServer? = null
    var port: Int = initialPort
        private set

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _clientCount = MutableStateFlow(0)
    val clientCount: StateFlow<Int> = _clientCount.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val activeSseClients = CopyOnWriteArrayList<OutputStream>()
    private val executor = Executors.newCachedThreadPool()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun log(message: String) {
        val timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        val entry = "[$timestamp] $message"
        _logs.value = (_logs.value + entry).takeLast(100)
    }

    fun start(): Boolean {
        if (_isRunning.value) return true
        return try {
            val address = InetSocketAddress(port)
            val newServer = HttpServer.create(address, 0)
            configureRoutes(newServer)
            newServer.executor = executor
            newServer.start()
            server = newServer
            _isRunning.value = true
            log("Hot-Reload Server online at http://localhost:$port")
            true
        } catch (e: Exception) {
            log("Failed to start server on port $port: ${e.message}")
            _isRunning.value = false
            false
        }
    }

    fun stop() {
        try {
            activeSseClients.forEach { stream ->
                try { stream.close() } catch (_: Exception) {}
            }
            activeSseClients.clear()
            _clientCount.value = 0
            server?.stop(0)
            server = null
            _isRunning.value = false
            log("Hot-Reload Server stopped.")
        } catch (e: Exception) {
            log("Error stopping server: ${e.message}")
        }
    }

    fun restart(newPort: Int): Boolean {
        stop()
        this.port = newPort
        return start()
    }

    private fun configureRoutes(httpServer: HttpServer) {
        // GET /api/status
        httpServer.createContext("/api/status", HttpHandler { exchange ->
            addCorsHeaders(exchange)
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                exchange.sendResponseHeaders(204, -1)
                return@HttpHandler
            }
            val status = buildJsonObject {
                put("status", "online")
                put("projectName", viewModel.projectName)
                put("packageName", viewModel.packageName)
                put("screensCount", viewModel.screens.size)
                put("activeScreenId", viewModel.activeScreenId)
                put("activeScreenName", viewModel.activeScreen?.name ?: "")
                put("clientsConnected", activeSseClients.size)
                put("port", port)
                put("timestamp", System.currentTimeMillis())
            }.toString()
            respondJson(exchange, status)
        })

        // GET /api/project
        httpServer.createContext("/api/project", HttpHandler { exchange ->
            addCorsHeaders(exchange)
            val project = M3cProject(
                name = viewModel.projectName,
                packageName = viewModel.packageName,
                screens = viewModel.screens,
                activeScreenId = viewModel.activeScreenId
            )
            val body = M3cProjectSerializer.encode(project)
            respondJson(exchange, body)
        })

        // GET /api/screens
        httpServer.createContext("/api/screens", HttpHandler { exchange ->
            addCorsHeaders(exchange)
            val body = json.encodeToString(viewModel.screens)
            respondJson(exchange, body)
        })

        // GET /api/screens/active
        httpServer.createContext("/api/screens/active", HttpHandler { exchange ->
            addCorsHeaders(exchange)
            val active = viewModel.activeScreen
            if (active != null) {
                val body = json.encodeToString(active)
                respondJson(exchange, body)
            } else {
                respondError(exchange, 404, "No active screen selected")
            }
        })

        // GET /api/code
        httpServer.createContext("/api/code", HttpHandler { exchange ->
            addCorsHeaders(exchange)
            val code = viewModel.generatedCode
            val response = buildJsonObject {
                put("screenName", viewModel.activeScreen?.name ?: "Screen")
                put("code", code)
            }.toString()
            respondJson(exchange, response)
        })

        // GET /api/live (Server-Sent Events)
        httpServer.createContext("/api/live", HttpHandler { exchange ->
            addCorsHeaders(exchange)
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                exchange.sendResponseHeaders(204, -1)
                return@HttpHandler
            }

            val headers = exchange.responseHeaders
            headers.set("Content-Type", "text/event-stream; charset=UTF-8")
            headers.set("Cache-Control", "no-cache, no-transform")
            headers.set("Connection", "keep-alive")
            headers.set("Access-Control-Allow-Origin", "*")

            // 0 indicates chunked transfer encoding in HttpServer
            exchange.sendResponseHeaders(200, 0)
            val os = exchange.responseBody
            activeSseClients.add(os)
            _clientCount.value = activeSseClients.size

            val remoteHost = exchange.remoteAddress?.hostString ?: "unknown"
            log("Companion client connected from $remoteHost (total: ${activeSseClients.size})")

            try {
                // Send initial sync event
                val initEvent = buildJsonObject {
                    put("event", "connected")
                    put("activeScreenId", viewModel.activeScreenId)
                    put("activeScreenName", viewModel.activeScreen?.name ?: "")
                    put("timestamp", System.currentTimeMillis())
                }.toString()
                os.write("data: $initEvent\n\n".toByteArray(StandardCharsets.UTF_8))
                os.flush()
            } catch (e: Exception) {
                activeSseClients.remove(os)
                _clientCount.value = activeSseClients.size
                log("Companion client disconnected prematurely: ${e.message}")
            }
        })

        // POST /api/intent
        httpServer.createContext("/api/intent", HttpHandler { exchange ->
            addCorsHeaders(exchange)
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                exchange.sendResponseHeaders(204, -1)
                return@HttpHandler
            }
            if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                respondError(exchange, 405, "Method Not Allowed")
                return@HttpHandler
            }
            try {
                val bodyStr = exchange.requestBody.bufferedReader(StandardCharsets.UTF_8).readText()
                log("Received remote intent: $bodyStr")
                val response = buildJsonObject {
                    put("status", "received")
                    put("timestamp", System.currentTimeMillis())
                }.toString()
                respondJson(exchange, response)
            } catch (e: Exception) {
                respondError(exchange, 400, "Bad Request: ${e.message}")
            }
        })
    }

    fun broadcastUpdate() {
        if (activeSseClients.isEmpty()) return
        val active = viewModel.activeScreen ?: return
        val code = viewModel.generatedCode

        val eventPayload = buildJsonObject {
            put("event", "screen_update")
            put("screenId", active.id)
            put("screenName", active.name)
            put("route", active.route)
            put("timestamp", System.currentTimeMillis())
            put("code", code)
        }.toString()

        val raw = "data: $eventPayload\n\n".toByteArray(StandardCharsets.UTF_8)
        val deadClients = mutableListOf<OutputStream>()

        for (client in activeSseClients) {
            try {
                client.write(raw)
                client.flush()
            } catch (e: IOException) {
                deadClients.add(client)
            }
        }

        if (deadClients.isNotEmpty()) {
            activeSseClients.removeAll(deadClients)
            _clientCount.value = activeSseClients.size
            log("Cleaned up ${deadClients.size} disconnected clients (remaining: ${activeSseClients.size})")
        } else {
            log("Pushed live screen update '${active.name}' to ${activeSseClients.size} client(s)")
        }
    }

    private fun addCorsHeaders(exchange: HttpExchange) {
        val headers = exchange.responseHeaders
        headers.set("Access-Control-Allow-Origin", "*")
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization")
    }

    private fun respondJson(exchange: HttpExchange, json: String, status: Int = 200) {
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun respondError(exchange: HttpExchange, status: Int, message: String) {
        val body = buildJsonObject {
            put("error", message)
            put("status", status)
        }.toString()
        respondJson(exchange, body, status)
    }

    fun getLocalNetworkIp(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            var fallback = "127.0.0.1"
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr.hostAddress.indexOf(':') == -1) {
                        return addr.hostAddress
                    }
                }
            }
            fallback
        } catch (_: Exception) {
            "127.0.0.1"
        }
    }
}
