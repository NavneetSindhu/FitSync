package com.example.fitsync.data.repository

import com.example.fitsync.data.local.dao.WorkoutDao
import com.example.fitsync.data.local.entity.WorkoutEntity
import com.example.fitsync.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val apiService: ApiService
) {
    // 1. Observe data as a Flow for the UI
    fun getAllWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    // 2. Simple helper for the ViewModel to get a snapshot
    suspend fun getAllWorkoutsSync(): List<WorkoutEntity> = workoutDao.getAllWorkouts().first()

    suspend fun insert(workout: WorkoutEntity, binId: String) {
        // Save locally first for speed
        workoutDao.insertWorkout(workout)

        // Trigger cloud sync if we have an ID
        if (binId.isNotEmpty()) {
            syncToCloud(binId)
        }
    }

    /**
     * THE MASTER SYNC: Pushes the whole journal to the cloud slot.
     */
    suspend fun syncToCloud(binId: String): Boolean {
        if (binId.isEmpty()) return false

        val allWorkouts = getAllWorkoutsSync()

        // Push the entire list using the Hex ID
        val success = apiService.updateJournal(binId, allWorkouts)

        if (success) {
            markAllAsSynced()
        }
        return success
    }

    /**
     * BULK UPDATE: Marks all local items as synced in one go.
     */
    suspend fun markAllAsSynced() {
        val allWorkouts = getAllWorkoutsSync()
        allWorkouts.forEach { workout ->
            if (!workout.isSynced) {
                workoutDao.insertWorkout(workout.copy(isSynced = true))
            }
        }
    }

    suspend fun deleteEverything(binId: String) {
        // 1. Wipe the specific cloud bin
        if (binId.isNotEmpty()) {
            apiService.deleteUserJournal(binId)
        }
        // 2. Wipe local DB
        workoutDao.deleteAllWorkouts()
    }

    suspend fun retryPendingSyncs(binId: String) {
        syncToCloud(binId)
    }

    suspend fun delete(workout: WorkoutEntity) = workoutDao.deleteWorkout(workout)
}