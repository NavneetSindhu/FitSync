package com.minimize.maximus.ui.screens.chat

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.data.remote.GeminiService
import com.minimize.maximus.data.repository.ChatRepository
import com.minimize.maximus.domain.model.body.RecoveryState
import com.minimize.maximus.domain.model.chat.ChatMessage
import com.minimize.maximus.domain.model.routine.RoutineExercise
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.domain.repository.IRoutineRepository
import com.minimize.maximus.domain.repository.IWorkoutRepository
import com.minimize.maximus.util.DateUtils
import com.minimize.maximus.util.ImageStorageHelper
import com.minimize.maximus.util.MuscleClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

private val routineJsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Serializable
private data class AiRoutinePayload(
    val name: String,
    val description: String = "AI-Generated Routine",
    val estimatedDurationMinutes: Int = 45,
    val exercises: List<AiExercisePayload> = emptyList()
) {
    fun toDomain(): WorkoutRoutine = WorkoutRoutine(
        id = 0L,
        name = name.ifBlank { "AI Custom Routine" },
        description = description,
        estimatedDurationMinutes = estimatedDurationMinutes,
        exercises = exercises.mapIndexed { idx, ex ->
            RoutineExercise(
                exerciseName = ex.name,
                targetSets = ex.sets.coerceIn(1, 10),
                targetReps = ex.reps.coerceIn(1, 100),
                defaultWeight = ex.weight,
                restSeconds = if (ex.rest > 0) ex.rest else 90,
                orderIndex = idx
            )
        },
        isCustom = true,
        isDefaultTemplate = false
    )
}

@Serializable
private data class AiExercisePayload(
    val name: String,
    val sets: Int = 3,
    val reps: Int = 10,
    val weight: Float = 0f,
    val rest: Int = 90
)

