package com.example.fitsync.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.example.fitsync.domain.model.chat.Macros
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GeminiService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyATyKnH8xv7-NGLjz6xnVU5Kqh5o-5SZhg" // USE BUILD_CONFIG!
    )

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val chatSession = generativeModel.startChat()

    suspend fun sendChatMessage(message: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // We inject a tiny bit of prompt engineering invisibly so it acts like a nutritionist
                val contextPrompt = "You are Sync AI, an expert, friendly nutritionist for a fitness app. Keep your answers brief, encouraging, and highly practical. User says: $message"

                val response = chatSession.sendMessage(contextPrompt)

                val replyText = response.text
                if (replyText.isNullOrBlank()) {
                    Result.failure(Exception("Empty text response"))
                } else {
                    Result.success(replyText)
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Chat error: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // NOTE: Returning MealAnalysisResponse instead of Macros now!
    suspend fun analyzeMealImage(image: Bitmap): Result<Macros> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are an expert nutritionist. Analyze the image provided.
                    First, determine if the image contains food. 
                    If it DOES NOT contain food, set "isFood" to false and provide a brief, friendly "nonFoodMessage".
                    If it DOES contain food, identify the "mealName", estimate the "estimatedQuantity" (e.g., "1 medium bowl", "approx 200g"), and calculate the macronutrients specifically for that estimated quantity.
                    
                    You MUST reply ONLY with a valid JSON object matching this exact structure. Do NOT include markdown formatting, backticks, or conversational text.
                    
                    Example for Food:
                    {
                        "isFood": true,
                        "mealName": "Chicken Quinoa Bowl",
                        "estimatedQuantity": "1 medium bowl (approx 350g)",
                        "calories": 450,
                        "protein": 35,
                        "carbs": 25,
                        "fat": 15,
                        "nonFoodMessage": ""
                    }
                    
                    Example for Non-Food:
                    {
                        "isFood": false,
                        "mealName": "",
                        "estimatedQuantity": "",
                        "calories": 0,
                        "protein": 0,
                        "carbs": 0,
                        "fat": 0,
                        "nonFoodMessage": "I don't see any food in this image! Please upload a clear photo of your meal."
                    }
                """.trimIndent()

                val inputContent = content {
                    image(image)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)
                val rawText = response.text

                if (rawText.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Gemini returned an empty response"))
                }

                // Strip markdown backticks just in case
                val cleanJson = rawText.replace("```json", "").replace("```", "").trim()

                // Decode into our new Response object
                val analysisResult = jsonParser.decodeFromString<Macros>(cleanJson)

                Result.success(analysisResult)

            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to analyze meal: ${e.message}")
                Result.failure(e)
            }
        }
    }


}