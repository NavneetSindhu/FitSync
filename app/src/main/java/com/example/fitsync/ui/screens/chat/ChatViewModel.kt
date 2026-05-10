package com.example.fitsync.ui.screens.chat

import android.content.Context // NEW: Required for Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.ImageStorageHelper // NEW: Import your helper
import com.example.fitsync.data.remote.GeminiService
import com.example.fitsync.data.repository.ChatRepository
import com.example.fitsync.domain.model.chat.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext // NEW: Required for Hilt injection
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
    @ApplicationContext private val context: Context // NEW: Safely inject the app context
) : ViewModel() {

    private val todayDateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val messages: StateFlow<List<ChatMessage>> = chatRepository.getMessagesForToday(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        // Optional: Ensure welcome message is there if needed
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isUser = true
        )

        viewModelScope.launch {
            chatRepository.insertMessage(userMessage, todayDateString)

            _isTyping.value = true

            val result = geminiService.sendChatMessage(text)

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
            // 1. Move file saving to a background thread so it doesn't freeze the UI
            val savedImagePath = withContext(Dispatchers.IO) {
                ImageStorageHelper.saveBitmapToCache(context, bitmap)
            }

            // 2. Create the ChatMessage using the PATH, not the Bitmap
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = displayMessage,
                isUser = true,
                imageLocalPath = savedImagePath // CHANGED: Pass the path string here
            )

            // 3. Save to Room DB
            chatRepository.insertMessage(userMessage, todayDateString)

            _isTyping.value = true

            // 4. Send the raw Bitmap to Gemini via the network
            val result = geminiService.analyzeMealImage(bitmap, userPrompt)

            result.onSuccess { parsedMacros ->
                if (parsedMacros.isFood) {
                    val botMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = parsedMacros.nutritionistReply.ifBlank { "Here is the breakdown of your meal:" },
                        isUser = false,
                        isAnalysis = true,
                        mealName = parsedMacros.mealName,
                        estimatedQuantity = parsedMacros.estimatedQuantity,
                        macros = parsedMacros
                    )
                    chatRepository.insertMessage(botMessage, todayDateString)
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
            isUser = false
        )
        chatRepository.insertMessage(msg, todayDateString)
    }

    fun clearTodayChat() {
        viewModelScope.launch {
            chatRepository.clearChatsForToday(todayDateString)

            // Optional: You can re-add the welcome message here if you want it to appear immediately after clearing
            // addBotMessage("Hi! I'm Sync AI. Send me a photo of your meal...")
        }
    }
}