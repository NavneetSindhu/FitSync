package com.example.fitsync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitsync.data.local.dao.WorkoutDao
import com.example.fitsync.domain.model.WorkoutSession

@Database(entities = [WorkoutSession::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FitSyncDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: FitSyncDatabase? = null

        fun getDatabase(context: Context): FitSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitSyncDatabase::class.java,
                    "fitsync_database"
                )
                    // This will clear the old data and fix the integrity error
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}