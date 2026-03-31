package com.example.fitsync.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WorkoutSet(
    val reps:Int,
    val setNumber:Int,
    val weight: Float,
    val isCompleted: Boolean=false
)
@Serializable
data class Exercise(
    val name:String,
    val sets:List<WorkoutSet>
)

@Serializable
data class WorkoutSession(
    val id:String = UUID.randomUUID().toString(),
    val date:Long = System.currentTimeMillis(),
    val exercise:List<Exercise>
)