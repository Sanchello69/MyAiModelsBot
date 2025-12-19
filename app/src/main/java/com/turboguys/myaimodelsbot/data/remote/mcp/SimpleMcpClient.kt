package com.turboguys.myaimodelsbot.data.remote.mcp

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Упрощенный MCP клиент для работы с локальным HTTP сервером
 */
class SimpleMcpClient(
    private val baseUrl: String = "http://192.168.1.67:3000" // IP адрес Mac в локальной сети
) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "SimpleMcpClient"
    }

    data class BitcoinPrice(
        val price: Double,
        val timestamp: Long
    )

    data class McpTool(
        val name: String,
        val description: String?,
        val inputSchema: Map<String, Any>?
    )

    /**
     * Получить текущий курс биткоина
     */
    suspend fun getBitcoinPrice(): Result<BitcoinPrice> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/bitcoin/price")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Bitcoin price response: $responseBody")

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP error: ${response.code} $responseBody")
                )
            }

            if (responseBody == null) {
                return@withContext Result.failure(Exception("Empty response"))
            }

            val price = gson.fromJson(responseBody, BitcoinPrice::class.java)
            Result.success(price)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Bitcoin price", e)
            Result.failure(e)
        }
    }

    /**
     * Получить список доступных MCP инструментов
     */
    suspend fun listTools(): Result<List<McpTool>> = withContext(Dispatchers.IO) {
        try {
            val requestBody = """
                {
                    "jsonrpc": "2.0",
                    "id": "list-tools-1",
                    "method": "tools/list",
                    "params": {}
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/mcp/coincap/tools/list")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "List tools response: $responseBody")

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP error: ${response.code} $responseBody")
                )
            }

            if (responseBody == null) {
                return@withContext Result.failure(Exception("Empty response"))
            }

            val jsonResponse = gson.fromJson(responseBody, Map::class.java)
            val result = jsonResponse["result"] as? Map<*, *>
            val tools = result?.get("tools") as? List<*>

            val mcpTools = tools?.mapNotNull { tool ->
                (tool as? Map<*, *>)?.let { toolMap ->
                    McpTool(
                        name = toolMap["name"] as? String ?: "",
                        description = toolMap["description"] as? String,
                        inputSchema = toolMap["inputSchema"] as? Map<String, Any>
                    )
                }
            } ?: emptyList()

            Result.success(mcpTools)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing tools", e)
            Result.failure(e)
        }
    }

    /**
     * Вызвать MCP инструмент
     */
    suspend fun callTool(toolName: String, arguments: Map<String, Any>): Result<String> =
        withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "jsonrpc" to "2.0",
                "id" to "call-tool-${System.currentTimeMillis()}",
                "method" to "tools/call",
                "params" to mapOf(
                    "name" to toolName,
                    "arguments" to arguments
                )
            )

            val request = Request.Builder()
                .url("$baseUrl/mcp/coincap/tools/call")
                .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Call tool response: $responseBody")

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP error: ${response.code} $responseBody")
                )
            }

            if (responseBody == null) {
                return@withContext Result.failure(Exception("Empty response"))
            }

            val jsonResponse = gson.fromJson(responseBody, Map::class.java)
            val result = jsonResponse["result"] as? Map<*, *>
            val content = result?.get("content") as? List<*>
            val textContent = content?.firstOrNull() as? Map<*, *>
            val text = textContent?.get("text") as? String

            Result.success(text ?: "No result")
        } catch (e: Exception) {
            Log.e(TAG, "Error calling tool", e)
            Result.failure(e)
        }
    }

    /**
     * Сохранить отчет об анализе биткоина в файл через File MCP сервер
     */
    suspend fun saveBitcoinReport(
        content: String,
        price: Double,
        previousPrice: Double? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val arguments = mutableMapOf<String, Any>(
                "content" to content,
                "price" to price
            )

            previousPrice?.let {
                arguments["previous_price"] = it
            }

            val requestBody = mapOf(
                "jsonrpc" to "2.0",
                "id" to "save-report-${System.currentTimeMillis()}",
                "method" to "tools/call",
                "params" to mapOf(
                    "name" to "save_bitcoin_report",
                    "arguments" to arguments
                )
            )

            val request = Request.Builder()
                .url("$baseUrl/mcp/file/tools/call")
                .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Save report response: $responseBody")

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP error: ${response.code} $responseBody")
                )
            }

            if (responseBody == null) {
                return@withContext Result.failure(Exception("Empty response"))
            }

            val jsonResponse = gson.fromJson(responseBody, Map::class.java)
            val result = jsonResponse["result"] as? Map<*, *>
            val contentList = result?.get("content") as? List<*>
            val textContent = contentList?.firstOrNull() as? Map<*, *>
            val text = textContent?.get("text") as? String

            Result.success(text ?: "Report saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving report", e)
            Result.failure(e)
        }
    }

    /**
     * Проверка здоровья сервера
     */
    suspend fun checkHealth(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                Result.success(responseBody ?: "OK")
            } else {
                Result.failure(Exception("Health check failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Health check error", e)
            Result.failure(e)
        }
    }
}
