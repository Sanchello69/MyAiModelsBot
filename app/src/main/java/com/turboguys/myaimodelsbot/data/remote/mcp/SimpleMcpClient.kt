package com.turboguys.myaimodelsbot.data.remote.mcp

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
