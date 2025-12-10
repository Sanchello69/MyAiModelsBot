package com.turboguys.myaimodelsbot.domain.usecase

import com.turboguys.myaimodelsbot.domain.model.AiModel
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        model: AiModel,
        messages: List<Message>,
        maxTokens: Int? = null
    ): Result<Message> {
        return repository.sendMessage(model, messages, maxTokens)
    }
}
