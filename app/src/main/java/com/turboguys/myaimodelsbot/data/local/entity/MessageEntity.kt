package com.turboguys.myaimodelsbot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.domain.model.MessageRole
import com.turboguys.myaimodelsbot.domain.model.TokenUsage

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String,
    val content: String,
    val timestamp: Long,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null
)

fun MessageEntity.toDomain(): Message {
    return Message(
        role = MessageRole.valueOf(role),
        content = content,
        timestamp = timestamp,
        tokenUsage = if (totalTokens != null) {
            TokenUsage(
                promptTokens = promptTokens ?: 0,
                completionTokens = completionTokens ?: 0,
                totalTokens = totalTokens
            )
        } else null
    )
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        role = role.name,
        content = content,
        timestamp = timestamp,
        promptTokens = tokenUsage?.promptTokens,
        completionTokens = tokenUsage?.completionTokens,
        totalTokens = tokenUsage?.totalTokens
    )
}