// Single state object — isTyping and messages update atomically in one emission
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val chatRepository: ChatRepository,
    private val workoutRepository: IWorkoutRepository,
    private val routineRepository: IRoutineRepository,
    private val prefs: PreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val todayDateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val _isTyping = MutableStateFlow(false)

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepository.getMessagesForToday(todayDateString),
        _isTyping
    ) { messages, isTyping ->
        ChatUiState(messages = messages, isTyping = isTyping)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    private suspend fun buildEnrichedPrompt(rawText: String): String {
        val name = prefs.getUserName()
        val goal = prefs.getUserGoal()
        val unit = if (prefs.isMetric()) "kg" else "lbs"
        val workouts = workoutRepository.getAllWorkouts().first()
        val recentWorkouts = workouts.take(3)
        val workoutContext = if (recentWorkouts.isNotEmpty()) {
            recentWorkouts.joinToString("; ") { "${it.name} (${it.exercise.size} exercises, ${DateUtils.getRelativeDateHeader(it.date)})" }
        } else {
            "No workouts logged yet"
        }

        val muscleStats = MuscleClassifier.computeMuscleStats(workouts)
        val recoveringMuscles = muscleStats.values
            .filter { it.recoveryState == RecoveryState.FATIGUED || it.recoveryState == RecoveryState.RECOVERING }
            .map { it.muscleGroup.displayName }
        val freshMuscles = muscleStats.values
            .filter { it.recoveryState == RecoveryState.FRESH }
            .map { it.muscleGroup.displayName }

        val recoveryContext = if (recoveringMuscles.isNotEmpty()) {
            "Recovering/Fatigued: ${recoveringMuscles.joinToString(", ")}; Fresh: ${freshMuscles.take(4).joinToString(", ")}"
        } else {
            "All muscle groups 100% Fresh & Ready"
        }

        return """
            [Athlete Profile: Name=$name, Goal="$goal", Units=$unit]
            [Recent Training: $workoutContext]
            [Muscle Recovery: $recoveryContext]

            User Question: $rawText
        """.trimIndent()
    }

    private data class ParsedAiResult(
        val cleanMarkdown: String,
        val routine: WorkoutRoutine?,
        val chips: List<String>
    )

    private fun parseAiResponse(rawReply: String): ParsedAiResult {
        var text = rawReply

        // 1. Extract Routine JSON
        val routineRegex = Regex("""```routine_json\s*([\s\S]*?)\s*```""")
        val routineMatch = routineRegex.find(text)
        val routine = if (routineMatch != null) {
            val jsonContent = routineMatch.groupValues[1].trim()
            text = text.replace(routineMatch.value, "").trim()
            try {
                routineJsonParser.decodeFromString<AiRoutinePayload>(jsonContent).toDomain()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to parse routine JSON: ${e.message}", e)
                null
            }
        } else null

        // 2. Extract Suggestions / Chips JSON
        val chipsRegex = Regex("""```chips_json\s*([\s\S]*?)\s*```""")
        val chipsMatch = chipsRegex.find(text)
        val chips = if (chipsMatch != null) {
            val jsonContent = chipsMatch.groupValues[1].trim()
            text = text.replace(chipsMatch.value, "").trim()
            try {
                routineJsonParser.decodeFromString<List<String>>(jsonContent)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to parse chips JSON: ${e.message}", e)
                emptyList()
            }
        } else emptyList()

        return ParsedAiResult(
            cleanMarkdown = text.trim(),
            routine = routine,
            chips = chips
        )
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            sentByUser = true
        )

        viewModelScope.launch {
            try {
                _isTyping.value = true
                chatRepository.insertMessage(userMessage, todayDateString)

                val enrichedPrompt = buildEnrichedPrompt(text)
                val result = geminiService.sendChatMessage(enrichedPrompt)

                result.onSuccess { reply ->
                    val parsed = parseAiResponse(reply)
                    saveBotMessageThenClearTyping(parsed.cleanMarkdown, parsed.routine, parsed.chips)
                }
                result.onFailure { err ->
                    val errorMsg = if (com.minimize.maximus.BuildConfig.DEBUG) {
                        "🛑 [DEBUG FAILURE: ${err.javaClass.simpleName}]\n\n${err.message ?: "Unknown error"}"
                    } else {
                        "I'm having a little trouble connecting right now. Please try again in a moment! 😅"
                    }
                    saveBotMessageThenClearTyping(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = if (com.minimize.maximus.BuildConfig.DEBUG) {
                    "🛑 [DEBUG EXCEPTION: ${e.javaClass.simpleName}]\n\n${e.message ?: "Unexpected glitch"}"
                } else {
                    "An unexpected glitch occurred. Let's try that request again."
                }
                saveBotMessageThenClearTyping(errorMsg)
            }
        }
    }

    fun analyzeMealImage(bitmap: Bitmap, userPrompt: String = "") {
        val displayMessage = if (userPrompt.isNotBlank()) userPrompt else "Can you analyze this?"

        viewModelScope.launch {
            try {
                val savedImagePath = withContext(Dispatchers.IO) {
                    ImageStorageHelper.saveBitmapToCache(context, bitmap)
                }

                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = displayMessage,
                    sentByUser = true,
                    imageLocalPath = savedImagePath
                )

                _isTyping.value = true
                chatRepository.insertMessage(userMessage, todayDateString)

                val enrichedPrompt = buildEnrichedPrompt(displayMessage)
                val result = geminiService.analyzeMealImage(bitmap, enrichedPrompt)

                result.onSuccess { parsedMacros ->
                    if (parsedMacros.isFood) {
                        val botMessage = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = parsedMacros.nutritionistReply.ifBlank { "Here is the breakdown of your meal:" },
                            sentByUser = false,
                            isAnalysis = true,
                            mealName = parsedMacros.mealName,
                            estimatedQuantity = parsedMacros.estimatedQuantity,
                            macros = parsedMacros
                        )
                        chatRepository.insertMessage(botMessage, todayDateString)
                        _isTyping.value = false
                    } else {
                        saveBotMessageThenClearTyping(parsedMacros.nonFoodMessage)
                    }
                }
                result.onFailure { err ->
                    val errorMsg = if (com.minimize.maximus.BuildConfig.DEBUG) {
                        "🛑 [DEBUG VISION FAILURE: ${err.javaClass.simpleName}]\n\n${err.message ?: "Vision decoding failed"}"
                    } else {
                        "Oops! I had trouble seeing that clearly. Make sure the food is well-lit and try again. 😅"
                    }
                    saveBotMessageThenClearTyping(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = if (com.minimize.maximus.BuildConfig.DEBUG) {
                    "🛑 [DEBUG VISION EXCEPTION: ${e.javaClass.simpleName}]\n\n${e.message ?: "Pipeline failure"}"
                } else {
                    "Vision extraction failed due to a system pipeline glitch."
                }
                saveBotMessageThenClearTyping(errorMsg)
            }
        }
    }

    fun saveGeneratedRoutine(messageId: String, routine: WorkoutRoutine) {
        viewModelScope.launch {
            try {
                routineRepository.saveRoutine(routine)
                chatRepository.updateRoutineSaved(messageId, true)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to save generated routine: ${e.message}", e)
            }
        }
    }

    private suspend fun saveBotMessageThenClearTyping(
        text: String,
        routinePayload: WorkoutRoutine? = null,
        suggestedReplies: List<String> = emptyList()
    ) {
        chatRepository.insertMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = text,
                sentByUser = false,
                routinePayload = routinePayload,
                suggestedReplies = suggestedReplies
            ),
            todayDateString
        )
        _isTyping.value = false
    }

    fun clearTodayChat() {
        viewModelScope.launch {
            chatRepository.clearChatsForToday(todayDateString)
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(id)
        }
    }
}