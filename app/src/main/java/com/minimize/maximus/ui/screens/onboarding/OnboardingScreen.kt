package com.minimize.maximus.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.minimize.maximus.ui.screens.onboarding.components.*
import com.minimize.maximus.ui.screens.onboarding.model.OnboardingGoals
import com.minimize.maximus.ui.screens.settings.AccentOptions
import com.minimize.maximus.ui.screens.settings.SettingsViewModel
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

@Composable
fun OnboardingScreen(
    settingsViewModel: SettingsViewModel,
    onOnboardingFinished: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    // Onboarding Form State
    var userName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Athlete") }
    var age by remember { mutableIntStateOf(23) }
    var weightInput by remember { mutableStateOf("75") }
    var isMetric by remember { mutableStateOf(true) }
    var heightInput by remember { mutableStateOf("175") }
    var isHeightMetric by remember { mutableStateOf(false) } // defaults to FT/IN
    var selectedGoalKey by remember { mutableStateOf(OnboardingGoals[0].goalKey) }
    val isDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var selectedAccent by remember { mutableStateOf(if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF18181B)) }

    val activeAccentColor = selectedAccent

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                accentColor = activeAccentColor,
                onBackClick = {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                    currentStep--
                }
            )
        },
        bottomBar = {
            OnboardingBottomBar(
                isFinalStep = currentStep == totalSteps - 1,
                accentColor = activeAccentColor,
                onContinueClick = {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP, context = context)
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        val finalWeight = weightInput.toFloatOrNull() ?: 75f
                        val finalHeight = heightInput.toFloatOrNull() ?: 175f
                        val finalName = userName.ifBlank { "Athlete" }

                        settingsViewModel.completeOnboarding(
                            name = finalName,
                            age = age,
                            weight = finalWeight,
                            height = finalHeight,
                            isHeightMetric = isHeightMetric,
                            gender = selectedGender,
                            goal = selectedGoalKey,
                            isMetric = isMetric,
                            accentColor = selectedAccent.toArgb()
                        )
                        com.minimize.maximus.util.notification.ReminderSchedulerUtil.scheduleDailyReminder(context)
                        onOnboardingFinished()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                } else {
                    slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "OnboardingStepTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.height(12.dp))

                when (step) {
                    0 -> StepAthleteIdentity(
                        userName = userName,
                        onNameChange = { userName = it },
                        selectedGender = selectedGender,
                        onGenderChange = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            selectedGender = it
                        },
                        accentColor = activeAccentColor
                    )
                    1 -> StepBodyMetrics(
                        age = age,
                        onAgeChange = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            age = it
                        },
                        weightInput = weightInput,
                        onWeightChange = { weightInput = it },
                        isMetric = isMetric,
                        onMetricToggle = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            isMetric = it
                        },
                        heightInput = heightInput,
                        onHeightChange = { heightInput = it },
                        isHeightMetric = isHeightMetric,
                        onHeightMetricToggle = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            isHeightMetric = it
                        },
                        accentColor = activeAccentColor
                    )
                    2 -> StepFitnessGoal(
                        selectedGoalKey = selectedGoalKey,
                        onGoalSelected = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            selectedGoalKey = it
                        },
                        accentColor = activeAccentColor
                    )
                    3 -> StepEnergyTheme(
                        userName = userName.ifBlank { "Athlete" },
                        selectedGoalKey = selectedGoalKey,
                        selectedAccent = selectedAccent,
                        onAccentSelected = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            selectedAccent = it
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
