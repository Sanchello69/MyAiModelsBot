package com.turboguys.myaimodelsbot.domain.usecase

import android.util.Log
import com.google.gson.Gson
import com.turboguys.myaimodelsbot.data.remote.OpenRouterApiService
import com.turboguys.myaimodelsbot.data.remote.dto.ChatRequest
import com.turboguys.myaimodelsbot.data.remote.dto.FunctionDefinition
import com.turboguys.myaimodelsbot.data.remote.dto.MessageDto
import com.turboguys.myaimodelsbot.data.remote.dto.ToolDefinition
import com.turboguys.myaimodelsbot.data.remote.mcp.SimpleMcpClient
import com.turboguys.myaimodelsbot.domain.model.AiModel

/**
 * UseCase для анализа курса биткоина с использованием MCP tools
 * AI сам решает когда вызывать MCP инструменты
 */
class AnalyzeBitcoinWithToolsUseCase(
    private val apiService: OpenRouterApiService,
    private val mcpClient: SimpleMcpClient,
    private val bitcoinRepository: com.turboguys.myaimodelsbot.domain.repository.BitcoinRepository
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "AnalyzeBitcoinWithTools"
        private const val MAX_ITERATIONS = 5 // Максимум итераций для избежания бесконечного цикла
    }

    suspend operator fun invoke(): Result<String> {
        return try {
            // 1. Получаем список MCP tools
            val toolsResult = mcpClient.listTools()
            if (toolsResult.isFailure) {
                return Result.failure(Exception("Failed to get MCP tools: ${toolsResult.exceptionOrNull()?.message}"))
            }

            val mcpTools = toolsResult.getOrNull() ?: emptyList()
            Log.d(TAG, "MCP tools available: ${mcpTools.size}")

            // 2. Конвертируем MCP tools в формат OpenRouter
            val toolDefinitions = mcpTools.map { mcpTool ->
                ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = mcpTool.name,
                        description = mcpTool.description ?: "",
                        parameters = mcpTool.inputSchema ?: emptyMap()
                    )
                )
            }

            // 3. Получаем предыдущий курс из БД
            val previousPrice = bitcoinRepository.getLatestPrice()
            val previousPriceText = if (previousPrice != null) {
                "Предыдущий сохраненный курс: $${previousPrice.price} (время: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(previousPrice.timestamp))})"
            } else {
                "Это первая проверка курса, предыдущих данных нет."
            }

            // 4. Начальный промпт для AI
            val messages = mutableListOf(
                MessageDto(
                    role = "system",
                    content = """Ты ассистент для мониторинга курса биткоина.
У тебя есть доступ к инструментам для получения информации о криптовалютах.
Используй инструмент get_asset_by_id с id="bitcoin" чтобы узнать текущий курс биткоина.

$previousPriceText

Твоя задача:
1. Получи текущий курс биткоина через инструмент
2. Сравни его с предыдущим значением
3. Дай краткий анализ (2-3 предложения):
   - Как изменился курс (вырос/упал/без изменений)
   - Насколько значительно изменение (в долларах и процентах)
   - Стоит ли обратить внимание на это изменение"""
                ),
                MessageDto(
                    role = "user",
                    content = "Проверь текущий курс биткоина и сравни его с предыдущим значением."
                )
            )

            // 5. Основной цикл: AI решает когда вызывать tools
            var iteration = 0
            while (iteration < MAX_ITERATIONS) {
                iteration++
                Log.d(TAG, "Iteration $iteration")

                // Отправляем запрос AI с доступными tools
                val request = ChatRequest(
                    model = AiModel.DEEPSEEK_V3.modelId,
                    messages = messages.toList(),
                    tools = toolDefinitions,
                    toolChoice = "auto",
                    maxTokens = 500
                )

                val response = apiService.sendMessage(request)
                val choice = response.choices.firstOrNull()
                    ?: return Result.failure(Exception("No response from AI"))

                val assistantMessage = choice.message

                // Добавляем ответ AI в историю
                messages.add(assistantMessage)

                // Проверяем finish_reason
                when (choice.finishReason) {
                    "tool_calls" -> {
                        // AI хочет вызвать tool
                        val toolCalls = assistantMessage.toolCalls
                        if (toolCalls.isNullOrEmpty()) {
                            return Result.failure(Exception("No tool calls in response"))
                        }

                        Log.d(TAG, "AI wants to call ${toolCalls.size} tools")

                        // Выполняем все tool calls
                        for (toolCall in toolCalls) {
                            val toolName = toolCall.function.name
                            val argumentsJson = toolCall.function.arguments

                            Log.d(TAG, "Calling tool: $toolName with args: $argumentsJson")

                            // Парсим аргументы
                            val arguments = try {
                                gson.fromJson(argumentsJson, Map::class.java) as Map<String, Any>
                            } catch (e: Exception) {
                                emptyMap()
                            }

                            // Вызываем MCP tool
                            val toolResult = mcpClient.callTool(toolName, arguments)
                            val toolResultText = toolResult.getOrElse {
                                "Error: ${it.message}"
                            }

                            Log.d(TAG, "Tool result: $toolResultText")

                            // Добавляем результат tool call в историю
                            messages.add(
                                MessageDto(
                                    role = "tool",
                                    content = toolResultText,
                                    toolCallId = toolCall.id
                                )
                            )
                        }

                        // Продолжаем цикл - AI обработает результаты tools
                        continue
                    }

                    "stop", "end_turn" -> {
                        // AI закончил - возвращаем его финальный ответ
                        val finalResponse = assistantMessage.content
                            ?: "No analysis generated"
                        Log.d(TAG, "Final response: $finalResponse")
                        return Result.success(finalResponse)
                    }

                    else -> {
                        // Другие случаи - тоже возвращаем ответ
                        val finalResponse = assistantMessage.content
                            ?: "Analysis incomplete: ${choice.finishReason}"
                        return Result.success(finalResponse)
                    }
                }
            }

            Result.failure(Exception("Max iterations reached"))
        } catch (e: Exception) {
            Log.e(TAG, "Error in analyze bitcoin with tools", e)
            Result.failure(e)
        }
    }
}
