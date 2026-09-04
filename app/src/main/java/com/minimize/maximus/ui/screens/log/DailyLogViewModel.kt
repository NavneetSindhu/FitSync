package com.minimize.maximus.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.domain.model.Exercise
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.domain.model.WorkoutSet
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.exercise.EquipmentType
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.domain.repository.IRoutineRepository
import com.minimize.maximus.domain.repository.IWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: IWorkoutRepository,
    private val routineRepository: IRoutineRepository,
    private val workoutManager: com.minimize.maximus.domain.manager.WorkoutManager,
    private val prefs: PreferenceManager
) : ViewModel() {

    val allExercises: StateFlow<List<ExerciseDefinition>> = routineRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private val _userId = MutableStateFlow(1L)
    val userId: StateFlow<Long> = _userId.asStateFlow()

    private var restTimerJob: Job? = null
    private var sessionStopwatchJob: Job? = null

    init {
        // Sync seamlessly with globally scoped WorkoutManager
        viewModelScope.launch {
            workoutManager.activeWorkout.collect { active ->
                if (active != null) {
                    val dateString = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(active.startTimestamp))
                    _uiState.update {
                        it.copy(
                            workoutName = active.sessionName,
                            workoutTimestamp = active.startTimestamp,
                            date = dateString,
                            exercises = active.exercises,
                            elapsedWorkoutSeconds = active.elapsedSeconds,
                            isWorkoutTimerRunning = active.isTimerRunning,
                            isRestTimerVisible = active.isRestTimerRunning,
                            isRestTimerRunning = active.isRestTimerRunning,
                            restTimerRemainingSeconds = active.restTimerRemainingSeconds,
                            restTimerTotalSeconds = active.restTimerTotalSeconds,
                            isEditingExistingWorkout = false
                        )
                    }
                }
            }
        }
    }

    val weightUnit: StateFlow<String> = MutableStateFlow(if (prefs.isMetric()) "kg" else "lbs").asStateFlow()

    fun minimizeWorkout(onMinimized: () -> Unit = {}) {
        workoutManager.minimizeWorkout()
        onMinimized()
    }

    fun finishWorkout(onSaved: () -> Unit = {}) {
        saveWorkout(onSaved)
    }

    fun discardWorkout(onDiscarded: () -> Unit = {}) {
        workoutManager.discardWorkout()
        _uiState.update { DailyLogUiState(isWorkoutTimerRunning = false) }
        onDiscarded()
    }

    fun toggleWorkoutTimerPause() {
        workoutManager.toggleTimerPause()
    }

    private var editingWorkoutId: Long? = null

    fun updateWorkoutName(newName: String) {
        if (_uiState.value.isEditingExistingWorkout) {
            _uiState.update { it.copy(workoutName = newName) }
        } else {
            workoutManager.updateSessionName(newName)
            _uiState.update { it.copy(workoutName = newName) }
        }
    }

    fun startWorkoutSession(name: String, timestamp: Long = System.currentTimeMillis()) {
        editingWorkoutId = null
        workoutManager.startEmptyWorkout(name)
    }

    fun loadWorkoutForEditing(workout: WorkoutSession) {
        editingWorkoutId = workout.id
        val dateString = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(workout.date))
        _uiState.update {
            it.copy(
                workoutName = workout.name,
                workoutTimestamp = workout.date,
                date = dateString,
                exercises = workout.exercise,
                isEditingExistingWorkout = true,
                elapsedWorkoutSeconds = 0L,
                isWorkoutTimerRunning = false,
                isRestTimerVisible = false,
                isRestTimerRunning = false
            )
        }
    }

    fun saveWorkout(onSaved: () -> Unit = {}) {
        val currentState = _uiState.value
        if (currentState.isEditingExistingWorkout && editingWorkoutId != null) {
            viewModelScope.launch {
                val workout = WorkoutSession(
                    id = editingWorkoutId ?: 0L,
                    name = currentState.workoutName,
                    date = currentState.workoutTimestamp,
                    exercise = currentState.exercises,
                    isSynced = false
                )
                repository.insert(workout, _userId.value)
                editingWorkoutId = null
                val today = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
                _uiState.update { DailyLogUiState(date = today, workoutTimestamp = System.currentTimeMillis()) }
                onSaved()
            }
        } else {
            workoutManager.finishWorkout(onSaved)
        }
    }

    // ── Rest Timer Engine ──────────────────────────────────────────────────────

    fun startRestTimer(totalSeconds: Int = 90) {
        workoutManager.startRestTimer(totalSeconds)
    }

    fun addRestSeconds(deltaSeconds: Int) {
        workoutManager.addRestTimerSeconds(deltaSeconds)
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        workoutManager.addRestTimerSeconds(deltaSeconds)
    }

    fun toggleRestTimerPause() {
        // Handled via workout manager
    }

    fun dismissRestTimer() {
        workoutManager.dismissRestTimer()
    }

    // ── Exercises & Sets Operations ───────────────────────────────────────────

    fun addExercise(exerciseName: String) {
        val isBarbellMovement = listOf("Barbell", "Bench Press", "Squat", "Deadlift", "Overhead Press")
            .any { exerciseName.contains(it, ignoreCase = true) }

        val startingWeight = if (isBarbellMovement) prefs.getDefaultBarbellWeight().toFloat() else 0f
        workoutManager.addExercise(name = exerciseName, defaultSets = 3, defaultReps = 10, defaultWeight = startingWeight)
    }

    fun createCustomExercise(
        name: String,
        primaryMuscle: MuscleGroup,
        equipment: EquipmentType,
        onCreated: (String) -> Unit
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val def = ExerciseDefinition(
                name = name.trim(),
                primaryMuscle = primaryMuscle,
                equipment = equipment,
                isCustom = true
            )
            routineRepository.saveCustomExercise(def)
            addExercise(def.name)
            onCreated(def.name)
        }
    }

    fun removeExercise(exerciseName: String) {
        workoutManager.removeExercise(exerciseName)
    }

    fun addSet(exerciseName: String) {
        workoutManager.addSetToExercise(exerciseName)
    }

    fun updateSet(exerciseName: String, setNumber: Int, weight: Float, reps: Int) {
        workoutManager.updateSet(exerciseName, setNumber - 1, weight, reps)
    }

    fun updateSetType(exerciseName: String, setNumber: Int, newType: String) {
        workoutManager.cycleSetType(exerciseName, setNumber - 1)
    }

    fun toggleSetCompletion(exerciseName: String, setNumber: Int) {
        workoutManager.toggleSetCompletion(exerciseName, setNumber - 1)
    }

    fun deleteSet(exerciseName: String, setNumber: Int) {
        workoutManager.removeSetFromExercise(exerciseName, setNumber - 1)
    }

    fun deleteExercise(exerciseName: String) = removeExercise(exerciseName)

    fun toggleSetComplete(exerciseName: String, setNumber: Int) = toggleSetCompletion(exerciseName, setNumber)
}