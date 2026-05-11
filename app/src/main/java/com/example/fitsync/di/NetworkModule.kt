package com.example.fitsync.di

import com.example.fitsync.data.remote.FitSyncApi // Make sure this matches your package!
import com.example.fitsync.data.remote.GeminiService
import com.example.fitsync.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService()
    }



    // --- NEW: Spring Boot & Retrofit Configuration ---

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            // 10.0.2.2 points the Android Emulator to your computer's localhost
            // VERY IMPORTANT: Retrofit base URLs must end with a trailing slash "/"
            .baseUrl("http://10.0.2.2:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFitSyncApi(retrofit: Retrofit): FitSyncApi {
        // Hilt will automatically pass the Retrofit instance from the function above into here
        return retrofit.create(FitSyncApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository = AuthRepository(auth)
}