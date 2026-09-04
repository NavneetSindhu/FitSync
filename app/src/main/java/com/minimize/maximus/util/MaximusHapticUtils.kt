package com.minimize.maximus.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Categorized interaction and lifecycle events with dedicated tactile & audio priorities.
 */
enum class MaximusEvent(val priority: Int) {
    SELECTION_TAP(1),
    ACTION_TAP(1),
    TAB_SWITCH(1),
    FILTER_SWITCH(1),
    SWIPE_REVEAL_THRESHOLD(2),
    SET_COMPLETED(3),
    PR_RECORD_UNLOCKED(4),
    TIMER_WARNING(2),
    REST_TIMER_EXPIRED(4),
    ERROR_ALERT(3)
}

object MaximusHapticUtils {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Executes tactile feedback mapped to specific UX event and priority.
     * Uses hardware-accelerated VibrationEffect on API 29+ with Compose fallback.
     */
    fun perform(
        haptic: HapticFeedback?,
        event: MaximusEvent,
        enabled: Boolean = true,
        context: Context? = null
    ) {
        if (!enabled) return

        // Check if user has globally disabled haptic feedback in Preferences
        val targetContext = context ?: appContext
        val isPrefEnabled = targetContext?.getSharedPreferences("fitsync_prefs", Context.MODE_PRIVATE)
            ?.getBoolean("haptics", true) ?: true

        if (!isPrefEnabled) return

        // 1. Compose UI Haptic Feedback (Immediate, Low-Latency)
        haptic?.let {
            try {
                when (event) {
                    MaximusEvent.SELECTION_TAP,
                    MaximusEvent.ACTION_TAP,
                    MaximusEvent.TAB_SWITCH,
                    MaximusEvent.FILTER_SWITCH,
                    MaximusEvent.TIMER_WARNING,
                    MaximusEvent.SWIPE_REVEAL_THRESHOLD -> it.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                    MaximusEvent.SET_COMPLETED,
                    MaximusEvent.PR_RECORD_UNLOCKED,
                    MaximusEvent.REST_TIMER_EXPIRED,
                    MaximusEvent.ERROR_ALERT -> it.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            } catch (_: Exception) {}
        }

        // 2. Hardware Vibrator API for High-Precision Tactical Actuation across all devices
        if (targetContext != null) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = targetContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    targetContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            when (event) {
                                MaximusEvent.SELECTION_TAP,
                                MaximusEvent.ACTION_TAP,
                                MaximusEvent.TAB_SWITCH,
                                MaximusEvent.FILTER_SWITCH -> {
                                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                                }
                                MaximusEvent.SWIPE_REVEAL_THRESHOLD,
                                MaximusEvent.TIMER_WARNING -> {
                                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                                }
                                MaximusEvent.SET_COMPLETED -> {
                                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                                }
                                MaximusEvent.PR_RECORD_UNLOCKED -> {
                                    val pattern = longArrayOf(0, 40, 50, 120)
                                    val amplitudes = intArrayOf(0, 180, 0, 255)
                                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                                }
                                MaximusEvent.REST_TIMER_EXPIRED -> {
                                    val pattern = longArrayOf(0, 80, 50, 80, 50, 120)
                                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                                }
                                MaximusEvent.ERROR_ALERT -> {
                                    val pattern = longArrayOf(0, 100, 60, 100)
                                    val amplitudes = intArrayOf(0, 255, 0, 255)
                                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                                }
                            }
                        } catch (_: Exception) {
                            // Fallback if predefined effect unsupported by hardware
                            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (event == MaximusEvent.PR_RECORD_UNLOCKED || event == MaximusEvent.REST_TIMER_EXPIRED) {
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 100), -1))
                        } else {
                            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(20L)
                    }
                }
            } catch (_: Exception) {
                // Graceful fallback if device restricts tactile motors
            }
        }
    }
}
