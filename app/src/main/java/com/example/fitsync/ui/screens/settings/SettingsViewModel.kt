package com.example.fitsync.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.PreferenceManager
import com.example.fitsync.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _userName = MutableStateFlow(prefs.getUserName())
    val userName = _userName.asStateFlow()

    private val _userGoal = MutableStateFlow(prefs.getUserGoal())
    val userGoal = _userGoal.asStateFlow()

    private val _isMetric = MutableStateFlow(true)
    val isMetric = _isMetric.asStateFlow()

    // --- DARK MODE LOGIC (PERSISTED) ---
    // 1. Initialize from your PreferenceManager so it remembers the choice
    private val _isDarkMode = MutableStateFlow(prefs.isDarkModeEnabled())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = enabled
            // 2. Save it to prefs immediately
            prefs.setDarkMode(enabled)
        }
    }

    fun updateProfile(name: String, goal: String) {
        prefs.saveUserData(name, goal)
        _userName.value = name
        _userGoal.value = goal
    }

    fun toggleUnits(metric: Boolean) {
        _isMetric.value = metric
    }

    fun fullReset(userId: String) {
        viewModelScope.launch {
            repository.deleteEverything(userId)
            prefs.clearAll()
            _userName.value = "Athlete"
            _userGoal.value = "Stay Fit"
            _isDarkMode.value = false // Reset theme on wipe
        }
    }
}