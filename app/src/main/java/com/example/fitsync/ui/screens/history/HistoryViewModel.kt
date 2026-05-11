package com.example.fitsync.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.PreferenceManager
import com.example.fitsync.data.repository.WorkoutRepository
import com.example.fitsync.domain.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val prefs: PreferenceManager // 1. INJECT PREFERENCES
) : ViewModel() {

    // 2. EXPOSE THE DYNAMIC WEIGHT UNIT
    private val _weightUnit = MutableStateFlow(if (prefs.isMetric()) "kg" else "lbs")
    val weightUnit: StateFlow<String> = _weightUnit.asStateFlow()

    val workoutHistory: StateFlow<List<WorkoutSession>> = repository.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteWorkout(workout: WorkoutSession) {
        viewModelScope.launch {
            repository.delete(workout)
            // Note: Room will automatically update the Flow,
            // so the UI will refresh itself!
        }
    }

    /**
     * Refreshes settings when returning to this screen
     * in case the user changed units in the Settings tab.
     */
    fun refreshSettings() {
        _weightUnit.value = if (prefs.isMetric()) "kg" else "lbs"
    }
}