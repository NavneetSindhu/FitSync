package com.example.fitsync.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitsync.data.local.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferenceManager
) : ViewModel() {

    // ── 1. Profile ─────────────────────────────────────────────────────────────
    private val _userName = MutableStateFlow(prefs.getUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userGoal = MutableStateFlow(prefs.getUserGoal())
    val userGoal: StateFlow<String> = _userGoal.asStateFlow()

    // ── 2. Appearance ──────────────────────────────────────────────────────────
    private val _isDarkMode = MutableStateFlow(prefs.isDarkModeEnabled())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _accentColor = MutableStateFlow(prefs.getAccentColor())
    val accentColor: StateFlow<Int> = _accentColor.asStateFlow()

    private val _navStyle = MutableStateFlow(prefs.getNavStyle())
    val navStyle: StateFlow<String> = _navStyle.asStateFlow()

    private val _fontScale = MutableStateFlow(prefs.getFontScale())
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _compactCards = MutableStateFlow(prefs.isCompactCards())
    val compactCards: StateFlow<Boolean> = _compactCards.asStateFlow()

    // ── 3. Units & Measurements ────────────────────────────────────────────────
    private val _isMetric = MutableStateFlow(prefs.isMetric())
    val isMetric: StateFlow<Boolean> = _isMetric.asStateFlow()

    private val _defaultBarbellWeight = MutableStateFlow(prefs.getDefaultBarbellWeight())
    val defaultBarbellWeight: StateFlow<Int> = _defaultBarbellWeight.asStateFlow()

    // ── 4. Workout Defaults ────────────────────────────────────────────────────
    private val _showVolumeInLog = MutableStateFlow(prefs.isVolumeInLogShown())
    val showVolumeInLog: StateFlow<Boolean> = _showVolumeInLog.asStateFlow()

    private val _autoStartRest = MutableStateFlow(prefs.isAutoStartRestEnabled())
    val autoStartRest: StateFlow<Boolean> = _autoStartRest.asStateFlow()

    private val _defaultRestSeconds = MutableStateFlow(prefs.getDefaultRestSeconds())
    val defaultRestSeconds: StateFlow<Int> = _defaultRestSeconds.asStateFlow()

    private val _trackWarmupVolume = MutableStateFlow(prefs.isTrackWarmupVolumeEnabled())
    val trackWarmupVolume: StateFlow<Boolean> = _trackWarmupVolume.asStateFlow()

    // ── 5. App Experience & Calendar ───────────────────────────────────────────
    private val _firstDayOfWeek = MutableStateFlow(prefs.getFirstDayOfWeek())
    val firstDayOfWeek: StateFlow<String> = _firstDayOfWeek.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.isHapticsEnabled())
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    // ── 6. Data & Cloud ────────────────────────────────────────────────────────
    private val _syncWifiOnly = MutableStateFlow(prefs.isSyncWifiOnlyEnabled())
    val syncWifiOnly: StateFlow<Boolean> = _syncWifiOnly.asStateFlow()


    // ── Mutators (UPDATE FLOW AND DISK SIMULTANEOUSLY) ─────────────────────────

    fun updateProfile(name: String, goal: String) {
        _userName.value = name
        _userGoal.value = goal
        prefs.saveUserData(name, goal)
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.setDarkMode(enabled)
    }

    fun setAccentColor(color: Color) {
        val colorInt = color.toArgb()
        _accentColor.value = colorInt
        prefs.setAccentColor(colorInt)
    }

    fun setNavStyle(style: String) {
        _navStyle.value = style
        prefs.setNavStyle(style)
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
        prefs.setFontScale(scale)
    }

    fun setCompactCards(compact: Boolean) {
        _compactCards.value = compact
        prefs.setCompactCards(compact)
    }

    fun toggleUnits(metric: Boolean) {
        _isMetric.value = metric
        prefs.setMetric(metric)
    }

    fun setDefaultBarbellWeight(weight: Int) {
        _defaultBarbellWeight.value = weight
        prefs.setDefaultBarbellWeight(weight)
    }

    fun setShowVolumeInLog(show: Boolean) {
        _showVolumeInLog.value = show
        prefs.setShowVolumeInLog(show)
    }

    fun setAutoStartRest(enabled: Boolean) {
        _autoStartRest.value = enabled
        prefs.setAutoStartRestEnabled(enabled)
    }

    fun setDefaultRestSeconds(seconds: Int) {
        _defaultRestSeconds.value = seconds
        prefs.setDefaultRestSeconds(seconds)
    }

    fun setTrackWarmupVolume(enabled: Boolean) {
        _trackWarmupVolume.value = enabled
        prefs.setTrackWarmupVolumeEnabled(enabled)
    }

    fun setFirstDayOfWeek(day: String) {
        _firstDayOfWeek.value = day
        prefs.setFirstDayOfWeek(day)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        prefs.setHapticsEnabled(enabled)
    }

    fun setSyncWifiOnly(enabled: Boolean) {
        _syncWifiOnly.value = enabled
        prefs.setSyncWifiOnlyEnabled(enabled)
    }


    fun fullReset(userId: String) {
        viewModelScope.launch {
            prefs.clearAll() // Wipe the disk

            // Reset Flows to defaults
            _userName.value = "Athlete"
            _userGoal.value = "Stay Fit"
            _isDarkMode.value = false
            _accentColor.value = Color(0xFFE53935).toArgb()
            _navStyle.value = "Floating Pill"
            _fontScale.value = 1f
            _compactCards.value = false
            _isMetric.value = true
            _defaultBarbellWeight.value = 20
            _showVolumeInLog.value = true
            _autoStartRest.value = true
            _defaultRestSeconds.value = 90
            _trackWarmupVolume.value = false
            _firstDayOfWeek.value = "Monday"
            _hapticsEnabled.value = true
            _syncWifiOnly.value = false
        }
    }
}