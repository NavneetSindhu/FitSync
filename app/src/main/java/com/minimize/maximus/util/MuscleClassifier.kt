package com.minimize.maximus.util

import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.body.MuscleStat
import com.minimize.maximus.domain.model.body.RecoveryState
import java.time.Instant
import java.time.temporal.ChronoUnit

object MuscleClassifier {

    fun getPrimaryMuscles(exerciseName: String): List<MuscleGroup> {
        val name = exerciseName.lowercase()
        return when {
            // Chest
            name.contains("bench") || name.contains("chest") || name.contains("pushup") || name.contains("fly") || name.contains("pec") ->
                listOf(MuscleGroup.CHEST, MuscleGroup.FRONT_DELTS, MuscleGroup.TRICEPS)

            // Shoulders
            name.contains("overhead") || name.contains("military") || name.contains("shoulder") || name.contains("ohp") ->
                listOf(MuscleGroup.FRONT_DELTS, MuscleGroup.SIDE_DELTS, MuscleGroup.TRICEPS)
            name.contains("lateral raise") || name.contains("side raise") ->
                listOf(MuscleGroup.SIDE_DELTS)
            name.contains("rear delt") || name.contains("face pull") || name.contains("reverse fly") ->
                listOf(MuscleGroup.REAR_DELTS, MuscleGroup.TRAPS)

            // Back & Lats
            name.contains("pullup") || name.contains("pull-up") || name.contains("chin-up") || name.contains("lat pulldown") || name.contains("row") ->
                listOf(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.TRAPS)
            name.contains("deadlift") || name.contains("rack pull") ->
                listOf(MuscleGroup.LOWER_BACK, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.TRAPS)
            name.contains("shrug") ->
                listOf(MuscleGroup.TRAPS)

            // Arms
            name.contains("curl") || name.contains("bicep") ->
                listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
            name.contains("tricep") || name.contains("dip") || name.contains("skull crusher") || name.contains("pushdown") || name.contains("extension") ->
                listOf(MuscleGroup.TRICEPS)
            name.contains("wrist") || name.contains("grip") || name.contains("farmer") ->
                listOf(MuscleGroup.FOREARMS)

            // Legs
            name.contains("squat") || name.contains("leg press") || name.contains("lunge") || name.contains("quad") || name.contains("hack") ->
                listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES)
            name.contains("romanian") || name.contains("rdl") || name.contains("hamstring") || name.contains("leg curl") ->
                listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES)
            name.contains("hip thrust") || name.contains("glute") || name.contains("kickback") ->
                listOf(MuscleGroup.GLUTES)
            name.contains("calf") || name.contains("calves") ->
                listOf(MuscleGroup.CALVES)

            // Core / Abs
            name.contains("plank") || name.contains("crunch") || name.contains("abs") || name.contains("leg raise") || name.contains("situp") ->
                listOf(MuscleGroup.ABS, MuscleGroup.OBLIQUES)

            else -> listOf(MuscleGroup.CHEST, MuscleGroup.LATS)
        }
    }

    fun computeMuscleStats(workouts: List<WorkoutSession>): Map<MuscleGroup, MuscleStat> {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)

        val recentWorkouts = workouts.filter { it.date >= sevenDaysAgo }

        val setsMap = mutableMapOf<MuscleGroup, Int>()
        val volumeMap = mutableMapOf<MuscleGroup, Double>()
        val latestTimestampMap = mutableMapOf<MuscleGroup, Long>()
        val exercisesMap = mutableMapOf<MuscleGroup, MutableSet<String>>()

        // Initialize all muscle groups
        MuscleGroup.entries.forEach { group ->
            setsMap[group] = 0
            volumeMap[group] = 0.0
            exercisesMap[group] = mutableSetOf()
        }

        recentWorkouts.forEach { session ->
            session.exercise.forEach { ex ->
                val muscles = getPrimaryMuscles(ex.name)
                val totalSets = ex.sets.size
                val exVolume = ex.sets.sumOf { (it.weight * it.reps).toDouble() }

                muscles.forEach { muscle ->
                    setsMap[muscle] = (setsMap[muscle] ?: 0) + totalSets
                    volumeMap[muscle] = (volumeMap[muscle] ?: 0.0) + exVolume
                    exercisesMap[muscle]?.add(ex.name)

                    val currLatest = latestTimestampMap[muscle] ?: 0L
                    if (session.date > currLatest) {
                        latestTimestampMap[muscle] = session.date
                    }
                }
            }
        }

        return MuscleGroup.entries.associateWith { group ->
            val sets = setsMap[group] ?: 0
            val vol = volumeMap[group] ?: 0.0
            val latestTime = latestTimestampMap[group]
            val hoursAgo = if (latestTime != null) {
                ((now - latestTime) / (1000 * 60 * 60)).coerceAtLeast(0)
            } else null

            // Determine recovery based on sets & hours elapsed
            val (recoveryState, percentage) = when {
                hoursAgo == null || sets == 0 -> RecoveryState.FRESH to 100
                hoursAgo < 18 -> RecoveryState.FATIGUED to (25 + (hoursAgo * 2)).toInt().coerceAtMost(60)
                hoursAgo in 18..36 -> RecoveryState.RECOVERING to (60 + ((hoursAgo - 18) * 1.8)).toInt().coerceAtMost(85)
                sets > 16 && hoursAgo < 48 -> RecoveryState.OPTIMAL to 85
                else -> RecoveryState.FRESH to 100
            }

            MuscleStat(
                muscleGroup = group,
                weeklySets = sets,
                weeklyVolume = vol,
                lastTrainedHoursAgo = hoursAgo,
                recoveryState = recoveryState,
                recoveryPercentage = percentage,
                contributingExercises = exercisesMap[group]?.toList() ?: emptyList()
            )
        }
    }
}
