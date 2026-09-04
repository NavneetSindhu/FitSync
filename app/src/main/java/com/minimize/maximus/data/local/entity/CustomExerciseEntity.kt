package com.minimize.maximus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.exercise.EquipmentType
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "custom_exercises")
data class CustomExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val primaryMuscleName: String,
    val secondaryMuscleNames: List<String> = emptyList(),
    val equipmentName: String = EquipmentType.BARBELL.name,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultWeight: Float = 0f
) {
    fun toDomain(): ExerciseDefinition = ExerciseDefinition(
        id = id,
        name = name,
        primaryMuscle = runCatching { MuscleGroup.valueOf(primaryMuscleName) }.getOrDefault(MuscleGroup.CHEST),
        secondaryMuscles = secondaryMuscleNames.mapNotNull { runCatching { MuscleGroup.valueOf(it) }.getOrNull() },
        equipment = runCatching { EquipmentType.valueOf(equipmentName) }.getOrDefault(EquipmentType.BARBELL),
        isCustom = true,
        defaultSets = defaultSets,
        defaultReps = defaultReps,
        defaultWeight = defaultWeight
    )

    companion object {
        fun fromDomain(def: ExerciseDefinition): CustomExerciseEntity = CustomExerciseEntity(
            id = def.id,
            name = def.name,
            primaryMuscleName = def.primaryMuscle.name,
            secondaryMuscleNames = def.secondaryMuscles.map { it.name },
            equipmentName = def.equipment.name,
            defaultSets = def.defaultSets,
            defaultReps = def.defaultReps,
            defaultWeight = def.defaultWeight
        )
    }
}
