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
        apiKey = "AIzaSyCVcDYLICCR2opHOzFExQ0IA1WAOpqLFZA" // 🚨 REMEMBER TO CHANGE TO BuildConfig.GEMINI_API_KEY 🚨
    )

    private val jsonParser = Json { ignoreUnknownKeys = true }

    // This session remembers the context of the text conversation!
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

    // UPDATED: Now accepts an optional userText parameter
    suspend fun analyzeMealImage(image: Bitmap, userText: String = ""): Result<Macros> {
        return withContext(Dispatchers.IO) {
            try {
                val basePrompt = """
                    You are an expert nutritionist. Analyze the image provided.
                    First, determine if the image contains food. 
                    If it DOES NOT contain food, set "isFood" to false and provide a brief, friendly "nonFoodMessage".
                    If it DOES contain food, identify the "mealName", estimate the "estimatedQuantity", and calculate the macronutrients specifically for that estimated quantity.
                    
                    CRITICALLY: You must also provide a "nutritionistReply". If the user asked a specific question, answer it directly and encouragingly here. If they didn't ask a question, provide a brief, healthy tip about the meal.
                    
                    You MUST reply ONLY with a valid JSON object matching this exact structure. Do NOT include markdown formatting, backticks, or conversational text outside the JSON.
                    
                    {
                        "isFood": true,
                        "mealName": "String",
                        "estimatedQuantity": "String",
                        "calories": 0,
                        "protein": 0,
                        "carbs": 0,
                        "fat": 0,
                        "nonFoodMessage": "",
                        "nutritionistReply": "String"
                    }
                """.trimIndent()

                // Dynamically inject the user's text into the prompt if they typed something!
                val finalPrompt = if (userText.isNotBlank()) {
                    "$basePrompt\n\nAdditionally, the user asked this context/question regarding the image: \"$userText\". Factor their context into your analysis while STRICTLY maintaining the JSON output format."
                } else {
                    basePrompt
                }

                val inputContent = content {
                    image(image)
                    text(finalPrompt) // Use the combined prompt here
                }

                val response = generativeModel.generateContent(inputContent)
                val rawText = response.text

                if (rawText.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Gemini returned an empty response"))
                }

                // Strip markdown backticks just in case
                val cleanJson = rawText.replace("```json", "").replace("```", "").trim()

                // Decode into our Unified Macros object
                val analysisResult = jsonParser.decodeFromString<Macros>(cleanJson)

                Result.success(analysisResult)

            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to analyze meal: ${e.message}")
                Result.failure(e)
            }
        }
    }
}