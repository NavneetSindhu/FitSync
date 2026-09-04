package com.minimize.maximus.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minimize.maximus.R
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.components.ToastType
import com.minimize.maximus.ui.screens.profile.components.FitnessGoalBottomSheet
import com.minimize.maximus.ui.screens.profile.components.InlineAvatarSelector
import com.minimize.maximus.ui.theme.LocalCompactCards
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    onBodyAnalyticsClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastManager = LocalMaximusToast.current
    val hapticFeedback = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    var showGoalSheet by remember { mutableStateOf(false) }
    var isEditingAvatar by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleNotifications(true)
            toastManager.showToast("Daily workout reminders activated!", ToastType.SUCCESS)
        } else {
            viewModel.toggleNotifications(false)
            toastManager.showToast("Notification permission denied in system settings.", ToastType.INFO)
        }
    }

    if (showReminderTimePicker) {
        com.minimize.maximus.ui.components.MaximusTimePickerDialog(
            initialHour = uiState.reminderHour,
            initialMinute = uiState.reminderMinute,
            onTimeSelected = { hour, minute ->
                viewModel.setReminderTime(hour, minute)
                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                val formatted = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
                toastManager.showToast("Reminder set for $formatted", ToastType.SUCCESS)
            },
            onDismiss = { showReminderTimePicker = false }
        )
    }

    if (uiState.isEditingPersonalDetails) {
        PersonalDetailsDialog(
            uiState = uiState,
            onDismiss = viewModel::closePersonalDetailsEditor,
            onSave = { age, weight, height, isHeightMetric, gender, goal ->
                MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.FILTER_SWITCH)
                viewModel.savePersonalDetails(age, weight, height, isHeightMetric, gender, goal)
                toastManager.showToast("Athlete profile updated!", ToastType.SUCCESS)
            }
        )
    }

    if (showGoalSheet) {
        FitnessGoalBottomSheet(
            currentGoal = uiState.fitnessGoal,
            onGoalSelected = { selectedGoal ->
                MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                viewModel.savePersonalDetails(
                    uiState.age,
                    uiState.weightKg,
                    uiState.heightCm,
                    uiState.isHeightMetric,
                    uiState.gender,
                    selectedGoal
                )
                showGoalSheet = false
                toastManager.showToast("Goal updated to \"$selectedGoal\"!", ToastType.SUCCESS)
            },
            onDismissRequest = { showGoalSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = MaximusIcons.Home.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = MaximusDimens.FloatingNavHeight + 16.dp)
        ) {
            // ── 1. Centered Profile Header with Inline Avatar Selector ─────────
            item {
                ProfileHeader(
                    uiState = uiState,
                    isEditingAvatar = isEditingAvatar,
                    onEditAvatarChange = { isEditingAvatar = it },
                    onAvatarSaved = { newAvatar ->
                        viewModel.setAvatar(newAvatar)
                        toastManager.showToast("Avatar updated!", ToastType.SUCCESS)
                    }
                )
            }

            // ── 2. Body metrics strip ──────────────────────────────────────────
            item { BodyMetricsStrip(uiState = uiState) }

            // ── 3. Quick stats ─────────────────────────────────────────────────
            item {
                val forceShimmer = com.minimize.maximus.ui.theme.LocalDebugShimmer.current
                if (uiState.isLoading || forceShimmer) {
                    com.minimize.maximus.ui.components.ProfileStatsSkeletonView()
                } else {
                    QuickStatsGrid(uiState = uiState)
                }
            }

            // ── 4. Achievements ────────────────────────────────────────────────
            item {
                val forceShimmer = com.minimize.maximus.ui.theme.LocalDebugShimmer.current
                if (uiState.isLoading || forceShimmer) {
                    com.minimize.maximus.ui.components.ProfileAchievementsSkeletonView()
                } else {
                    AchievementsSection(achievements = uiState.achievements)
                }
            }

            // ── 5. Account / preferences menu ──────────────────────────────────
            item {
                SettingsSection(
                    uiState = uiState,
                    onEditPersonalDetails = {
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                        viewModel.openPersonalDetailsEditor()
                    },
                    onEditGoal = {
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                        showGoalSheet = true
                    },
                    onToggleNotifications = { enabled ->
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                        if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (!hasPerm) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                return@SettingsSection
                            }
                        }
                        viewModel.toggleNotifications(enabled)
                        toastManager.showToast(
                            if (enabled) "Daily reminders enabled" else "Daily reminders disabled",
                            ToastType.INFO
                        )
                    },
                    onPickReminderTime = {
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.ACTION_TAP)
                        showReminderTimePicker = true
                    },
                    onToggleRestTimer = { enabled ->
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                        viewModel.toggleRestTimer(enabled)
                    },
                    onWeightUnitChange = { unit ->
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.FILTER_SWITCH)
                        viewModel.setWeightUnit(unit)
                        toastManager.showToast("Weight unit changed to ${unit.uppercase()}", ToastType.SUCCESS)
                    }
                )
            }

            // ── 6. Rate Us & Other Apps (1 Row, 2 Cards 50% width each) ─────────
            item {
                ProfileAppLinksRow(
                    onRateUsClick = {
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.ACTION_TAP)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                toastManager.showToast("Could not open Play Store", ToastType.ERROR)
                            }
                        }
                    },
                    onOtherAppsClick = {
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.ACTION_TAP)
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/developer?id=Minimize++Studios")
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            toastManager.showToast("Could not open Play Store", ToastType.ERROR)
                        }
                    }
                )
            }
        }
    }
}

