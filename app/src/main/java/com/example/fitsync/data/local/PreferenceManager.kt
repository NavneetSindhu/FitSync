package com.example.fitsync.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("fitsync_prefs", Context.MODE_PRIVATE)

    // --- DARK MODE (This fixes the red error) ---
    fun isDarkModeEnabled(): Boolean = sharedPreferences.getBoolean("dark_mode_enabled", false)

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode_enabled", enabled).apply()
    }

    // --- PROFILE & FIRST RUN ---
    fun isFirstRun(): Boolean = sharedPreferences.getBoolean("is_first_run", true)

    fun setFirstRunCompleted() {
        sharedPreferences.edit().putBoolean("is_first_run", false).apply()
    }

    fun saveUserData(name: String, goal: String) {
        sharedPreferences.edit()
            .putString("user_name", name)
            .putString("user_goal", goal)
            .apply()
    }

    fun getUserName(): String = sharedPreferences.getString("user_name", "Athlete") ?: "Athlete"
    fun getUserGoal(): String = sharedPreferences.getString("user_goal", "Stay Fit") ?: "Stay Fit"

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    // Add these to your existing PreferenceManager class
    fun getBinId(): String = sharedPreferences.getString("json_bin_id", "") ?: ""

    fun saveBinId(id: String) {
        sharedPreferences.edit().putString("json_bin_id", id).apply()
    }
}