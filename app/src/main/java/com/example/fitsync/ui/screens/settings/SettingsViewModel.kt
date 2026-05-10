package com.example.fitsync.ui.screens.settings

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    // ── Profile ────────────────────────────────────────────────────────────────
    private val _userName = MutableStateFlow("Navneet")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userGoal = MutableStateFlow("Build Muscle")
    val userGoal: StateFlow<String> = _userGoal.asStateFlow()

    // ── Theme ──────────────────────────────────────────────────────────────────
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Stored as Int (ARGB) so it can be persisted / passed across composables easily
    private val _accentColor = MutableStateFlow(Color(0xFFE53935).toArgb())
    val accentColor: StateFlow<Int> = _accentColor.asStateFlow()

    private val _navStyle = MutableStateFlow("Floating Pill")   // "Floating Pill" | "Classic Bar"
    val navStyle: StateFlow<String> = _navStyle.asStateFlow()

    private val _fontScale = MutableStateFlow(1f)               // 0.85 | 1.0 | 1.15
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _compactCards = MutableStateFlow(false)
    val compactCards: StateFlow<Boolean> = _compactCards.asStateFlow()

    // ── Units ──────────────────────────────────────────────────────────────────
    private val _isMetric = MutableStateFlow(true)
    val isMetric: StateFlow<Boolean> = _isMetric.asStateFlow()

    // ── Workout defaults ───────────────────────────────────────────────────────
    private val _showVolumeInLog = MutableStateFlow(true)
    val showVolumeInLog: StateFlow<Boolean> = _showVolumeInLog.asStateFlow()

    private val _defaultRestSeconds = MutableStateFlow(90)      // 30 – 300
    val defaultRestSeconds: StateFlow<Int> = _defaultRestSeconds.asStateFlow()

    // ── Feedback ───────────────────────────────────────────────────────────────
    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    // ── Mutators ───────────────────────────────────────────────────────────────

    fun updateProfile(name: String, goal: String) {
        _userName.value = name
        _userGoal.value = goal
        // TODO: persist via DataStore / repository
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setAccentColor(color: Color) {
        _accentColor.value = color.toArgb()
        // TODO: persist via DataStore
    }

    fun setNavStyle(style: String) {
        _navStyle.value = style
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    fun setCompactCards(compact: Boolean) {
        _compactCards.value = compact
    }

    fun toggleUnits(metric: Boolean) {
        _isMetric.value = metric
    }

    fun setShowVolumeInLog(show: Boolean) {
        _showVolumeInLog.value = show
    }

    fun setDefaultRestSeconds(seconds: Int) {
        _defaultRestSeconds.value = seconds
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }

    fun fullReset(userId: String) {
        viewModelScope.launch {
            // TODO: call repository to wipe local DB + cloud data
            _userName.value = "Navneet"
            _userGoal.value = "Build Muscle"
            _isDarkMode.value = false
            _accentColor.value = Color(0xFFE53935).toArgb()
            _navStyle.value = "Floating Pill"
            _fontScale.value = 1f
            _compactCards.value = false
            _isMetric.value = true
            _showVolumeInLog.value = true
            _defaultRestSeconds.value = 90
            _hapticsEnabled.value = true
        }
    }
}