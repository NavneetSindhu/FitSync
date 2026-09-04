package com.minimize.maximus.domain.repository

import com.minimize.maximus.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface IWorkoutRepository {
    fun getAllWorkouts(): Flow<List<WorkoutSession>>
    suspend fun insert(workout: WorkoutSession, userId: Long = 1L): Long
    suspend fun insertAll(workouts: List<WorkoutSession>)
    suspend fun delete(workout: WorkoutSession)
}
