package com.minimize.maximus.data.repository

import com.minimize.maximus.data.local.dao.WorkoutDao
import com.minimize.maximus.di.IoDispatcher
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.domain.repository.IWorkoutRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IWorkoutRepository {

    override fun getAllWorkouts(): Flow<List<WorkoutSession>> = workoutDao.getAllWorkouts()

    override suspend fun insert(workout: WorkoutSession, userId: Long): Long = withContext(ioDispatcher) {
        workoutDao.insertWorkout(workout)
    }

    override suspend fun insertAll(workouts: List<WorkoutSession>) = withContext(ioDispatcher) {
        workoutDao.insertAll(workouts)
    }

    override suspend fun delete(workout: WorkoutSession) = withContext(ioDispatcher) {
        workoutDao.deleteWorkout(workout)
    }
}