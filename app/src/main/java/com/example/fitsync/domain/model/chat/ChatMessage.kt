package com.example.fitsync.domain.model.chat

import android.graphics.Bitmap

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val isAnalysis: Boolean = false,
    val mealName: String? = null,
    val estimatedQuantity: String? = null, // <--- NEW
    val macros: Macros? = null,
    val imageRes: Int? = null,
    val imageBitmap: android.graphics.Bitmap? = null
)