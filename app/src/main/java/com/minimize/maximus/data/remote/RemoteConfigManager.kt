package com.minimize.maximus.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.minimize.maximus.BuildConfig
import com.minimize.maximus.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AiCoachConfig(
    val model_name: String = "gemini-3.5-flash-lite",
    val temperature: Float = 0.6f,
    val top_p: Float = 0.9f,
    val max_tokens: Int = 800,
    val system_instruction: String = DEFAULT_SYSTEM_INSTRUCTION,
    val meal_analyser_instruction: String = DEFAULT_MEAL_ANALYSER_INSTRUCTION
) {
    companion object {
        const val DEFAULT_SYSTEM_INSTRUCTION = "You are Maximus AI, an elite, highly concise strength, hypertrophy, and sports nutrition coach.\n\nRULES FOR TOKEN EFFICIENCY & QUALITY:\n- Proportional Length: For quick questions, answer directly in 2–4 concise bullet points or sentences (under 60 words).\n- Zero Fluff: Never use filler greetings, pleasantries, or introductory monologues.\n- High Density: Use clean Markdown with bold metrics (**30g protein**, **90s rest**, **4x8 reps**).\n- Routine Generation: When the user asks you to create, design, or suggest a workout routine or split, provide your brief rationale in Markdown, followed by a valid micro-JSON block fenced with ```routine_json:\n```routine_json\n{\n  \"name\": \"Routine Title\",\n  \"description\": \"Brief 1-line description\",\n  \"estimatedDurationMinutes\": 45,\n  \"exercises\": [\n    {\"name\": \"Barbell Bench Press\", \"sets\": 4, \"reps\": 8, \"weight\": 60, \"rest\": 90}\n  ]\n}\n```\n- Quick-Reply Suggestions: Whenever helpful (e.g. after suggesting workouts, nutrition, or advice), optionally append 2–3 short contextual follow-up quick-reply suggestion pills fenced with ```chips_json:\n```chips_json\n[\"Make it under 30 min\", \"Swap for Dumbbells\", \"Add Core Work\"]\n```\nKeep all JSON schemas clean and exact."

        const val DEFAULT_MEAL_ANALYSER_INSTRUCTION = "You are an expert nutritionist. Analyze the image provided.\nFirst, determine if the image contains food.\nIf it DOES NOT contain food, set \"isFood\" to false and provide a brief, friendly \"nonFoodMessage\".\nIf it DOES contain food, identify the \"mealName\", estimate the \"estimatedQuantity\", and calculate the macronutrients specifically for that estimated quantity.\n\nCRITICALLY: You must also provide a \"nutritionistReply\". If the user asked a specific question, answer it directly and encouragingly here. If they didn't ask a question, provide a brief, healthy tip about the meal.\n\nYou MUST reply ONLY with a valid JSON object matching this exact structure. Do NOT include markdown formatting, backticks, or conversational text outside the JSON.\n\n{\n    \"isFood\": true,\n    \"mealName\": \"String\",\n    \"estimatedQuantity\": \"String\",\n    \"calories\": 0,\n    \"protein\": 0,\n    \"carbs\": 0,\n    \"fat\": 0,\n    \"nonFoodMessage\": \"\",\n    \"nutritionistReply\": \"String\"\n}"
    }
}

@Serializable
data class NotificationConfig(
    val title_template: String = "Time to Crush It, {name}! 💪",
    val body_template: String = "Keep your momentum burning toward \"{goal}\". Log your workout session to maintain your streak!",
    val title_variations: List<String> = listOf(
        "Time to Crush It, {name}! 💪",
        "Your Workout is Waiting, {name}! 🔥",
        "Protect Your Streak, {name}! ⚡",
        "Consistency Breeds Champions, {name}! 🏆"
    ),
    val body_variations: List<String> = listOf(
        "Keep your momentum burning toward \"{goal}\". Log your workout session to maintain your streak!",
        "A 45-minute workout is only 3% of your day. Make it count today!",
        "Every rep counts toward your \"{goal}\". Let's get to work!",
        "Don't break the chain. Show up for yourself today!"
    )
)

