package com.minimize.maximus.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.domain.model.routine.RoutineExercise
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.domain.repository.IRoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineBuilderUiState(
    val routineId: Long = 0L,
    val name: String = "",
    val description: String = "",
    val targetMuscleGroups: List<String> = emptyList(),
    val exercises: List<RoutineExercise> = emptyList(),
    val estimatedDurationMinutes: Int = 45,
    val colorTagHex: String = "#FF5722",
    val isSaved: Boolean = false
)

@HiltViewModel
class RoutineBuilderViewModel @Inject constructor(
    private val routineRepository: IRoutineRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineBuilderUiState())
    val uiState: StateFlow<RoutineBuilderUiState> = _uiState.asStateFlow()

    init {
        val routineId: Long? = savedStateHandle.get<Long>("routineId")?.takeIf { it > 0 }
        if (routineId != null) {
            viewModelScope.launch {
                routineRepository.getRoutineById(routineId)?.let { routine ->
                    _uiState.value = RoutineBuilderUiState(
                        routineId = routine.id,
                        name = routine.name,
                        description = routine.description,
                        targetMuscleGroups = routine.targetMuscleGroups,
                        exercises = routine.exercises,
                        estimatedDurationMinutes = routine.estimatedDurationMinutes,
                        colorTagHex = routine.colorTagHex
                    )
                }
            }
        }

        // Observe selected exercise returned from ExerciseLibraryScreen
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>("selected_exercise_name", null)
                .collect { exName ->
                    if (!exName.isNullOrBlank()) {
                        addExerciseByName(exName)
                        savedStateHandle["selected_exercise_name"] = null
                    }
                }
        }
    }

    fun addExerciseByName(name: String) {
        _uiState.update { current ->
            val nextOrder = current.exercises.size
            val routineEx = RoutineExercise(
                exerciseName = name,
                targetSets = 3,
                targetReps = 10,
                targetRepsRange = "8-12",
                defaultWeight = 0f,
                restSeconds = 90,
                orderIndex = nextOrder
            )
            current.copy(exercises = current.exercises + routineEx)
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun toggleMuscleGroup(muscle: MuscleGroup) {
        _uiState.update { current ->
            val list = current.targetMuscleGroups.toMutableList()
            if (list.contains(muscle.name)) {
                list.remove(muscle.name)
            } else {
                list.add(muscle.name)
            }
            current.copy(targetMuscleGroups = list)
        }
    }

    fun addExercise(def: ExerciseDefinition) {
        _uiState.update { current ->
            val nextOrder = current.exercises.size
            val routineEx = RoutineExercise(
                exerciseName = def.name,
                targetSets = def.defaultSets,
                targetReps = def.defaultReps,
                targetRepsRange = "${def.defaultReps - 2}-${def.defaultReps + 2}",
                defaultWeight = def.defaultWeight,
                restSeconds = 90,
                orderIndex = nextOrder
            )
            current.copy(exercises = current.exercises + routineEx)
        }
    }

    fun removeExercise(index: Int) {
        _uiState.update { current ->
            if (index in current.exercises.indices) {
                val list = current.exercises.toMutableList().apply { removeAt(index) }
                current.copy(exercises = list.mapIndexed { idx, ex -> ex.copy(orderIndex = idx) })
            } else current
        }
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            if (fromIndex in current.exercises.indices && toIndex in current.exercises.indices) {
                val list = current.exercises.toMutableList()
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                current.copy(exercises = list.mapIndexed { idx, ex -> ex.copy(orderIndex = idx) })
            } else current
        }
    }

    fun updateExerciseSets(index: Int, sets: Int) {
        _uiState.update { current ->
            if (index in current.exercises.indices) {
                val list = current.exercises.toMutableList()
                list[index] = list[index].copy(targetSets = sets.coerceAtLeast(1))
                current.copy(exercises = list)
            } else current
        }
    }

    fun updateExerciseReps(index: Int, reps: Int) {
        _uiState.update { current ->
            if (index in current.exercises.indices) {
                val list = current.exercises.toMutableList()
                list[index] = list[index].copy(
                    targetReps = reps.coerceAtLeast(1),
                    targetRepsRange = "${reps.coerceAtLeast(1)}-${reps + 2}"
                )
                current.copy(exercises = list)
            } else current
        }
    }

    fun updateExerciseDefaultWeight(index: Int, weight: Float) {
        _uiState.update { current ->
            if (index in current.exercises.indices) {
                val list = current.exercises.toMutableList()
                list[index] = list[index].copy(defaultWeight = weight.coerceAtLeast(0f))
                current.copy(exercises = list)
            } else current
        }
    }

    fun updateExerciseRestSeconds(index: Int, seconds: Int) {
        _uiState.update { current ->
            if (index in current.exercises.indices) {
                val list = current.exercises.toMutableList()
                list[index] = list[index].copy(restSeconds = seconds)
                current.copy(exercises = list)
            } else current
        }
    }

    fun saveRoutine(onSaved: () -> Unit) {
        val current = _uiState.value
        if (current.name.isBlank()) return

        viewModelScope.launch {
            val routine = WorkoutRoutine(
                id = current.routineId,
                name = current.name,
                description = current.description,
                targetMuscleGroups = current.targetMuscleGroups,
                exercises = current.exercises,
                estimatedDurationMinutes = current.estimatedDurationMinutes,
                colorTagHex = current.colorTagHex,
                isCustom = true,
                isDefaultTemplate = false
            )
            routineRepository.saveRoutine(routine)
            _uiState.update { it.copy(isSaved = true) }
            onSaved()
        }
    }
}
