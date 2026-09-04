package com.minimize.maximus.domain.model.exercise

import com.minimize.maximus.domain.model.body.MuscleGroup

enum class EquipmentType(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    CABLE("Cable"),
    MACHINE("Machine"),
    BODYWEIGHT("Bodyweight"),
    SMITH_MACHINE("Smith Machine"),
    KETTLEBELL("Kettlebell"),
    OTHER("Other")
}

data class ExerciseDefinition(
    val id: Long = 0L,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: EquipmentType = EquipmentType.BARBELL,
    val isCustom: Boolean = false,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultWeight: Float = 0f
)
