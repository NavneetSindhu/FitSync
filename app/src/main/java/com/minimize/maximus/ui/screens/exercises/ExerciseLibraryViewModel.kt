package com.minimize.maximus.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.exercise.EquipmentType
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.domain.repository.IRoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseLibraryUiState(
    val allExercises: List<ExerciseDefinition> = emptyList(),
    val filteredExercises: List<ExerciseDefinition> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleFilter: MuscleGroup? = null,
    val selectedEquipmentFilter: EquipmentType? = null
)

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val routineRepository: IRoutineRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMuscle = MutableStateFlow<MuscleGroup?>(null)
    private val _selectedEquipment = MutableStateFlow<EquipmentType?>(null)

    val uiState: StateFlow<ExerciseLibraryUiState> = combine(
        routineRepository.getAllExercises(),
        _searchQuery,
        _selectedMuscle,
        _selectedEquipment
    ) { exercises, query, muscle, equipment ->
        val filtered = exercises.filter { ex ->
            val matchesQuery = query.isBlank() || ex.name.contains(query, ignoreCase = true)
            val matchesMuscle = muscle == null || ex.primaryMuscle == muscle || ex.secondaryMuscles.contains(muscle)
            val matchesEquipment = equipment == null || ex.equipment == equipment
            matchesQuery && matchesMuscle && matchesEquipment
        }
        ExerciseLibraryUiState(
            allExercises = exercises,
            filteredExercises = filtered,
            searchQuery = query,
            selectedMuscleFilter = muscle,
            selectedEquipmentFilter = equipment
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseLibraryUiState())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectMuscleFilter(muscle: MuscleGroup?) {
        _selectedMuscle.value = if (_selectedMuscle.value == muscle) null else muscle
    }

    fun selectEquipmentFilter(equipment: EquipmentType?) {
        _selectedEquipment.value = if (_selectedEquipment.value == equipment) null else equipment
    }

    fun createCustomExercise(
        name: String,
        primaryMuscle: MuscleGroup,
        equipment: EquipmentType,
        onCreated: (ExerciseDefinition) -> Unit
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val def = ExerciseDefinition(
                name = name.trim(),
                primaryMuscle = primaryMuscle,
                equipment = equipment,
                isCustom = true
            )
            val newId = routineRepository.saveCustomExercise(def)
            onCreated(def.copy(id = newId))
        }
    }
}
