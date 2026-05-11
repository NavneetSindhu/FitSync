package com.example.fitsync.ui.screens.log

import com.example.fitsync.domain.model.Exercise

data class DailyLogUiState(
    val date: String = "",
    val workoutName: String = "Custom Workout",
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false
)
