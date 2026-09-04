package com.minimize.maximus.domain.model.routine

import java.time.DayOfWeek

/**
 * Split preset types available for 1-tap split configuration.
 */
enum class SplitPresetType(val title: String, val description: String) {
    PUSH_PULL_LEGS("Push / Pull / Legs", "6-Day or 3-Day Hypertrophy Split"),
    UPPER_LOWER("Upper / Lower", "4-Day Balanced Strength Split"),
    FULL_BODY("Full Body (3-Day)", "Mon / Wed / Fri Compound Training"),
    ARNOLD_SPLIT("Arnold Split", "Chest/Back, Shoulders/Arms, Legs"),
    CUSTOM("Custom Split", "Personalized Day-by-Day Schedule")
}

/**
 * Schedule assignment for a single day of the week.
 */
data class DaySchedule(
    val dayOfWeek: DayOfWeek,
    val routineId: Long? = null,
    val routineName: String? = null,
    val isRestDay: Boolean = routineId == null
)

/**
 * 7-Day Weekly Training Plan mapping Monday through Sunday.
 */
data class WeeklyPlan(
    val id: Long = 1L,
    val presetType: SplitPresetType = SplitPresetType.PUSH_PULL_LEGS,
    val days: Map<DayOfWeek, DaySchedule> = defaultPplSchedule()
) {
    companion object {
        fun defaultPplSchedule(): Map<DayOfWeek, DaySchedule> {
            return mapOf(
                DayOfWeek.MONDAY to DaySchedule(DayOfWeek.MONDAY, routineName = "Push Day (Chest/Triceps)", isRestDay = false),
                DayOfWeek.TUESDAY to DaySchedule(DayOfWeek.TUESDAY, routineName = "Pull Day (Back/Biceps)", isRestDay = false),
                DayOfWeek.WEDNESDAY to DaySchedule(DayOfWeek.WEDNESDAY, routineName = "Leg Day (Quads/Hamstrings)", isRestDay = false),
                DayOfWeek.THURSDAY to DaySchedule(DayOfWeek.THURSDAY, routineName = "Push Day B", isRestDay = false),
                DayOfWeek.FRIDAY to DaySchedule(DayOfWeek.FRIDAY, routineName = "Pull Day B", isRestDay = false),
                DayOfWeek.SATURDAY to DaySchedule(DayOfWeek.SATURDAY, routineName = "Leg Day B", isRestDay = false),
                DayOfWeek.SUNDAY to DaySchedule(DayOfWeek.SUNDAY, isRestDay = true)
            )
        }

        fun defaultUpperLowerSchedule(): Map<DayOfWeek, DaySchedule> {
            return mapOf(
                DayOfWeek.MONDAY to DaySchedule(DayOfWeek.MONDAY, routineName = "Upper Body Strength", isRestDay = false),
                DayOfWeek.TUESDAY to DaySchedule(DayOfWeek.TUESDAY, routineName = "Lower Body Strength", isRestDay = false),
                DayOfWeek.WEDNESDAY to DaySchedule(DayOfWeek.WEDNESDAY, isRestDay = true),
                DayOfWeek.THURSDAY to DaySchedule(DayOfWeek.THURSDAY, routineName = "Upper Body Hypertrophy", isRestDay = false),
                DayOfWeek.FRIDAY to DaySchedule(DayOfWeek.FRIDAY, routineName = "Lower Body Hypertrophy", isRestDay = false),
                DayOfWeek.SATURDAY to DaySchedule(DayOfWeek.SATURDAY, isRestDay = true),
                DayOfWeek.SUNDAY to DaySchedule(DayOfWeek.SUNDAY, isRestDay = true)
            )
        }

        fun defaultFullBodySchedule(): Map<DayOfWeek, DaySchedule> {
            return mapOf(
                DayOfWeek.MONDAY to DaySchedule(DayOfWeek.MONDAY, routineName = "Full Body A", isRestDay = false),
                DayOfWeek.TUESDAY to DaySchedule(DayOfWeek.TUESDAY, isRestDay = true),
                DayOfWeek.WEDNESDAY to DaySchedule(DayOfWeek.WEDNESDAY, routineName = "Full Body B", isRestDay = false),
                DayOfWeek.THURSDAY to DaySchedule(DayOfWeek.THURSDAY, isRestDay = true),
                DayOfWeek.FRIDAY to DaySchedule(DayOfWeek.FRIDAY, routineName = "Full Body C", isRestDay = false),
                DayOfWeek.SATURDAY to DaySchedule(DayOfWeek.SATURDAY, isRestDay = true),
                DayOfWeek.SUNDAY to DaySchedule(DayOfWeek.SUNDAY, isRestDay = true)
            )
        }

        fun defaultArnoldSchedule(): Map<DayOfWeek, DaySchedule> {
            return mapOf(
                DayOfWeek.MONDAY to DaySchedule(DayOfWeek.MONDAY, routineName = "Chest & Back", isRestDay = false),
                DayOfWeek.TUESDAY to DaySchedule(DayOfWeek.TUESDAY, routineName = "Shoulders & Arms", isRestDay = false),
                DayOfWeek.WEDNESDAY to DaySchedule(DayOfWeek.WEDNESDAY, routineName = "Legs & Core", isRestDay = false),
                DayOfWeek.THURSDAY to DaySchedule(DayOfWeek.THURSDAY, routineName = "Chest & Back B", isRestDay = false),
                DayOfWeek.FRIDAY to DaySchedule(DayOfWeek.FRIDAY, routineName = "Shoulders & Arms B", isRestDay = false),
                DayOfWeek.SATURDAY to DaySchedule(DayOfWeek.SATURDAY, routineName = "Legs & Core B", isRestDay = false),
                DayOfWeek.SUNDAY to DaySchedule(DayOfWeek.SUNDAY, isRestDay = true)
            )
        }
    }
}
