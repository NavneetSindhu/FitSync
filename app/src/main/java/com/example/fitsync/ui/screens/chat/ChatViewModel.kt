package com.example.fitsync.ui.screens.chat

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.ImageStorageHelper
import com.example.fitsync.data.local.PreferenceManager // <-- INJECTED
import com.example.fitsync.data.remote.GeminiService
import com.example.fitsync.data.repository.ChatRepository
import com.example.fitsync.domain.model.chat.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val chatRepository: ChatRepository,
    private val prefs: PreferenceManager, // 1. INJECT PREFERENCES
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val todayDateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // For now, we hardcode this to match the PostgreSQL user we created.
    private val currentUserId = 1L

    val messages: StateFlow<List<ChatMessage>> = chatRepository.getMessagesForToday(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        // Fetch history from Spring Boot when the screen opens
        viewModelScope.launch {
            chatRepository.fetchChatsFromServer(currentUserId)
        }
    }

    // --- 2. INVISIBLE AI CONTEXT ---
    // This secretly attaches the user's settings to their prompt so Gemini knows
    // exactly who it's talking to without ruining the UI message bubble.
    private fun buildEnrichedPrompt(rawText: String): String {
        val name = prefs.getUserName()
        val goal = prefs.getUserGoal()
        val unit = if (prefs.isMetric()) "kg" else "lbs"

        return """
            [System Instructions: The user you are talking to is named $name. 
            Their primary fitness goal is to "$goal". 
            They use $unit for weight tracking. Tailor your advice to fit these parameters.]
            
            User Message: $rawText
        """.trimIndent()
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        // 3. Save the clean, normal text to the database (This is what the user sees in the UI)
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            sentByUser = true
        )

        viewModelScope.launch {
            chatRepository.insertMessage(userMessage, todayDateString, currentUserId)

            _isTyping.value = true

            // 4. Send the ENRICHED text to Gemini (This is what the AI sees)
            val enrichedPrompt = buildEnrichedPrompt(text)
            val result = geminiService.sendChatMessage(enrichedPrompt)

            result.onSuccess { reply ->
                saveBotMessage(reply)
            }
            result.onFailure {
                saveBotMessage("I'm having a little trouble connecting right now. Please try again in a moment! 😅")
            }

            _isTyping.value = false
        }
    }

    fun analyzeMealImage(bitmap: Bitmap, userPrompt: String = "") {
        val displayMessage = if (userPrompt.isNotBlank()) userPrompt else "Can you analyze this?"

        viewModelScope.launch {
            val savedImagePath = withContext(Dispatchers.IO) {
                ImageStorageHelper.saveBitmapToCache(context, bitmap)
            }

            // Save clean message to DB
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = displayMessage,
                sentByUser = true,
                imageLocalPath = savedImagePath
            )

            chatRepository.insertMessage(userMessage, todayDateString, currentUserId)

            _isTyping.value = true

            // Send ENRICHED prompt to Gemini Vision so it calculates macros based on the user's goals
            val enrichedPrompt = buildEnrichedPrompt(displayMessage)
            val result = geminiService.analyzeMealImage(bitmap, enrichedPrompt)

            result.onSuccess { parsedMacros ->
                if (parsedMacros.isFood) {
                    val botMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = parsedMacros.nutritionistReply.ifBlank { "Here is the breakdown of your meal:" },
                        sentByUser = false,
                        isAnalysis = true,
                        mealName = parsedMacros.mealName,
                        estimatedQuantity = parsedMacros.estimatedQuantity,
                        macros = parsedMacros
                    )
                    chatRepository.insertMessage(botMessage, todayDateString, currentUserId)
                } else {
                    saveBotMessage(parsedMacros.nonFoodMessage)
                }
            }

            result.onFailure { error ->
                saveBotMessage("Oops! I had trouble seeing that clearly. Make sure the food is well-lit and try again. 😅")
            }

            _isTyping.value = false
        }
    }

    private suspend fun saveBotMessage(text: String) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            sentByUser = false
        )
        chatRepository.insertMessage(msg, todayDateString, currentUserId)
    }

    fun clearTodayChat() {
        viewModelScope.launch {
            chatRepository.clearChatsForToday(todayDateString)
        }
    }
}