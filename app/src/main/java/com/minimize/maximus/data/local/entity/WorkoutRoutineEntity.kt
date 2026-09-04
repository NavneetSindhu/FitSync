package com.minimize.maximus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minimize.maximus.domain.model.routine.RoutineExercise
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
    @PrimaryKey(autoGenerate = true)
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
) {
    fun toDomain(): WorkoutRoutine = WorkoutRoutine(
        id = id,
        name = name,
        description = description,
        targetMuscleGroups = targetMuscleGroups,
        exercises = exercises,
        estimatedDurationMinutes = estimatedDurationMinutes,
        colorTagHex = colorTagHex,
        isCustom = isCustom,
        isDefaultTemplate = isDefaultTemplate,
        usageCount = usageCount,
        lastUsedTimestamp = lastUsedTimestamp
    )

    companion object {
        fun fromDomain(routine: WorkoutRoutine): WorkoutRoutineEntity = WorkoutRoutineEntity(
            id = routine.id,
            name = routine.name,
            description = routine.description,
            targetMuscleGroups = routine.targetMuscleGroups,
            exercises = routine.exercises,
            estimatedDurationMinutes = routine.estimatedDurationMinutes,
            colorTagHex = routine.colorTagHex,
            isCustom = routine.isCustom,
            isDefaultTemplate = routine.isDefaultTemplate,
            usageCount = routine.usageCount,
            lastUsedTimestamp = routine.lastUsedTimestamp
        )
    }
}
