package com.example.fitsync.data.local.dao


import androidx.room.*
import com.example.fitsync.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutSession): Long

    @Delete
    suspend fun deleteWorkout(workout: WorkoutSession)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    @Query("UPDATE workouts SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllAsSynced()
}