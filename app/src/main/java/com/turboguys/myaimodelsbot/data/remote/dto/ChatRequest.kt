package com.turboguys.myaimodelsbot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<MessageDto>,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null,
    @SerializedName("tools")
    val tools: List<ToolDefinition>? = null,
    @SerializedName("tool_choice")
    val toolChoice: String? = null // "auto", "none", or specific tool
)

data class MessageDto(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("tool_calls")
    val toolCalls: List<ToolCall>? = null,
    @SerializedName("tool_call_id")
    val toolCallId: String? = null
)