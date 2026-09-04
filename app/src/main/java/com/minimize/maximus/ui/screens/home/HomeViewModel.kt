package com.minimize.maximus.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.data.remote.RemoteConfigManager
import com.minimize.maximus.domain.manager.ActiveLiveWorkout
import com.minimize.maximus.domain.manager.WorkoutManager
import com.minimize.maximus.domain.model.WorkoutSummary
import com.minimize.maximus.domain.model.routine.DaySchedule
import com.minimize.maximus.domain.model.routine.WeeklyPlan
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.domain.repository.IRoutineRepository
import com.minimize.maximus.domain.repository.IWorkoutRepository
import com.minimize.maximus.ui.components.ChartPoint
import com.minimize.maximus.util.DateUtils
import com.minimize.maximus.util.WorkoutMathUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class PersonalRecord(
    val exerciseName: String,
    val bestWeight: Float,
    val bestReps: Int,
    val estimatedOneRepMax: Float
)

data class WeekDayStatus(
    val dayOfWeek: DayOfWeek,
    val date: LocalDate,
    val isToday: Boolean,
    val isPast: Boolean,
    val isRestDay: Boolean,
    val isFrozen: Boolean,
    val routineName: String?,
    val isCompleted: Boolean,
    val isSkipped: Boolean
)

