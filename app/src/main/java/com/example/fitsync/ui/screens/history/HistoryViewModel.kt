package com.example.fitsync.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.repository.WorkoutRepository
import com.example.fitsync.domain.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    // FIX: Changed repository.allWorkouts to repository.getAllWorkouts()
    val workoutHistory: StateFlow<List<WorkoutSession>> = repository.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Add this function to your HistoryViewModel
    fun deleteWorkout(workout: WorkoutSession) {
        viewModelScope.launch {
            repository.delete(workout)
            // Note: Room will automatically update the Flow,
            // so the UI will refresh itself!
        }
    }
}