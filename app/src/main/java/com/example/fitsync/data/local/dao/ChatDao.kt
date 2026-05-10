package com.example.fitsync.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitsync.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // Get all messages for a specific day, ordered by time
    @Query("SELECT * FROM chat_messages WHERE dateString = :date ORDER BY timestamp ASC")
    fun getMessagesForDate(date: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE dateString = :date")
    suspend fun clearChatsForDate(date: String)
}