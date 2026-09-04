package com.minimize.maximus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minimize.maximus.domain.model.chat.ChatMessage
import com.minimize.maximus.domain.model.chat.Macros
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val chatJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val text: String,
    val isUser: Boolean,
    val isAnalysis: Boolean,
    val mealName: String?,
    val estimatedQuantity: String?,
    val macrosJson: String?,
    val routineJson: String? = null,
    val isRoutineSaved: Boolean = false,
    val suggestedRepliesJson: String? = null,
    val imageUri: String?,   // Stores the file path
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): ChatMessage {
        val macros = if (macrosJson != null) {
            try { chatJson.decodeFromString<Macros>(macrosJson) }
            catch (e: Exception) { null }
        } else null

        val routine = if (routineJson != null) {
            try { chatJson.decodeFromString<WorkoutRoutine>(routineJson) }
            catch (e: Exception) { null }
        } else null

        val suggestedReplies = if (suggestedRepliesJson != null) {
            try { chatJson.decodeFromString<List<String>>(suggestedRepliesJson) }
            catch (e: Exception) { emptyList() }
        } else emptyList()

        return ChatMessage(
            id = id,
            text = text,
            sentByUser = isUser,
            isAnalysis = isAnalysis,
            mealName = mealName,
            estimatedQuantity = estimatedQuantity,
            macros = macros,
            routinePayload = routine,
            isRoutineSaved = isRoutineSaved,
            suggestedReplies = suggestedReplies,
            imageLocalPath = imageUri
        )
    }
}

fun ChatMessage.toEntity(dateString: String): ChatMessageEntity {
    val macrosJson = if (this.macros != null) chatJson.encodeToString(this.macros) else null
    val routineJson = if (this.routinePayload != null) chatJson.encodeToString(this.routinePayload) else null
    val suggestedRepliesJson = if (this.suggestedReplies.isNotEmpty()) chatJson.encodeToString(this.suggestedReplies) else null

    return ChatMessageEntity(
        id = this.id,
        text = this.text,
        isUser = this.sentByUser,
        isAnalysis = this.isAnalysis,
        mealName = this.mealName,
        estimatedQuantity = this.estimatedQuantity,
        macrosJson = macrosJson,
        routineJson = routineJson,
        isRoutineSaved = this.isRoutineSaved,
        suggestedRepliesJson = suggestedRepliesJson,
        imageUri = this.imageLocalPath,
        dateString = dateString
    )
}