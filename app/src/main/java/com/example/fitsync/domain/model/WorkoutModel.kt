package com.example.fitsync.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

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
@Entity(tableName = "workouts")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val date:Long = System.currentTimeMillis(),
    val exercise:List<Exercise>,
    val isSynced: Boolean = false
)

