package com.turboguys.myaimodelsbot

import android.util.Log
import com.turboguys.myaimodelsbot.data.remote.mcp.McpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Пример использования MCP клиента
 *
 * Вызовите McpTestExample.testMcpConnection() из MainActivity
 * для проверки работы MCP подключения
 */
object McpTestExample {

    private const val TAG = "McpTestExample"

    /**
     * Тестовая функция для проверки MCP подключения
     * Вызовите её из onCreate в MainActivity для теста
     */
    fun testMcpConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "=== Начало теста MCP ===")

                // Создание MCP клиента
                val mcpClient = McpClient(
                    serverUrl = "https://remote.mcpservers.org/fetch/mcp"
                )

                // Шаг 0: Проверка доступности сервера
                Log.d(TAG, "Шаг 0: Проверка доступности MCP сервера...")
                val testResult = mcpClient.testConnection()
                Log.d(TAG, "Ответ сервера:\n$testResult")

                // Шаг 1: Инициализация соединения
                Log.d(TAG, "\nШаг 1: Инициализация MCP соединения...")
                val initResult = mcpClient.initialize()

                Log.d(TAG, "✓ MCP соединение установлено!")
                Log.d(TAG, "  Протокол версия: ${initResult.protocolVersion}")
                Log.d(TAG, "  Сервер: ${initResult.serverInfo.name} v${initResult.serverInfo.version}")

                // Шаг 1.5: Отправка уведомления initialized
                Log.d(TAG, "\nШаг 1.5: Отправка уведомления initialized...")
                mcpClient.sendInitializedNotification()
                Log.d(TAG, "✓ Уведомление отправлено")

                // Шаг 2: Получение списка инструментов
                Log.d(TAG, "\nШаг 2: Получение списка доступных инструментов...")
                val tools = mcpClient.listTools()

                Log.d(TAG, "✓ Получено инструментов: ${tools.size}")

                // Вывод информации о каждом инструменте
                if (tools.isNotEmpty()) {
                    Log.d(TAG, "\n=== Список доступных инструментов ===")
                    tools.forEachIndexed { index, tool ->
                        Log.d(TAG, "\n[${index + 1}] ${tool.name}")
                        Log.d(TAG, "    Описание: ${tool.description ?: "Нет описания"}")

                        tool.inputSchema?.let { schema ->
                            Log.d(TAG, "    Тип схемы: ${schema.type}")
                            schema.properties?.let { props ->
                                Log.d(TAG, "    Параметры:")
                                props.forEach { (key, value) ->
                                    Log.d(TAG, "      - $key: $value")
                                }
                            }
                            schema.required?.let { required ->
                                Log.d(TAG, "    Обязательные: ${required.joinToString(", ")}")
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "⚠ Инструменты не найдены")
                }

                // Закрытие соединения
                mcpClient.close()

                Log.d(TAG, "\n=== Тест MCP успешно завершен ===")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка при тестировании MCP:", e)
            }
        }
    }

    /**
     * Асинхронная версия для использования в suspend функциях
     */
    suspend fun testMcpConnectionAsync() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== Начало теста MCP (Async) ===")

            val mcpClient = McpClient()

            // Инициализация
            val initResult = mcpClient.initialize()
            Log.d(TAG, "Сервер: ${initResult.serverInfo.name}")

            // Получение инструментов
            val tools = mcpClient.listTools()
            Log.d(TAG, "Найдено инструментов: ${tools.size}")

            tools.forEach { tool ->
                Log.d(TAG, "- ${tool.name}: ${tool.description}")
            }

            mcpClient.close()

            tools // Возвращаем список инструментов

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка MCP:", e)
            emptyList()
        }
    }
}
