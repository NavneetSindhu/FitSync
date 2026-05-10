package com.example.fitsync.data.repository

import com.example.fitsync.data.local.dao.ChatDao
import com.example.fitsync.data.local.entity.toEntity
import com.example.fitsync.domain.model.chat.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// --- 1. THE INTERFACE (The Contract) ---
// This tells the rest of the app WHAT the repository can do, without exposing HOW it does it.
interface ChatRepository {
    fun getMessagesForToday(dateString: String): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage, dateString: String)
    suspend fun clearChatsForToday(dateString: String)
}

// --- 2. THE IMPLEMENTATION (The Engine) ---
// This is the actual class that Hilt will build and provide to your ViewModel.
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getMessagesForToday(dateString: String): Flow<List<ChatMessage>> {
        // 1. Fetch the Flow of Entities from Room
        // 2. Map the Flow
        // 3. Transform each ChatMessageEntity back into your clean ChatMessage UI model
        return chatDao.getMessagesForDate(dateString).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertMessage(message: ChatMessage, dateString: String) {
        // Convert the clean UI model into the database-friendly Entity, then save it
        chatDao.insertMessage(message.toEntity(dateString))
    }
    override suspend fun clearChatsForToday(dateString: String) {
        chatDao.clearChatsForDate(dateString) // NEW
    }
}