// ── Centered Uniswap Profile Header ───────────────────────────────────────────

@Composable
fun ProfileHeader(
    uiState: ProfileUiState,
    isEditingAvatar: Boolean,
    onEditAvatarChange: (Boolean) -> Unit,
    onAvatarSaved: (String) -> Unit
) {
    val currentAccent = LocalAccentColor.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InlineAvatarSelector(
            currentAvatarId = uiState.userAvatar,
            isEditing = isEditingAvatar,
            onEditChange = onEditAvatarChange,
            onAvatarSaved = onAvatarSaved
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = uiState.userName.ifBlank { "Athlete" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = currentAccent.copy(alpha = 0.14f)
        ) {
            Text(
                text = uiState.fitnessGoal,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = currentAccent,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

// ── Body Metrics Strip ─────────────────────────────────────────────────────────

@Composable
fun BodyMetricsStrip(uiState: ProfileUiState) {
    val isCompact = LocalCompactCards.current

    val bmi = if (uiState.heightCm > 0f) {
        uiState.weightKg / ((uiState.heightCm / 100f) * (uiState.heightCm / 100f))
    } else 0f

    val bmiLabel = when {
        bmi <= 0f   -> "—"
        bmi < 18.5f -> "Underweight"
        bmi < 25f   -> "Normal"
        bmi < 30f   -> "Overweight"
        else        -> "Obese"
    }
    val bmiColor = when {
        bmi <= 0f   -> MaterialTheme.colorScheme.onSurfaceVariant
        bmi < 18.5f -> Color(0xFF1E88E5)
        bmi < 25f   -> Color(0xFF4CAF50)
        bmi < 30f   -> Color(0xFFFFA726)
        else        -> MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isCompact) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Age",
                value = "${uiState.age}",
                unit = "yrs"
            )
            VerticalDivider()
            MetricItem(
                label = "Weight",
                value = if (uiState.weightUnit == "kg")
                    "%.1f".format(uiState.weightKg)
                else
                    "%.1f".format(uiState.weightKg * 2.205f),
                unit = uiState.weightUnit
            )
            VerticalDivider()
            val isHeightMetric = uiState.isHeightMetric
            val (ft, inches) = remember(uiState.heightCm) { com.minimize.maximus.util.UnitConverter.cmToFtIn(uiState.heightCm) }
            MetricItem(
                label = "Height",
                value = if (isHeightMetric) "%.0f".format(uiState.heightCm) else "${ft}'${inches}\"",
                unit = if (isHeightMetric) "cm" else "ft/in"
            )
            VerticalDivider()
            MetricItem(
                label = "BMI",
                value = if (bmi > 0f) "%.1f".format(bmi) else "—",
                unit = bmiLabel,
                unitColor = bmiColor
            )
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    unit: String,
    unitColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = unitColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

// ── Quick Stats Grid ───────────────────────────────────────────────────────────

@Composable
fun QuickStatsGrid(uiState: ProfileUiState) {
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current
    val spacing = if (isCompact) 8.dp else 12.dp

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        Text(
            text = "Your Stats",
            style = MaterialTheme.typography.titleSmall,
            color = currentAccent,
            modifier = Modifier.padding(start = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Workouts",
                value = "${uiState.totalWorkouts}",
                icon = MaximusIcons.Profile.WorkoutsCount
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Streak",
                value = "${uiState.currentStreak}d",
                icon = MaximusIcons.Profile.Streak
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Working Sets",
                value = "${uiState.totalWorkingSets}",
                icon = MaximusIcons.Profile.WorkingSetsCount
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Avg Session",
                value = "${uiState.avgSessionMin}m",
                icon = MaximusIcons.Profile.AvgSession
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = currentAccent.copy(alpha = 0.14f),
                modifier = Modifier.size(if (isCompact) 38.dp else 44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(if (isCompact) 20.dp else 22.dp)
                    )
                }
            }
        }
    }
}

// ── Achievements ───────────────────────────────────────────────────────────────

@Composable
fun AchievementsSection(achievements: List<Achievement>) {
    val currentAccent = LocalAccentColor.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleSmall,
                color = currentAccent,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                text = "${achievements.count { it.unlocked }}/${achievements.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 🔥 REMOVED IntrinsicSize.Max to stop the crash
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(achievements) { achievement ->
                AchievementChip(achievement = achievement)
            }
        }
    }
}

