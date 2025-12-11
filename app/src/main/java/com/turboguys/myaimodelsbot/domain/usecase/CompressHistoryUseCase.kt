package com.turboguys.myaimodelsbot.domain.usecase

import com.turboguys.myaimodelsbot.domain.model.AiModel
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.domain.model.MessageRole
import com.turboguys.myaimodelsbot.domain.repository.ChatRepository

class CompressHistoryUseCase(
    private val repository: ChatRepository
) {
    companion object {
        private const val COMPRESSION_THRESHOLD = 10 // Сжимаем каждые 10 сообщений
    }

    suspend operator fun invoke(
        model: AiModel,
        messages: List<Message>
    ): List<Message> {
        if (messages.size < COMPRESSION_THRESHOLD) {
            return messages
        }

        // Берем последние 10 сообщений - они остаются без изменений
        val recentMessages = messages.takeLast(COMPRESSION_THRESHOLD)

        // Всё что старше последних 10 сообщений
        val oldMessages = messages.dropLast(COMPRESSION_THRESHOLD)

        if (oldMessages.isEmpty()) {
            return messages
        }

        // Создаем ОДНО резюме для всех старых сообщений
        val summary = createSummary(model, oldMessages)

        // Результат: [резюме] + [последние 10 сообщений]
        return if (summary != null) {
            listOf(summary) + recentMessages
        } else {
            // Если не удалось создать резюме, возвращаем как есть
            messages
        }
    }

    private suspend fun createSummary(model: AiModel, messages: List<Message>): Message? {
        if (messages.isEmpty()) return null

        // Создаем запрос на суммаризацию
        val summaryPrompt = buildSummaryPrompt(messages)
        val summaryMessages = listOf(
            Message(
                role = MessageRole.USER,
                content = summaryPrompt
            )
        )

        return try {
            val result = repository.sendMessage(
                model = model,
                messages = summaryMessages,
                maxTokens = 500
            )
            result.getOrNull()?.copy(
                role = MessageRole.USER,
                content = "[РЕЗЮМЕ ПРЕДЫДУЩИХ СООБЩЕНИЙ]: ${result.getOrNull()?.content ?: ""}"
            )
        } catch (e: Exception) {
            // В случае ошибки возвращаем простое текстовое резюме
            Message(
                role = MessageRole.USER,
                content = "[РЕЗЮМЕ]: ${messages.size} сообщений были сжаты"
            )
        }
    }

    private fun buildSummaryPrompt(messages: List<Message>): String {
        val messageTexts = messages.joinToString("\n") { message ->
            val roleName = when (message.role) {
                MessageRole.USER -> "Пользователь"
                MessageRole.ASSISTANT -> "Ассистент"
                MessageRole.SYSTEM -> "Система"
            }
            "$roleName: ${message.content}"
        }
        return """
Создай краткое резюме следующего диалога, сохраняя ключевые темы и важную информацию.
Резюме должно быть кратким (2-3 предложения) и содержать только самое важное.

Диалог:
$messageTexts

Твоё резюме:
        """.trimIndent()
    }
}
