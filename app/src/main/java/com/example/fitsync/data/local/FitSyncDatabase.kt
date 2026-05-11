package com.example.fitsync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitsync.data.local.dao.ChatDao
import com.example.fitsync.data.local.dao.WorkoutDao
import com.example.fitsync.data.local.entity.ChatMessageEntity // Make sure to import this!
import com.example.fitsync.domain.model.WorkoutSession

// 1. ADDED ChatMessageEntity::class to the entities array!
// 2. BUMPED version to 4 to trigger the creation of the new table.
@Database(entities = [WorkoutSession::class, ChatMessageEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FitSyncDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun chatDao(): ChatDao

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
                    // This will clear the old data and rebuild with both tables
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}