@Composable
fun AchievementChip(achievement: Achievement) {
    val currentAccent = LocalAccentColor.current
    val containerColor = if (achievement.unlocked)
        currentAccent.copy(alpha = 0.15f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val contentAlpha = if (achievement.unlocked) 1f else 0.4f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier
            .width(115.dp)
            .height(130.dp) // 🔥 Fixed height ensures all cards look uniform and prevents crash
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = achievement.emoji,
                fontSize = 28.sp,
                modifier = Modifier.graphicsLayerAlpha(contentAlpha)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                maxLines = 1 // Keeps title clean
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                lineHeight = 13.sp,
                maxLines = 2 // Limits description height for uniformity
            )
        }
    }
}

// Helper extension for alpha on Modifier (avoids extra import)
private fun Modifier.graphicsLayerAlpha(alpha: Float): Modifier =
    this.then(Modifier.graphicsLayer(alpha = alpha))

// ── Preferences & Account Section ─────────────────────────────────────────────

@Composable
fun SettingsSection(
    uiState: ProfileUiState,
    onEditPersonalDetails: () -> Unit,
    onEditGoal: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onPickReminderTime: () -> Unit,
    onToggleRestTimer: (Boolean) -> Unit,
    onWeightUnitChange: (String) -> Unit
) {
    val currentAccent = LocalAccentColor.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Account group ──────────────────────────────────────────────────────
        SectionLabel(stringResource(R.string.profile_account_section), currentAccent)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.PersonOutline,
                    title = stringResource(R.string.profile_personal_details),
                    subtitle = stringResource(R.string.profile_personal_details_subtitle),
                    accentColor = currentAccent,
                    onClick = onEditPersonalDetails
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    title = stringResource(R.string.profile_fitness_goal),
                    subtitle = uiState.fitnessGoal,
                    accentColor = currentAccent,
                    onClick = onEditGoal
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Preferences group ──────────────────────────────────────────────────
        SectionLabel(stringResource(R.string.profile_preferences_section), currentAccent)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                // Notifications toggle
                ToggleMenuItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.profile_workout_reminders),
                    subtitle = stringResource(R.string.profile_workout_reminders_subtitle),
                    checked = uiState.notificationsEnabled,
                    accentColor = currentAccent,
                    onCheckedChange = onToggleNotifications
                )
                if (uiState.notificationsEnabled) {
                    MenuDivider()
                    val amPm = if (uiState.reminderHour < 12) "AM" else "PM"
                    val displayHour = if (uiState.reminderHour % 12 == 0) 12 else uiState.reminderHour % 12
                    val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, uiState.reminderMinute, amPm)
                    ProfileMenuItem(
                        icon = MaximusIcons.WorkoutLog.Timer,
                        title = "Reminder Time",
                        subtitle = formattedTime,
                        accentColor = currentAccent,
                        onClick = onPickReminderTime
                    )
                }
                MenuDivider()
                // Rest timer toggle
                ToggleMenuItem(
                    icon = Icons.Default.Timer,
                    title = stringResource(R.string.profile_rest_timer),
                    subtitle = stringResource(R.string.profile_rest_timer_subtitle),
                    checked = uiState.restTimerEnabled,
                    accentColor = currentAccent,
                    onCheckedChange = onToggleRestTimer
                )
                MenuDivider()
                // Weight unit picker
                WeightUnitMenuItem(
                    currentUnit = uiState.weightUnit,
                    accentColor = currentAccent,
                    onUnitChange = onWeightUnitChange
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ProfileIcon(icon: ImageVector, accentColor: Color) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val effectiveColor = if (isDark && accentColor.luminance() < 0.2f) Color.White else accentColor

    Surface(
        modifier = Modifier.size(38.dp),
        color = effectiveColor.copy(alpha = if (isDark) 0.16f else 0.12f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = effectiveColor
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    accentColor: Color = LocalAccentColor.current,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileIcon(icon = icon, accentColor = accentColor)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ToggleMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color = LocalAccentColor.current,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val effectiveAccent = when {
        isDark && accentColor.luminance() < 0.2f -> Color.White
        !isDark && accentColor.luminance() > 0.85f -> Color(0xFF18181B)
        else -> accentColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileIcon(icon = icon, accentColor = effectiveAccent)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (effectiveAccent.luminance() > 0.4f) Color(0xFF121214) else Color.White,
                checkedTrackColor = effectiveAccent,
                uncheckedThumbColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF71717A),
                uncheckedTrackColor = if (isDark) Color(0xFF27272A) else Color(0xFFE4E4E7),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun WeightUnitMenuItem(currentUnit: String, accentColor: Color = LocalAccentColor.current, onUnitChange: (String) -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val effectiveAccent = when {
        isDark && accentColor.luminance() < 0.2f -> Color.White
        !isDark && accentColor.luminance() > 0.85f -> Color(0xFF18181B)
        else -> accentColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileIcon(icon = Icons.Default.Scale, accentColor = effectiveAccent)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Weight Unit",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Used across the app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isDark) Color(0xFF27272A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(2.dp)
        ) {
            listOf("kg", "lbs").forEach { unit ->
                val selected = currentUnit.equals(unit, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selected) effectiveAccent
                            else Color.Transparent
                        )
                        .clickable { onUnitChange(unit) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) {
                            if (effectiveAccent.luminance() > 0.4f) Color(0xFF121214) else Color.White
                        } else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}



// ── Logout Button ──────────────────────────────────────────────────────────────

@Composable
fun LogoutButton(isLoggingOut: Boolean, onLogout: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoggingOut, onClick = onLogout)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Log out",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Log Out",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── App Links: Rate Us & Other Apps ──────────────────────────────────────────

@Composable
fun ProfileAppLinksRow(
    onRateUsClick: () -> Unit,
    onOtherAppsClick: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 1. RATE US PILL (50% width) ──
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onRateUsClick() },
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaximusIcons.Profile.RateUs,
                        contentDescription = stringResource(R.string.profile_rate_us_title),
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_rate_us_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.profile_rate_us_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // ── 2. OTHER APPS PILL (50% width) ──
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOtherAppsClick() },
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(currentAccent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaximusIcons.Profile.OtherApps,
                        contentDescription = stringResource(R.string.profile_more_apps_title),
                        tint = currentAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_more_apps_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.profile_more_apps_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ── Personal Details Dialog ────────────────────────────────────────────────────

@Composable
fun PersonalDetailsDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onSave: (Int, Float, Float, Boolean, String, String) -> Unit
) {
    val currentAccent = LocalAccentColor.current

    var age     by remember { mutableStateOf(uiState.age.toString()) }
    val initialWeightDisplay = if (uiState.weightKg > 0) {
        if (uiState.weightUnit == "lbs") "%.1f".format(uiState.weightKg * 2.205f) else "%.1f".format(uiState.weightKg)
    } else ""
    var weight  by remember { mutableStateOf(initialWeightDisplay) }
    var height  by remember { mutableStateOf(if (uiState.heightCm > 0) "%.0f".format(uiState.heightCm) else "175") }
    var gender  by remember { mutableStateOf(uiState.gender) }

    val genders = listOf("Male", "Female", "Other")

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = currentAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = currentAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Athlete Metrics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keep your body stats accurate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Age input
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Age") },
                    trailingIcon = {
                        Text("yrs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentAccent)
                )

                // Gender selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        genders.forEach { option ->
                            val isSelected = gender.equals(option, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) currentAccent else Color.Transparent)
                                    .clickable { gender = option }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) (if (currentAccent.luminance() > 0.4f) Color.Black else Color.White) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Weight
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    trailingIcon = {
                        Text(uiState.weightUnit, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentAccent)
                )

                // Height Section with Unit Toggle
                var isHeightMetric by remember { mutableStateOf(uiState.isHeightMetric) }
                val initialFtIn = remember(uiState.heightCm) { com.minimize.maximus.util.UnitConverter.cmToFtIn(uiState.heightCm) }
                var heightFeet by remember { mutableStateOf(initialFtIn.first.toString()) }
                var heightInches by remember { mutableStateOf(initialFtIn.second.toString()) }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Height",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isHeightMetric) currentAccent else Color.Transparent)
                                    .clickable {
                                        if (!isHeightMetric) {
                                            isHeightMetric = true
                                            val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(
                                                heightFeet.toIntOrNull() ?: 5,
                                                heightInches.toIntOrNull() ?: 9
                                            )
                                            height = "%.0f".format(cm)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "CM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHeightMetric) (if (currentAccent.luminance() > 0.4f) Color(0xFF121214) else Color.White) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (!isHeightMetric) currentAccent else Color.Transparent)
                                    .clickable {
                                        if (isHeightMetric) {
                                            isHeightMetric = false
                                            val (f, i) = com.minimize.maximus.util.UnitConverter.cmToFtIn(height.toFloatOrNull() ?: 175f)
                                            heightFeet = f.toString()
                                            heightInches = i.toString()
                                            val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(f, i)
                                            height = "%.0f".format(cm)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "FT / IN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isHeightMetric) (if (currentAccent.luminance() > 0.4f) Color(0xFF121214) else Color.White) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isHeightMetric) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            placeholder = { Text("175") },
                            trailingIcon = {
                                Text("cm", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentAccent)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = heightFeet,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() }.take(1)
                                    heightFeet = filtered
                                    val f = filtered.toIntOrNull() ?: 5
                                    val i = heightInches.toIntOrNull() ?: 0
                                    val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(f, i)
                                    height = "%.0f".format(cm)
                                },
                                placeholder = { Text("5") },
                                trailingIcon = {
                                    Text("ft", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentAccent),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = heightInches,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() }.take(2)
                                    heightInches = filtered
                                    val f = heightFeet.toIntOrNull() ?: 5
                                    val i = filtered.toIntOrNull() ?: 0
                                    val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(f, i)
                                    height = "%.0f".format(cm)
                                },
                                placeholder = { Text("10") },
                                trailingIcon = {
                                    Text("in", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentAccent),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val finalHeight = if (isHeightMetric) {
                                height.toFloatOrNull() ?: uiState.heightCm
                            } else {
                                val f = heightFeet.toIntOrNull() ?: initialFtIn.first
                                val i = heightInches.toIntOrNull() ?: initialFtIn.second
                                com.minimize.maximus.util.UnitConverter.ftInToCm(f, i)
                            }
                            val finalWeight = if (uiState.weightUnit == "lbs") {
                                weight.toFloatOrNull()?.div(2.205f) ?: uiState.weightKg
                            } else {
                                weight.toFloatOrNull() ?: uiState.weightKg
                            }
                            onSave(
                                age.toIntOrNull() ?: uiState.age,
                                finalWeight,
                                finalHeight,
                                isHeightMetric,
                                gender,
                                uiState.fitnessGoal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = currentAccent)
                    ) {
                        Text(
                            stringResource(R.string.action_save),
                            fontWeight = FontWeight.Bold,
                            color = if (currentAccent.luminance() > 0.4f) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}