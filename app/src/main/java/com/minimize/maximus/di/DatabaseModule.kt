package com.minimize.maximus.di

import android.content.Context
import com.minimize.maximus.data.local.MaximusDatabase
import com.minimize.maximus.data.local.dao.ChatDao
import com.minimize.maximus.data.local.dao.ExerciseDao
import com.minimize.maximus.data.local.dao.RoutineDao
import com.minimize.maximus.data.local.dao.WorkoutDao
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
    fun provideDatabase(@ApplicationContext context: Context): MaximusDatabase {
        return MaximusDatabase.getDatabase(context)
    }

    @Provides
    fun provideWorkoutDao(database: MaximusDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideChatDao(database: MaximusDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    fun provideRoutineDao(database: MaximusDatabase): RoutineDao {
        return database.routineDao()
    }

    @Provides
    fun provideExerciseDao(database: MaximusDatabase): ExerciseDao {
        return database.exerciseDao()
    }
}