@Singleton
class RemoteConfigManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    companion object {
        private const val TAG = "RemoteConfigManager"
        const val KEY_AI_COACH_ENABLED = "ai_coach_enabled"
        const val KEY_AI_COACH_CONFIG = "ai_coach_config"
        const val KEY_NOTIFICATION_CONFIG = "notification_config"
        const val KEY_AI_MAX_TOKENS = "ai_chat_response_max_tokens"
        const val KEY_MIN_SUPPORTED_VERSION = "min_supported_version"
        const val KEY_MAX_STREAK_FREEZE_COUNT = "max_streak_freeze_count"
        const val KEY_MAINTENANCE_MODE = "maintenance_mode"
    }

    private val _isAiCoachEnabled = MutableStateFlow(true)
    val isAiCoachEnabled: StateFlow<Boolean> = _isAiCoachEnabled.asStateFlow()

    private val _notificationConfig = MutableStateFlow(NotificationConfig())
    val notificationConfig: StateFlow<NotificationConfig> = _notificationConfig.asStateFlow()

    private val _aiCoachConfig = MutableStateFlow(AiCoachConfig())
    val aiCoachConfig: StateFlow<AiCoachConfig> = _aiCoachConfig.asStateFlow()

    private val _maxAiTokens = MutableStateFlow(400L)
    val maxAiTokens: StateFlow<Long> = _maxAiTokens.asStateFlow()

    private val _minSupportedVersion = MutableStateFlow(100L)
    val minSupportedVersion: StateFlow<Long> = _minSupportedVersion.asStateFlow()

    private val _maxStreakFreezeCount = MutableStateFlow(2L)
    val maxStreakFreezeCount: StateFlow<Long> = _maxStreakFreezeCount.asStateFlow()

    private val _isMaintenanceMode = MutableStateFlow(false)
    val isMaintenanceMode: StateFlow<Boolean> = _isMaintenanceMode.asStateFlow()

    fun initialize() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
            fetchTimeoutInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // Read initially loaded in-app defaults
        updateLocalState()

        // Fetch new values from Firebase Remote Config backend
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Remote config fetched and activated (updated=$updated)")
                    updateLocalState()
                } else {
                    Log.w(TAG, "Remote config fetch failed: ${task.exception?.message}")
                }
            }

        // Real-time listener for live updates without app restarts
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d(TAG, "Remote config updated keys: ${configUpdate.updatedKeys}")
                remoteConfig.activate().addOnCompleteListener {
                    updateLocalState()
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Remote config real-time update error", error)
            }
        })
    }

    private fun updateLocalState() {
        _isAiCoachEnabled.value = remoteConfig.getBoolean(KEY_AI_COACH_ENABLED)
        _maxAiTokens.value = remoteConfig.getLong(KEY_AI_MAX_TOKENS).let { if (it > 0) it else 400L }
        _minSupportedVersion.value = remoteConfig.getLong(KEY_MIN_SUPPORTED_VERSION)
        _maxStreakFreezeCount.value = remoteConfig.getLong(KEY_MAX_STREAK_FREEZE_COUNT).let { if (it > 0) it else 2L }
        _isMaintenanceMode.value = remoteConfig.getBoolean(KEY_MAINTENANCE_MODE)

        // Parse AI Coach JSON Config with guaranteed local fallback
        _aiCoachConfig.value = try {
            val rawJson = remoteConfig.getString(KEY_AI_COACH_CONFIG)
            if (rawJson.isNotBlank()) {
                json.decodeFromString<AiCoachConfig>(rawJson)
            } else {
                AiCoachConfig()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse ai_coach_config JSON, falling back to defaults", e)
            AiCoachConfig()
        }

        // Parse Notification JSON Config with guaranteed local fallback
        _notificationConfig.value = try {
            val rawJson = remoteConfig.getString(KEY_NOTIFICATION_CONFIG)
            if (rawJson.isNotBlank()) {
                json.decodeFromString<NotificationConfig>(rawJson)
            } else {
                NotificationConfig()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse notification_config JSON, falling back to defaults", e)
            NotificationConfig()
        }
    }

    // Direct synchronous getters
    fun getAiCoachEnabled(): Boolean = _isAiCoachEnabled.value
    fun getAiCoachConfig(): AiCoachConfig = _aiCoachConfig.value
    fun getNotificationConfig(): NotificationConfig = _notificationConfig.value
    fun getMaxAiTokens(): Long = _maxAiTokens.value
    fun getMinSupportedVersion(): Long = _minSupportedVersion.value
    fun getMaxStreakFreezeCount(): Int = _maxStreakFreezeCount.value.toInt()
    fun getIsMaintenanceMode(): Boolean = _isMaintenanceMode.value
}
