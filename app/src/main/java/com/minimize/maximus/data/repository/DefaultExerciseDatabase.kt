package com.minimize.maximus.data.repository

import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.exercise.EquipmentType
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.domain.model.routine.RoutineExercise
import com.minimize.maximus.domain.model.routine.WorkoutRoutine

object DefaultExerciseDatabase {

    val BUILT_IN_EXERCISES: List<ExerciseDefinition> = listOf(
        // ── CHEST ──
        ExerciseDefinition(1, "Barbell Bench Press", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.FRONT_DELTS), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 8, defaultWeight = 60f),
        ExerciseDefinition(2, "Incline Dumbbell Press", MuscleGroup.CHEST, listOf(MuscleGroup.FRONT_DELTS, MuscleGroup.TRICEPS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 24f),
        ExerciseDefinition(3, "Incline Barbell Bench Press", MuscleGroup.CHEST, listOf(MuscleGroup.FRONT_DELTS, MuscleGroup.TRICEPS), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 8, defaultWeight = 50f),
        ExerciseDefinition(4, "Cable Chest Fly", MuscleGroup.CHEST, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 12, defaultWeight = 12f),
        ExerciseDefinition(5, "Dips (Chest Focus)", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.FRONT_DELTS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(6, "Machine Chest Press", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 10, defaultWeight = 45f),
        ExerciseDefinition(7, "Pec Deck Fly", MuscleGroup.CHEST, emptyList(), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 12, defaultWeight = 40f),
        ExerciseDefinition(8, "Push-ups", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.FRONT_DELTS, MuscleGroup.ABS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 15),

        // ── BACK ──
        ExerciseDefinition(10, "Barbell Deadlift", MuscleGroup.GLUTES, listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK, MuscleGroup.TRAPS), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 6, defaultWeight = 100f),
        ExerciseDefinition(11, "Barbell Bent Over Row", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS, MuscleGroup.TRAPS), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 8, defaultWeight = 60f),
        ExerciseDefinition(12, "Lat Pulldown (Wide Grip)", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS), EquipmentType.CABLE, defaultSets = 3, defaultReps = 10, defaultWeight = 50f),
        ExerciseDefinition(13, "Seated Cable Row", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS, MuscleGroup.TRAPS), EquipmentType.CABLE, defaultSets = 3, defaultReps = 10, defaultWeight = 45f),
        ExerciseDefinition(14, "Pull-ups", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 8),
        ExerciseDefinition(15, "Single Arm Dumbbell Row", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 26f),
        ExerciseDefinition(16, "T-Bar Row", MuscleGroup.LATS, listOf(MuscleGroup.TRAPS, MuscleGroup.BICEPS), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 8, defaultWeight = 40f),
        ExerciseDefinition(17, "Face Pulls", MuscleGroup.TRAPS, listOf(MuscleGroup.REAR_DELTS, MuscleGroup.SIDE_DELTS), EquipmentType.CABLE, defaultSets = 3, defaultReps = 15, defaultWeight = 15f),
        ExerciseDefinition(18, "Barbell Shrugs", MuscleGroup.TRAPS, emptyList(), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 12, defaultWeight = 70f),

        // ── SHOULDERS ──
        ExerciseDefinition(20, "Overhead Barbell Press (OHP)", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 8, defaultWeight = 40f),
        ExerciseDefinition(21, "Seated Dumbbell Shoulder Press", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 20f),
        ExerciseDefinition(22, "Dumbbell Lateral Raise", MuscleGroup.FRONT_DELTS, emptyList(), EquipmentType.DUMBBELL, defaultSets = 4, defaultReps = 12, defaultWeight = 10f),
        ExerciseDefinition(23, "Cable Lateral Raise", MuscleGroup.FRONT_DELTS, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 12, defaultWeight = 7.5f),
        ExerciseDefinition(24, "Rear Delt Reverse Fly", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRAPS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 15, defaultWeight = 8f),
        ExerciseDefinition(25, "Arnold Press", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 18f),

        // ── LEGS (QUADS, HAMSTRINGS, GLUTES, CALVES) ──
        ExerciseDefinition(30, "Barbell Back Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 8, defaultWeight = 80f),
        ExerciseDefinition(31, "Leg Press", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 10, defaultWeight = 120f),
        ExerciseDefinition(32, "Romanian Deadlift (RDL)", MuscleGroup.HAMSTRINGS, listOf(MuscleGroup.GLUTES), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 8, defaultWeight = 70f),
        ExerciseDefinition(33, "Leg Extension", MuscleGroup.QUADS, emptyList(), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 12, defaultWeight = 45f),
        ExerciseDefinition(34, "Seated Leg Curl", MuscleGroup.HAMSTRINGS, emptyList(), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 12, defaultWeight = 40f),
        ExerciseDefinition(35, "Lying Leg Curl", MuscleGroup.HAMSTRINGS, emptyList(), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 10, defaultWeight = 35f),
        ExerciseDefinition(36, "Barbell Hip Thrust", MuscleGroup.GLUTES, listOf(MuscleGroup.HAMSTRINGS), EquipmentType.BARBELL, defaultSets = 4, defaultReps = 10, defaultWeight = 90f),
        ExerciseDefinition(37, "Walking Lunges", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 12, defaultWeight = 16f),
        ExerciseDefinition(38, "Standing Calf Raise", MuscleGroup.CALVES, emptyList(), EquipmentType.MACHINE, defaultSets = 4, defaultReps = 15, defaultWeight = 50f),
        ExerciseDefinition(39, "Seated Calf Raise", MuscleGroup.CALVES, emptyList(), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 15, defaultWeight = 30f),

        // ── ARMS (BICEPS, TRICEPS, FOREARMS) ──
        ExerciseDefinition(40, "Barbell Bicep Curl", MuscleGroup.BICEPS, listOf(MuscleGroup.FOREARMS), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 30f),
        ExerciseDefinition(41, "Dumbbell Hammer Curl", MuscleGroup.BICEPS, listOf(MuscleGroup.FOREARMS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 12, defaultWeight = 14f),
        ExerciseDefinition(42, "Incline Dumbbell Bicep Curl", MuscleGroup.BICEPS, emptyList(), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 12f),
        ExerciseDefinition(43, "Cable Bicep Curl", MuscleGroup.BICEPS, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 12, defaultWeight = 20f),
        ExerciseDefinition(44, "Preacher Curl", MuscleGroup.BICEPS, emptyList(), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 25f),
        ExerciseDefinition(45, "Tricep Rope Pushdown", MuscleGroup.TRICEPS, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 12, defaultWeight = 25f),
        ExerciseDefinition(46, "Skull Crushers (EZ Bar)", MuscleGroup.TRICEPS, emptyList(), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 25f),
        ExerciseDefinition(47, "Overhead Cable Tricep Extension", MuscleGroup.TRICEPS, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 12, defaultWeight = 20f),
        ExerciseDefinition(48, "Close Grip Bench Press", MuscleGroup.TRICEPS, listOf(MuscleGroup.CHEST, MuscleGroup.FRONT_DELTS), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 8, defaultWeight = 50f),
        ExerciseDefinition(49, "Wrist Curls (Forearms)", MuscleGroup.FOREARMS, emptyList(), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 15, defaultWeight = 20f),

        // ── CORE ──
        ExerciseDefinition(50, "Hanging Leg Raise", MuscleGroup.ABS, emptyList(), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 12),
        ExerciseDefinition(51, "Cable Crunch", MuscleGroup.ABS, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 15, defaultWeight = 30f),
        ExerciseDefinition(52, "Ab Rollout", MuscleGroup.ABS, emptyList(), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(53, "Plank", MuscleGroup.ABS, emptyList(), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 60),
        ExerciseDefinition(54, "Russian Twists", MuscleGroup.ABS, emptyList(), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 20),

        // ── ADDITIONAL COMPOUNDS & ISOLATIONS ──
        ExerciseDefinition(55, "Incline Dumbbell Fly", MuscleGroup.CHEST, listOf(MuscleGroup.FRONT_DELTS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 12, defaultWeight = 14f),
        ExerciseDefinition(56, "Cable Crossover", MuscleGroup.CHEST, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 12, defaultWeight = 15f),
        ExerciseDefinition(57, "Straight Arm Cable Pulldown", MuscleGroup.LATS, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 15, defaultWeight = 20f),
        ExerciseDefinition(58, "Dumbbell Pullover", MuscleGroup.LATS, listOf(MuscleGroup.CHEST), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 12, defaultWeight = 22f),
        ExerciseDefinition(59, "Meadows Row", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 30f),
        ExerciseDefinition(60, "Barbell Upright Row", MuscleGroup.SIDE_DELTS, listOf(MuscleGroup.TRAPS), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 30f),
        ExerciseDefinition(61, "Face Pull (Rope)", MuscleGroup.REAR_DELTS, listOf(MuscleGroup.TRAPS), EquipmentType.CABLE, defaultSets = 4, defaultReps = 15, defaultWeight = 17.5f),
        ExerciseDefinition(62, "Bulgarian Split Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 16f),
        ExerciseDefinition(63, "Hack Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), EquipmentType.MACHINE, defaultSets = 3, defaultReps = 10, defaultWeight = 80f),
        ExerciseDefinition(64, "Goblet Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES, MuscleGroup.ABS), EquipmentType.KETTLEBELL, defaultSets = 3, defaultReps = 12, defaultWeight = 24f),
        ExerciseDefinition(65, "Hamstring Curl (Nordic)", MuscleGroup.HAMSTRINGS, listOf(MuscleGroup.GLUTES), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 8),
        ExerciseDefinition(66, "Cable Overhead Tricep Extension", MuscleGroup.TRICEPS, emptyList(), EquipmentType.CABLE, defaultSets = 3, defaultReps = 12, defaultWeight = 22.5f),
        ExerciseDefinition(67, "Incline Dumbbell Hammer Curl", MuscleGroup.BICEPS, listOf(MuscleGroup.FOREARMS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 10, defaultWeight = 12f),
        ExerciseDefinition(68, "Reverse Barbell Curl", MuscleGroup.FOREARMS, listOf(MuscleGroup.BICEPS), EquipmentType.BARBELL, defaultSets = 3, defaultReps = 12, defaultWeight = 20f),
        ExerciseDefinition(69, "Farmer's Walk", MuscleGroup.FOREARMS, listOf(MuscleGroup.TRAPS, MuscleGroup.ABS), EquipmentType.DUMBBELL, defaultSets = 3, defaultReps = 1, defaultWeight = 32f),
        ExerciseDefinition(70, "Cable Woodchopper", MuscleGroup.OBLIQUES, listOf(MuscleGroup.ABS), EquipmentType.CABLE, defaultSets = 3, defaultReps = 15, defaultWeight = 15f),

        // ── CALISTHENICS & ADVANCED BODYWEIGHT ──
        ExerciseDefinition(71, "Chin-ups", MuscleGroup.BICEPS, listOf(MuscleGroup.LATS, MuscleGroup.FOREARMS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 8),
        ExerciseDefinition(72, "Inverted Row (Australian Pull-up)", MuscleGroup.LATS, listOf(MuscleGroup.TRAPS, MuscleGroup.BICEPS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(73, "Pike Push-ups", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(74, "Handstand Push-ups", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 5),
        ExerciseDefinition(75, "Diamond Push-ups", MuscleGroup.TRICEPS, listOf(MuscleGroup.CHEST, MuscleGroup.FRONT_DELTS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 12),
        ExerciseDefinition(76, "Muscle-up", MuscleGroup.LATS, listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 5),
        ExerciseDefinition(77, "Pistol Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES, MuscleGroup.CALVES), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 6),
        ExerciseDefinition(78, "L-Sit Hold", MuscleGroup.ABS, listOf(MuscleGroup.QUADS, MuscleGroup.FOREARMS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 20),
        ExerciseDefinition(79, "Dragon Flag", MuscleGroup.ABS, listOf(MuscleGroup.LOWER_BACK, MuscleGroup.GLUTES), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 8),

        ExerciseDefinition(80, "Planche Lean", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS, MuscleGroup.ABS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 15), // Reps = seconds
        ExerciseDefinition(81, "Tuck Planche Hold", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS, MuscleGroup.ABS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(82, "Straddle Planche", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.TRICEPS, MuscleGroup.ABS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 5),
        ExerciseDefinition(83, "Planche Push-up", MuscleGroup.FRONT_DELTS, listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 5),
        ExerciseDefinition(84, "Front Lever Hold", MuscleGroup.LATS, listOf(MuscleGroup.ABS, MuscleGroup.LOWER_BACK), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(85, "Front Lever Raises", MuscleGroup.LATS, listOf(MuscleGroup.ABS, MuscleGroup.FRONT_DELTS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 8),
        ExerciseDefinition(86, "Back Lever Hold", MuscleGroup.CHEST, listOf(MuscleGroup.FRONT_DELTS, MuscleGroup.LATS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(87, "Human Flag", MuscleGroup.OBLIQUES, listOf(MuscleGroup.LATS, MuscleGroup.SIDE_DELTS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(88, "Ring Muscle-up", MuscleGroup.LATS, listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 5),

        ExerciseDefinition(89, "Shrimp Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 8),
        ExerciseDefinition(90, "Sissy Squat", MuscleGroup.QUADS, listOf(MuscleGroup.ABS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(91, "Cossack Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(92, "Jump Squats", MuscleGroup.QUADS, listOf(MuscleGroup.CALVES, MuscleGroup.GLUTES), EquipmentType.BODYWEIGHT, defaultSets = 4, defaultReps = 15),
        ExerciseDefinition(93, "Burpees", MuscleGroup.CHEST, listOf(MuscleGroup.QUADS, MuscleGroup.ABS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 15),
        ExerciseDefinition(94, "Mountain Climbers", MuscleGroup.ABS, listOf(MuscleGroup.FRONT_DELTS, MuscleGroup.QUADS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 30),
        ExerciseDefinition(95, "Box Jumps", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES, MuscleGroup.CALVES), EquipmentType.OTHER, defaultSets = 3, defaultReps = 10),
        ExerciseDefinition(96, "Tuck Jumps", MuscleGroup.QUADS, listOf(MuscleGroup.ABS), EquipmentType.BODYWEIGHT, defaultSets = 3, defaultReps = 12),




    )

    val DEFAULT_ROUTINE_TEMPLATES: List<WorkoutRoutine> = listOf(
        WorkoutRoutine(
            name = "Push Day (Chest, Shoulders & Triceps)",
            description = "Classic hypertrophy pushing session focusing on horizontal and vertical pressing power.",
            targetMuscleGroups = listOf(MuscleGroup.CHEST.name, MuscleGroup.FRONT_DELTS.name, MuscleGroup.TRICEPS.name),
            exercises = listOf(
                RoutineExercise("Barbell Bench Press", targetSets = 4, targetReps = 8, defaultWeight = 60f, restSeconds = 90, orderIndex = 0),
                RoutineExercise("Incline Dumbbell Press", targetSets = 3, targetReps = 10, defaultWeight = 24f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Overhead Barbell Press (OHP)", targetSets = 3, targetReps = 8, defaultWeight = 40f, restSeconds = 90, orderIndex = 2),
                RoutineExercise("Dumbbell Lateral Raise", targetSets = 4, targetReps = 12, defaultWeight = 10f, restSeconds = 60, orderIndex = 3),
                RoutineExercise("Tricep Rope Pushdown", targetSets = 3, targetReps = 12, defaultWeight = 25f, restSeconds = 60, orderIndex = 4)
            ),
            estimatedDurationMinutes = 55,
            colorTagHex = "#FF5722",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Pull Day (Back & Biceps)",
            description = "Complete back width and thickness builder with direct bicep volume.",
            targetMuscleGroups = listOf(MuscleGroup.LATS.name, MuscleGroup.TRAPS.name, MuscleGroup.BICEPS.name),
            exercises = listOf(
                RoutineExercise("Barbell Deadlift", targetSets = 4, targetReps = 6, defaultWeight = 100f, restSeconds = 120, orderIndex = 0),
                RoutineExercise("Lat Pulldown (Wide Grip)", targetSets = 3, targetReps = 10, defaultWeight = 50f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Barbell Bent Over Row", targetSets = 3, targetReps = 8, defaultWeight = 60f, restSeconds = 90, orderIndex = 2),
                RoutineExercise("Face Pulls", targetSets = 3, targetReps = 15, defaultWeight = 15f, restSeconds = 60, orderIndex = 3),
                RoutineExercise("Barbell Bicep Curl", targetSets = 3, targetReps = 10, defaultWeight = 30f, restSeconds = 60, orderIndex = 4),
                RoutineExercise("Dumbbell Hammer Curl", targetSets = 3, targetReps = 12, defaultWeight = 14f, restSeconds = 60, orderIndex = 5)
            ),
            estimatedDurationMinutes = 60,
            colorTagHex = "#2196F3",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Leg Day (Quads, Hamstrings & Calves)",
            description = "High-octane lower body session targeting quad teardrops, posterior chain, and calves.",
            targetMuscleGroups = listOf(MuscleGroup.QUADS.name, MuscleGroup.HAMSTRINGS.name, MuscleGroup.CALVES.name, MuscleGroup.GLUTES.name),
            exercises = listOf(
                RoutineExercise("Barbell Back Squat", targetSets = 4, targetReps = 8, defaultWeight = 80f, restSeconds = 120, orderIndex = 0),
                RoutineExercise("Romanian Deadlift (RDL)", targetSets = 4, targetReps = 8, defaultWeight = 70f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Leg Press", targetSets = 3, targetReps = 10, defaultWeight = 120f, restSeconds = 90, orderIndex = 2),
                RoutineExercise("Leg Extension", targetSets = 3, targetReps = 12, defaultWeight = 45f, restSeconds = 60, orderIndex = 3),
                RoutineExercise("Standing Calf Raise", targetSets = 4, targetReps = 15, defaultWeight = 50f, restSeconds = 60, orderIndex = 4)
            ),
            estimatedDurationMinutes = 55,
            colorTagHex = "#4CAF50",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Upper Body Strength",
            description = "Balanced horizontal/vertical push-pull compound session for 4-day splits.",
            targetMuscleGroups = listOf(MuscleGroup.CHEST.name, MuscleGroup.LATS.name, MuscleGroup.FRONT_DELTS.name, MuscleGroup.BICEPS.name, MuscleGroup.TRICEPS.name),
            exercises = listOf(
                RoutineExercise("Barbell Bench Press", targetSets = 4, targetReps = 6, defaultWeight = 65f, restSeconds = 90, orderIndex = 0),
                RoutineExercise("Barbell Bent Over Row", targetSets = 4, targetReps = 6, defaultWeight = 65f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Overhead Barbell Press (OHP)", targetSets = 3, targetReps = 8, defaultWeight = 40f, restSeconds = 90, orderIndex = 2),
                RoutineExercise("Lat Pulldown (Wide Grip)", targetSets = 3, targetReps = 10, defaultWeight = 55f, restSeconds = 90, orderIndex = 3),
                RoutineExercise("Dips (Chest Focus)", targetSets = 3, targetReps = 10, defaultWeight = 0f, restSeconds = 60, orderIndex = 4)
            ),
            estimatedDurationMinutes = 50,
            colorTagHex = "#9C27B0",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Lower Body Strength",
            description = "Compound squatting and hip hinge progression for maximum lower body output.",
            targetMuscleGroups = listOf(MuscleGroup.QUADS.name, MuscleGroup.HAMSTRINGS.name, MuscleGroup.GLUTES.name),
            exercises = listOf(
                RoutineExercise("Barbell Back Squat", targetSets = 4, targetReps = 6, defaultWeight = 85f, restSeconds = 120, orderIndex = 0),
                RoutineExercise("Barbell Deadlift", targetSets = 3, targetReps = 5, defaultWeight = 110f, restSeconds = 120, orderIndex = 1),
                RoutineExercise("Barbell Hip Thrust", targetSets = 3, targetReps = 8, defaultWeight = 90f, restSeconds = 90, orderIndex = 2),
                RoutineExercise("Seated Leg Curl", targetSets = 3, targetReps = 12, defaultWeight = 40f, restSeconds = 60, orderIndex = 3)
            ),
            estimatedDurationMinutes = 45,
            colorTagHex = "#FF9800",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Full Body Hypertrophy",
            description = "High-efficiency compound workout hitting every major muscle group in a single session.",
            targetMuscleGroups = listOf(MuscleGroup.QUADS.name, MuscleGroup.CHEST.name, MuscleGroup.LATS.name, MuscleGroup.HAMSTRINGS.name),
            exercises = listOf(
                RoutineExercise("Barbell Back Squat", targetSets = 3, targetReps = 8, defaultWeight = 80f, restSeconds = 120, orderIndex = 0),
                RoutineExercise("Barbell Bench Press", targetSets = 3, targetReps = 8, defaultWeight = 60f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Barbell Bent Over Row", targetSets = 3, targetReps = 8, defaultWeight = 60f, restSeconds = 90, orderIndex = 2),
                RoutineExercise("Romanian Deadlift (RDL)", targetSets = 3, targetReps = 10, defaultWeight = 70f, restSeconds = 90, orderIndex = 3),
                RoutineExercise("Overhead Barbell Press (OHP)", targetSets = 3, targetReps = 8, defaultWeight = 40f, restSeconds = 90, orderIndex = 4)
            ),
            estimatedDurationMinutes = 60,
            colorTagHex = "#00BCD4",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Chest & Back Antagonist",
            description = "Classic volume push-pull superset split designed to maximize upper-torso pump and density.",
            targetMuscleGroups = listOf(MuscleGroup.CHEST.name, MuscleGroup.LATS.name),
            exercises = listOf(
                RoutineExercise("Barbell Bench Press", targetSets = 4, targetReps = 8, defaultWeight = 60f, restSeconds = 90, orderIndex = 0),
                RoutineExercise("Lat Pulldown (Wide Grip)", targetSets = 4, targetReps = 10, defaultWeight = 50f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Incline Dumbbell Press", targetSets = 3, targetReps = 10, defaultWeight = 24f, restSeconds = 90, orderIndex = 2),
                RoutineExercise("Seated Cable Row", targetSets = 3, targetReps = 10, defaultWeight = 45f, restSeconds = 90, orderIndex = 3),
                RoutineExercise("Cable Chest Fly", targetSets = 3, targetReps = 12, defaultWeight = 12f, restSeconds = 60, orderIndex = 4),
                RoutineExercise("Face Pulls", targetSets = 3, targetReps = 15, defaultWeight = 15f, restSeconds = 60, orderIndex = 5)
            ),
            estimatedDurationMinutes = 65,
            colorTagHex = "#E91E63",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Arms & Shoulders Blast",
            description = "Direct arm isolation and 3D shoulder cap volume.",
            targetMuscleGroups = listOf(MuscleGroup.FRONT_DELTS.name, MuscleGroup.BICEPS.name, MuscleGroup.TRICEPS.name),
            exercises = listOf(
                RoutineExercise("Overhead Barbell Press (OHP)", targetSets = 4, targetReps = 8, defaultWeight = 40f, restSeconds = 90, orderIndex = 0),
                RoutineExercise("Dumbbell Lateral Raise", targetSets = 4, targetReps = 12, defaultWeight = 10f, restSeconds = 60, orderIndex = 1),
                RoutineExercise("Barbell Bicep Curl", targetSets = 3, targetReps = 10, defaultWeight = 30f, restSeconds = 60, orderIndex = 2),
                RoutineExercise("Skull Crushers (EZ Bar)", targetSets = 3, targetReps = 10, defaultWeight = 25f, restSeconds = 60, orderIndex = 3),
                RoutineExercise("Dumbbell Hammer Curl", targetSets = 3, targetReps = 12, defaultWeight = 14f, restSeconds = 60, orderIndex = 4),
                RoutineExercise("Tricep Rope Pushdown", targetSets = 3, targetReps = 12, defaultWeight = 25f, restSeconds = 60, orderIndex = 5)
            ),
            estimatedDurationMinutes = 50,
            colorTagHex = "#673AB7",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Pure Calisthenics Upper Body",
            description = "Complete bodyweight upper body workout focusing on relative strength, control, and scapular stability.",
            targetMuscleGroups = listOf(MuscleGroup.CHEST.name, MuscleGroup.LATS.name, MuscleGroup.FRONT_DELTS.name, MuscleGroup.ABS.name),
            exercises = listOf(
                RoutineExercise("Pull-ups", targetSets = 4, targetReps = 8, defaultWeight = 0f, restSeconds = 90, orderIndex = 0),
                RoutineExercise("Dips (Chest Focus)", targetSets = 4, targetReps = 10, defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Inverted Row (Australian Pull-up)", targetSets = 3, targetReps = 10, defaultWeight = 0f, restSeconds = 60, orderIndex = 2),
                RoutineExercise("Pike Push-ups", targetSets = 3, targetReps = 8, defaultWeight = 0f, restSeconds = 60, orderIndex = 3),
                RoutineExercise("L-Sit Hold", targetSets = 3, targetReps = 20, defaultWeight = 0f, restSeconds = 60, orderIndex = 4)
            ),
            estimatedDurationMinutes = 45,
            colorTagHex = "#10B981",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Calisthenics Lower Body Control",
            description = "Unilateral strength and deep range-of-motion mastery for bodyweight leg development.",
            targetMuscleGroups = listOf(MuscleGroup.QUADS.name, MuscleGroup.GLUTES.name, MuscleGroup.HAMSTRINGS.name),
            exercises = listOf(
                RoutineExercise("Pistol Squat", targetSets = 3, targetReps = 6, defaultWeight = 0f, restSeconds = 90, orderIndex = 0),
                RoutineExercise("Shrimp Squat", targetSets = 3, targetReps = 8, defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                RoutineExercise("Cossack Squat", targetSets = 3, targetReps = 10, defaultWeight = 0f, restSeconds = 60, orderIndex = 2),
                RoutineExercise("Sissy Squat", targetSets = 3, targetReps = 10, defaultWeight = 0f, restSeconds = 60, orderIndex = 3),
                RoutineExercise("Hamstring Curl (Nordic)", targetSets = 3, targetReps = 5, defaultWeight = 0f, restSeconds = 90, orderIndex = 4)
            ),
            estimatedDurationMinutes = 45,
            colorTagHex = "#F59E0B",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Advanced Statics (Planche & Lever)",
            description = "Isometric strength and straight-arm conditioning. (Note: Reps represent seconds for hold durations).",
            targetMuscleGroups = listOf(MuscleGroup.FRONT_DELTS.name, MuscleGroup.LATS.name, MuscleGroup.ABS.name),
            exercises = listOf(
                RoutineExercise("Planche Lean", targetSets = 4, targetReps = 15, defaultWeight = 0f, restSeconds = 90, orderIndex = 0),
                RoutineExercise("Tuck Planche Hold", targetSets = 4, targetReps = 10, defaultWeight = 0f, restSeconds = 120, orderIndex = 1),
                RoutineExercise("Front Lever Hold", targetSets = 4, targetReps = 10, defaultWeight = 0f, restSeconds = 120, orderIndex = 2),
                RoutineExercise("Front Lever Raises", targetSets = 3, targetReps = 8, defaultWeight = 0f, restSeconds = 90, orderIndex = 3),
                RoutineExercise("Dragon Flag", targetSets = 3, targetReps = 8, defaultWeight = 0f, restSeconds = 90, orderIndex = 4)
            ),
            estimatedDurationMinutes = 50,
            colorTagHex = "#8B5CF6",
            isDefaultTemplate = true,
            isCustom = false
        ),
        WorkoutRoutine(
            name = "Bodyweight Metcon (Conditioning)",
            description = "High-intensity metabolic conditioning circuit to build cardiovascular endurance and explosive power.",
            targetMuscleGroups = listOf(MuscleGroup.QUADS.name, MuscleGroup.CHEST.name, MuscleGroup.ABS.name),
            exercises = listOf(
                RoutineExercise("Burpees", targetSets = 4, targetReps = 15, defaultWeight = 0f, restSeconds = 60, orderIndex = 0),
                RoutineExercise("Jump Squats", targetSets = 4, targetReps = 15, defaultWeight = 0f, restSeconds = 60, orderIndex = 1),
                RoutineExercise("Mountain Climbers", targetSets = 4, targetReps = 30, defaultWeight = 0f, restSeconds = 60, orderIndex = 2),
                RoutineExercise("Tuck Jumps", targetSets = 4, targetReps = 12, defaultWeight = 0f, restSeconds = 60, orderIndex = 3),
                RoutineExercise("Plank", targetSets = 3, targetReps = 60, defaultWeight = 0f, restSeconds = 60, orderIndex = 4) // Reps = seconds
            ),
            estimatedDurationMinutes = 35,
            colorTagHex = "#EF4444",
            isDefaultTemplate = true,
            isCustom = false
        )
    )
}
