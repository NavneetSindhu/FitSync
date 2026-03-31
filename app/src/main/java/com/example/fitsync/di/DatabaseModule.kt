package com.example.fitsync.di

import android.content.Context
import com.example.fitsync.data.local.FitSyncDatabase
import com.example.fitsync.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitSyncDatabase {
        // Uses the singleton pattern we wrote in FitSyncDatabase.kt
        return FitSyncDatabase.getDatabase(context)
    }

    @Provides
    fun provideWorkoutDao(database: FitSyncDatabase): WorkoutDao {
        // Tells Hilt how to get the Dao from the Database
        return database.workoutDao()
    }
}