package com.turboguys.myaimodelsbot.data.local

import com.turboguys.myaimodelsbot.data.local.dao.MessageDao
import com.turboguys.myaimodelsbot.data.local.entity.MessageEntity
import com.turboguys.myaimodelsbot.data.local.entity.toDomain
import com.turboguys.myaimodelsbot.data.local.entity.toEntity
import com.turboguys.myaimodelsbot.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatLocalDataSource(
    private val messageDao: MessageDao
) {

    fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAllMessagesSync(): List<Message> {
        return messageDao.getAllMessagesSync().map { it.toDomain() }
    }

    suspend fun saveMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    suspend fun saveMessages(messages: List<Message>) {
        messageDao.insertMessages(messages.map { it.toEntity() })
    }

    suspend fun clearAllMessages() {
        messageDao.deleteAllMessages()
    }
}
