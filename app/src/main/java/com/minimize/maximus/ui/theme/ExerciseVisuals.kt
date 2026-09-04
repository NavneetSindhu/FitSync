package com.minimize.maximus.ui.theme

import androidx.compose.ui.graphics.Color
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.body.MuscleGroup

/**
 * Metadata for exercise visuals using PNG resources from Flaticon.
 */
data class ExerciseMeta(
    val iconRes: Int,
    val accentColor: Color
)

object ExerciseVisuals {
    operator fun get(name: String): ExerciseMeta = getMetaData(name)

    // ── Tier 1: Canonical Name Map (O(1) Exact Match) ──────────────────────────
    private val CANONICAL_MAP: Map<String, Int> = mapOf(
        // Chest
        "barbell bench press" to R.drawable.ic_chest_bench,
        "incline dumbbell press" to R.drawable.ic_chest_incline_press,
        "incline barbell bench press" to R.drawable.ic_chest_incline_press,
        "cable chest fly" to R.drawable.ic_chest,
        "incline dumbbell fly" to R.drawable.ic_chest,
        "cable crossover" to R.drawable.ic_chest,
        "dips (chest focus)" to R.drawable.ic_chest,
        "machine chest press" to R.drawable.ic_chest_bench,
        "pec deck fly" to R.drawable.ic_chest,
        "push-ups" to R.drawable.ic_chest_pushup,
        "pushups" to R.drawable.ic_chest_pushup,
        "diamond push-ups" to R.drawable.ic_chest_pushup,

        // Back & Lats
        "barbell deadlift" to R.drawable.ic_back_deadlift,
        "romanian deadlift (rdl)" to R.drawable.ic_back_deadlift,
        "barbell bent over row" to R.drawable.ic_back_rowing,
        "lat pulldown (wide grip)" to R.drawable.ic_back_lats_pulldown,
        "seated cable row" to R.drawable.ic_back_rowing,
        "pull-ups" to R.drawable.ic_back_pullup,
        "single arm dumbbell row" to R.drawable.ic_back_rowing,
        "t-bar row" to R.drawable.ic_back_rowing,
        "meadows row" to R.drawable.ic_back_rowing,
        "face pulls" to R.drawable.ic_back_rowing,
        "face pull (rope)" to R.drawable.ic_back_rowing,
        "barbell shrugs" to R.drawable.ic_back_rowing,
        "straight arm cable pulldown" to R.drawable.ic_back_lats_pulldown,
        "dumbbell pullover" to R.drawable.ic_back_lats_pulldown,
        "inverted row (australian pull-up)" to R.drawable.ic_back_rowing,

        // Shoulders
        "overhead barbell press (ohp)" to R.drawable.ic_shoulder_press,
        "seated dumbbell shoulder press" to R.drawable.ic_shoulder_press,
        "dumbbell lateral raise" to R.drawable.ic_shoulder_press,
        "cable lateral raise" to R.drawable.ic_shoulder_press,
        "rear delt reverse fly" to R.drawable.ic_shoulder_press,
        "arnold press" to R.drawable.ic_shoulder_press,
        "barbell upright row" to R.drawable.ic_shoulder_press,

        // Legs
        "barbell back squat" to R.drawable.ic_leg_squat,
        "leg press" to R.drawable.ic_leg_squat,
        "hack squat" to R.drawable.ic_leg_squat,
        "goblet squat" to R.drawable.ic_leg_squat,
        "pistol squat" to R.drawable.ic_leg_squat,
        "shrimp squat" to R.drawable.ic_leg_squat,
        "sissy squat" to R.drawable.ic_leg_squat,
        "cossack squat" to R.drawable.ic_leg_squat,
        "jump squats" to R.drawable.ic_leg_squat,
        "walking lunges" to R.drawable.ic_leg_lunges,
        "bulgarian split squat" to R.drawable.ic_leg_lunges,
        "leg extension" to R.drawable.ic_leg_lunges,
        "seated leg curl" to R.drawable.ic_leg_lunges,
        "lying leg curl" to R.drawable.ic_leg_lunges,
        "hamstring curl (nordic)" to R.drawable.ic_leg_lunges,
        "barbell hip thrust" to R.drawable.ic_leg_lunges,
        "standing calf raise" to R.drawable.ic_leg_calves,
        "seated calf raise" to R.drawable.ic_leg_calves,

        // Arms - Biceps & Forearms
        "barbell bicep curl" to R.drawable.ic_arm_bicep,
        "dumbbell hammer curl" to R.drawable.ic_arm_bicep,
        "incline dumbbell bicep curl" to R.drawable.ic_arm_bicep,
        "incline dumbbell hammer curl" to R.drawable.ic_arm_bicep,
        "cable bicep curl" to R.drawable.ic_arm_bicep,
        "preacher curl" to R.drawable.ic_arm_bicep,
        "reverse barbell curl" to R.drawable.ic_arm_bicep,
        "wrist curls (forearms)" to R.drawable.ic_arm_bicep,
        "farmer's walk" to R.drawable.ic_kettlebell,

        // Arms - Triceps
        "tricep rope pushdown" to R.drawable.ic_shoulder_dips,
        "skull crushers (ez bar)" to R.drawable.ic_shoulder_dips,
        "overhead cable tricep extension" to R.drawable.ic_shoulder_dips,
        "cable overhead tricep extension" to R.drawable.ic_shoulder_dips,
        "close grip bench press" to R.drawable.ic_shoulder_dips,

        // Calisthenics & Advanced Bodyweight
        "chin-ups" to R.drawable.ic_back_pullup,
        "muscle-up" to R.drawable.ic_back_pullup,
        "ring muscle-up" to R.drawable.ic_back_pullup,
        "pike push-ups" to R.drawable.ic_cali_handstand,
        "handstand push-ups" to R.drawable.ic_cali_handstand,
        "l-sit hold" to R.drawable.ic_cali_human_flag,
        "dragon flag" to R.drawable.ic_cali_human_flag,
        "planche lean" to R.drawable.ic_cali_human_flag,
        "tuck planche hold" to R.drawable.ic_cali_human_flag,
        "straddle planche" to R.drawable.ic_cali_human_flag,
        "planche push-up" to R.drawable.ic_cali_human_flag,
        "front lever hold" to R.drawable.ic_cali_human_flag,
        "front lever raises" to R.drawable.ic_cali_human_flag,
        "back lever hold" to R.drawable.ic_cali_human_flag,
        "human flag" to R.drawable.ic_cali_human_flag,

        // Core & Cardio
        "hanging leg raise" to R.drawable.ic_core_abs,
        "cable crunch" to R.drawable.ic_core_abs,
        "ab rollout" to R.drawable.ic_core_abs,
        "plank" to R.drawable.ic_core_abs,
        "russian twists" to R.drawable.ic_core_abs,
        "cable woodchopper" to R.drawable.ic_core_abs,
        "burpees" to R.drawable.ic_cardio_running,
        "mountain climbers" to R.drawable.ic_core_abs,
        "box jumps" to R.drawable.ic_cardio_running,
        "tuck jumps" to R.drawable.ic_cardio_running
    )

