package com.example.fitsync.ui.theme

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
    fun getMetaData(name: String): ExerciseMeta {
        return when {
            // Chest
            name.contains("Bench", true) ->
                ExerciseMeta(R.drawable.ic_chest_bench, AccentRed)
            name.contains("Push", true) || name.contains("Pushup", true) ->
                ExerciseMeta(R.drawable.ic_chest_pushup, AccentRed)

            // Back
            name.contains("Deadlift", true) ->
                ExerciseMeta(R.drawable.ic_back_deadlift, AccentRed)
            name.contains("Pull", true) || name.contains("Pullup", true) ->
                ExerciseMeta(R.drawable.ic_back_pullup, AccentRed)
            name.contains("Row", true) || name.contains("Rowing", true) ->
                ExerciseMeta(R.drawable.ic_back_rowing, AccentRed)

            // Legs
            name.contains("Squat", true) ->
                ExerciseMeta(R.drawable.ic_leg_squat, AccentRed)
            name.contains("Lunge", true) || name.contains("Lunges", true) ->
                ExerciseMeta(R.drawable.ic_leg_lunges, AccentRed)

            // Shoulders & Arms
            name.contains("Press", true) ->
                ExerciseMeta(R.drawable.ic_shoulder_press, AccentRed)
            name.contains("Dip", true) || name.contains("Dips", true) ->
                ExerciseMeta(R.drawable.ic_shoulder_dips, AccentRed)

            // Cardio
            name.contains("Run", true) || name.contains("Cardio", true) ->
                ExerciseMeta(R.drawable.ic_cardio_running, AccentRed)

            // Fallback (Using Pullup as the default icon)
            else -> ExerciseMeta(R.drawable.ic_back_pullup, Color.Gray)
        }
    }
}