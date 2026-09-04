package com.minimize.maximus.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.domain.repository.IWorkoutRepository
import com.minimize.maximus.util.WorkoutMathUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val workouts: List<WorkoutSession> = emptyList(),
    val totalSetsCount: Int = 0,
    val totalSessionsCount: Int = 0,
    val totalPRsCount: Int = 0,
    val totalVolume: Int = 0,
    val weightUnit: String = "kg"
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: IWorkoutRepository,
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _weightUnit = MutableStateFlow(if (prefs.isMetric()) "kg" else "lbs")
    val weightUnit: StateFlow<String> = _weightUnit.asStateFlow()

    val userName: String get() = prefs.getUserName()
    val userAvatar: String get() = prefs.getUserAvatar()

    // ── Search & Filter State ──
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMonth = MutableStateFlow("All")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedSplit = MutableStateFlow("All")
    val selectedSplit: StateFlow<String> = _selectedSplit.asStateFlow()

    private val _selectedSort = MutableStateFlow(WorkoutSortOption.NEWEST)
    val selectedSort: StateFlow<WorkoutSortOption> = _selectedSort.asStateFlow()

    // 🔥 ATOMIC UNIFIED UI STATE (Prevents flash of empty state on initial screen load)
    val uiState: StateFlow<HistoryUiState> = repository.getAllWorkouts()
        .map { workouts ->
            var totalSets = 0
            val isMetric = prefs.isMetric()
            val exerciseBests = mutableMapOf<String, Float>()

            val calculatedVolume = workouts.sumOf { session ->
                session.exercise.sumOf { ex ->
                    val isCalisthenicsHold = listOf("Planche", "Lever", "Handstand", "Hold", "Plank")
                        .any { ex.name.contains(it, ignoreCase = true) }

                    ex.sets.sumOf { set ->
                        totalSets++
                        val est1RM = WorkoutMathUtils.calculate1RMEpley(set.weight, set.reps)
                        val prevBest = exerciseBests[ex.name] ?: 0f
                        if (est1RM > prevBest) {
                            exerciseBests[ex.name] = est1RM
                        }

                        if (isCalisthenicsHold) {
                            set.reps.toDouble()
                        } else {
                            (set.weight * set.reps).toDouble()
                        }
                    }
                }
            }

            val finalVolume = if (isMetric) calculatedVolume.toInt() else (calculatedVolume * 2.20462).toInt()

            HistoryUiState(
                isLoading = false,
                workouts = workouts,
                totalSetsCount = totalSets,
                totalSessionsCount = workouts.size,
                totalPRsCount = exerciseBests.size,
                totalVolume = finalVolume,
                weightUnit = if (isMetric) "kg" else "lbs"
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true)
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setSelectedSplit(split: String) {
        _selectedSplit.value = split
    }

    fun setSelectedSort(sort: WorkoutSortOption) {
        _selectedSort.value = sort
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedMonth.value = "All"
        _selectedSplit.value = "All"
        _selectedSort.value = WorkoutSortOption.NEWEST
    }

    fun deleteWorkout(workout: WorkoutSession) {
        viewModelScope.launch {
            repository.delete(workout)
        }
    }

    fun restoreWorkouts(workouts: List<WorkoutSession>) {
        viewModelScope.launch {
            repository.insertAll(workouts)
        }
    }

    fun refreshSettings() {
        _weightUnit.value = if (prefs.isMetric()) "kg" else "lbs"
    }
}

enum class WorkoutSortOption(val displayName: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST_VOLUME("Highest Volume"),
    MOST_EXERCISES("Most Exercises")
}