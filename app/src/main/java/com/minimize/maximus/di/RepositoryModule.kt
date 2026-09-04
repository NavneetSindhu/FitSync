package com.minimize.maximus.di

import com.minimize.maximus.data.repository.ChatRepository
import com.minimize.maximus.data.repository.ChatRepositoryImpl
import com.minimize.maximus.data.repository.RoutineRepository
import com.minimize.maximus.data.repository.WorkoutRepository
import com.minimize.maximus.domain.repository.IRoutineRepository
import com.minimize.maximus.domain.repository.IWorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepository: WorkoutRepository
    ): IWorkoutRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        routineRepository: RoutineRepository
    ): IRoutineRepository
}