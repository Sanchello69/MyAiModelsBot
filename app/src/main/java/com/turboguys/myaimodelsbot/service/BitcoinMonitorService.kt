package com.turboguys.myaimodelsbot.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.turboguys.myaimodelsbot.R
import com.turboguys.myaimodelsbot.data.remote.mcp.SimpleMcpClient
import com.turboguys.myaimodelsbot.domain.repository.BitcoinRepository
import com.turboguys.myaimodelsbot.domain.usecase.AnalyzeBitcoinWithToolsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BitcoinMonitorService : Service() {

    private val bitcoinRepository: BitcoinRepository by inject()
    private val analyzeBitcoinWithToolsUseCase: AnalyzeBitcoinWithToolsUseCase by inject()
    private val mcpClient: SimpleMcpClient by inject()

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "BitcoinMonitorService"
        private const val CHANNEL_ID = "bitcoin_monitor_channel"
        private const val NOTIFICATION_ID = 1
        private const val MONITORING_INTERVAL = 10_000L // 10 секунд

        fun start(context: Context) {
            val intent = Intent(context, BitcoinMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BitcoinMonitorService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Мониторинг запущен"))
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bitcoin Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Мониторинг курса биткоина"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bitcoin Monitor")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    // AI агент сам получит курс через MCP и проанализирует его
                    val analysisResult = analyzeBitcoinWithToolsUseCase()

                    analysisResult.onSuccess { analysisText ->
                        Log.d(TAG, "AI Analysis: $analysisText")

                        val timestamp = System.currentTimeMillis()

                        // Получаем текущий и предыдущий курс
                        val priceResult = bitcoinRepository.fetchCurrentBitcoinPrice()
                        val currentPrice = priceResult.getOrNull() ?: 0.0
                        val previousPriceEntity = bitcoinRepository.getLatestPrice()
                        val previousPrice = previousPriceEntity?.price

                        // Сохраняем отчет в файл через File MCP сервер
                        launch {
                            val saveResult = mcpClient.saveBitcoinReport(
                                content = analysisText,
                                price = currentPrice,
                                previousPrice = previousPrice
                            )

                            saveResult.onSuccess { saveResponse ->
                                Log.d(TAG, "Report saved: $saveResponse")
                            }

                            saveResult.onFailure { error ->
                                Log.e(TAG, "Failed to save report", error)
                            }
                        }

                        // Обновляем уведомление с кратким анализом
                        val shortAnalysis = analysisText.take(50) + "..."
                        updateNotification("BTC: $$currentPrice - $shortAnalysis")

                        // Отправляем broadcast для обновления UI
                        sendBroadcast(Intent(ACTION_BITCOIN_UPDATE).apply {
                            putExtra(EXTRA_PRICE, currentPrice)
                            putExtra(EXTRA_ANALYSIS, analysisText)
                            putExtra(EXTRA_TIMESTAMP, timestamp)
                        })

                        // Сохраняем курс в БД
                        bitcoinRepository.saveBitcoinPrice(
                            price = currentPrice,
                            timestamp = timestamp,
                            analysis = analysisText
                        )

                        Log.d(TAG, "Bitcoin price updated: $currentPrice")
                    }

                    analysisResult.onFailure { error ->
                        Log.e(TAG, "AI Analysis error", error)
                        updateNotification("Ошибка анализа: ${error.message}")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Monitoring error", e)
                }

                // Ждем 30 секунд
                delay(MONITORING_INTERVAL)
            }
        }
    }
}

const val ACTION_BITCOIN_UPDATE = "com.turboguys.myaimodelsbot.BITCOIN_UPDATE"
const val EXTRA_PRICE = "price"
const val EXTRA_ANALYSIS = "analysis"
const val EXTRA_TIMESTAMP = "timestamp"
