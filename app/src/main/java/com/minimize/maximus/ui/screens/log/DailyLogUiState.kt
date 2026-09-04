package com.minimize.maximus.ui.screens.log

import com.minimize.maximus.domain.model.Exercise

data class DailyLogUiState(
    val date: String = "",
    val workoutTimestamp: Long = System.currentTimeMillis(),
    val workoutName: String = "Custom Workout",
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,

    // Live Session Stopwatch
    val elapsedWorkoutSeconds: Long = 0L,
    val isWorkoutTimerRunning: Boolean = true,
    val isEditingExistingWorkout: Boolean = false,

    // Rest Timer Engine
    val restTimerTotalSeconds: Int = 90,
    val restTimerRemainingSeconds: Int = 90,
    val isRestTimerRunning: Boolean = false,
    val isRestTimerVisible: Boolean = false
)
