package com.minimize.maximus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minimize.maximus.data.local.entity.ChatMessageEntity
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

    @Query("UPDATE chat_messages SET isRoutineSaved = :isSaved WHERE id = :messageId")
    suspend fun updateRoutineSaved(messageId: String, isSaved: Boolean)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)
}