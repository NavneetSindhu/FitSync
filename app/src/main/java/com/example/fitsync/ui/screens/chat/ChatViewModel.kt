package com.example.fitsync.ui.screens.chat

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.remote.GeminiService
import com.example.fitsync.domain.model.chat.ChatMessage
import com.example.fitsync.domain.model.chat.Macros
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val geminiService: GeminiService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        addBotMessage("Hi! I'm Sync AI. Send me a photo of your meal and I'll break down the macros, or just ask me a nutrition question! 🥗")
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add user's text to the UI
        _messages.update { currentList ->
            currentList + ChatMessage(
                id = UUID.randomUUID().toString(),
                text = text,
                isUser = true
            )
        }

        // 2. Call Gemini for a text response
        viewModelScope.launch {
            _isTyping.value = true

            // We pass the new text to our service
            val result = geminiService.sendChatMessage(text)

            result.onSuccess { reply ->
                addBotMessage(reply)
            }
            result.onFailure {
                addBotMessage("I'm having a little trouble connecting right now. Please try again in a moment! 😅")
            }

            _isTyping.value = false
        }
    }

    fun analyzeMealImage(bitmap: Bitmap) {
        _messages.update { currentList ->
            currentList + ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Can you analyze this?",
                isUser = true,
                imageBitmap = bitmap
            )
        }

        viewModelScope.launch {
            _isTyping.value = true

            val result = geminiService.analyzeMealImage(bitmap)

            result.onSuccess { parsedMacros ->
                if (parsedMacros.isFood) {
                    _messages.update { currentList ->
                        currentList + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = "Here is the breakdown based on the estimated portion:",
                            isUser = false,
                            isAnalysis = true,
                            mealName = parsedMacros.mealName,
                            estimatedQuantity = parsedMacros.estimatedQuantity,
                            macros = parsedMacros
                        )
                    }
                } else {
                    addBotMessage(parsedMacros.nonFoodMessage)
                }
            }

            result.onFailure { error ->
                addBotMessage("Oops! I had trouble seeing that clearly. Make sure the food is well-lit and try again. 😅")
            }

            _isTyping.value = false
        }
    }

    private fun addBotMessage(text: String) {
        _messages.update { currentList ->
            currentList + ChatMessage(
                id = UUID.randomUUID().toString(),
                text = text,
                isUser = false
            )
        }
    }
}