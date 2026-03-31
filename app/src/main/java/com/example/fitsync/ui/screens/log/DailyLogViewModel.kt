package com.example.fitsync.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.entity.WorkoutEntity
import com.example.fitsync.data.repository.WorkoutRepository
import com.example.fitsync.domain.model.Exercise
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private val _userId = MutableStateFlow("FIT-GUEST")
    val userId: StateFlow<String> = _userId.asStateFlow()

    init {
        // Generating a unique ID. Ideally, this should be saved to DataStore later.
        _userId.value = "FIT-" + UUID.randomUUID().toString().take(8).uppercase()
        loadTodayWorkout()
    }

    /**
     * Point 2: Optimized Save logic.
     * Instead of just inserting, we want the repository to know it should
     * eventually update the full "Journal" bin on JSONbin.
     */
    fun saveWorkout() {
        viewModelScope.launch {
            // Only save if there are actual exercises logged
            if (_uiState.value.exercises.isNotEmpty()) {
                val workout = WorkoutEntity(
                    date = System.currentTimeMillis(),
                    exerciseList = _uiState.value.exercises,
                    isSynced = false
                )
                repository.insert(workout, _userId.value)
            }
        }
    }

    private fun loadTodayWorkout() {
        val today = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
        // Start with an empty list for a cleaner "Current Session" look
        _uiState.update { it.copy(date = today, exercises = emptyList()) }
    }

    /**
     * Point 4: Delete the entire Exercise Card
     */
    fun removeExercise(exerciseName: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.exercises.filter { it.name != exerciseName }
            currentState.copy(exercises = updatedList)
        }
    }

    fun addExercise(exerciseName: String) {
        _uiState.update { currentState ->
            // Prevent adding the same exercise twice in one session
            if (currentState.exercises.any { it.name == exerciseName }) return@update currentState

            val newExercise = Exercise(
                name = exerciseName,
                sets = listOf(WorkoutSet(setNumber = 1, weight = 0f, reps = 0, isCompleted = false))
            )
            currentState.copy(exercises = currentState.exercises + newExercise)
        }
    }

    fun addSet(exerciseName: String) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.exercises.map { exercise ->
                if (exercise.name == exerciseName) {
                    val nextSetNumber = exercise.sets.size + 1
                    // FIX: Ensure parameters match WorkoutSet(Int, Float, Int, Boolean)
                    exercise.copy(sets = exercise.sets + WorkoutSet(nextSetNumber, 0, 0f, false))
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
                            set.copy(isCompleted = !set.isCompleted)
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