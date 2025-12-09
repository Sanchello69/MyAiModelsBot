package com.turboguys.myaimodelsbot.data.repository

import com.turboguys.myaimodelsbot.data.mapper.toDomain
import com.turboguys.myaimodelsbot.data.mapper.toDto
import com.turboguys.myaimodelsbot.data.remote.OpenRouterApiService
import com.turboguys.myaimodelsbot.data.remote.dto.ChatRequest
import com.turboguys.myaimodelsbot.domain.model.AiModel
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.domain.model.TokenUsage
import com.turboguys.myaimodelsbot.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val apiService: OpenRouterApiService
) : ChatRepository {

    override suspend fun sendMessage(
        model: AiModel,
        messages: List<Message>
    ): Result<Message> {
        return try {
            val request = ChatRequest(
                model = model.modelId,
                messages = messages.map { it.toDto() }
            )

            val response = apiService.sendMessage(request)
            val assistantMessage = response.choices.firstOrNull()?.message
                ?: throw Exception("No response from AI")

            // Добавляем информацию о токенах
            val tokenUsage = response.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            }

            val message = assistantMessage.toDomain().copy(tokenUsage = tokenUsage)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
