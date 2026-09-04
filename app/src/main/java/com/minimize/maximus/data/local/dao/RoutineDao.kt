package com.minimize.maximus.data.local.dao

import androidx.room.*
import com.minimize.maximus.data.local.entity.WorkoutRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM workout_routines ORDER BY isDefaultTemplate ASC, lastUsedTimestamp DESC, id DESC")
    fun getAllRoutines(): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT * FROM workout_routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): WorkoutRoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: WorkoutRoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: WorkoutRoutineEntity)

    @Query("DELETE FROM workout_routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    @Query("UPDATE workout_routines SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementRoutineUsage(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM workout_routines")
    suspend fun getRoutineCount(): Int

    @Query("SELECT COUNT(*) FROM workout_routines WHERE isDefaultTemplate = 1")
    suspend fun getTemplateCount(): Int
}
