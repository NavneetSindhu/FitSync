package com.example.fitsync.data.remote

import com.google.gson.annotations.SerializedName

// This perfectly matches what Spring Boot expects!
data class ChatSyncPayload(
    val messageText: String,
    val sentByUser: Boolean,
    val macrosJson: String?,
    val timestamp: Long,
    val user: UserIdPayload
)

data class UserIdPayload(
    val id: Long
)



data class WorkoutSyncPayload(
    val dayTitle: String,
    val totalVolume: Double,
    val totalSets: Int,
    val timestamp: Long,
    val user: UserIdPayload
)