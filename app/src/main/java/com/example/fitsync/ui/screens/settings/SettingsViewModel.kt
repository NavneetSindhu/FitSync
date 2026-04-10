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

    // --- DARK MODE LOGIC ---
    private val _isDarkMode = MutableStateFlow(prefs.isDarkModeEnabled())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // --- NEW: ACCENT COLOR LOGIC ---
    // Default to the original Deep Aqua (0xFF0D6890) if no custom color is saved
    private val defaultAccentInt = 0xFF0D6890.toInt()

    // Fetch the saved color from SharedPreferences/DataStore, falling back to default
    private val _accentColor = MutableStateFlow(prefs.getAccentColor(defaultAccentInt))
    val accentColor: StateFlow<Int> = _accentColor.asStateFlow()

    fun updateAccentColor(colorInt: Int) {
        viewModelScope.launch {
            _accentColor.value = colorInt
            // Save it to prefs so it survives app restarts
            prefs.setAccentColor(colorInt)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = enabled
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

            // Reset state flows back to default
            _userName.value = "Athlete"
            _userGoal.value = "Stay Fit"
            _isDarkMode.value = false
            _accentColor.value = defaultAccentInt // Reset theme color
        }
    }
}