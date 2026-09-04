package com.minimize.maximus.data.local

import androidx.room.TypeConverter
import com.minimize.maximus.domain.model.Exercise
import com.minimize.maximus.domain.model.routine.RoutineExercise
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String {
        return try {
            json.encodeToString(value)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        return try {
            json.decodeFromString<List<Exercise>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromRoutineExerciseList(value: List<RoutineExercise>): String {
        return try {
            json.encodeToString(value)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toRoutineExerciseList(value: String): List<RoutineExercise> {
        return try {
            json.decodeFromString<List<RoutineExercise>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return try {
            json.encodeToString(value)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}