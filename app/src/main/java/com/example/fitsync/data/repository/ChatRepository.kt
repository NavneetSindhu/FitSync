package com.example.fitsync.data.repository

import android.util.Log
import com.example.fitsync.data.local.dao.ChatDao
import com.example.fitsync.data.local.entity.toEntity
import com.example.fitsync.data.remote.ChatSyncPayload
import com.example.fitsync.data.remote.FitSyncApi
import com.example.fitsync.data.remote.UserIdPayload
import com.example.fitsync.domain.model.chat.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// --- 1. THE INTERFACE ---
// We need to add the new server fetch function to the contract!
interface ChatRepository {
    fun getMessagesForToday(dateString: String): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage, dateString: String, userId: Long) // Added userId
    suspend fun fetchChatsFromServer(userId: Long) // NEW
    suspend fun clearChatsForToday(dateString: String)
}

// --- 2. THE IMPLEMENTATION ---
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val api: FitSyncApi // <-- NEW: Injected your Retrofit API
) : ChatRepository {

    // 1. UI observes local DB (Stays exactly the same)
    override fun getMessagesForToday(dateString: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForDate(dateString).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // 2. Fetch History from Spring Boot
    override suspend fun fetchChatsFromServer(userId: Long) {
        try {
            val remoteChats = api.getUserChats(userId)
            // Save them to Room so the UI updates instantly
            remoteChats.forEach { chat ->
                // Note: You might need to extract the dateString from the chat's timestamp here
                // chatDao.insertMessage(chat.toEntity(dateString))
            }
        } catch (e: Exception) {
            Log.e("ChatSync", "Failed to fetch chat history: ${e.message}")
        }
    }

    // 3. Save Locally + Sync to Cloud
    override suspend fun insertMessage(message: ChatMessage, dateString: String, userId: Long) {
        // 1. Save locally for instant UI response
        chatDao.insertMessage(message.toEntity(dateString))

        // 2. Try to sync to PostgreSQL
        try {
            // --- NEW: Translate UI Model to Network Payload ---
            val networkPayload = ChatSyncPayload(
                messageText = message.text,
                sentByUser = message.sentByUser,
                macrosJson = null, // TODO: We can serialize your macros here later!
                timestamp = System.currentTimeMillis(),
                user = UserIdPayload(id = userId)
            )

            // Send the perfectly shaped payload to Spring Boot
            api.syncChatMessage(networkPayload)
            Log.d("ChatSync", "Message saved to server!")
        } catch (e: Exception) {
            Log.e("ChatSync", "Retrofit crashed because: ", e)
        }
    }

    override suspend fun clearChatsForToday(dateString: String) {
        chatDao.clearChatsForDate(dateString)
        // TODO: Eventually add a call to api.deleteChatsForDate(userId, dateString)
    }
}