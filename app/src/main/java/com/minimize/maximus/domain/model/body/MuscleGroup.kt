package com.minimize.maximus.domain.model.body

import androidx.compose.ui.graphics.Color

enum class BodyViewMode {
    FRONT,
    BACK
}

enum class RecoveryState(val label: String, val color: Color) {
    FRESH("Fresh & Ready", Color(0xFF10B981)),          // Emerald Green
    OPTIMAL("Optimal Volume", Color(0xFF3B82F6)),       // Electric Blue / Accent
    RECOVERING("Recovering", Color(0xFFF59E0B)),        // Amber
    FATIGUED("High Fatigue", Color(0xFFEF4444))         // Red
}

enum class MuscleGroup(
    val id: String,
    val displayName: String,
    val anatomicalName: String,
    val isFront: Boolean,
    val isBack: Boolean
) {
    CHEST("chest", "Chest", "Pectoralis Major", isFront = true, isBack = false),
    FRONT_DELTS("front_delts", "Front Delts", "Anterior Deltoid", isFront = true, isBack = false),
    SIDE_DELTS("side_delts", "Side Delts", "Lateral Deltoid", isFront = true, isBack = true),
    REAR_DELTS("rear_delts", "Rear Delts", "Posterior Deltoid", isFront = false, isBack = true),
    BICEPS("biceps", "Biceps", "Biceps Brachii", isFront = true, isBack = false),
    TRICEPS("triceps", "Triceps", "Triceps Brachii", isFront = false, isBack = true),
    FOREARMS("forearms", "Forearms", "Brachioradialis", isFront = true, isBack = true),
    ABS("abs", "Core / Abs", "Rectus Abdominis", isFront = true, isBack = false),
    OBLIQUES("obliques", "Obliques", "External Obliques", isFront = true, isBack = false),
    QUADS("quads", "Quads", "Quadriceps Femoris", isFront = true, isBack = false),
    CALVES("calves", "Calves", "Gastrocnemius", isFront = true, isBack = true),
    TRAPS("traps", "Traps", "Trapezius", isFront = false, isBack = true),
    LATS("lats", "Lats / Back", "Latissimus Dorsi", isFront = false, isBack = true),
    LOWER_BACK("lower_back", "Lower Back", "Erector Spinae", isFront = false, isBack = true),
    GLUTES("glutes", "Glutes", "Gluteus Maximus", isFront = false, isBack = true),
    HAMSTRINGS("hamstrings", "Hamstrings", "Biceps Femoris", isFront = false, isBack = true)
}

data class MuscleStat(
    val muscleGroup: MuscleGroup,
    val weeklySets: Int = 0,
    val weeklyVolume: Double = 0.0,
    val lastTrainedHoursAgo: Long? = null,
    val recoveryState: RecoveryState = RecoveryState.FRESH,
    val recoveryPercentage: Int = 100,
    val contributingExercises: List<String> = emptyList()
)
