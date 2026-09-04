package com.minimize.maximus.util

object UnitConverter {
    private const val KG_TO_LBS_RATIO = 2.20462f
    private const val CM_TO_INCH_RATIO = 0.393701f

    fun kgToLbs(kg: Float): Float = kg * KG_TO_LBS_RATIO
    fun lbsToKg(lbs: Float): Float = lbs / KG_TO_LBS_RATIO

    fun cmToInches(cm: Float): Float = cm * CM_TO_INCH_RATIO
    fun inchesToCm(inches: Float): Float = inches / CM_TO_INCH_RATIO

    fun cmToFtIn(cm: Float): Pair<Int, Int> {
        val totalInches = (cm * CM_TO_INCH_RATIO).toInt()
        val feet = totalInches / 12
        val inches = totalInches % 12
        return Pair(feet, inches)
    }

    fun ftInToCm(feet: Int, inches: Int): Float {
        val totalInches = (feet * 12) + inches
        return totalInches / CM_TO_INCH_RATIO
    }

    fun formatWeight(weight: Float, isMetric: Boolean): String {
        val unit = if (isMetric) "kg" else "lbs"
        val displayWeight = if (isMetric) weight else kgToLbs(weight)
        return if (displayWeight % 1f == 0f) {
            "${displayWeight.toInt()} $unit"
        } else {
            "%.1f %s".format(displayWeight, unit)
        }
    }
}
