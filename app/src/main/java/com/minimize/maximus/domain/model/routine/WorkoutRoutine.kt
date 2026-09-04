package com.minimize.maximus.domain.model.routine

import kotlinx.serialization.Serializable

/**
 * Immutable Workout Routine Blueprint / Template.
 * Distinct from live WorkoutSession instances.
 */
@Serializable
data class WorkoutRoutine(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val targetMuscleGroups: List<String> = emptyList(),
    val exercises: List<RoutineExercise> = emptyList(),
    val estimatedDurationMinutes: Int = 45,
    val colorTagHex: String = "#FF5722",
    val isCustom: Boolean = true,
    val isDefaultTemplate: Boolean = false,
    val usageCount: Int = 0,
    val lastUsedTimestamp: Long? = null
)

/**
 * Exercise configuration within a routine blueprint.
 * Stores target sets, rep ranges, preferred starting weight, and rest duration.
 */
@Serializable
data class RoutineExercise(
    val exerciseName: String,
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val targetRepsRange: String = "8-12",
    val defaultWeight: Float = 0f,
    val targetRpe: Float? = null,
    val restSeconds: Int = 90,
    val notes: String = "",
    val orderIndex: Int = 0
)
