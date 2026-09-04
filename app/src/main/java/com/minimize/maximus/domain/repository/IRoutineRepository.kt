package com.minimize.maximus.domain.repository

import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.domain.model.routine.SplitPresetType
import com.minimize.maximus.domain.model.routine.WeeklyPlan
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface IRoutineRepository {
    fun getAllRoutines(): Flow<List<WorkoutRoutine>>
    suspend fun getRoutineById(id: Long): WorkoutRoutine?
    suspend fun saveRoutine(routine: WorkoutRoutine): Long
    suspend fun deleteRoutine(id: Long)
    suspend fun duplicateRoutine(id: Long): Long
    suspend fun incrementRoutineUsage(id: Long)
    suspend fun seedDefaultTemplatesIfEmpty()

    // Weekly Plan Engine
    fun getWeeklyPlan(): Flow<WeeklyPlan>
    suspend fun saveWeeklyPlan(plan: WeeklyPlan)
    suspend fun applySplitPreset(preset: SplitPresetType)
    suspend fun assignRoutineToDay(day: DayOfWeek, routineId: Long?, routineName: String?)

    // Exercise Library
    fun getAllExercises(): Flow<List<ExerciseDefinition>>
    suspend fun saveCustomExercise(exercise: ExerciseDefinition): Long
    suspend fun deleteCustomExercise(id: Long)
}
