package com.example.fitsync.data.repository

import android.util.Log
import com.example.fitsync.data.local.dao.WorkoutDao
import com.example.fitsync.data.remote.FitSyncApi
import com.example.fitsync.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val api: FitSyncApi // <-- Swapped ApiService for your new FitSyncApi
) {
    // 1. UI observes local DB (This stays exactly the same!)
    fun getAllWorkouts(): Flow<List<WorkoutSession>> = workoutDao.getAllWorkouts()

    // 2. Fetch from Spring Boot Server
    // Instead of a binId, we use your PostgreSQL userId (e.g., 1L)
    suspend fun fetchWorkoutsFromServer(userId: Long) {
        try {
            // Get workouts from your GET /api/workouts/user/{userId} endpoint
            val remoteWorkouts = api.getUserWorkouts(userId)

            // Overwrite or update local DB with the truth from the server
            // Note: You might need an insertAll() function in your DAO!
            remoteWorkouts.forEach { workout ->
                workoutDao.insertWorkout(workout)
            }
        } catch (e: Exception) {
            Log.e("Sync", "Failed to fetch from server: ${e.message}")
        }
    }

    // 3. Save a single workout
    suspend fun insert(workout: WorkoutSession, userId: Long) {
        // Save locally first so the UI feels instantly fast
        workoutDao.insertWorkout(workout)

        // Try to push just this ONE workout to your POST /api/workouts endpoint
        try {
            // Note: Your workout model needs to include the user ID for the backend
            // e.g., workout.user = User(id = userId) before sending
            val savedRemote = api.syncWorkout(workout)

            // If successful, you could update the local DB to mark it as synced
            // workoutDao.markAsSynced(savedRemote.id)
            Log.d("Sync", "Successfully saved to Postgres!")
        } catch (e: Exception) {
            Log.e("Sync", "Server unreachable, workout saved locally to sync later.")
        }
    }

    // 4. Delete locally and remotely
    suspend fun delete(workout: WorkoutSession) {
        // Delete locally
        workoutDao.deleteWorkout(workout)

        // TODO: You will eventually need a DELETE endpoint in Spring Boot
        // api.deleteWorkout(workout.id)
    }
}