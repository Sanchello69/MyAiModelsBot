package com.turboguys.myaimodelsbot.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Модели для поддержки tool use (function calling) в OpenRouter API
 */

data class ToolDefinition(
    @SerializedName("type")
    val type: String = "function",
    @SerializedName("function")
    val function: FunctionDefinition
)

data class FunctionDefinition(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("parameters")
    val parameters: Map<String, Any>
)

data class ToolCall(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("function")
    val function: FunctionCall
)

data class FunctionCall(
    @SerializedName("name")
    val name: String,
    @SerializedName("arguments")
    val arguments: String // JSON string
)
