package com.minimize.maximus.data.repository

import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.data.local.dao.ExerciseDao
import com.minimize.maximus.data.local.dao.RoutineDao
import com.minimize.maximus.data.local.entity.CustomExerciseEntity
import com.minimize.maximus.data.local.entity.WorkoutRoutineEntity
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.domain.model.routine.*
import com.minimize.maximus.domain.repository.IRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao,
    private val preferenceManager: PreferenceManager
) : IRoutineRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val _weeklyPlanFlow = MutableStateFlow(loadWeeklyPlanFromPrefs())

    init {
        // Seed built-in routines on first launch
    }

    override fun getAllRoutines(): Flow<List<WorkoutRoutine>> {
        return routineDao.getAllRoutines().map { entities ->
            val dbRoutines = entities.map { it.toDomain() }
            val customRoutines = dbRoutines.filter { it.isCustom && !it.isDefaultTemplate }
            val dbTemplates = dbRoutines.filter { it.isDefaultTemplate || !it.isCustom }

            val templates = if (dbTemplates.isEmpty()) {
                DefaultExerciseDatabase.DEFAULT_ROUTINE_TEMPLATES.mapIndexed { index, routine ->
                    if (routine.id == 0L) routine.copy(id = -(index + 1L)) else routine
                }
            } else {
                dbTemplates
            }
            customRoutines + templates
        }
    }

    override suspend fun getRoutineById(id: Long): WorkoutRoutine? {
        val fromDb = routineDao.getRoutineById(id)?.toDomain()
        if (fromDb != null) return fromDb
        return DefaultExerciseDatabase.DEFAULT_ROUTINE_TEMPLATES.find { it.id == id }
            ?: DefaultExerciseDatabase.DEFAULT_ROUTINE_TEMPLATES.getOrNull((-id - 1).toInt())
    }

    override suspend fun saveRoutine(routine: WorkoutRoutine): Long {
        val entity = WorkoutRoutineEntity.fromDomain(routine)
        return routineDao.insertRoutine(entity)
    }

    override suspend fun deleteRoutine(id: Long) {
        routineDao.deleteRoutineById(id)
    }

    override suspend fun duplicateRoutine(id: Long): Long {
        val original = getRoutineById(id) ?: return 0L
        val duplicate = original.copy(
            id = 0L,
            name = "${original.name} (Copy)",
            isCustom = true,
            isDefaultTemplate = false,
            usageCount = 0,
            lastUsedTimestamp = null
        )
        return saveRoutine(duplicate)
    }

    override suspend fun incrementRoutineUsage(id: Long) {
        routineDao.incrementRoutineUsage(id, System.currentTimeMillis())
    }

    override suspend fun seedDefaultTemplatesIfEmpty() {
        val count = routineDao.getTemplateCount()
        if (count == 0) {
            DefaultExerciseDatabase.DEFAULT_ROUTINE_TEMPLATES.forEach { template ->
                routineDao.insertRoutine(WorkoutRoutineEntity.fromDomain(template.copy(isDefaultTemplate = true, isCustom = false)))
            }
        }
    }

    // ── Weekly Plan Engine ───────────────────────────────────────────────
    override fun getWeeklyPlan(): Flow<WeeklyPlan> = _weeklyPlanFlow.asStateFlow()

    override suspend fun saveWeeklyPlan(plan: WeeklyPlan) {
        _weeklyPlanFlow.value = plan
        persistWeeklyPlan(plan)
    }

    override suspend fun applySplitPreset(preset: SplitPresetType) {
        val schedule = when (preset) {
            SplitPresetType.PUSH_PULL_LEGS -> WeeklyPlan.defaultPplSchedule()
            SplitPresetType.UPPER_LOWER -> WeeklyPlan.defaultUpperLowerSchedule()
            SplitPresetType.FULL_BODY -> WeeklyPlan.defaultFullBodySchedule()
            SplitPresetType.ARNOLD_SPLIT -> WeeklyPlan.defaultArnoldSchedule()
            SplitPresetType.CUSTOM -> _weeklyPlanFlow.value.days
        }
        val newPlan = WeeklyPlan(presetType = preset, days = schedule)
        saveWeeklyPlan(newPlan)
    }

    override suspend fun assignRoutineToDay(
        day: DayOfWeek,
        routineId: Long?,
        routineName: String?
    ) {
        val currentDays = _weeklyPlanFlow.value.days.toMutableMap()
        currentDays[day] = DaySchedule(
            dayOfWeek = day,
            routineId = routineId,
            routineName = routineName,
            isRestDay = routineId == null && routineName == null
        )
        val updatedPlan = _weeklyPlanFlow.value.copy(
            presetType = SplitPresetType.CUSTOM,
            days = currentDays
        )
        saveWeeklyPlan(updatedPlan)
    }

    private fun loadWeeklyPlanFromPrefs(): WeeklyPlan {
        val jsonStr = preferenceManager.getWeeklyPlanJson()
        if (!jsonStr.isNullOrBlank()) {
            return try {
                val plan = json.decodeFromString<WeeklyPlanDto>(jsonStr)
                plan.toDomain()
            } catch (e: Exception) {
                WeeklyPlan()
            }
        }
        return WeeklyPlan()
    }

    private fun persistWeeklyPlan(plan: WeeklyPlan) {
        try {
            val dto = WeeklyPlanDto.fromDomain(plan)
            val jsonStr = json.encodeToString(dto)
            preferenceManager.setWeeklyPlanJson(jsonStr)
        } catch (_: Exception) {}
    }

    // ── Exercise Library ─────────────────────────────────────────────────
    override fun getAllExercises(): Flow<List<ExerciseDefinition>> {
        return exerciseDao.getAllCustomExercises().map { customEntities ->
            val customs = customEntities.map { it.toDomain() }
            // Merge built-in database with custom movements
            customs + DefaultExerciseDatabase.BUILT_IN_EXERCISES
        }
    }

    override suspend fun saveCustomExercise(exercise: ExerciseDefinition): Long {
        val entity = CustomExerciseEntity.fromDomain(exercise)
        return exerciseDao.insertCustomExercise(entity)
    }

    override suspend fun deleteCustomExercise(id: Long) {
        exerciseDao.deleteCustomExerciseById(id)
    }
}