data class HomeDashboardUiState(
    val isLoading: Boolean = true,
    val totalVolume: Double = 0.0,
    val totalSets: Int = 0,
    val totalWorkouts: Int = 0,
    val currentStreakDays: Int = 0,
    val availableFreezes: Int = 2,
    val maxFreezes: Int = 3,
    val frozenDates: Set<LocalDate> = emptySet(),
    val isStreakFrozenToday: Boolean = false,
    val weeklyVolumeChart: List<ChartPoint> = emptyList(),
    val personalRecords: List<PersonalRecord> = emptyList(),
    val weightUnit: String = "kg",
    val todaySchedule: DaySchedule? = null,
    val todayRoutine: WorkoutRoutine? = null,
    val weekDaysStatus: List<WeekDayStatus> = emptyList(),
    val skippedWorkoutAlert: DaySchedule? = null,
    val activeLiveWorkout: ActiveLiveWorkout? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val routineRepository: IRoutineRepository,
    private val workoutManager: WorkoutManager,
    private val prefs: PreferenceManager,
    private val remoteConfigManager: RemoteConfigManager
) : ViewModel() {

    private val _userName = MutableStateFlow(prefs.getUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()
    val isAiCoachEnabled: StateFlow<Boolean> = remoteConfigManager.isAiCoachEnabled

    private val _dismissedSkippedAlert = MutableStateFlow(false)
    private val _freezeTrigger = MutableStateFlow(0)

    val activeWorkout: StateFlow<ActiveLiveWorkout?> = workoutManager.activeWorkout

    val workoutHistory: StateFlow<Map<LocalDate, WorkoutSummary>> = workoutRepository.getAllWorkouts()
        .map { workouts ->
            val calendarData = mutableMapOf<LocalDate, WorkoutSummary>()
            val weightUnitString = if (prefs.isMetric()) "kg" else "lbs"

            workouts.forEach { workout ->
                val date = DateUtils.toLocalDate(workout.date)
                var totalVolume = 0.0
                var totalSets = 0

                workout.exercise.forEach { exercise ->
                    totalSets += exercise.sets.size
                    exercise.sets.forEach { set ->
                        totalVolume += (set.reps * set.weight)
                    }
                }

                val intensityLevel = when {
                    totalSets == 0 -> 0
                    totalSets in 1..5 -> 1
                    totalSets in 6..12 -> 2
                    totalSets in 13..20 -> 3
                    else -> 4
                }

                calendarData[date] = WorkoutSummary(
                    intensity = intensityLevel,
                    volume = "${totalVolume.toInt()} $weightUnitString",
                    sets = totalSets
                )
            }
            calendarData
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _uiSignals = combine(_dismissedSkippedAlert, _freezeTrigger) { dismissed, trigger ->
        dismissed to trigger
    }

    val dashboardState: StateFlow<HomeDashboardUiState> = combine(
        workoutRepository.getAllWorkouts(),
        routineRepository.getWeeklyPlan(),
        routineRepository.getAllRoutines(),
        workoutManager.activeWorkout,
        _uiSignals
    ) { workouts, weeklyPlan, routines, activeSession, uiSignal ->
        val (isDismissed, _) = uiSignal
        val weightUnitString = if (prefs.isMetric()) "kg" else "lbs"
        val includeWarmups = prefs.isTrackWarmupVolumeEnabled()
        var allTimeVolume = 0.0
        var allTimeSets = 0
        val workoutDates = mutableSetOf<LocalDate>()
        val exerciseBests = mutableMapOf<String, PersonalRecord>()

        workouts.forEach { session ->
            val sessionDate = DateUtils.toLocalDate(session.date)
            workoutDates.add(sessionDate)

            session.exercise.forEach { exercise ->
                allTimeSets += exercise.sets.size
                exercise.sets.forEach { set ->
                    if (includeWarmups || set.setType != "W") {
                        allTimeVolume += (set.reps * set.weight).toDouble()
                    }
                    val est1RM = WorkoutMathUtils.calculate1RMEpley(set.weight, set.reps)
                    val currentBest = exerciseBests[exercise.name]
                    if (currentBest == null || est1RM > currentBest.estimatedOneRepMax || set.weight > currentBest.bestWeight) {
                        exerciseBests[exercise.name] = PersonalRecord(
                            exerciseName = exercise.name,
                            bestWeight = set.weight,
                            bestReps = set.reps,
                            estimatedOneRepMax = est1RM
                        )
                    }
                }
            }
        }

        val today = LocalDate.now()
        val todayDow = today.dayOfWeek
        val frozenDates = prefs.getFrozenDates().toMutableSet()
        var availableFreezes = prefs.getStreakFreezesAvailable()

        // Today's Routine & Schedule
        val todaySchedule = weeklyPlan.days[todayDow] ?: DaySchedule(todayDow, isRestDay = true)
        val matchedRoutine = routines.find { it.id == todaySchedule.routineId || it.name.equals(todaySchedule.routineName, ignoreCase = true) }
        val todayRoutine = if (!todaySchedule.isRestDay && todaySchedule.routineName != null) {
            matchedRoutine ?: if (routines.isNotEmpty()) {
                WorkoutRoutine(
                    name = todaySchedule.routineName,
                    description = "Scheduled workout for ${todayDow.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                    exercises = emptyList()
                )
            } else null
        } else null

        val hasAnyWorkoutHistory = workouts.isNotEmpty()

        // Compute 7-day strip status for current Monday-Sunday
        val currentMon = today.minusDays((todayDow.value - 1).toLong())
        var skippedAlert: DaySchedule? = null

        val weekDaysStatus = (0..6).map { offset ->
            val dayDate = currentMon.plusDays(offset.toLong())
            val dow = dayDate.dayOfWeek
            val sched = weeklyPlan.days[dow] ?: DaySchedule(dow, isRestDay = true)
            val hasWorkoutLogged = workouts.any { DateUtils.toLocalDate(it.date) == dayDate }
            val isPast = dayDate.isBefore(today)
            var isDayFrozen = frozenDates.contains(dayDate)

            // Auto-Freeze Protection: only if user has logged workouts before (not first launch)
            if (hasAnyWorkoutHistory && isPast && !sched.isRestDay && !hasWorkoutLogged && !isDayFrozen && availableFreezes > 0) {
                // Check how many consecutive days immediately preceding this day were frozen
                var prevConsecutiveFrozen = 0
                var checkBack = dayDate.minusDays(1)
                while (frozenDates.contains(checkBack)) {
                    prevConsecutiveFrozen++
                    checkBack = checkBack.minusDays(1)
                }

                if (prevConsecutiveFrozen < 2) {
                    prefs.useStreakFreeze(dayDate)
                    frozenDates.add(dayDate)
                    isDayFrozen = true
                    availableFreezes = prefs.getStreakFreezesAvailable()
                }
            }

            val isSkipped = hasAnyWorkoutHistory && isPast && !sched.isRestDay && !hasWorkoutLogged && !isDayFrozen

            if (isSkipped && skippedAlert == null && !isDismissed) {
                skippedAlert = sched
            }

            WeekDayStatus(
                dayOfWeek = dow,
                date = dayDate,
                isToday = dayDate == today,
                isPast = isPast,
                isRestDay = sched.isRestDay,
                isFrozen = isDayFrozen,
                routineName = sched.routineName,
                isCompleted = hasWorkoutLogged,
                isSkipped = isSkipped
            )
        }

        val streak = WorkoutMathUtils.calculateConsecutiveStreak(workoutDates, frozenDates, today)

        // 7-Day Weekly Volume Curve
        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val weeklyPoints = last7Days.map { date ->
            val dayWorkouts = workouts.filter { DateUtils.toLocalDate(it.date) == date }
            var dayVolume = 0.0
            dayWorkouts.forEach { s ->
                s.exercise.forEach { ex ->
                    ex.sets.forEach { st ->
                        if (includeWarmups || st.setType != "W") dayVolume += (st.reps * st.weight)
                    }
                }
            }
            ChartPoint(label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), value = dayVolume.toFloat())
        }

        val topPRs = exerciseBests.values
            .filter { it.bestWeight > 0f || it.bestReps > 0 }
            .sortedByDescending { it.estimatedOneRepMax }
            .take(4)

        HomeDashboardUiState(
            isLoading = false,
            totalVolume = allTimeVolume,
            totalSets = allTimeSets,
            totalWorkouts = workouts.size,
            currentStreakDays = streak,
            availableFreezes = availableFreezes,
            maxFreezes = remoteConfigManager.getMaxStreakFreezeCount(),
            frozenDates = frozenDates,
            isStreakFrozenToday = frozenDates.contains(today),
            weeklyVolumeChart = weeklyPoints,
            personalRecords = topPRs,
            weightUnit = weightUnitString,
            todaySchedule = todaySchedule,
            todayRoutine = todayRoutine,
            weekDaysStatus = weekDaysStatus,
            skippedWorkoutAlert = skippedAlert,
            activeLiveWorkout = activeSession
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeDashboardUiState(isLoading = true))

    fun startTodayWorkout(onStarted: () -> Unit) {
        val routine = dashboardState.value.todayRoutine
        if (routine != null) {
            workoutManager.startFromRoutine(routine)
        } else {
            val sched = dashboardState.value.todaySchedule
            if (sched != null && !sched.isRestDay && sched.routineName != null) {
                workoutManager.startFromRoutine(
                    WorkoutRoutine(
                        name = sched.routineName,
                        exercises = emptyList()
                    )
                )
            } else {
                workoutManager.startFromRoutine(
                    WorkoutRoutine(
                        name = "Full Body Workout",
                        exercises = emptyList()
                    )
                )
            }
        }
        onStarted()
    }

    fun startEmptyWorkout(onStarted: () -> Unit) {
        workoutManager.startEmptyWorkout("Freestyle Workout")
        onStarted()
    }

    fun startSpecificRoutine(routine: WorkoutRoutine, onStarted: () -> Unit) {
        workoutManager.startFromRoutine(routine)
        onStarted()
    }

    fun markTodayAsRestDay(onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            val todayDow = LocalDate.now().dayOfWeek
            val currentPlan = routineRepository.getWeeklyPlan().first()
            val updatedDays = currentPlan.days.toMutableMap()
            updatedDays[todayDow] = DaySchedule(dayOfWeek = todayDow, isRestDay = true, routineId = null, routineName = null)
            routineRepository.saveWeeklyPlan(currentPlan.copy(days = updatedDays))
            onCompleted()
        }
    }

    fun shiftSplitForward(onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            val todayDow = LocalDate.now().dayOfWeek
            val currentPlan = routineRepository.getWeeklyPlan().first()
            val daysList = DayOfWeek.entries
            val todayIndex = daysList.indexOf(todayDow)

            val updatedDays = currentPlan.days.toMutableMap()
            // Shift remaining days forward by 1 starting from today
            for (i in daysList.size - 1 downTo todayIndex + 1) {
                val prevDow = daysList[i - 1]
                val curDow = daysList[i]
                val prevSched = currentPlan.days[prevDow]
                if (prevSched != null) {
                    updatedDays[curDow] = prevSched.copy(dayOfWeek = curDow)
                }
            }
            // Today becomes a Rest Day
            updatedDays[todayDow] = DaySchedule(dayOfWeek = todayDow, isRestDay = true, routineId = null, routineName = null)

            routineRepository.saveWeeklyPlan(currentPlan.copy(days = updatedDays))
            onCompleted()
        }
    }

    fun dismissSkippedAlert() {
        _dismissedSkippedAlert.value = true
    }

    fun resumeActiveWorkout(onResumed: () -> Unit) {
        workoutManager.restoreWorkout()
        onResumed()
    }

    fun discardActiveWorkout() {
        workoutManager.discardWorkout()
    }

    fun refreshUserData() {
        _userName.value = prefs.getUserName()
        _freezeTrigger.value += 1
    }
}