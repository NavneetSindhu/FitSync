package com.minimize.maximus.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.domain.manager.WorkoutManager
import com.minimize.maximus.domain.model.routine.SplitPresetType
import com.minimize.maximus.domain.model.routine.WeeklyPlan
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.domain.repository.IRoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

data class RoutinesUiState(
    val routines: List<WorkoutRoutine> = emptyList(),
    val defaultTemplates: List<WorkoutRoutine> = emptyList(),
    val weeklyPlan: WeeklyPlan = WeeklyPlan(),
    val selectedTab: Int = 0, // 0: My Routines, 1: Weekly Plan, 2: Templates
    val isLoading: Boolean = false
)

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineRepository: IRoutineRepository,
    private val workoutManager: WorkoutManager
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)

    val uiState: StateFlow<RoutinesUiState> = combine(
        routineRepository.getAllRoutines(),
        routineRepository.getWeeklyPlan(),
        _selectedTab
    ) { routines, plan, tab ->
        val customRoutines = routines.filter { it.isCustom && !it.isDefaultTemplate }
        val templates = routines.filter { it.isDefaultTemplate || !it.isCustom }
        RoutinesUiState(
            routines = customRoutines,
            defaultTemplates = templates,
            weeklyPlan = plan,
            selectedTab = tab,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoutinesUiState(isLoading = true))

    init {
        viewModelScope.launch {
            routineRepository.seedDefaultTemplatesIfEmpty()
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun startWorkoutFromRoutine(routine: WorkoutRoutine, onStarted: () -> Unit) {
        workoutManager.startFromRoutine(routine)
        onStarted()
    }

    fun duplicateRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.duplicateRoutine(routineId)
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
        }
    }

    fun applySplitPreset(preset: SplitPresetType) {
        viewModelScope.launch {
            routineRepository.applySplitPreset(preset)
        }
    }

    fun assignRoutineToDay(day: DayOfWeek, routineId: Long?, routineName: String?) {
        viewModelScope.launch {
            routineRepository.assignRoutineToDay(day, routineId, routineName)
        }
    }
}
