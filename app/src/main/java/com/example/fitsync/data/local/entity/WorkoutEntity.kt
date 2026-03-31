package com.example.fitsync.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitsync.domain.model.Exercise
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val exerciseList: List<Exercise>,
    val isSynced: Boolean = false // NEW: Track cloud status
)