package com.example.fitsync.data.local

import androidx.room.TypeConverter
import com.example.fitsync.domain.model.Exercise
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString // Ensure this import is present

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true // Good practice for Room
    }

    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String {
        return try {
            json.encodeToString(value)
        } catch (e: Exception) {
            "[]" // Return empty JSON array on error
        }
    }

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        return try {
            // Adding the explicit type here prevents potential mapping issues
            json.decodeFromString<List<Exercise>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}