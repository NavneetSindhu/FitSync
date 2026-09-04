package com.minimize.maximus.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.domain.repository.IWorkoutRepository
import com.minimize.maximus.util.BackupUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferenceManager,
    private val workoutRepository: IWorkoutRepository,
    private val database: com.minimize.maximus.data.local.MaximusDatabase
) : ViewModel() {

    // ── Navigation & Scroll Events ─────────────────────────────────────────────
    private val _homeScrollToTopEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homeScrollToTopEvent: SharedFlow<Unit> = _homeScrollToTopEvent.asSharedFlow()

    fun requestHomeScrollToTop() {
        _homeScrollToTopEvent.tryEmit(Unit)
    }

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

    // ── 7. Debug & Preview Options ─────────────────────────────────────────────
    private val _forceShimmer = MutableStateFlow(prefs.isDebugShimmerEnabled())
    val forceShimmer: StateFlow<Boolean> = _forceShimmer.asStateFlow()

    // ── Mutators (UPDATE FLOW AND DISK SIMULTANEOUSLY) ─────────────────────────

    fun toggleForceShimmer(enabled: Boolean) {
        _forceShimmer.value = enabled
        prefs.setDebugShimmerEnabled(enabled)
    }

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


    // ── 7. Notifications ──────────────────────────────────────────────────────
    private val _notificationsEnabled = MutableStateFlow(prefs.isNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(prefs.getReminderHour())
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(prefs.getReminderMinute())
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    // ── 8. First Run Flow ──────────────────────────────────────────────────────
    private val _isFirstRun = MutableStateFlow(prefs.isFirstRun())
    val isFirstRun: StateFlow<Boolean> = _isFirstRun.asStateFlow()

    fun completeOnboarding(
        name: String,
        age: Int,
        weight: Float,
        height: Float,
        isHeightMetric: Boolean = false,
        gender: String,
        goal: String,
        isMetric: Boolean,
        accentColor: Int
    ) {
        val defaultAvatar = com.minimize.maximus.ui.components.getAvatarForGender(gender)
        prefs.setUserAvatar(defaultAvatar)
        prefs.saveUserData(name, goal)
        prefs.setAge(age)
        prefs.setWeight(weight)
        prefs.setHeight(height)
        prefs.setHeightMetric(isHeightMetric)
        prefs.setGender(gender)
        prefs.setMetric(isMetric)
        prefs.setAccentColor(accentColor)
        prefs.setFirstRunCompleted()

        _userName.value = name
        _userGoal.value = goal
        _isMetric.value = isMetric
        _accentColor.value = accentColor
        _isFirstRun.value = false
    }

    fun setNotificationsEnabled(enabled: Boolean, context: Context) {
        _notificationsEnabled.value = enabled
        prefs.setNotificationsEnabled(enabled)
        if (enabled) {
            com.minimize.maximus.util.notification.ReminderSchedulerUtil.scheduleDailyReminder(context)
        } else {
            com.minimize.maximus.util.notification.ReminderSchedulerUtil.cancelDailyReminder(context)
        }
    }

    fun setReminderTime(hour: Int, minute: Int, context: Context) {
        _reminderHour.value = hour
        _reminderMinute.value = minute
        prefs.setReminderTime(hour, minute)
        if (_notificationsEnabled.value) {
            com.minimize.maximus.util.notification.ReminderSchedulerUtil.scheduleDailyReminder(context, hour, minute)
        }
    }

    fun fullReset(userId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            database.clearAllTables()
            prefs.clearAll() // Wipe the disk

            // Reset Flows to defaults
            _userName.value = "Athlete"
            _userGoal.value = "Stay Fit"
            _isDarkMode.value = false
            _accentColor.value = Color(0xFF18181B).toArgb()
            _navStyle.value = "Floating Pill"
            _fontScale.value = 1f
            _compactCards.value = false
            _isMetric.value = true
            _defaultBarbellWeight.value = 20
            _showVolumeInLog.value = true
            _autoStartRest.value = true
            _trackWarmupVolume.value = false
            _firstDayOfWeek.value = "Monday"
            _hapticsEnabled.value = true
            _syncWifiOnly.value = false
            _isFirstRun.value = true
        }
    }

    fun exportWorkoutsToCsv(context: Context, onError: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val workouts = workoutRepository.getAllWorkouts().first()
                val fileUri = BackupUtils.exportCsvFile(context, workouts)
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Maximus Workout History (CSV)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Workout History (CSV)").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                onError?.invoke(e.localizedMessage ?: "Export failed")
            }
        }
    }

    // ── Developer & Debug Actions ──────────────────────────────────────────────

    fun seedSampleWorkouts() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val oneDayMillis = 86400000L

            val sampleSessions = listOf(
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Full Body Primer",
                    date = now - (13 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Bench Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 1, weight = 50f, isCompleted = true, setType = "W"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 2, weight = 70f, isCompleted = true, setType = "N")
                            )
                        ),
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Back Squat",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 1, weight = 80f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Push Hypertrophy",
                    date = now - (12 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Incline Dumbbell Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 1, weight = 24f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 2, weight = 26f, isCompleted = true, setType = "N")
                            )
                        ),
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Tricep Rope Pushdown",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 12, setNumber = 1, weight = 20f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Pull & Lats",
                    date = now - (10 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Deadlift",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 6, setNumber = 1, weight = 100f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 6, setNumber = 2, weight = 120f, isCompleted = true, setType = "N")
                            )
                        ),
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Lat Pulldown",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 1, weight = 55f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Leg Day Burnout",
                    date = now - (9 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Back Squat",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 1, weight = 90f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 6, setNumber = 2, weight = 105f, isCompleted = true, setType = "N")
                            )
                        ),
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Romanian Deadlift",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 1, weight = 70f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Shoulders & Arms",
                    date = now - (7 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Overhead Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 1, weight = 45f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 2, weight = 50f, isCompleted = true, setType = "N")
                            )
                        ),
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Bicep Curl",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 12, setNumber = 1, weight = 30f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Upper Power",
                    date = now - (6 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Bench Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 5, setNumber = 1, weight = 85f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 5, setNumber = 2, weight = 95f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Lower Quad Focus",
                    date = now - (5 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Leg Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 12, setNumber = 1, weight = 160f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 2, weight = 180f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Chest & Front Delts",
                    date = now - (4 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Incline Dumbbell Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 1, weight = 30f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 2, weight = 32f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Upper Body Hypertrophy",
                    date = now - (3 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Bench Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 1, weight = 60f, isCompleted = true, setType = "W"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 2, weight = 80f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 6, setNumber = 3, weight = 90f, isCompleted = true, setType = "N")
                            )
                        ),
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Incline Dumbbell Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 1, weight = 28f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 2, weight = 28f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Leg Day Power",
                    date = now - (2 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Barbell Back Squat",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 5, setNumber = 1, weight = 100f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 5, setNumber = 2, weight = 110f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 3, setNumber = 3, weight = 120f, isCompleted = true, setType = "PR")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Pull & Core",
                    date = now - (1 * oneDayMillis),
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Pull-ups",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 12, setNumber = 1, weight = 0f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 10, setNumber = 2, weight = 0f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                ),
                com.minimize.maximus.domain.model.WorkoutSession(
                    id = 0L,
                    name = "Today's Grind",
                    date = now,
                    exercise = listOf(
                        com.minimize.maximus.domain.model.Exercise(
                            name = "Overhead Press",
                            sets = listOf(
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 8, setNumber = 1, weight = 50f, isCompleted = true, setType = "N"),
                                com.minimize.maximus.domain.model.WorkoutSet(reps = 6, setNumber = 2, weight = 55f, isCompleted = true, setType = "N")
                            )
                        )
                    )
                )
            )

            sampleSessions.forEach { workoutRepository.insert(it, 1L) }
        }
    }

    fun triggerImmediateTestNotification(context: Context) {
        val request = androidx.work.OneTimeWorkRequestBuilder<com.minimize.maximus.util.notification.WorkoutReminderWorker>()
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(request)
    }

    fun triggerFirstRunReset() {
        prefs.clearAll()
        _isFirstRun.value = true
    }
}