// ── Serialization DTOs for WeeklyPlan ─────────────────────────────────────
@kotlinx.serialization.Serializable
private data class DayScheduleDto(
    val dayName: String,
    val routineId: Long? = null,
    val routineName: String? = null,
    val isRestDay: Boolean
) {
    fun toDomain(): DaySchedule = DaySchedule(
        dayOfWeek = DayOfWeek.valueOf(dayName),
        routineId = routineId,
        routineName = routineName,
        isRestDay = isRestDay
    )

    companion object {
        fun fromDomain(d: DaySchedule): DayScheduleDto = DayScheduleDto(
            dayName = d.dayOfWeek.name,
            routineId = d.routineId,
            routineName = d.routineName,
            isRestDay = d.isRestDay
        )
    }
}

@kotlinx.serialization.Serializable
private data class WeeklyPlanDto(
    val presetName: String,
    val days: List<DayScheduleDto>
) {
    fun toDomain(): WeeklyPlan {
        val map = days.associate { it.toDomain().dayOfWeek to it.toDomain() }
        val preset = runCatching { SplitPresetType.valueOf(presetName) }.getOrDefault(SplitPresetType.PUSH_PULL_LEGS)
        return WeeklyPlan(presetType = preset, days = map)
    }

    companion object {
        fun fromDomain(p: WeeklyPlan): WeeklyPlanDto = WeeklyPlanDto(
            presetName = p.presetType.name,
            days = p.days.values.map { DayScheduleDto.fromDomain(it) }
        )
    }
}
