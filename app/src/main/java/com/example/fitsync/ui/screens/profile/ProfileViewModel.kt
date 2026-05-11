package com.example.fitsync.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "",
    val email: String = "navneet@example.com",
    val avatarInitial: String = "",
    val planLabel: String = "Free Plan",

    // Personal details
    val age: Int = 0,
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val gender: String = "",
    val fitnessGoal: String = "",

    // Quick stats (Mocked for now, easily wireable to WorkoutRepository later)
    val totalWorkouts: Int = 24,
    val currentStreak: Int = 5,
    val totalVolumeKg: String = "48.2k",
    val avgSessionMin: Int = 42,

    // Achievements
    val achievements: List<Achievement> = defaultAchievements(),

    // Settings toggles
    val notificationsEnabled: Boolean = true,
    val restTimerEnabled: Boolean = true,
    val weightUnit: String = "kg",

    // Cloud sync
    val syncStatus: SyncStatus = SyncStatus.Idle,

    // UI state
    val isEditingPersonalDetails: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null
)

enum class SyncStatus { Idle, Syncing, Success, Error }

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlocked: Boolean
)

fun defaultAchievements() = listOf(
    Achievement("first_workout",  "First Step",      "Completed your first workout",   "🏃", true),
    Achievement("streak_5",       "On Fire",         "Achieved a 5-day streak",        "🔥", true),
    Achievement("workouts_10",    "Double Digits",   "Logged 10 workouts",             "💪", true),
    Achievement("workouts_50",    "Half Century",    "Logged 50 workouts",             "🏋️", false),
    Achievement("volume_100k",    "Volume King",     "Lifted 100k kg total",           "👑", false),
    Achievement("streak_30",      "Iron Will",       "Achieved a 30-day streak",       "⚡", false),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: PreferenceManager // 1. INJECT PREFERENCES
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    // 2. LOAD INITIAL VALUES FROM DISK
    fun loadProfileData() {
        val name = prefs.getUserName()
        _uiState.update {
            it.copy(
                userName = name,
                avatarInitial = name.take(1).uppercase(),
                fitnessGoal = prefs.getUserGoal(),
                age = prefs.getAge(),
                weightKg = prefs.getWeight(),
                heightCm = prefs.getHeight(),
                gender = prefs.getGender(),
                weightUnit = if (prefs.isMetric()) "kg" else "lbs",
                restTimerEnabled = prefs.isAutoStartRestEnabled(),
                notificationsEnabled = prefs.isNotificationsEnabled()
            )
        }
    }

    // ── Personal details editing ──────────────────────────────────────────────

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
        gender: String,
        fitnessGoal: String
    ) {
        // 3. PERSIST TO DISK IMMEDIATELY
        prefs.setAge(age)
        prefs.setWeight(weightKg)
        prefs.setHeight(heightCm)
        prefs.setGender(gender)
        prefs.saveUserData(prefs.getUserName(), fitnessGoal) // Keep name same, update goal

        _uiState.update {
            it.copy(
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                gender = gender,
                fitnessGoal = fitnessGoal,
                isEditingPersonalDetails = false
            )
        }
    }

    // ── Settings toggles ──────────────────────────────────────────────────────

    fun toggleNotifications(enabled: Boolean) {
        prefs.setNotificationsEnabled(enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleRestTimer(enabled: Boolean) {
        prefs.setAutoStartRestEnabled(enabled)
        _uiState.update { it.copy(restTimerEnabled = enabled) }
    }

    fun setWeightUnit(unit: String) {
        prefs.setMetric(unit == "kg")
        _uiState.update { it.copy(weightUnit = unit) }
    }

    // ── Cloud sync ────────────────────────────────────────────────────────────

    fun triggerCloudSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncStatus = SyncStatus.Syncing) }
            kotlinx.coroutines.delay(2000) // Simulate network call
            _uiState.update { it.copy(syncStatus = SyncStatus.Success) }
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(syncStatus = SyncStatus.Idle) }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            kotlinx.coroutines.delay(500)
            _uiState.update { it.copy(isLoggingOut = false) }
            onComplete()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}