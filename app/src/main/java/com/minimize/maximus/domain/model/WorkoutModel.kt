package com.minimize.maximus.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.LocalDate

enum class SetType(val code: String, val label: String) {
    NORMAL("N", "Normal"),
    WARMUP("W", "Warmup"),
    DROP("D", "Drop Set"),
    FAILURE("F", "Failure");

    companion object {
        fun fromCode(code: String): SetType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NORMAL
        }
    }
}

@Serializable
data class WorkoutSet(
    val reps: Int,
    val setNumber: Int,
    val weight: Float,
    val isCompleted: Boolean = false,
    val setType: String = "N"
)

@Serializable
data class Exercise(
    val name: String,
    val sets: List<WorkoutSet>
)

@Serializable
@Entity(tableName = "workouts")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val exercise: List<Exercise>,
    val isSynced: Boolean = false
)

data class CalendarUiState @RequiresApi(Build.VERSION_CODES.O) constructor(
    val selectedDate: LocalDate = LocalDate.now(),
    val workoutMap: Map<LocalDate, WorkoutSummary> = emptyMap()
)

data class WorkoutSummary(
    val intensity: Int,
    val volume: String,
    val sets: Int
)
