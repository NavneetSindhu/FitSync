package com.example.fitsync.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.PreferenceManager
import com.example.fitsync.data.repository.WorkoutRepository
import com.example.fitsync.domain.model.Exercise
import com.example.fitsync.domain.model.WorkoutSession
import com.example.fitsync.domain.model.WorkoutSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val prefs: PreferenceManager // 1. INJECT PREFERENCES HERE
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private val _userId = MutableStateFlow(1L)
    val userId: StateFlow<Long> = _userId.asStateFlow()

    init {
        loadTodayWorkout()
    }

    /**
     * Save the current session to Room and Cloud.
     */
    fun saveWorkout() {
        viewModelScope.launch {
            if (_uiState.value.exercises.isNotEmpty()) {
                val workout = WorkoutSession(
                    id = 0, // 🔥 Room uses 0 as a signal to auto-generate a new Long ID
                    date = System.currentTimeMillis(),
                    exercise = _uiState.value.exercises,
                    isSynced = false
                )
                repository.insert(workout, _userId.value)
            }
        }
    }

    private fun loadTodayWorkout() {
        val today = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
        _uiState.update { it.copy(date = today, exercises = emptyList()) }
    }

    fun removeExercise(exerciseName: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.exercises.filter { it.name != exerciseName }
            currentState.copy(exercises = updatedList)
        }
    }

    fun addExercise(exerciseName: String) {
        _uiState.update { currentState ->
            if (currentState.exercises.any { it.name == exerciseName }) return@update currentState

            // 2. SMART BARBELL DEFAULTS
            // If the exercise is a major barbell movement, start the weight at the empty bar setting.
            val isBarbellMovement = listOf("Barbell", "Bench Press", "Squat", "Deadlift", "Overhead Press")
                .any { exerciseName.contains(it, ignoreCase = true) }

            val startingWeight = if (isBarbellMovement) prefs.getDefaultBarbellWeight().toFloat() else 0f

            val newExercise = Exercise(
                name = exerciseName,
                sets = listOf(WorkoutSet(reps = 0, setNumber = 1, weight = startingWeight, isCompleted = false))
            )
            currentState.copy(exercises = currentState.exercises + newExercise)
        }
    }

    fun addSet(exerciseName: String) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.exercises.map { exercise ->
                if (exercise.name == exerciseName) {
                    val nextSetNumber = exercise.sets.size + 1

                    // 3. SMART CARRY-OVER
                    // Automatically copy the weight and reps from the previous set so the user doesn't have to re-type it.
                    val lastSet = exercise.sets.lastOrNull()
                    val nextWeight = lastSet?.weight ?: 0f
                    val nextReps = lastSet?.reps ?: 0

                    exercise.copy(sets = exercise.sets + WorkoutSet(nextReps, nextSetNumber, nextWeight, false))
                } else exercise
            }
            currentState.copy(exercises = updatedExercises)
        }
    }

    fun updateSet(exerciseName: String, setNumber: Int, weight: Float, reps: Int) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.exercises.map { exercise ->
                if (exercise.name == exerciseName) {
                    val updatedSets = exercise.sets.map { set ->
                        if (set.setNumber == setNumber) {
                            set.copy(weight = weight, reps = reps)
                        } else set
                    }
                    exercise.copy(sets = updatedSets)
                } else exercise
            }
            currentState.copy(exercises = updatedExercises)
        }
    }

    fun toggleSetCompletion(exerciseName: String, setNumber: Int) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.exercises.map { exercise ->
                if (exercise.name == exerciseName) {
                    val updatedSets = exercise.sets.map { set ->
                        if (set.setNumber == setNumber) {
                            val isNowCompleted = !set.isCompleted

                            // NOTE: If you build a floating Rest Timer later,
                            // you can trigger it right here if `isNowCompleted` && `prefs.isAutoStartRestEnabled()`

                            set.copy(isCompleted = isNowCompleted)
                        } else set
                    }
                    exercise.copy(sets = updatedSets)
                } else exercise
            }
            currentState.copy(exercises = updatedExercises)
        }
    }

    fun deleteSet(exerciseName: String, setNumber: Int) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.exercises.map { exercise ->
                if (exercise.name == exerciseName) {
                    val filteredSets = exercise.sets.filter { it.setNumber != setNumber }
                    val reIndexedSets = filteredSets.mapIndexed { index, set ->
                        set.copy(setNumber = index + 1)
                    }
                    exercise.copy(sets = reIndexedSets)
                } else exercise
            }
            currentState.copy(exercises = updatedExercises)
        }
    }
}