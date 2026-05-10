package com.example.fitsync.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitsync.domain.model.chat.ChatMessage
import com.example.fitsync.domain.model.chat.Macros
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val text: String,
    val isUser: Boolean,
    val isAnalysis: Boolean,
    val mealName: String?,
    val estimatedQuantity: String?,
    val macrosJson: String?,
    val imageUri: String?,   // Stores the file path
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): ChatMessage {
        val macros = if (macrosJson != null) {
            try { Json { ignoreUnknownKeys = true }.decodeFromString<Macros>(macrosJson) }
            catch (e: Exception) { null }
        } else null

        return ChatMessage(
            id = id,
            text = text,
            isUser = isUser,
            isAnalysis = isAnalysis,
            mealName = mealName,
            estimatedQuantity = estimatedQuantity,
            macros = macros,
            imageLocalPath = imageUri // Simply pass the path string to the UI!
        )
    }
}

fun ChatMessage.toEntity(dateString: String): ChatMessageEntity {
    val macrosJson = if (this.macros != null) Json.encodeToString(this.macros) else null

    return ChatMessageEntity(
        id = this.id,
        text = this.text,
        isUser = this.isUser,
        isAnalysis = this.isAnalysis,
        mealName = this.mealName,
        estimatedQuantity = this.estimatedQuantity,
        macrosJson = macrosJson,
        imageUri = this.imageLocalPath, // Read the path string from the UI
        dateString = dateString
    )
}