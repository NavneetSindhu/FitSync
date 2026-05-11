package com.example.fitsync.data.local

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

    fun getAccentColor(): Int = sharedPreferences.getInt("accent_color", Color(0xFFE53935).toArgb())
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
    fun setFirstRunCompleted() = sharedPreferences.edit().putBoolean("is_first_run", false).apply()


    // Add to PROFILE section in PreferenceManager.kt
    fun getAge(): Int = sharedPreferences.getInt("user_age", 21)
    fun setAge(age: Int) = sharedPreferences.edit().putInt("user_age", age).apply()

    fun getWeight(): Float = sharedPreferences.getFloat("user_weight", 75f)
    fun setWeight(weight: Float) = sharedPreferences.edit().putFloat("user_weight", weight).apply()

    fun getHeight(): Float = sharedPreferences.getFloat("user_height", 175f)
    fun setHeight(height: Float) = sharedPreferences.edit().putFloat("user_height", height).apply()

    fun getGender(): String = sharedPreferences.getString("user_gender", "Male") ?: "Male"
    fun setGender(gender: String) = sharedPreferences.edit().putString("user_gender", gender).apply()

    // Add to NOTIFICATIONS section
    fun isNotificationsEnabled(): Boolean = sharedPreferences.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()

    fun clearAll() = sharedPreferences.edit().clear().apply()
}