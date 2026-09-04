package com.minimize.maximus.domain.model.chat

import com.minimize.maximus.domain.model.routine.WorkoutRoutine

data class ChatMessage(
    val id: String,
    val text: String,
    val sentByUser: Boolean,
    val isAnalysis: Boolean = false,
    val mealName: String? = null,
    val estimatedQuantity: String? = null,
    val macros: Macros? = null,
    val routinePayload: WorkoutRoutine? = null,
    val isRoutineSaved: Boolean = false,
    val suggestedReplies: List<String> = emptyList(),
    val imageLocalPath: String? = null
)