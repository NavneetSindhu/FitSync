package com.minimize.maximus.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.data.repository.AuthRepository
import com.minimize.maximus.domain.repository.IWorkoutRepository
import com.minimize.maximus.util.DateUtils
import com.minimize.maximus.util.WorkoutMathUtils
import com.minimize.maximus.util.notification.ReminderSchedulerUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userAvatar: String = "avatar_male",
    val email: String = "",
    val avatarInitial: String = "",
    val planLabel: String = "",

    // Personal details
    val age: Int = 21,
    val weightKg: Float = 75f,
    val heightCm: Float = 175f,
    val isHeightMetric: Boolean = false,
    val gender: String = "Athlete",
    val fitnessGoal: String = "Build Muscle",

    // Dynamic Stats from Room
    val totalWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val totalWorkingSets: Int = 0,
    val totalVolumeFormatted: String = "0kg",
    val avgSessionMin: Int = 0,

    // Dynamic Achievements from Room
    val achievements: List<Achievement> = emptyList(),

    // Settings toggles
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val restTimerEnabled: Boolean = true,
    val weightUnit: String = "kg",

    // Storage Mode
    val isLocalFirst: Boolean = true,

    // UI state
    val isEditingPersonalDetails: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlocked: Boolean
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: PreferenceManager,
    private val workoutRepository: IWorkoutRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
        observeWorkoutStats()
    }

    fun loadProfileData() {
        val name = prefs.getUserName()
        val firebaseUser = authRepository.currentUser
        val userEmail = firebaseUser?.email ?: ""
        val avatar = prefs.getUserAvatar()

        _uiState.update {
            it.copy(
                userName = name,
                userAvatar = avatar,
                email = userEmail,
                avatarInitial = name.take(1).uppercase().ifBlank { "A" },
                fitnessGoal = prefs.getUserGoal(),
                age = prefs.getAge(),
                weightKg = prefs.getWeight(),
                heightCm = prefs.getHeight(),
                isHeightMetric = prefs.isHeightMetric(),
                gender = prefs.getGender(),
                weightUnit = if (prefs.isMetric()) "kg" else "lbs",
                restTimerEnabled = prefs.isAutoStartRestEnabled(),
                notificationsEnabled = prefs.isNotificationsEnabled(),
                reminderHour = prefs.getReminderHour(),
                reminderMinute = prefs.getReminderMinute()
            )
        }
    }

    fun setAvatar(avatarId: String) {
        prefs.setUserAvatar(avatarId)
        _uiState.update { it.copy(userAvatar = avatarId) }
    }

    private fun observeWorkoutStats() {
        viewModelScope.launch {
            workoutRepository.getAllWorkouts().collect { workouts ->
                val unit = if (prefs.isMetric()) "kg" else "lbs"
                var totalVolume = 0.0
                val workoutDates = mutableSetOf<LocalDate>()

                workouts.forEach { session ->
                    val date = DateUtils.toLocalDate(session.date)
                    workoutDates.add(date)
                    session.exercise.forEach { ex ->
                        ex.sets.forEach { set ->
                            totalVolume += (set.weight * set.reps)
                        }
                    }
                }

                // Compute real streak via pure math util
                val streak = WorkoutMathUtils.calculateConsecutiveStreak(workoutDates)

                val formattedVolume = if (totalVolume >= 1000) {
                    "%.1fk%s".format(totalVolume / 1000, unit)
                } else {
                    "${totalVolume.toInt()}$unit"
                }

                // Compute real dynamic achievements
                val totalCount = workouts.size
                val achievementsList = listOf(
                    Achievement("first_workout", "First Step", "Completed your first workout", "🏃", totalCount >= 1),
                    Achievement("streak_3", "Consistency", "Achieved a 3-day streak", "🔥", streak >= 3),
                    Achievement("workouts_10", "Double Digits", "Logged 10 workouts", "💪", totalCount >= 10),
                    Achievement("volume_10k", "Heavy Lifter", "Lifted 10,000 $unit total", "👑", totalVolume >= 10000),
                    Achievement("workouts_50", "Half Century", "Logged 50 workouts", "🏋️", totalCount >= 50),
                    Achievement("volume_100k", "Iron Master", "Lifted 100,000 $unit total", "⚡", totalVolume >= 100000)
                )

                // Compute dynamic average session duration from real set volume and pacing
                val totalSetsInAllWorkouts = workouts.sumOf { session -> session.exercise.sumOf { it.sets.size } }
                val avgSetsPerWorkout = if (totalCount > 0) totalSetsInAllWorkouts.toDouble() / totalCount else 0.0
                val calculatedAvgSessionMin = (avgSetsPerWorkout * 2.5).toInt().coerceIn(if (totalCount > 0) 15 else 0, 120)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalWorkouts = totalCount,
                        currentStreak = streak,
                        totalWorkingSets = totalSetsInAllWorkouts,
                        totalVolumeFormatted = formattedVolume,
                        avgSessionMin = calculatedAvgSessionMin,
                        achievements = achievementsList
                    )
                }
            }
        }
    }

    fun openPersonalDetailsEditor() {
        _uiState.update { it.copy(isEditingPersonalDetails = true) }
    }

    fun closePersonalDetailsEditor() {
        _uiState.update { it.copy(isEditingPersonalDetails = false) }
    }

    fun savePersonalDetails(
        age: Int,
        weightKg: Float,
        heightCm: Float,
        isHeightMetric: Boolean,
        gender: String,
        fitnessGoal: String
    ) {
        prefs.setAge(age)
        prefs.setWeight(weightKg)
        prefs.setHeight(heightCm)
        prefs.setHeightMetric(isHeightMetric)
        prefs.setGender(gender)
        prefs.saveUserData(prefs.getUserName(), fitnessGoal)

        _uiState.update {
            it.copy(
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                isHeightMetric = isHeightMetric,
                gender = gender,
                fitnessGoal = fitnessGoal,
                isEditingPersonalDetails = false
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        prefs.setNotificationsEnabled(enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        if (enabled) {
            ReminderSchedulerUtil.scheduleDailyReminder(context)
        } else {
            ReminderSchedulerUtil.cancelDailyReminder(context)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.setReminderTime(hour, minute)
        _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        if (_uiState.value.notificationsEnabled) {
            ReminderSchedulerUtil.scheduleDailyReminder(context, hour, minute)
        }
    }

    fun toggleRestTimer(enabled: Boolean) {
        prefs.setAutoStartRestEnabled(enabled)
        _uiState.update { it.copy(restTimerEnabled = enabled) }
    }

    fun setWeightUnit(unit: String) {
        prefs.setMetric(unit == "kg")
        _uiState.update { it.copy(weightUnit = unit) }
        loadProfileData()
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.signOut()
            _uiState.update { it.copy(isLoggingOut = false) }
            onComplete()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}