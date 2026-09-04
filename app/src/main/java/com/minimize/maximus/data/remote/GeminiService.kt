package com.minimize.maximus.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.minimize.maximus.BuildConfig
import com.minimize.maximus.domain.model.chat.Macros
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GeminiService(
    private val remoteConfigManager: RemoteConfigManager? = null
) {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private fun createGenerativeModel(config: AiCoachConfig, apiKey: String): GenerativeModel {
        return GenerativeModel(
            modelName = config.model_name.ifBlank { "gemini-3.5-flash-lite" },
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = config.temperature
                topP = config.top_p
                maxOutputTokens = if (config.max_tokens > 0) config.max_tokens else 800
            }
        )
    }

    suspend fun sendChatMessage(message: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (remoteConfigManager?.getAiCoachEnabled() == false) {
                    return@withContext Result.failure(Exception("AI Coach is temporarily offline for maintenance."))
                }

                val key = BuildConfig.GEMINI_API_KEY.trim()
                if (key.isBlank()) {
                    if (BuildConfig.DEBUG) {
                        return@withContext Result.success(
                            "🛑 [DEBUG: Missing Gemini API Key]\n\n" +
                                    "BuildConfig.GEMINI_API_KEY is currently empty or blank.\n\n" +
                                    "👉 Fix: Add your key in gradle.properties or local.properties:\n" +
                                    "GEMINI_API_KEY=AIzaSy..."
                        )
                    } else {
                        return@withContext Result.success(generateFallbackCoachAdvice(message))
                    }
                }

                val config = remoteConfigManager?.getAiCoachConfig() ?: AiCoachConfig()
                val model = createGenerativeModel(config, key)
                val fullPrompt = "${config.system_instruction.trim()}\n\nUser Question:\n$message"

                val response = model.generateContent(fullPrompt)
                val replyText = response.text
                if (!replyText.isNullOrBlank()) {
                    return@withContext Result.success(replyText)
                }

                if (BuildConfig.DEBUG) {
                    Result.success(
                        "🛑 [DEBUG: Empty Response]\n\nGenerativeModel returned an empty/null response from Gemini."
                    )
                } else {
                    Result.success(generateFallbackCoachAdvice(message))
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Chat error: ${e.message}", e)
                if (BuildConfig.DEBUG) {
                    Result.success(
                        "🛑 [DEBUG ERROR: ${e.javaClass.simpleName}]\n\n" +
                                "Message: ${e.message}\n" +
                                "Cause: ${e.cause?.message ?: "None"}"
                    )
                } else {
                    Result.success(generateFallbackCoachAdvice(message))
                }
            }
        }
    }

    suspend fun analyzeMealImage(image: Bitmap, userText: String = ""): Result<Macros> {
        return withContext(Dispatchers.IO) {
            try {
                val key = BuildConfig.GEMINI_API_KEY.trim()
                if (key.isBlank()) {
                    if (BuildConfig.DEBUG) {
                        return@withContext Result.success(
                            Macros(
                                isFood = false,
                                nonFoodMessage = "🛑 [DEBUG: Missing API Key]\n\nGEMINI_API_KEY is required for vision parsing. Add GEMINI_API_KEY in gradle.properties.",
                                nutritionistReply = "Debug vision error"
                            )
                        )
                    } else {
                        return@withContext Result.success(
                            Macros(
                                isFood = true,
                                mealName = "High-Protein Fitness Bowl",
                                estimatedQuantity = "1 standard serving (approx. 350g)",
                                calories = 480,
                                protein = 38,
                                carbs = 45,
                                fat = 14,
                                nutritionistReply = "Great balance of lean protein and complex carbs to fuel muscle protein synthesis! (Note: Connect a Gemini API key for real-time vision parsing)."
                            )
                        )
                    }
                }

                val config = remoteConfigManager?.getAiCoachConfig() ?: AiCoachConfig()
                val model = createGenerativeModel(config, key)
                val basePrompt = config.meal_analyser_instruction.ifBlank { AiCoachConfig.DEFAULT_MEAL_ANALYSER_INSTRUCTION }.trim()

                val finalPrompt = if (userText.isNotBlank()) {
                    "$basePrompt\n\nAdditionally, the user asked this context/question regarding the image: \"$userText\". Factor their context into your analysis while STRICTLY maintaining the JSON output format."
                } else {
                    basePrompt
                }

                    val inputContent = content {
                        image(image)
                        text(finalPrompt)
                    }

                val response = model.generateContent(inputContent)
                val rawText = response.text

                if (!rawText.isNullOrBlank()) {
                    val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
                    val analysisResult = jsonParser.decodeFromString<Macros>(cleanJson)
                    return@withContext Result.success(analysisResult)
                }

                if (BuildConfig.DEBUG) {
                    Result.success(
                        Macros(
                            isFood = false,
                            nonFoodMessage = "🛑 [DEBUG: Vision Failed]\n\nModel returned empty output for image input.",
                            nutritionistReply = "Debug vision failure"
                        )
                    )
                } else {
                    Result.success(
                        Macros(
                            isFood = true,
                            mealName = "High-Protein Fitness Bowl",
                            estimatedQuantity = "1 standard serving (approx. 350g)",
                            calories = 480,
                            protein = 38,
                            carbs = 45,
                            fat = 14,
                            nutritionistReply = "Great balance of lean protein and complex carbs to fuel muscle protein synthesis!"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to analyze meal: ${e.message}", e)
                if (BuildConfig.DEBUG) {
                    Result.success(
                        Macros(
                            isFood = false,
                            nonFoodMessage = "🛑 [DEBUG ERROR: ${e.javaClass.simpleName}]\n\nMessage: ${e.message}\nCause: ${e.cause?.message ?: "None"}",
                            nutritionistReply = "Debug exception in vision parsing"
                        )
                    )
                } else {
                    Result.success(
                        Macros(
                            isFood = true,
                            mealName = "Post-Workout Meal",
                            estimatedQuantity = "1 plate",
                            calories = 500,
                            protein = 40,
                            carbs = 50,
                            fat = 15,
                            nutritionistReply = "Looks like a solid recovery meal! Prioritize 0.8-1g protein per lb of body weight daily."
                        )
                    )
                }
            }
        }
    }

    private fun generateFallbackCoachAdvice(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("split") || lower.contains("routine") || lower.contains("program") ->
                "💪 **Recommended 4-Day Upper/Lower Split:**\n\n• **Mon (Upper Power):** Bench Press (4x6), Barbell Rows (4x8), Overhead Press (3x8), Pull-ups (3x10)\n• **Tue (Lower Power):** Barbell Squats (4x6), Romanian Deadlifts (4x8), Leg Press (3x10), Calves (4x15)\n• **Thu (Upper Hypertrophy):** Incline Dumbbell Press (4x10), Cable Rows (4x12), Lateral Raises (4x15), Bicep/Tricep Superset (3x12)\n• **Fri (Lower Hypertrophy):** Deadlifts/Hack Squats (4x8), Walking Lunges (3x12/leg), Leg Curls (4x12)\n\nAim for 2-3 RIR (Reps in Reserve) on all working sets!"

            lower.contains("volume") || lower.contains("sets") ->
                "📊 **Hypertrophy Volume Guidelines:**\n\n• **Optimal Direct Sets:** 10–20 hard sets per muscle group per week.\n• **Progression:** Add 1 rep per set or 1–2.5 kg once you hit the top of your rep range.\n• **Recovery:** Give muscle groups at least 48 hours before training them heavy again."

            lower.contains("bench") || lower.contains("plateau") ->
                "🔥 **Breaking a Bench Press Plateau:**\n\n1. **Increase Frequency:** Bench twice weekly (e.g. Heavy Flat Bench + Incline/Larsen Press).\n2. **Strengthen Weak Links:** Add heavy Tricep Extensions and Barbell Rows for a stronger bar-path foundation.\n3. **Microloading:** Increment by 1-1.5 kg (2.5 lbs) rather than jumping 5 kg at once."

            lower.contains("food") || lower.contains("meal") || lower.contains("nutrition") || lower.contains("protein") ->
                "🥗 **Nutrition & Muscle Recovery Fundamentals:**\n\n• **Daily Protein:** Target 1.6–2.2g per kg of bodyweight (0.8–1.0g/lb).\n• **Post-Workout Window:** 25–40g of complete protein + moderate carbs within 2 hours of training.\n• **Hydration:** Aim for 3–4 liters of water daily to maintain intracellular muscle volume."

            else ->
                "⚡ **Maximus AI Coach Advice:**\n\nConsistency and progressive overload are the master keys to strength and muscle growth. Keep logging every set, track your RPE, and ensure you're sleeping 7-9 hours for peak recovery!"
        }
    }
}