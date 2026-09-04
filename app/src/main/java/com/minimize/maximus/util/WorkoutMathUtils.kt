package com.minimize.maximus.util

import java.time.LocalDate

data class PlatePair(
    val weight: Float,
    val countPerSide: Int
)

data class PlateCalculationResult(
    val targetWeight: Float,
    val barWeight: Float,
    val platesPerSide: List<PlatePair>,
    val loadedWeightPerSide: Float,
    val totalWeightLoaded: Float,
    val remainderWeight: Float
)

object WorkoutMathUtils {

    /**
     * Epley 1RM Formula: 1RM = Weight * (1 + Reps / 30)
     */
    fun calculate1RMEpley(weight: Float, reps: Int): Float {
        if (weight <= 0f) return 0f
        if (reps <= 1) return weight
        return weight * (1f + reps / 30f)
    }

    /**
     * Calculates estimated 1 Rep Max as an integer.
     */
    fun calculateOneRepMax(weight: Float, reps: Int): Int {
        return calculate1RMEpley(weight, reps).toInt()
    }

    /**
     * Brzycki 1RM Formula: 1RM = Weight * (36 / (37 - Reps))
     */
    fun calculate1RMBrzycki(weight: Float, reps: Int): Float {
        if (weight <= 0f) return 0f
        if (reps <= 1) return weight
        if (reps >= 37) return weight * 2f
        return weight * (36f / (37f - reps))
    }

    /**
     * Calculates the exact plate combination needed per sleeve on a standard barbell.
     */
    fun calculateBarbellPlates(
        targetTotalWeight: Float,
        barWeight: Float = 20f,
        isMetric: Boolean = true
    ): PlateCalculationResult {
        val standardPlates = if (isMetric) {
            listOf(25f, 20f, 15f, 10f, 5f, 2.5f, 1.25f)
        } else {
            listOf(45f, 35f, 25f, 10f, 5f, 2.5f)
        }

        if (targetTotalWeight <= barWeight) {
            return PlateCalculationResult(
                targetWeight = targetTotalWeight,
                barWeight = barWeight,
                platesPerSide = emptyList(),
                loadedWeightPerSide = 0f,
                totalWeightLoaded = barWeight,
                remainderWeight = 0f
            )
        }

        var weightNeededPerSide = (targetTotalWeight - barWeight) / 2f
        val resultPlates = mutableListOf<PlatePair>()
        var loadedPerSide = 0f

        for (plate in standardPlates) {
            val count = (weightNeededPerSide / plate).toInt()
            if (count > 0) {
                resultPlates.add(PlatePair(plate, count))
                val totalPlateWeight = count * plate
                loadedPerSide += totalPlateWeight
                weightNeededPerSide -= totalPlateWeight
            }
        }

        val totalLoaded = barWeight + (loadedPerSide * 2f)
        val remainder = targetTotalWeight - totalLoaded

        return PlateCalculationResult(
            targetWeight = targetTotalWeight,
            barWeight = barWeight,
            platesPerSide = resultPlates,
            loadedWeightPerSide = loadedPerSide,
            totalWeightLoaded = totalLoaded,
            remainderWeight = remainder
        )
    }

    /**
     * Computes consecutive workout days from a set of workout LocalDates with streak freeze protection.
     * Enforces a maximum of 2 continuous/consecutive freeze days before resetting the streak.
     */
    fun calculateConsecutiveStreak(
        workoutDates: Set<LocalDate>,
        frozenDates: Set<LocalDate> = emptySet(),
        today: LocalDate = LocalDate.now()
    ): Int {
        if (workoutDates.isEmpty() && frozenDates.isEmpty()) return 0

        var streak = 0
        var checkDate = today
        var consecutiveFrozenCount = 0

        if (!workoutDates.contains(today) && !frozenDates.contains(today)) {
            checkDate = today.minusDays(1)
        }

        while (true) {
            if (workoutDates.contains(checkDate)) {
                streak++
                consecutiveFrozenCount = 0
            } else if (frozenDates.contains(checkDate)) {
                consecutiveFrozenCount++
                if (consecutiveFrozenCount > 2) {
                    // Maximum 2 continuous freezes reached; streak is broken
                    break
                }
            } else {
                // Gap without workout or freeze; streak broken
                break
            }
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
