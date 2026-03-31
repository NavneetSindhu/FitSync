package com.example.fitsync.data.remote

import com.example.fitsync.data.local.entity.WorkoutEntity
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

// --- MODELS FOR JSONBIN.IO ---
@Serializable
data class BinResponse(val metadata: BinMetadata)

@Serializable
data class BinMetadata(val id: String)

@Serializable
data class SyncPayload(
    val lastUpdated: Long,
    val workouts: List<WorkoutEntity>
)

@Singleton
class ApiService @Inject constructor(private val client: HttpClient) {

    private val MASTER_KEY = "$2a$10$5T1FIv9KUG.xChBIT8fVpeOrwh1TtAtCh8UobVDCLt.KBQ2Znddqa"
    private val BASE_URL = "https://api.jsonbin.io/v3/b"

    /**
     * 1. CREATE: Use this once to get a random hex ID from JSONBin.
     */
    suspend fun createJournal(allWorkouts: List<WorkoutEntity>): String? {
        return try {
            val response: HttpResponse = client.post(BASE_URL) {
                header("X-Master-Key", MASTER_KEY)
                contentType(ContentType.Application.Json)
                setBody(SyncPayload(System.currentTimeMillis(), allWorkouts))
            }

            if (response.status.isSuccess()) {
                val body: BinResponse = response.body()
                println("🚀 Success! Generated Bin ID: ${body.metadata.id}")
                body.metadata.id
            } else {
                println("⚠️ Creation Failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("⚠️ Network Error: ${e.message}")
            null
        }
    }

    /**
     * 2. UPDATE: Use this for all future syncs using the generated Hex ID.
     */
    suspend fun updateJournal(binId: String, allWorkouts: List<WorkoutEntity>): Boolean {
        if (binId.isBlank()) return false
        return try {
            val response: HttpResponse = client.put("$BASE_URL/$binId") {
                header("X-Master-Key", MASTER_KEY)
                header("X-Bin-Versioning", "false")
                contentType(ContentType.Application.Json)
                setBody(SyncPayload(System.currentTimeMillis(), allWorkouts))
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 3. DELETE: Permanently wipes the bin from JSONBin.io.
     */
    suspend fun deleteUserJournal(binId: String): Boolean {
        if (binId.isBlank()) return false
        return try {
            val response = client.delete("$BASE_URL/$binId") {
                header("X-Master-Key", MASTER_KEY)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("⚠️ Delete Error: ${e.message}")
            false
        }
    }
}