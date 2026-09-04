package com.minimize.maximus.domain.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class Macros(
    val isFood: Boolean,
    val mealName: String = "",
    val estimatedQuantity: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val nonFoodMessage: String = "",
    val nutritionistReply: String = ""
)
