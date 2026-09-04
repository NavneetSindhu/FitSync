package com.minimize.maximus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minimize.maximus.data.local.dao.ChatDao
import com.minimize.maximus.data.local.dao.ExerciseDao
import com.minimize.maximus.data.local.dao.RoutineDao
import com.minimize.maximus.data.local.dao.WorkoutDao
import com.minimize.maximus.data.local.entity.ChatMessageEntity
import com.minimize.maximus.data.local.entity.CustomExerciseEntity
import com.minimize.maximus.data.local.entity.WorkoutRoutineEntity
import com.minimize.maximus.domain.model.WorkoutSession

@Database(
    entities = [
        WorkoutSession::class,
        ChatMessageEntity::class,
        WorkoutRoutineEntity::class,
        CustomExerciseEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MaximusDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun chatDao(): ChatDao
    abstract fun routineDao(): RoutineDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: MaximusDatabase? = null

        fun getDatabase(context: Context): MaximusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MaximusDatabase::class.java,
                    "fitsync_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}