package com.minimize.maximus.data.repository

import com.minimize.maximus.data.local.dao.ChatDao
import com.minimize.maximus.data.local.entity.toEntity
import com.minimize.maximus.domain.model.chat.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    fun getMessagesForToday(dateString: String): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage, dateString: String)
    suspend fun clearChatsForToday(dateString: String)
    suspend fun updateRoutineSaved(messageId: String, isSaved: Boolean)
    suspend fun deleteMessage(id: String)
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getMessagesForToday(dateString: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForDate(dateString).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertMessage(message: ChatMessage, dateString: String) {
        withContext(Dispatchers.IO) {
            chatDao.insertMessage(message.toEntity(dateString))
        }
    }

    override suspend fun clearChatsForToday(dateString: String) {
        withContext(Dispatchers.IO) {
            chatDao.clearChatsForDate(dateString)
        }
    }

    override suspend fun updateRoutineSaved(messageId: String, isSaved: Boolean) {
        withContext(Dispatchers.IO) {
            chatDao.updateRoutineSaved(messageId, isSaved)
        }
    }

    override suspend fun deleteMessage(id: String) {
        withContext(Dispatchers.IO) {
            chatDao.deleteMessageById(id)
        }
    }
}