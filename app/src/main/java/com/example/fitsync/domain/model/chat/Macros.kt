package com.example.fitsync.domain.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class Macros(
    val isFood: Boolean,
    val mealName: String,
    val estimatedQuantity: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val nonFoodMessage: String = "",
    val nutritionistReply: String = ""
)
