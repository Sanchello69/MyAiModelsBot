package com.turboguys.myaimodelsbot.data.remote.mcp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class McpClient(
    private val serverUrl: String = "https://remote.mcpservers.org/fetch/mcp"
) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Session ID для последующих запросов
    private var sessionId: String? = null

    companion object {
        private const val TAG = "McpClient"

        /**
         * Парсинг SSE (Server-Sent Events) ответа
         * Извлекает JSON из строки формата "data: {...}"
         */
        private fun parseSseResponse(sseResponse: String): String? {
            val lines = sseResponse.lines()
            for (line in lines) {
                if (line.startsWith("data: ")) {
                    return line.substring(6) // Убираем "data: "
                }
            }
            return null
        }
    }

    /**
     * Инициализация соединения с MCP сервером
     */
    suspend fun initialize(): InitializeResult = suspendCancellableCoroutine { continuation ->
        val requestId = UUID.randomUUID().toString()

        val initRequest = JsonRpcRequest(
            id = requestId,
            method = "initialize",
            params = InitializeParams(
                clientInfo = ClientInfo(
                    name = "MyAiModelsBot",
                    version = "1.0.0"
                )
            )
        )

        val json = gson.toJson(initRequest)
        Log.d(TAG, "Sending initialize request: $json")

        val requestBody = json.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")

        // Добавляем session ID если есть
        sessionId?.let { requestBuilder.header("Mcp-Session-Id", it) }

        val request = requestBuilder.build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()

                Log.d(TAG, "Initialize response status: ${response.code} ${response.message}")
                Log.d(TAG, "Initialize response headers: ${response.headers}")
                Log.d(TAG, "Initialize response body: $responseBody")

                if (!response.isSuccessful) {
                    val errorMessage = "HTTP error: ${response.code} ${response.message}\nBody: $responseBody"
                    Log.e(TAG, errorMessage)
                    continuation.resumeWithException(Exception(errorMessage))
                    return@suspendCancellableCoroutine
                }

                if (responseBody == null) {
                    continuation.resumeWithException(Exception("Empty response body"))
                    return@suspendCancellableCoroutine
                }

                // Парсим SSE ответ, если это SSE формат
                val jsonBody = if (responseBody.contains("data:")) {
                    val parsed = parseSseResponse(responseBody)
                    Log.d(TAG, "Parsed SSE to JSON: $parsed")
                    parsed
                } else {
                    responseBody
                }

                if (jsonBody == null) {
                    continuation.resumeWithException(
                        Exception("Failed to parse SSE response: $responseBody")
                    )
                    return@suspendCancellableCoroutine
                }

                // Извлекаем session ID из заголовков
                sessionId = response.header("Mcp-Session-Id")
                    ?: response.header("mcp-session-id")
                    ?: response.header("X-Session-Id")

                Log.d(TAG, "Session ID from headers: $sessionId")

                val responseType = object : TypeToken<JsonRpcResponse<InitializeResult>>() {}.type
                val jsonRpcResponse: JsonRpcResponse<InitializeResult> =
                    gson.fromJson(jsonBody, responseType)

                if (jsonRpcResponse.error != null) {
                    continuation.resumeWithException(
                        Exception("MCP error: ${jsonRpcResponse.error.message}")
                    )
                    return@suspendCancellableCoroutine
                }

                val result = jsonRpcResponse.result
                if (result == null) {
                    continuation.resumeWithException(Exception("No result in response"))
                    return@suspendCancellableCoroutine
                }

                continuation.resume(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialize error", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Отправка уведомления initialized после успешной инициализации
     */
    suspend fun sendInitializedNotification() = suspendCancellableCoroutine<Unit> { continuation ->
        val notification = mapOf(
            "jsonrpc" to "2.0",
            "method" to "notifications/initialized"
        )

        val json = gson.toJson(notification)
        Log.d(TAG, "Sending initialized notification: $json")

        val requestBody = json.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")

        // Добавляем session ID если есть
        sessionId?.let { requestBuilder.header("Mcp-Session-Id", it) }

        val request = requestBuilder.build()

        try {
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Initialized notification response: ${response.code}")
                // Уведомления обычно не возвращают ответ, просто проверяем успешность
                if (response.isSuccessful || response.code == 204) {
                    continuation.resume(Unit)
                } else {
                    // Некоторые серверы могут не требовать этого уведомления
                    Log.w(TAG, "Initialized notification returned ${response.code}, continuing anyway")
                    continuation.resume(Unit)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialized notification error (non-critical)", e)
            continuation.resume(Unit) // Продолжаем даже при ошибке
        }
    }

    /**
     * Получение списка доступных инструментов
     */
    suspend fun listTools(): List<McpTool> = suspendCancellableCoroutine { continuation ->
        val requestId = UUID.randomUUID().toString()

        val toolsRequest = JsonRpcRequest(
            id = requestId,
            method = "tools/list",
            params = null
        )

        val json = gson.toJson(toolsRequest)
        Log.d(TAG, "Sending tools/list request: $json")

        val requestBody = json.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")

        // Добавляем session ID если есть
        sessionId?.let { requestBuilder.header("Mcp-Session-Id", it) }

        val request = requestBuilder.build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()

                Log.d(TAG, "Tools list response status: ${response.code} ${response.message}")
                Log.d(TAG, "Tools list response headers: ${response.headers}")
                Log.d(TAG, "Tools list response body: $responseBody")

                if (!response.isSuccessful) {
                    val errorMessage = "HTTP error: ${response.code} ${response.message}\nBody: $responseBody"
                    Log.e(TAG, errorMessage)
                    continuation.resumeWithException(Exception(errorMessage))
                    return@suspendCancellableCoroutine
                }

                if (responseBody == null) {
                    continuation.resumeWithException(Exception("Empty response body"))
                    return@suspendCancellableCoroutine
                }

                // Парсим SSE ответ, если это SSE формат
                val jsonBody = if (responseBody.contains("data:")) {
                    val parsed = parseSseResponse(responseBody)
                    Log.d(TAG, "Parsed SSE to JSON: $parsed")
                    parsed
                } else {
                    responseBody
                }

                if (jsonBody == null) {
                    continuation.resumeWithException(
                        Exception("Failed to parse SSE response: $responseBody")
                    )
                    return@suspendCancellableCoroutine
                }

                val responseType = object : TypeToken<JsonRpcResponse<ListToolsResult>>() {}.type
                val jsonRpcResponse: JsonRpcResponse<ListToolsResult> =
                    gson.fromJson(jsonBody, responseType)

                if (jsonRpcResponse.error != null) {
                    continuation.resumeWithException(
                        Exception("MCP error: ${jsonRpcResponse.error.message}")
                    )
                    return@suspendCancellableCoroutine
                }

                val result = jsonRpcResponse.result
                if (result == null) {
                    continuation.resumeWithException(Exception("No result in response"))
                    return@suspendCancellableCoroutine
                }

                continuation.resume(result.tools)
            }
        } catch (e: Exception) {
            Log.e(TAG, "List tools error", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Тестовый метод для проверки доступности сервера
     */
    suspend fun testConnection(): String = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder()
            .url(serverUrl)
            .get()
            .header("Accept", "application/json, text/event-stream, */*")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: "Empty body"
                val result = """
                    Status: ${response.code} ${response.message}
                    Headers: ${response.headers}
                    Body: $responseBody
                """.trimIndent()

                Log.d(TAG, "Test connection result:\n$result")
                continuation.resume(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Test connection error", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Закрытие соединения
     */
    fun close() {
        // Очистка ресурсов если необходимо
    }
}
