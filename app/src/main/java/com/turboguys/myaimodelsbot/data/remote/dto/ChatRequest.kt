package com.turboguys.myaimodelsbot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<MessageDto>,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null
)

data class MessageDto(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)