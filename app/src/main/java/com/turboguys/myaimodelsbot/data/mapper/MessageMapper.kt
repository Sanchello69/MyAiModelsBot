package com.turboguys.myaimodelsbot.data.mapper

import com.turboguys.myaimodelsbot.data.remote.dto.MessageDto
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.domain.model.MessageRole

fun Message.toDto(): MessageDto {
    return MessageDto(
        role = when (role) {
            MessageRole.USER -> "user"
            MessageRole.ASSISTANT -> "assistant"
            MessageRole.SYSTEM -> "system"
        },
        content = content
    )
}

fun MessageDto.toDomain(): Message {
    return Message(
        role = when (role.lowercase()) {
            "user" -> MessageRole.USER
            "assistant" -> MessageRole.ASSISTANT
            "system" -> MessageRole.SYSTEM
            else -> MessageRole.ASSISTANT
        },
        content = content ?: "" // Обрабатываем null для tool calls
    )
}
