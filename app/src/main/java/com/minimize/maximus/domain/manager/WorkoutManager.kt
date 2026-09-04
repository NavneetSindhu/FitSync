package com.minimize.maximus.domain.manager

import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.domain.model.Exercise
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.domain.model.WorkoutSet
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.domain.repository.IRoutineRepository
import com.minimize.maximus.domain.repository.IWorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveLiveWorkout(
    val sessionName: String,
    val routineId: Long? = null,
    val startTimestamp: Long = System.currentTimeMillis(),
    val exercises: List<Exercise> = emptyList(),
    val elapsedSeconds: Long = 0L,
    val isTimerRunning: Boolean = true,
    val isMinimized: Boolean = false,
    val restTimerRemainingSeconds: Int = 0,
    val restTimerTotalSeconds: Int = 90,
    val isRestTimerRunning: Boolean = false
)

@Singleton
class WorkoutManager @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val routineRepository: IRoutineRepository,
    private val preferenceManager: PreferenceManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _activeWorkout = MutableStateFlow<ActiveLiveWorkout?>(null)
    val activeWorkout: StateFlow<ActiveLiveWorkout?> = _activeWorkout.asStateFlow()

    private var stopwatchJob: Job? = null
    private var restTimerJob: Job? = null

    fun startFromRoutine(routine: WorkoutRoutine) {
        val routineExercises = if (routine.exercises.isNotEmpty()) {
            routine.exercises
        } else {
            val name = routine.name.lowercase()
            when {
                name.contains("push") -> listOf(
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Bench Press", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Incline Dumbbell Press", targetSets = 3, targetReps = 10, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Overhead Shoulder Press", targetSets = 3, targetReps = 10, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Tricep Rope Pushdown", targetSets = 3, targetReps = 12, defaultWeight = 0f)
                )
                name.contains("pull") -> listOf(
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Deadlift", targetSets = 4, targetReps = 6, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Bent-Over Row", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Lat Pulldown", targetSets = 3, targetReps = 10, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Bicep Curl", targetSets = 3, targetReps = 12, defaultWeight = 0f)
                )
                name.contains("leg") -> listOf(
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Back Squat", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Romanian Deadlift", targetSets = 3, targetReps = 10, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Leg Press", targetSets = 3, targetReps = 12, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Calf Raises", targetSets = 4, targetReps = 15, defaultWeight = 0f)
                )
                name.contains("upper") -> listOf(
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Bench Press", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Row", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Overhead Press", targetSets = 3, targetReps = 10, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Pull Ups", targetSets = 3, targetReps = 10, defaultWeight = 0f)
                )
                name.contains("lower") -> listOf(
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Squat", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Romanian Deadlift", targetSets = 3, targetReps = 10, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Walking Lunges", targetSets = 3, targetReps = 12, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Hamstring Curls", targetSets = 3, targetReps = 12, defaultWeight = 0f)
                )
                else -> listOf(
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Back Squat", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Bench Press", targetSets = 4, targetReps = 8, defaultWeight = 0f),
                    com.minimize.maximus.domain.model.routine.RoutineExercise(exerciseName = "Barbell Row", targetSets = 4, targetReps = 8, defaultWeight = 0f)
                )
            }
        }

        val exercises = routineExercises.map { re ->
            val initialSets = (1..re.targetSets).map { setIndex ->
                WorkoutSet(
                    setNumber = setIndex,
                    reps = re.targetReps,
                    weight = re.defaultWeight,
                    isCompleted = false,
                    setType = "N"
                )
            }
            Exercise(name = re.exerciseName, sets = initialSets)
        }

        _activeWorkout.value = ActiveLiveWorkout(
            sessionName = routine.name,
            routineId = routine.id,
            startTimestamp = System.currentTimeMillis(),
            exercises = exercises,
            elapsedSeconds = 0L,
            isTimerRunning = true,
            isMinimized = false,
            restTimerTotalSeconds = preferenceManager.getDefaultRestSeconds()
        )
        startStopwatch()
    }

    fun startEmptyWorkout(name: String = "Freestyle Workout") {
        _activeWorkout.value = ActiveLiveWorkout(
            sessionName = name,
            routineId = null,
            startTimestamp = System.currentTimeMillis(),
            exercises = emptyList(),
            elapsedSeconds = 0L,
            isTimerRunning = true,
            isMinimized = false,
            restTimerTotalSeconds = preferenceManager.getDefaultRestSeconds()
        )
        startStopwatch()
    }

    fun updateSessionName(newName: String) {
        _activeWorkout.update { current ->
            current?.copy(sessionName = newName.ifBlank { "Custom Workout" })
        }
    }

    fun minimizeWorkout() {
        _activeWorkout.update { it?.copy(isMinimized = true) }
    }

    fun restoreWorkout() {
        _activeWorkout.update { it?.copy(isMinimized = false) }
    }

    fun toggleTimerPause() {
        val current = _activeWorkout.value ?: return
        val willRun = !current.isTimerRunning
        if (willRun) {
            _activeWorkout.update { it?.copy(isTimerRunning = true) }
            startStopwatch()
        } else {
            stopwatchJob?.cancel()
            _activeWorkout.update { it?.copy(isTimerRunning = false) }
        }
    }

    fun addExercise(name: String, defaultSets: Int = 3, defaultReps: Int = 10, defaultWeight: Float = 0f) {
        _activeWorkout.update { current ->
            if (current == null) return@update null
            val initialSets = (1..defaultSets).map { i ->
                WorkoutSet(setNumber = i, reps = defaultReps, weight = defaultWeight, isCompleted = false)
            }
            val newEx = Exercise(name = name, sets = initialSets)
            current.copy(exercises = current.exercises + newEx)
        }
    }

    fun removeExercise(exerciseName: String) {
        _activeWorkout.update { current ->
            if (current == null) return@update null
            current.copy(exercises = current.exercises.filter { it.name != exerciseName })
        }
    }

    fun addSetToExercise(exerciseName: String) {
        _activeWorkout.update { current ->
            if (current == null) return@update null
            val updated = current.exercises.map { ex ->
                if (ex.name == exerciseName) {
                    val nextNum = ex.sets.size + 1
                    val lastSet = ex.sets.lastOrNull()
                    val newSet = WorkoutSet(
                        setNumber = nextNum,
                        reps = lastSet?.reps ?: 10,
                        weight = lastSet?.weight ?: 0f,
                        isCompleted = false
                    )
                    ex.copy(sets = ex.sets + newSet)
                } else ex
            }
            current.copy(exercises = updated)
        }
    }

    fun removeSetFromExercise(exerciseName: String, setIndex: Int) {
        _activeWorkout.update { current ->
            if (current == null) return@update null
            val updated = current.exercises.map { ex ->
                if (ex.name == exerciseName && setIndex in ex.sets.indices) {
                    val remaining = ex.sets.toMutableList().apply { removeAt(setIndex) }
                    val reindexed = remaining.mapIndexed { idx, s -> s.copy(setNumber = idx + 1) }
                    ex.copy(sets = reindexed)
                } else ex
            }
            current.copy(exercises = updated)
        }
    }

    fun updateSet(exerciseName: String, setIndex: Int, weight: Float, reps: Int) {
        _activeWorkout.update { current ->
            if (current == null) return@update null
            val updated = current.exercises.map { ex ->
                if (ex.name == exerciseName && setIndex in ex.sets.indices) {
                    val newSets = ex.sets.toMutableList()
                    newSets[setIndex] = newSets[setIndex].copy(weight = weight, reps = reps)
                    ex.copy(sets = newSets)
                } else ex
            }
            current.copy(exercises = updated)
        }
    }

    fun toggleSetCompletion(exerciseName: String, setIndex: Int) {
        _activeWorkout.update { current ->
            if (current == null) return@update null
            var justCompleted = false
            val updated = current.exercises.map { ex ->
                if (ex.name == exerciseName && setIndex in ex.sets.indices) {
                    val newSets = ex.sets.toMutableList()
                    val target = newSets[setIndex]
                    val nextStatus = !target.isCompleted
                    if (nextStatus) justCompleted = true
                    newSets[setIndex] = target.copy(isCompleted = nextStatus)
                    ex.copy(sets = newSets)
                } else ex
            }

            if (justCompleted && preferenceManager.isAutoStartRestEnabled()) {
                startRestTimer(preferenceManager.getDefaultRestSeconds())
            }

            current.copy(exercises = updated)
        }
    }

    fun cycleSetType(exerciseName: String, setIndex: Int) {
        _activeWorkout.update { current ->
            if (current == null) return@update null
            val updated = current.exercises.map { ex ->
                if (ex.name == exerciseName && setIndex in ex.sets.indices) {
                    val newSets = ex.sets.toMutableList()
                    val curType = newSets[setIndex].setType
                    val nextType = when (curType) {
                        "N" -> "W"
                        "W" -> "D"
                        "D" -> "F"
                        else -> "N"
                    }
                    newSets[setIndex] = newSets[setIndex].copy(setType = nextType)
                    ex.copy(sets = newSets)
                } else ex
            }
            current.copy(exercises = updated)
        }
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _activeWorkout.update {
            it?.copy(
                restTimerTotalSeconds = seconds,
                restTimerRemainingSeconds = seconds,
                isRestTimerRunning = true
            )
        }
        restTimerJob = scope.launch {
            while (isActive) {
                delay(1000)
                val curRemaining = _activeWorkout.value?.restTimerRemainingSeconds ?: 0
                if (curRemaining <= 1) {
                    _activeWorkout.update { it?.copy(restTimerRemainingSeconds = 0, isRestTimerRunning = false) }
                    break
                } else {
                    _activeWorkout.update { it?.copy(restTimerRemainingSeconds = curRemaining - 1) }
                }
            }
        }
    }

    fun addRestTimerSeconds(seconds: Int) {
        val cur = _activeWorkout.value?.restTimerRemainingSeconds ?: 0
        _activeWorkout.update { it?.copy(restTimerRemainingSeconds = cur + seconds) }
    }

    fun dismissRestTimer() {
        restTimerJob?.cancel()
        _activeWorkout.update { it?.copy(isRestTimerRunning = false, restTimerRemainingSeconds = 0) }
    }

    fun finishWorkout(onSaved: () -> Unit = {}) {
        val current = _activeWorkout.value ?: return
        if (current.exercises.isNotEmpty()) {
            scope.launch {
                val session = WorkoutSession(
                    id = 0L,
                    name = current.sessionName,
                    date = current.startTimestamp,
                    exercise = current.exercises,
                    isSynced = false
                )
                workoutRepository.insert(session, 1L)
                current.routineId?.let { routineRepository.incrementRoutineUsage(it) }
                discardWorkout()
                onSaved()
            }
        } else {
            discardWorkout()
            onSaved()
        }
    }

    fun discardWorkout() {
        stopwatchJob?.cancel()
        restTimerJob?.cancel()
        _activeWorkout.value = null
    }

    private fun startStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = scope.launch {
            while (isActive) {
                delay(1000)
                _activeWorkout.update { it?.copy(elapsedSeconds = (it.elapsedSeconds + 1)) }
            }
        }
    }
}
