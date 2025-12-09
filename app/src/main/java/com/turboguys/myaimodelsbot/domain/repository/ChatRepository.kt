package com.turboguys.myaimodelsbot.domain.repository

import com.turboguys.myaimodelsbot.domain.model.AiModel
import com.turboguys.myaimodelsbot.domain.model.Message

interface ChatRepository {
    suspend fun sendMessage(
        model: AiModel,
        messages: List<Message>
    ): Result<Message>
}