    fun getMetaData(name: String, muscleGroup: MuscleGroup? = null): ExerciseMeta {
        val normalized = name.trim().lowercase()

        // 1. Direct O(1) Canonical Exact Match Check
        CANONICAL_MAP[normalized]?.let { iconRes ->
            return ExerciseMeta(iconRes = iconRes, accentColor = AccentRed)
        }

        // 2. Tokenized & Compound Pattern Matching for Custom / Varied Names
        val words = normalized.split(Regex("\\W+")).filter { it.isNotBlank() }
        fun has(vararg terms: String) = terms.any { normalized.contains(it) }
        fun hasWord(vararg terms: String) = terms.any { words.contains(it) }

        val icon = when {
            // Handstand & Inversion Skills (Must be checked before generic pushups)
            has("handstand", "hspu", "pike push", "wall walk", "hand stand") ->
                R.drawable.ic_cali_handstand

            // Flag, Levers & Advanced Statics
            has("flag", "human flag", "dragon flag", "front lever", "back lever", "planche", "maltese", "iron cross", "l-sit", "v-sit") ->
                R.drawable.ic_cali_human_flag

            // Boxing, Combat & Heavy Bag (Checked before generic box/cardio)
            has("boxing", "shadow box", "heavy bag", "punch", "sparring", "kickbox", "mma", "muay thai") || (hasWord("box") && !has("jump", "squat", "step")) ->
                R.drawable.ic_combat_boxing

            // Swimming & Water Aerobics
            has("swim", "swimming", "freestyle", "breaststroke", "pool cardio") ->
                R.drawable.ic_cardio_swimming

            // Jump Rope & Skipping
            has("jump rope", "skipping", "skip rope", "double under", "double-under", "speed rope") ->
                R.drawable.ic_cardio_jump_rope

            // Cycling & Bikes
            has("cycle", "cycling", "bike", "spin", "biking", "assault bike", "air bike", "echo bike") ->
                R.drawable.ic_cardio_cycling

            // Rowing Machine & Erg
            has("rower", "rowing machine", "concept2", "erg", "ergometer", "ski erg") ->
                R.drawable.ic_cardio_rower

            // Stair Climber & Step Mill
            has("stair", "stairs", "stairmaster", "step mill", "stepmill", "stair climber") ->
                R.drawable.ic_cardio_stairs

            // Kettlebell & Functional Weighted Carries
            has("kettlebell", "turkish get", "snatch", "clean and jerk", "clean & jerk", "farmer", "suitcase carry", "zercher carry") || hasWord("kb") ->
                R.drawable.ic_kettlebell

            // Mobility, Yoga, Stretching & Recovery
            has("meditation", "yoga", "stretch", "stretching", "mobility", "warmup", "warm up", "cooldown", "cool down", "breath", "pose", "foam roll") ->
                R.drawable.ic_meditation

            // Deadlift Variations & Posterior Hinge
            has("deadlift", "rdl", "romanian dead", "stiff leg", "sumo dead", "rack pull", "good morning") ->
                R.drawable.ic_back_deadlift

            // Calves (Checked before generic lower body)
            has("calf", "calves", "soleus", "gastrocnemius", "tibialis") ->
                R.drawable.ic_leg_calves

            // Leg Curls (Must be checked before generic Bicep "curl" check!)
            has("curl") && has("leg", "hamstring", "nordic", "seated leg", "lying leg") ->
                R.drawable.ic_leg_lunges

            // Arm Curls & Biceps (Incline curl must NOT match incline press)
            has("curl", "bicep", "hammer", "preacher", "wrist", "forearm", "reverse curl", "grip") ->
                R.drawable.ic_arm_bicep

            // Core & Abs
            has("plank", "crunch", "ab", "abs", "core", "twist", "woodchopper", "rollout", "leg raise", "knee raise", "sit-up", "sit up", "situp", "hollow body", "dead bug", "v-up") ->
                R.drawable.ic_core_abs

            // Tricep Specific Isolations & Dips
            has("tricep", "skull crusher", "skullcrusher", "pushdown", "kickback", "close grip") || (has("dip") && !has("chest")) ->
                R.drawable.ic_shoulder_dips

            // Incline Chest Pressing
            has("incline") && (has("press", "bench", "chest", "dumbbell", "barbell") || muscleGroup == MuscleGroup.CHEST) ->
                R.drawable.ic_chest_incline_press

            // Chest Pushups
            has("pushup", "push up", "push-up", "diamond push") ->
                R.drawable.ic_chest_pushup

            // Chest Flies & General Chest
            has("fly", "pec", "crossover", "cable chest") || (has("chest") && has("dip")) ->
                R.drawable.ic_chest

            // Flat & Decline Chest Press
            has("bench", "chest press", "decline") || (has("chest") && has("press")) ->
                R.drawable.ic_chest_bench

            // Shoulders & Delts (Upright row checked here instead of back row)
            has("upright row", "shoulder", "overhead", "ohp", "military", "lateral raise", "front raise", "rear delt", "arnold") || hasWord("delt", "delts") ->
                R.drawable.ic_shoulder_press

            // Back Pulldowns & Pullovers
            has("pulldown", "lat pull", "pullover") ->
                R.drawable.ic_back_lats_pulldown

            // Pull-ups & Chin-ups
            has("pullup", "pull-up", "pull up", "chin-up", "chin up", "chinup", "muscle-up", "muscle up") || hasWord("lat", "lats") ->
                R.drawable.ic_back_pullup

            // Back Rows & Shrugs
            has("row", "rowing", "t-bar", "shrug", "face pull") ->
                R.drawable.ic_back_rowing

            // Lower Body - Squats & Heavy Compound Pressing
            has("squat", "leg press", "hack", "thruster") ->
                R.drawable.ic_leg_squat

            // Lower Body - Lunges, Extensions, Hip Thrust
            has("lunge", "split squat", "extension", "step up", "quad", "hip thrust", "glute") ->
                R.drawable.ic_leg_lunges

            // Treadmill & Walking (Exclude farmer's walk which was handled in kettlebell/carries)
            has("treadmill") || (has("walk", "walking") && !has("farmer", "wall")) ->
                R.drawable.ic_cardio_treadmill

            // Running & General Cardio Sprints
            has("run", "running", "sprint", "cardio", "burpee", "climber", "hiit") ->
                R.drawable.ic_cardio_running

            // 3. Categorical Fallback by explicit Muscle Group if provided
            else -> when (muscleGroup) {
                MuscleGroup.CHEST -> R.drawable.ic_chest
                MuscleGroup.LATS -> R.drawable.ic_back_lats_pulldown
                MuscleGroup.TRAPS -> R.drawable.ic_back_rowing
                MuscleGroup.LOWER_BACK -> R.drawable.ic_back_deadlift
                MuscleGroup.FRONT_DELTS, MuscleGroup.SIDE_DELTS, MuscleGroup.REAR_DELTS -> R.drawable.ic_shoulder_press
                MuscleGroup.QUADS -> R.drawable.ic_leg_squat
                MuscleGroup.CALVES -> R.drawable.ic_leg_calves
                MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES -> R.drawable.ic_leg_lunges
                MuscleGroup.BICEPS, MuscleGroup.FOREARMS -> R.drawable.ic_arm_bicep
                MuscleGroup.TRICEPS -> R.drawable.ic_shoulder_dips
                MuscleGroup.ABS, MuscleGroup.OBLIQUES -> R.drawable.ic_core_abs
                else -> R.drawable.ic_back_pullup
            }
        }

        return ExerciseMeta(iconRes = icon, accentColor = AccentRed)
    }
}