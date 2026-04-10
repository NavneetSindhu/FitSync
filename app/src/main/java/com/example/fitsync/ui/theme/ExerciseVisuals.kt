package com.example.fitsync.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.fitsync.R

/**
 * Metadata for exercise visuals using PNG resources from Flaticon.
 */
data class ExerciseMeta(
    val iconRes: Int,
    val accentColor: Color
)

object ExerciseVisuals {

    // 🔥 Added @Composable so it can read your dynamic theme colors
    @Composable
    fun getMetaData(name: String): ExerciseMeta {

        // Grab the color once to make the code cleaner and more efficient
        val themeAccent = LocalAccentColor.current

        return when {
            // Chest
            name.contains("Bench", true) ->
                ExerciseMeta(R.drawable.ic_chest_bench, themeAccent)
            name.contains("Push", true) || name.contains("Pushup", true) ->
                ExerciseMeta(R.drawable.ic_chest_pushup, themeAccent)

            // Back
            name.contains("Deadlift", true) ->
                ExerciseMeta(R.drawable.ic_back_deadlift, themeAccent)
            name.contains("Pull", true) || name.contains("Pullup", true) ->
                ExerciseMeta(R.drawable.ic_back_pullup, themeAccent)
            name.contains("Row", true) || name.contains("Rowing", true) ->
                ExerciseMeta(R.drawable.ic_back_rowing, themeAccent)

            // Legs
            name.contains("Squat", true) ->
                ExerciseMeta(R.drawable.ic_leg_squat, themeAccent)
            name.contains("Lunge", true) || name.contains("Lunges", true) ->
                ExerciseMeta(R.drawable.ic_leg_lunges, themeAccent)

            // Shoulders & Arms
            name.contains("Press", true) ->
                ExerciseMeta(R.drawable.ic_shoulder_press, themeAccent)
            name.contains("Dip", true) || name.contains("Dips", true) ->
                ExerciseMeta(R.drawable.ic_shoulder_dips, themeAccent)

            // Cardio
            name.contains("Run", true) || name.contains("Cardio", true) ->
                ExerciseMeta(R.drawable.ic_cardio_running, themeAccent)

            // Fallback (Using Pullup as the default icon)
            else -> ExerciseMeta(R.drawable.ic_back_pullup, Color.Gray)
        }
    }
}