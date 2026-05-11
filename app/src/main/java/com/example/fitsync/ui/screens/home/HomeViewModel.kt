package com.example.fitsync.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.repository.WorkoutRepository
import com.example.fitsync.domain.model.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    // Keep your user name logic here (this can eventually come from Firebase!)
    private val _userName = MutableStateFlow("Navneet")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // --- THE REAL DATA ENGINE ---
    // We observe Room. Every time a workout is added, this recalculates instantly.
    val workoutHistory: StateFlow<Map<LocalDate, WorkoutSummary>> = workoutRepository.getAllWorkouts()
        .map { workouts ->
            val calendarData = mutableMapOf<LocalDate, WorkoutSummary>()

            workouts.forEach { workout ->
                // 1. Convert the Long timestamp to a LocalDate for the Heatmap
                val date = Instant.ofEpochMilli(workout.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                // 2. Crunch the numbers for Volume and Sets
                var totalVolume = 0.0
                var totalSets = 0

                workout.exercise.forEach { exercise ->
                    totalSets += exercise.sets.size
                    exercise.sets.forEach { set ->
                        totalVolume += (set.reps * set.weight)
                    }
                }

                // 3. Calculate intensity for the Heatmap colors (0 to 4)
                val intensityLevel = when {
                    totalSets == 0 -> 0
                    totalSets in 1..5 -> 1
                    totalSets in 6..12 -> 2
                    totalSets in 13..20 -> 3
                    else -> 4
                }

                // 4. Save it into the map!
                calendarData[date] = WorkoutSummary(
                    intensity = intensityLevel,
                    volume = "${totalVolume.toInt()} kg",
                    sets = totalSets
                )
            }

            calendarData // Return the completed map to the UI
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}