package com.turboguys.myaimodelsbot.presentation.chat

import com.turboguys.myaimodelsbot.domain.model.AiModel

sealed class ChatEvent {
    data class OnMessageChange(val message: String) : ChatEvent()
    data object OnSendMessage : ChatEvent()
    data class OnModelSelect(val model: AiModel) : ChatEvent()
    data object OnErrorDismiss : ChatEvent()
}
