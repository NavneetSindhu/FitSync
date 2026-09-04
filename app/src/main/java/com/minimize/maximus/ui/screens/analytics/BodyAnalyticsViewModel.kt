package com.minimize.maximus.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.domain.model.body.BodyViewMode
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.body.MuscleStat
import com.minimize.maximus.domain.repository.IWorkoutRepository
import com.minimize.maximus.util.MuscleClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class BodyAnalyticsUiState(
    val isLoading: Boolean = true,
    val viewMode: BodyViewMode = BodyViewMode.FRONT,
    val muscleStats: Map<MuscleGroup, MuscleStat> = emptyMap(),
    val selectedMuscle: MuscleGroup? = MuscleGroup.CHEST,
    val overallRecoveryScore: Int = 100,
    val weightUnit: String = "kg"
)

@HiltViewModel
class BodyAnalyticsViewModel @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _viewMode = MutableStateFlow(BodyViewMode.FRONT)
    val viewMode: StateFlow<BodyViewMode> = _viewMode.asStateFlow()

    private val _selectedMuscle = MutableStateFlow<MuscleGroup?>(MuscleGroup.CHEST)
    val selectedMuscle: StateFlow<MuscleGroup?> = _selectedMuscle.asStateFlow()

    val uiState: StateFlow<BodyAnalyticsUiState> = combine(
        workoutRepository.getAllWorkouts(),
        _viewMode,
        _selectedMuscle
    ) { workouts, mode, selected ->
        val stats = MuscleClassifier.computeMuscleStats(workouts)
        val avgRecovery = if (stats.isNotEmpty()) {
            stats.values.map { it.recoveryPercentage }.average().toInt()
        } else 100

        BodyAnalyticsUiState(
            isLoading = false,
            viewMode = mode,
            muscleStats = stats,
            selectedMuscle = selected,
            overallRecoveryScore = avgRecovery,
            weightUnit = if (prefs.isMetric()) "kg" else "lbs"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BodyAnalyticsUiState(isLoading = true)
    )

    fun setViewMode(mode: BodyViewMode) {
        _viewMode.value = mode
    }

    fun selectMuscle(muscle: MuscleGroup?) {
        _selectedMuscle.value = muscle
    }
}
