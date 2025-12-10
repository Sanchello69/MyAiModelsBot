package com.turboguys.myaimodelsbot.presentation.chat

import com.turboguys.myaimodelsbot.domain.model.AiModel
import com.turboguys.myaimodelsbot.domain.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val selectedModel: AiModel = AiModel.DEEPSEEK_CHIMERA,
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = "",
    val maxTokens: Int = 1000 // Значение по умолчанию
)
