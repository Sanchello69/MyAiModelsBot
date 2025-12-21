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
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.data.mapper.toDomain
import com.turboguys.myaimodelsbot.data.mapper.toDto

/**
 * UseCase для отправки сообщений с поддержкой MCP инструментов
 * AI агент может вызывать инструменты из CoinCap, File и Mobile MCP серверов
 */
class SendMessageWithToolsUseCase(
    private val apiService: OpenRouterApiService,
    private val mcpClient: SimpleMcpClient
) {
    private val gson = Gson()

    companion object {
        private const val TAG = "SendMessageWithTools"
        private const val MAX_ITERATIONS = 10 // Максимум итераций для tool calls
    }

    suspend operator fun invoke(
        model: AiModel,
        messages: List<Message>,
        maxTokens: Int? = null
    ): Result<Message> {
        return try {
            // 1. Получаем инструменты из всех MCP серверов
            val allTools = mutableListOf<Pair<String, SimpleMcpClient.McpTool>>()

            // CoinCap tools
            val coinCapToolsResult = mcpClient.listTools()
            coinCapToolsResult.getOrNull()?.forEach { tool ->
                allTools.add("coincap" to tool)
            }

            // File tools
            val fileToolsResult = mcpClient.listFileTools()
            fileToolsResult.getOrNull()?.forEach { tool ->
                allTools.add("file" to tool)
            }

            // Mobile tools
            val mobileToolsResult = mcpClient.listMobileTools()
            mobileToolsResult.getOrNull()?.forEach { tool ->
                allTools.add("mobile" to tool)
            }

            Log.d(TAG, "Total MCP tools available: ${allTools.size}")

            // 2. Конвертируем в формат OpenRouter
            val toolDefinitions = allTools.map { (_, tool) ->
                ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = tool.name,
                        description = tool.description ?: "",
                        parameters = tool.inputSchema ?: emptyMap()
                    )
                )
            }

            // 3. Конвертируем сообщения в DTO
            val messageDtos = messages.map { it.toDto() }.toMutableList()

            // 4. Основной цикл с поддержкой tool calls
            var iteration = 0
            while (iteration < MAX_ITERATIONS) {
                iteration++
                Log.d(TAG, "Iteration $iteration")

                // Отправляем запрос с инструментами
                val request = ChatRequest(
                    model = model.modelId,
                    messages = messageDtos.toList(),
                    tools = if (toolDefinitions.isNotEmpty()) toolDefinitions else null,
                    toolChoice = if (toolDefinitions.isNotEmpty()) "auto" else null,
                    maxTokens = maxTokens
                )

                val response = apiService.sendMessage(request)
                val choice = response.choices.firstOrNull()
                    ?: return Result.failure(Exception("No response from AI"))

                val assistantMessage = choice.message

                // Добавляем ответ AI в историю
                messageDtos.add(assistantMessage)

                // Проверяем finish_reason
                when (choice.finishReason) {
                    "tool_calls" -> {
                        // AI хочет вызвать инструменты
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

                            // Находим, к какому серверу относится этот инструмент
                            val toolServer = allTools.find { it.second.name == toolName }?.first

                            // Вызываем соответствующий MCP сервер
                            val toolResult = when (toolServer) {
                                "coincap" -> mcpClient.callTool(toolName, arguments)
                                "file" -> mcpClient.callFileTool(toolName, arguments)
                                "mobile" -> mcpClient.callMobileTool(toolName, arguments)
                                else -> Result.failure(Exception("Unknown tool: $toolName"))
                            }

                            val toolResultText = toolResult.getOrElse {
                                "Error: ${it.message}"
                            }

                            Log.d(TAG, "Tool result: $toolResultText")

                            // Добавляем результат tool call в историю
                            messageDtos.add(
                                MessageDto(
                                    role = "tool",
                                    content = toolResultText,
                                    toolCallId = toolCall.id
                                )
                            )
                        }

                        // Продолжаем цикл - AI обработает результаты
                        continue
                    }

                    "stop", "end_turn" -> {
                        // AI закончил - возвращаем финальное сообщение
                        val message = assistantMessage.toDomain()
                        Log.d(TAG, "Final response: ${message.content}")
                        return Result.success(message)
                    }

                    else -> {
                        // Другие случаи - тоже возвращаем ответ
                        val message = assistantMessage.toDomain()
                        return Result.success(message)
                    }
                }
            }

            Result.failure(Exception("Max iterations reached"))
        } catch (e: Exception) {
            Log.e(TAG, "Error in send message with tools", e)
            Result.failure(e)
        }
    }
}
