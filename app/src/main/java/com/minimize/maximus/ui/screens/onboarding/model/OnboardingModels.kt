package com.minimize.maximus.ui.screens.onboarding.model

import androidx.annotation.StringRes
import com.minimize.maximus.R

data class GoalOption(
    val goalKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val emoji: String
)

val OnboardingGoals = listOf(
    GoalOption(
        goalKey = "Build Muscle",
        titleRes = R.string.onboarding_goal_muscle_title,
        descRes = R.string.onboarding_goal_muscle_desc,
        emoji = "🏋️‍♂️"
    ),
    GoalOption(
        goalKey = "Increase Strength",
        titleRes = R.string.onboarding_goal_strength_title,
        descRes = R.string.onboarding_goal_strength_desc,
        emoji = "💥"
    ),
    GoalOption(
        goalKey = "Calisthenics Skills",
        titleRes = R.string.onboarding_goal_calisthenics_title,
        descRes = R.string.onboarding_goal_calisthenics_desc,
        emoji = "🤸‍♂️"
    ),
    GoalOption(
        goalKey = "Fat Loss & Tone",
        titleRes = R.string.onboarding_goal_fat_loss_title,
        descRes = R.string.onboarding_goal_fat_loss_desc,
        emoji = "🏃‍♂️"
    ),
    GoalOption(
        goalKey = "Health & Longevity",
        titleRes = R.string.onboarding_goal_health_title,
        descRes = R.string.onboarding_goal_health_desc,
        emoji = "🛡️"
    )
)
