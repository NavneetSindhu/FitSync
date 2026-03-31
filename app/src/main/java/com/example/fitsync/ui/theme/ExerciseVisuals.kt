package com.example.fitsync.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

data class ExerciseMetaData(
    val icon: ImageVector,
    val accentColor: Color,
    val category: String
)

object ExerciseVisuals {
    private val default = ExerciseMetaData(Icons.Default.FitnessCenter, SoftGray, "Other")

    private val mapping = mapOf(
        "Barbell Squat" to ExerciseMetaData(Icons.Default.AccessibilityNew, Color(0xFF4CAF50), "Legs"),
        "Bench Press" to ExerciseMetaData(Icons.Default.LineWeight, Color(0xFF2196F3), "Chest"),
        "Deadlift" to ExerciseMetaData(Icons.Default.FitnessCenter, Color(0xFFF44336), "Back/Legs"),
        "Overhead Press" to ExerciseMetaData(Icons.Default.Upgrade, Color(0xFFFF9800), "Shoulders"),
        "Pull Ups" to ExerciseMetaData(Icons.Default.VerticalAlignTop, Color(0xFF9C27B0), "Back"),
        "Dips" to ExerciseMetaData(Icons.Default.KeyboardDoubleArrowDown, Color(0xFF00BCD4), "Triceps")
    )

    fun getMetaData(name: String): ExerciseMetaData = mapping[name] ?: default
}