package com.example.fitsync.data.remote

import com.example.fitsync.domain.model.WorkoutSession
import com.example.fitsync.domain.model.chat.ChatMessage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FitSyncApi {

    // Matches your Spring Boot @GetMapping("/api/workouts/user/{userId}")
    @GET("api/workouts/user/{userId}")
    suspend fun getUserWorkouts(@Path("userId") userId: Long): List<WorkoutSession>

    // Matches your Spring Boot @PostMapping("/api/workouts")
    @POST("api/workouts")
    suspend fun syncWorkout(@Body workout: WorkoutSession): WorkoutSession

    // --- Inside FitSyncApi.kt ---

    // Matches your Spring Boot @GetMapping("/api/chats/user/{userId}")
    @GET("api/chats/user/{userId}")
    suspend fun getUserChats(@Path("userId") userId: Long): List<ChatMessage>

    // Matches your Spring Boot @PostMapping("/api/chats")
    @POST("api/chats")
    suspend fun syncChatMessage(@Body payload: ChatSyncPayload): ChatSyncPayload
}