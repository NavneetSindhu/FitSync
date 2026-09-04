package com.minimize.maximus.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("fitsync_prefs", Context.MODE_PRIVATE)

    // --- APPEARANCE ---
    fun isDarkModeEnabled(): Boolean = sharedPreferences.getBoolean("dark_mode", false)
    fun setDarkMode(enabled: Boolean) = sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()

    fun getAccentColor(): Int = sharedPreferences.getInt("accent_color", Color(0xFF18181B).toArgb())
    fun setAccentColor(colorInt: Int) = sharedPreferences.edit().putInt("accent_color", colorInt).apply()

    fun getNavStyle(): String = sharedPreferences.getString("nav_style", "Floating Pill") ?: "Floating Pill"
    fun setNavStyle(style: String) = sharedPreferences.edit().putString("nav_style", style).apply()

    fun getFontScale(): Float = sharedPreferences.getFloat("font_scale", 1f)
    fun setFontScale(scale: Float) = sharedPreferences.edit().putFloat("font_scale", scale).apply()

    fun isCompactCards(): Boolean = sharedPreferences.getBoolean("compact_cards", false)
    fun setCompactCards(compact: Boolean) = sharedPreferences.edit().putBoolean("compact_cards", compact).apply()

    // --- UNITS & DEFAULTS ---
    fun isMetric(): Boolean = sharedPreferences.getBoolean("is_metric", true)
    fun setMetric(isMetric: Boolean) = sharedPreferences.edit().putBoolean("is_metric", isMetric).apply()

    fun isVolumeInLogShown(): Boolean = sharedPreferences.getBoolean("show_volume", true)
    fun setShowVolumeInLog(show: Boolean) = sharedPreferences.edit().putBoolean("show_volume", show).apply()

    fun getDefaultRestSeconds(): Int = sharedPreferences.getInt("rest_seconds", 90)
    fun setDefaultRestSeconds(seconds: Int) = sharedPreferences.edit().putInt("rest_seconds", seconds).apply()

    fun isHapticsEnabled(): Boolean = sharedPreferences.getBoolean("haptics", true)
    fun setHapticsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("haptics", enabled).apply()

    // --- PROFILE ---
    fun getUserName(): String = sharedPreferences.getString("user_name", "Athlete") ?: "Athlete"
    fun getUserGoal(): String = sharedPreferences.getString("user_goal", "Stay Fit") ?: "Stay Fit"

    fun saveUserData(name: String, goal: String) {
        sharedPreferences.edit()
            .putString("user_name", name)
            .putString("user_goal", goal)
            .apply()
    }

    // Add to UNITS & MEASUREMENTS
    fun getDefaultBarbellWeight(): Int = sharedPreferences.getInt("default_barbell", 20)
    fun setDefaultBarbellWeight(weight: Int) = sharedPreferences.edit().putInt("default_barbell", weight).apply()

    // Add to WORKOUT DEFAULTS
    fun isAutoStartRestEnabled(): Boolean = sharedPreferences.getBoolean("auto_start_rest", true)
    fun setAutoStartRestEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("auto_start_rest", enabled).apply()

    fun isTrackWarmupVolumeEnabled(): Boolean = sharedPreferences.getBoolean("track_warmups", false)
    fun setTrackWarmupVolumeEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("track_warmups", enabled).apply()

    // Add to APP EXPERIENCE & CALENDAR
    fun getFirstDayOfWeek(): String = sharedPreferences.getString("first_day_of_week", "Monday") ?: "Monday"
    fun setFirstDayOfWeek(day: String) = sharedPreferences.edit().putString("first_day_of_week", day).apply()

    // Add to DATA & CLOUD
    fun isSyncWifiOnlyEnabled(): Boolean = sharedPreferences.getBoolean("sync_wifi_only", false)
    fun setSyncWifiOnlyEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("sync_wifi_only", enabled).apply()

    // --- UTILS ---
    fun isFirstRun(): Boolean = sharedPreferences.getBoolean("is_first_run", true)
    fun setFirstRunCompleted(completed: Boolean = true) = sharedPreferences.edit().putBoolean("is_first_run", !completed).apply()


    // Add to PROFILE section in PreferenceManager.kt
    fun getAge(): Int = sharedPreferences.getInt("user_age", 21)
    fun setAge(age: Int) = sharedPreferences.edit().putInt("user_age", age).apply()

    fun getWeight(): Float = sharedPreferences.getFloat("user_weight", 75f)
    fun setWeight(weight: Float) = sharedPreferences.edit().putFloat("user_weight", weight).apply()

    fun getHeight(): Float = sharedPreferences.getFloat("user_height", 175f)
    fun setHeight(height: Float) = sharedPreferences.edit().putFloat("user_height", height).apply()

    fun isHeightMetric(): Boolean = sharedPreferences.getBoolean("is_height_metric", false)
    fun setHeightMetric(isMetric: Boolean) = sharedPreferences.edit().putBoolean("is_height_metric", isMetric).apply()

    fun getGender(): String = sharedPreferences.getString("user_gender", "Male") ?: "Male"
    fun setGender(gender: String) = sharedPreferences.edit().putString("user_gender", gender).apply()

    fun getUserAvatar(): String = sharedPreferences.getString("user_avatar", "avatar_male") ?: "avatar_male"
    fun setUserAvatar(avatar: String) = sharedPreferences.edit().putString("user_avatar", avatar).apply()

    // Add to NOTIFICATIONS section
    fun isNotificationsEnabled(): Boolean = sharedPreferences.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()

    fun getReminderHour(): Int = sharedPreferences.getInt("reminder_hour", 19)
    fun getReminderMinute(): Int = sharedPreferences.getInt("reminder_minute", 0)
    fun setReminderTime(hour: Int, minute: Int) = sharedPreferences.edit()
        .putInt("reminder_hour", hour)
        .putInt("reminder_minute", minute)
        .apply()

    // --- DEBUG ---
    fun isDebugShimmerEnabled(): Boolean = sharedPreferences.getBoolean("debug_force_shimmer", false)
    fun setDebugShimmerEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("debug_force_shimmer", enabled).apply()

    // --- WEEKLY PLAN & SPLIT ENGINE ---
    fun getWeeklyPlanJson(): String? = sharedPreferences.getString("weekly_plan_json", null)
    fun setWeeklyPlanJson(json: String) = sharedPreferences.edit().putString("weekly_plan_json", json).apply()

    fun getActiveSplitPreset(): String = sharedPreferences.getString("active_split_preset", "PUSH_PULL_LEGS") ?: "PUSH_PULL_LEGS"
    fun setActiveSplitPreset(preset: String) = sharedPreferences.edit().putString("active_split_preset", preset).apply()

    // --- STREAK FREEZE ENGINE ---
    fun getStreakFreezesAvailable(): Int = sharedPreferences.getInt("streak_freezes_available", 2).coerceIn(0, 2)
    fun setStreakFreezesAvailable(count: Int) = sharedPreferences.edit().putInt("streak_freezes_available", count.coerceIn(0, 2)).apply()

    fun getFrozenDates(): Set<java.time.LocalDate> {
        val stringSet = sharedPreferences.getStringSet("frozen_dates_set", emptySet()) ?: emptySet()
        return stringSet.mapNotNull {
            try {
                java.time.LocalDate.parse(it)
            } catch (_: Exception) {
                null
            }
        }.toSet()
    }

    fun saveFrozenDates(dates: Set<java.time.LocalDate>) {
        val stringSet = dates.map { it.toString() }.toSet()
        sharedPreferences.edit().putStringSet("frozen_dates_set", stringSet).apply()
    }

    fun useStreakFreeze(date: java.time.LocalDate): Boolean {
        val currentAvailable = getStreakFreezesAvailable()
        if (currentAvailable <= 0) return false
        val currentFrozen = getFrozenDates().toMutableSet()
        if (currentFrozen.contains(date)) return true // already frozen

        currentFrozen.add(date)
        saveFrozenDates(currentFrozen)
        setStreakFreezesAvailable(currentAvailable - 1)
        return true
    }

    fun clearAll() = sharedPreferences.edit().clear().apply()
}