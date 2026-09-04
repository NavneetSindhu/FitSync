package com.minimize.maximus.data.local.dao

import androidx.room.*
import com.minimize.maximus.data.local.entity.CustomExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM custom_exercises ORDER BY name ASC")
    fun getAllCustomExercises(): Flow<List<CustomExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomExercise(exercise: CustomExerciseEntity): Long

    @Delete
    suspend fun deleteCustomExercise(exercise: CustomExerciseEntity)

    @Query("DELETE FROM custom_exercises WHERE id = :id")
    suspend fun deleteCustomExerciseById(id: Long)
}
