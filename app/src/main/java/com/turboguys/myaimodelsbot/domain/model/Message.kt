package com.turboguys.myaimodelsbot.domain.model

data class Message(
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenUsage: TokenUsage? = null
)

data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
