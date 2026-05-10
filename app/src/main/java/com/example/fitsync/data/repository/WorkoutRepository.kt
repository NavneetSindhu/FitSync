package com.example.fitsync.data.repository

import android.util.Log
import com.example.fitsync.data.local.dao.WorkoutDao
import com.example.fitsync.data.remote.FitSyncApi
import com.example.fitsync.data.remote.UserIdPayload
import com.example.fitsync.data.remote.WorkoutSyncPayload
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
    // We are temporarily hardcoding the userId to 1L for testing!
    // We are temporarily hardcoding the userId to 1L for testing!
    suspend fun insert(workout: WorkoutSession, userId: Long = 1L) {
        // 1. Save locally first for instant speed.
        // (Room handles the Lists because you are using @Serializable!)
        workoutDao.insertWorkout(workout)

        // 2. Try to sync to the Spring Boot server
        try {
            // --- NEW: Calculate the totals from your List<Exercise> ---
            var calculatedVolume = 0.0
            var calculatedSets = 0

            workout.exercise.forEach { ex ->
                calculatedSets += ex.sets.size
                ex.sets.forEach { set ->
                    // Volume = Reps * Weight
                    calculatedVolume += (set.reps * set.weight)
                }
            }

            // --- NEW: Generate a dynamic title ---
            // e.g., "Bench Press & 2 more exercises" or "Quick Session"
            val dynamicTitle = if (workout.exercise.isNotEmpty()) {
                val firstExercise = workout.exercise.first().name
                val extraCount = workout.exercise.size - 1
                if (extraCount > 0) "$firstExercise & $extraCount more" else firstExercise
            } else {
                "Quick Session"
            }

            // --- Translate to the Network Payload ---
            val networkPayload = WorkoutSyncPayload(
                dayTitle = dynamicTitle,
                totalVolume = calculatedVolume,
                totalSets = calculatedSets,
                timestamp = workout.date,
                user = UserIdPayload(id = userId)
            )

            // Send to the server
            api.syncWorkout(networkPayload)
            Log.d("WorkoutSync", "Workout successfully backed up to Postgres!")

            // BONUS: Since you added 'isSynced' to your model, you could update Room here!
            // workoutDao.updateSyncStatus(workout.id, true)

        } catch (e: Exception) {
            // If the server is offline, the app stays alive and the data is safe in Room.
            Log.e("WorkoutSync", "Server offline, saved locally. Error: ", e)
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