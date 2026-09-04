package com.minimize.maximus.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.domain.model.WorkoutSummary
import com.minimize.maximus.domain.model.body.BodyViewMode
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.body.MuscleStat
import com.minimize.maximus.domain.repository.IWorkoutRepository
import com.minimize.maximus.ui.components.ChartPoint
import com.minimize.maximus.ui.screens.home.PersonalRecord
import com.minimize.maximus.util.DateUtils
import com.minimize.maximus.util.MuscleClassifier
import com.minimize.maximus.util.WorkoutMathUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

enum class StatsGraphMetric(val displayName: String) {
    SETS("Total Sets"),
    VOLUME("Volume Load"),
    WORKOUTS("Workouts Frequency"),
    INTENSITY("Avg Reps / Set")
}

data class ExerciseProgressionPoint(
    val date: LocalDate,
    val bestWeight: Float,
    val bestReps: Int,
    val estimated1RM: Float,
    val totalVolume: Float
)

data class ExerciseProgressData(
    val exerciseName: String,
    val initial1RM: Float,
    val peak1RM: Float,
    val percentageGain: Float,
    val sessionCount: Int,
    val historyPoints: List<ChartPoint>,
    val sessions: List<ExerciseProgressionPoint>
)

data class StatsScreenUiState(
    val isLoading: Boolean = true,
    val totalVolume: Double = 0.0,
    val totalSets: Int = 0,
    val totalWorkouts: Int = 0,
    val currentStreakDays: Int = 0,
    val weightUnit: String = "kg",
    val selectedGraphMetric: StatsGraphMetric = StatsGraphMetric.SETS,
    val currentMetricChart: List<ChartPoint> = emptyList(),
    val weeklyVolumeChart: List<ChartPoint> = emptyList(),
    val weeklySetsChart: List<ChartPoint> = emptyList(),
    val weeklyWorkoutsChart: List<ChartPoint> = emptyList(),
    val weeklyIntensityChart: List<ChartPoint> = emptyList(),
    val monthlyVolumeChart: List<ChartPoint> = emptyList(),
    val workoutMap: Map<LocalDate, WorkoutSummary> = emptyMap(),
    val frozenDates: Set<LocalDate> = emptySet(),
    val personalRecords: List<PersonalRecord> = emptyList(),
    val progressiveExercises: List<ExerciseProgressData> = emptyList(),
    val selectedExerciseProgression: ExerciseProgressData? = null,
    val viewMode: BodyViewMode = BodyViewMode.FRONT,
    val muscleStats: Map<MuscleGroup, MuscleStat> = emptyMap(),
    val selectedMuscle: MuscleGroup? = MuscleGroup.CHEST,
    val overallRecoveryScore: Int = 100
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _selectedExerciseName = MutableStateFlow<String?>(null)
    val selectedExerciseName: StateFlow<String?> = _selectedExerciseName.asStateFlow()

    private val _selectedGraphMetric = MutableStateFlow(StatsGraphMetric.SETS)
    val selectedGraphMetric: StateFlow<StatsGraphMetric> = _selectedGraphMetric.asStateFlow()

    private val _viewMode = MutableStateFlow(BodyViewMode.FRONT)
    val viewMode: StateFlow<BodyViewMode> = _viewMode.asStateFlow()

    private val _selectedMuscle = MutableStateFlow<MuscleGroup?>(MuscleGroup.CHEST)
    val selectedMuscle: StateFlow<MuscleGroup?> = _selectedMuscle.asStateFlow()

    val uiState: StateFlow<StatsScreenUiState> = combine(
        workoutRepository.getAllWorkouts(),
        _selectedExerciseName,
        _selectedGraphMetric,
        _viewMode,
        _selectedMuscle
    ) { workouts, selectedExName, selectedMetric, mode, selMuscle ->
        val isMetric = prefs.isMetric()
        val weightUnitString = if (isMetric) "kg" else "lbs"
        val weightMultiplier = if (isMetric) 1.0f else 2.20462f
        val includeWarmups = prefs.isTrackWarmupVolumeEnabled()

        var allTimeVolume = 0.0
        var allTimeSets = 0
        val workoutDates = mutableSetOf<LocalDate>()
        val workoutCalendarMap = mutableMapOf<LocalDate, WorkoutSummary>()
        val exerciseSessionsMap = mutableMapOf<String, MutableList<ExerciseProgressionPoint>>()
        val exerciseBests = mutableMapOf<String, PersonalRecord>()

        val sortedAscending = workouts.sortedBy { it.date }

        sortedAscending.forEach { session ->
            val sessionDate = DateUtils.toLocalDate(session.date)
            workoutDates.add(sessionDate)

            var dayVolume = 0.0
            var daySets = 0

            session.exercise.forEach { exercise ->
                var sessionBestWeight = 0f
                var sessionBestReps = 0
                var sessionBest1RM = 0f
                var sessionExerciseVolume = 0f

                exercise.sets.forEach { set ->
                    daySets++
                    allTimeSets++
                    val setVol = (set.reps * set.weight * weightMultiplier).toDouble()
                    if (includeWarmups || set.setType != "W") {
                        allTimeVolume += setVol
                        dayVolume += setVol
                        sessionExerciseVolume += setVol.toFloat()
                    }

                    val raw1RM = WorkoutMathUtils.calculate1RMEpley(set.weight, set.reps)
                    val est1RM = raw1RM * weightMultiplier
                    val displayWeight = set.weight * weightMultiplier

                    if (est1RM > sessionBest1RM) {
                        sessionBest1RM = est1RM
                        sessionBestWeight = displayWeight
                        sessionBestReps = set.reps
                    }

                    val currentBest = exerciseBests[exercise.name]
                    if (currentBest == null || est1RM > currentBest.estimatedOneRepMax || displayWeight > currentBest.bestWeight) {
                        exerciseBests[exercise.name] = PersonalRecord(
                            exerciseName = exercise.name,
                            bestWeight = displayWeight,
                            bestReps = set.reps,
                            estimatedOneRepMax = est1RM
                        )
                    }
                }

                if (sessionBest1RM > 0f) {
                    val list = exerciseSessionsMap.getOrPut(exercise.name) { mutableListOf() }
                    list.add(
                        ExerciseProgressionPoint(
                            date = sessionDate,
                            bestWeight = sessionBestWeight,
                            bestReps = sessionBestReps,
                            estimated1RM = sessionBest1RM,
                            totalVolume = sessionExerciseVolume
                        )
                    )
                }
            }

            val intensity = when {
                daySets == 0 -> 0
                daySets in 1..5 -> 1
                daySets in 6..12 -> 2
                daySets in 13..20 -> 3
                else -> 4
            }

            workoutCalendarMap[sessionDate] = WorkoutSummary(
                intensity = intensity,
                volume = "${dayVolume.toInt()} $weightUnitString",
                sets = daySets
            )
        }

        val today = LocalDate.now()
        val frozenDates = prefs.getFrozenDates()
        val streak = WorkoutMathUtils.calculateConsecutiveStreak(workoutDates, frozenDates, today)

        // 14-Day Multi-Metric Points (Scrollable Timeline)
        val last14Days = (13 downTo 0).map { today.minusDays(it.toLong()) }
        
        val weeklyVolumePoints = last14Days.map { date ->
            val dayWorkouts = workouts.filter { DateUtils.toLocalDate(it.date) == date }
            var dayVolume = 0.0
            dayWorkouts.forEach { s ->
                s.exercise.forEach { ex ->
                    ex.sets.forEach { st ->
                        if (includeWarmups || st.setType != "W") dayVolume += (st.reps * st.weight * weightMultiplier)
                    }
                }
            }
            ChartPoint(
                label = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                value = dayVolume.toFloat()
            )
        }

        val weeklySetsPoints = last14Days.map { date ->
            val dayWorkouts = workouts.filter { DateUtils.toLocalDate(it.date) == date }
            val setsCount = dayWorkouts.sumOf { it.exercise.sumOf { ex -> ex.sets.size } }
            ChartPoint(
                label = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                value = setsCount.toFloat()
            )
        }

        val weeklyWorkoutsPoints = last14Days.map { date ->
            val dayWorkouts = workouts.filter { DateUtils.toLocalDate(it.date) == date }
            ChartPoint(
                label = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                value = dayWorkouts.size.toFloat()
            )
        }

        val weeklyIntensityPoints = last14Days.map { date ->
            val dayWorkouts = workouts.filter { DateUtils.toLocalDate(it.date) == date }
            var totalReps = 0
            var setsCount = 0
            dayWorkouts.forEach { s ->
                s.exercise.forEach { ex ->
                    ex.sets.forEach { st ->
                        totalReps += st.reps
                        setsCount++
                    }
                }
            }
            val avgReps = if (setsCount > 0) totalReps.toFloat() / setsCount else 0f
            ChartPoint(
                label = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                value = avgReps
            )
        }

        val activeMetricPoints = when (selectedMetric) {
            StatsGraphMetric.SETS -> weeklySetsPoints
            StatsGraphMetric.VOLUME -> weeklyVolumePoints
            StatsGraphMetric.WORKOUTS -> weeklyWorkoutsPoints
            StatsGraphMetric.INTENSITY -> weeklyIntensityPoints
        }

        // Monthly Volume Curve
        val last4Weeks = (3 downTo 0).map { weekOffset ->
            val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong() + (weekOffset * 7))
            val weekEnd = weekStart.plusDays(6)
            val weekWorkouts = workouts.filter {
                val d = DateUtils.toLocalDate(it.date)
                !d.isBefore(weekStart) && !d.isAfter(weekEnd)
            }
            var weekVolume = 0.0
            weekWorkouts.forEach { s ->
                s.exercise.forEach { ex ->
                    ex.sets.forEach { st ->
                        if (includeWarmups || st.setType != "W") weekVolume += (st.reps * st.weight)
                    }
                }
            }
            ChartPoint(label = "W${4 - weekOffset}", value = weekVolume.toFloat())
        }

        // Compute Progressive Overload
        val progressiveList = exerciseSessionsMap.map { (name, history) ->
            val initial = history.firstOrNull()?.estimated1RM ?: 0f
            val peak = history.maxOfOrNull { it.estimated1RM } ?: 0f
            val gain = if (initial > 0f) ((peak - initial) / initial) * 100f else 0f
            val chartPoints = history.mapIndexed { idx, point ->
                ChartPoint(label = point.date.dayOfMonth.toString(), value = point.estimated1RM)
            }
            ExerciseProgressData(
                exerciseName = name,
                initial1RM = initial,
                peak1RM = peak,
                percentageGain = gain,
                sessionCount = history.size,
                historyPoints = chartPoints,
                sessions = history
            )
        }.sortedByDescending { it.sessionCount }

        val activeSelectedProgression = progressiveList.find { it.exerciseName == selectedExName }
            ?: progressiveList.firstOrNull()

        val topPRs = exerciseBests.values
            .filter { it.bestWeight > 0f || it.bestReps > 0 }
            .sortedByDescending { it.estimatedOneRepMax }

        // Compute 3D Holographic Muscle Stats
        val stats = MuscleClassifier.computeMuscleStats(workouts)
        val avgRecovery = if (stats.isNotEmpty()) {
            stats.values.map { it.recoveryPercentage }.average().toInt()
        } else 100

        StatsScreenUiState(
            isLoading = false,
            totalVolume = allTimeVolume,
            totalSets = allTimeSets,
            totalWorkouts = workouts.size,
            currentStreakDays = streak,
            weightUnit = weightUnitString,
            selectedGraphMetric = selectedMetric,
            currentMetricChart = activeMetricPoints,
            weeklyVolumeChart = weeklyVolumePoints,
            weeklySetsChart = weeklySetsPoints,
            weeklyWorkoutsChart = weeklyWorkoutsPoints,
            weeklyIntensityChart = weeklyIntensityPoints,
            monthlyVolumeChart = last4Weeks,
            workoutMap = workoutCalendarMap,
            frozenDates = frozenDates,
            personalRecords = topPRs,
            progressiveExercises = progressiveList,
            selectedExerciseProgression = activeSelectedProgression,
            viewMode = mode,
            muscleStats = stats,
            selectedMuscle = selMuscle,
            overallRecoveryScore = avgRecovery
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsScreenUiState(isLoading = true))

    fun selectGraphMetric(metric: StatsGraphMetric) {
        _selectedGraphMetric.value = metric
    }

    fun selectProgressiveExercise(exerciseName: String) {
        _selectedExerciseName.value = exerciseName
    }

    fun setViewMode(mode: BodyViewMode) {
        _viewMode.value = mode
    }

    fun selectMuscle(muscle: MuscleGroup) {
        _selectedMuscle.value = muscle
    }
}
