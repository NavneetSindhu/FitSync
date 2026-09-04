package com.minimize.maximus.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.components.ToastType
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.DebugUtils
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

// ── Accent palette ───────────────────────────────────────────────────────────
fun getAccentOptions(isDarkMode: Boolean): List<Pair<String, Color>> = listOf(
    if (isDarkMode) ("Pure White" to Color(0xFFFFFFFF)) else ("Onyx Black" to Color(0xFF18181B)),
    "Crimson" to Color(0xFFFF3B30),
    "Amber" to Color(0xFFFF9800),
    "Emerald" to Color(0xFF34C759),
    "Cyan" to Color(0xFF32ADE6),
    "Indigo" to Color(0xFF5856D6),
    "Violet" to Color(0xFFAF52DE),
    "Rose" to Color(0xFFFF2D55)
)

val AllAccentNames = mapOf(
    Color(0xFFFFFFFF) to "Pure White",
    Color(0xFF18181B) to "Onyx Black",
    Color(0xFFFF3B30) to "Crimson",
    Color(0xFFFF9800) to "Amber",
    Color(0xFF34C759) to "Emerald",
    Color(0xFF32ADE6) to "Cyan",
    Color(0xFF5856D6) to "Indigo",
    Color(0xFFAF52DE) to "Violet",
    Color(0xFFFF2D55) to "Rose"
)

val AccentOptions = listOf(
    "Pure White" to Color(0xFFFFFFFF),
    "Onyx Black" to Color(0xFF18181B),
    "Crimson" to Color(0xFFFF3B30),
    "Amber" to Color(0xFFFF9800),
    "Emerald" to Color(0xFF34C759),
    "Cyan" to Color(0xFF32ADE6),
    "Indigo" to Color(0xFF5856D6),
    "Violet" to Color(0xFFAF52DE),
    "Rose" to Color(0xFFFF2D55)
)

val NavStyleOptions = listOf("Floating Pill", "Classic Bar")
val FontScaleOptions = listOf("Small" to 0.85f, "Default" to 1f, "Large" to 1.15f)
val FirstDayOptions = listOf("Monday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel
) {
    val isMetric     by viewModel.isMetric.collectAsState()
    val isDarkMode   by viewModel.isDarkMode.collectAsState()
    val accentInt    by viewModel.accentColor.collectAsState()
    val navStyle     by viewModel.navStyle.collectAsState()
    val fontScale    by viewModel.fontScale.collectAsState()
    val compactCards by viewModel.compactCards.collectAsState()
    val showVolume   by viewModel.showVolumeInLog.collectAsState()
    val restSeconds  by viewModel.defaultRestSeconds.collectAsState()
    val haptics      by viewModel.hapticsEnabled.collectAsState()

    val autoStartRest by viewModel.autoStartRest.collectAsState(initial = true)
    val defaultBarbell by viewModel.defaultBarbellWeight.collectAsState(initial = 20)
    val trackWarmups by viewModel.trackWarmupVolume.collectAsState(initial = false)
    val firstDayOfWeek by viewModel.firstDayOfWeek.collectAsState(initial = "Monday")
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()

    val rawAccent = Color(accentInt.toLong() and 0xFFFFFFFF)
    val currentAccent = when {
        isDarkMode && rawAccent.luminance() < 0.2f -> Color.White
        !isDarkMode && rawAccent.luminance() > 0.85f -> Color(0xFF18181B)
        else -> rawAccent
    }
    val context = LocalContext.current
    val toastManager = LocalMaximusToast.current
    val hapticFeedback = LocalHapticFeedback.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setNotificationsEnabled(true, context)
            toastManager.showToast("Daily workout reminders activated!", ToastType.SUCCESS)
        } else {
            viewModel.setNotificationsEnabled(false, context)
            toastManager.showToast("Notification permission denied in system settings.", ToastType.INFO)
        }
    }

    if (showReminderTimePicker) {
        com.minimize.maximus.ui.components.MaximusTimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onTimeSelected = { hour, minute ->
                viewModel.setReminderTime(hour, minute, context)
                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                val formatted = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
                toastManager.showToast("Reminder time set to $formatted", ToastType.SUCCESS)
            },
            onDismiss = { showReminderTimePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = currentAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = MaximusDimens.FloatingNavHeight + 32.dp)
        ) {

            // ── 1. Developer & Debug Tools (TOP Priority in Debug Builds) ───────
            if (DebugUtils.isDebug) {
                item {
                    val forceShimmer by viewModel.forceShimmer.collectAsState()
                    SettingsSection(title = stringResource(R.string.settings_debug_section), accentColor = currentAccent, isDebug = true) {
                        ToggleRow(
                            icon = Icons.Default.AutoAwesome,
                            title = stringResource(R.string.settings_debug_shimmer),
                            subtitle = stringResource(R.string.settings_debug_shimmer_subtitle),
                            checked = forceShimmer,
                            accentColor = currentAccent,
                            onCheckedChange = { viewModel.toggleForceShimmer(it) }
                        )
                        SectionDivider()
                        val isPersistentMockActive = toastManager.currentToast?.isPersistent == true
                        ToggleRow(
                            icon = Icons.Default.CloudOff,
                            title = "Always-Shown Toast (Offline Mock)",
                            subtitle = "Keep top header active to test UI layout shift across all screens",
                            checked = isPersistentMockActive,
                            accentColor = currentAccent,
                            onCheckedChange = { active ->
                                if (active) {
                                    toastManager.showToast(
                                        title = "You're Offline (Debug Mock)",
                                        message = "Testing top header drop-down and downward UI shift across all screens",
                                        type = ToastType.WARNING,
                                        isPersistent = true
                                    )
                                } else {
                                    toastManager.dismiss()
                                }
                            }
                        )
                        SectionDivider()
                        SettingsItem(
                            icon = Icons.Default.CheckCircle,
                            title = "Test Momentary Toast",
                            subtitle = "Preview drop-down header with auto-dismiss & UI shift",
                            accentColor = currentAccent,
                            onClick = {
                                toastManager.showToast(
                                    title = "Success Notification",
                                    message = "Uniswap drop-down header active with downward UI shift!",
                                    type = ToastType.SUCCESS
                                )
                            }
                        )
                        SectionDivider()
                        SettingsItem(
                            icon = Icons.Default.FitnessCenter,
                            title = stringResource(R.string.settings_debug_prefill_workouts),
                            subtitle = stringResource(R.string.settings_debug_prefill_subtitle),
                            accentColor = currentAccent,
                            onClick = {
                                viewModel.seedSampleWorkouts()
                                toastManager.showToast(
                                    title = "Data Seeded",
                                    message = "Sample workouts seeded into local database!",
                                    type = ToastType.SUCCESS
                                )
                            }
                        )
                        SectionDivider()
                        SettingsItem(
                            icon = Icons.Default.NotificationsActive,
                            title = "Dispatch Test Reminder",
                            subtitle = "Trigger workout reminder notification immediately",
                            accentColor = currentAccent,
                            onClick = {
                                viewModel.triggerImmediateTestNotification(context)
                                toastManager.showToast(
                                    title = "Notification Sent",
                                    message = "High-priority reminder sent to system tray!",
                                    type = ToastType.INFO
                                )
                            }
                        )
                        SectionDivider()
                        SettingsItem(
                            icon = Icons.Default.Vibration,
                            title = "Test PR Record Haptics",
                            subtitle = "Simulate personal record unlocked vibration pattern",
                            accentColor = currentAccent,
                            onClick = {
                                MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.PR_RECORD_UNLOCKED)
                                toastManager.showToast(
                                    title = "Haptics Triggered",
                                    message = "Tactile celebration vibration executed",
                                    type = ToastType.SUCCESS
                                )
                            }
                        )
                        SectionDivider()
                        SettingsItem(
                            icon = Icons.Default.RestartAlt,
                            title = stringResource(R.string.settings_debug_reset_onboarding),
                            subtitle = stringResource(R.string.settings_debug_reset_onboarding_subtitle),
                            accentColor = currentAccent,
                            onClick = {
                                viewModel.triggerFirstRunReset()
                            }
                        )
                    }
                }
            }

            // ── 2. Appearance & Theme ──────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_appearance_section), accentColor = currentAccent) {
                    ToggleRow(
                        icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        title = stringResource(R.string.settings_dark_mode),
                        subtitle = if (isDarkMode) stringResource(R.string.settings_dark_mode_active) else stringResource(R.string.settings_light_mode_active),
                        checked = isDarkMode,
                        accentColor = currentAccent,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                    SectionDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Navigation, currentAccent)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.settings_nav_style), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(stringResource(R.string.settings_nav_style_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        PillToggleRow(
                            options = NavStyleOptions,
                            selected = navStyle,
                            accentColor = currentAccent,
                            onSelect = { viewModel.setNavStyle(it) }
                        )
                    }
                    SectionDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Palette, currentAccent)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.settings_accent_color), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    AllAccentNames[currentAccent] ?: AccentOptions.find { it.second == currentAccent }?.first ?: "Custom",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        AccentColorPicker(
                            currentColor = currentAccent,
                            isDarkMode = isDarkMode,
                            onColorSelected = { viewModel.setAccentColor(it) }
                        )
                    }
                    SectionDivider()
                    ToggleRow(
                        icon = Icons.Default.ViewCompact,
                        title = stringResource(R.string.settings_compact_cards),
                        subtitle = stringResource(R.string.settings_compact_cards_subtitle),
                        checked = compactCards,
                        accentColor = currentAccent,
                        onCheckedChange = { viewModel.setCompactCards(it) }
                    )
                }
            }

            // ── 3. Units & Barbell ─────────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_units_section), accentColor = currentAccent) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Scale, currentAccent)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.settings_weight_unit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(if (isMetric) stringResource(R.string.settings_unit_kg_desc) else stringResource(R.string.settings_unit_lbs_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .clip(MaximusShapes.Pill)
                                .background(if (isDarkMode) Color(0xFF27272A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(2.dp)
                        ) {
                            listOf("KG" to true, "LBS" to false).forEach { (unitLabel, isKg) ->
                                val selected = (isMetric == isKg)
                                Box(
                                    modifier = Modifier
                                        .clip(MaximusShapes.Pill)
                                        .background(if (selected) currentAccent else Color.Transparent)
                                        .clickable { viewModel.toggleUnits(isKg) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unitLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) {
                                            if (currentAccent.luminance() > 0.4f) Color(0xFF121214) else Color.White
                                        } else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    SectionDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.FitnessCenter, currentAccent)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.settings_default_barbell), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    stringResource(
                                        R.string.settings_barbell_weight_format,
                                        defaultBarbell,
                                        if (isMetric) stringResource(R.string.unit_kg) else stringResource(R.string.unit_lbs)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Slider(
                            value = defaultBarbell.toFloat(),
                            onValueChange = { viewModel.setDefaultBarbellWeight(it.toInt()) },
                            valueRange = if (isMetric) 5f..25f else 10f..55f,
                            steps = 3,
                            colors = SliderDefaults.colors(thumbColor = currentAccent, activeTrackColor = currentAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── 4. Workout & Logging Preferences ───────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_workout_prefs_section), accentColor = currentAccent) {
                    ToggleRow(
                        icon = Icons.Default.PlayCircle,
                        title = stringResource(R.string.settings_auto_rest_timer),
                        subtitle = stringResource(R.string.settings_auto_rest_subtitle),
                        checked = autoStartRest,
                        accentColor = currentAccent,
                        onCheckedChange = { viewModel.setAutoStartRest(it) }
                    )
                    SectionDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Timer, currentAccent)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.settings_default_rest_duration), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    stringResource(R.string.settings_rest_duration_format, restSeconds / 60, restSeconds % 60),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Slider(
                            value = restSeconds.toFloat(),
                            onValueChange = { viewModel.setDefaultRestSeconds(it.toInt()) },
                            valueRange = 30f..300f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = currentAccent, activeTrackColor = currentAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SectionDivider()
                    ToggleRow(
                        icon = Icons.Default.TrendingUp,
                        title = stringResource(R.string.settings_track_warmups),
                        subtitle = stringResource(R.string.settings_track_warmups_subtitle),
                        checked = trackWarmups,
                        accentColor = currentAccent,
                        onCheckedChange = { viewModel.setTrackWarmupVolume(it) }
                    )
                }
            }

            // ── 5. App Experience & Calendar ────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_app_experience_section), accentColor = currentAccent) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.CalendarMonth, currentAccent)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.settings_first_day_week), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(stringResource(R.string.settings_first_day_week_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        PillToggleRow(
                            options = FirstDayOptions,
                            selected = firstDayOfWeek,
                            accentColor = currentAccent,
                            onSelect = { viewModel.setFirstDayOfWeek(it) }
                        )
                    }
                    SectionDivider()
                    ToggleRow(
                        icon = Icons.Default.Vibration,
                        title = stringResource(R.string.settings_haptic_feedback),
                        subtitle = stringResource(R.string.settings_haptic_subtitle),
                        checked = haptics,
                        accentColor = currentAccent,
                        onCheckedChange = { viewModel.setHapticsEnabled(it) }
                    )
                }
            }

            // ── 6. Notifications & Reminders ───────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_notifications_section), accentColor = currentAccent) {
                    ToggleRow(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_daily_reminders),
                        subtitle = stringResource(R.string.settings_daily_reminders_subtitle),
                        checked = notificationsEnabled,
                        accentColor = currentAccent,
                        onCheckedChange = { enabled ->
                            MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                            if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (!hasPerm) {
                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    return@ToggleRow
                                }
                            }
                            viewModel.setNotificationsEnabled(enabled, context)
                        }
                    )
                    if (notificationsEnabled) {
                        SectionDivider()
                        val amPm = if (reminderHour < 12) "AM" else "PM"
                        val displayHour = if (reminderHour % 12 == 0) 12 else reminderHour % 12
                        val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, reminderMinute, amPm)
                        SettingsItem(
                            icon = Icons.Default.Timer,
                            title = stringResource(R.string.settings_reminder_time),
                            subtitle = formattedTime,
                            accentColor = currentAccent,
                            onClick = {
                                MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.ACTION_TAP)
                                showReminderTimePicker = true
                            }
                        )
                    }
                }
            }

            // ── 7. Data & Export (Cloud sync hidden) ───────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_data_storage_section), accentColor = currentAccent) {
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = stringResource(R.string.settings_export_csv),
                        subtitle = stringResource(R.string.settings_export_csv_subtitle),
                        accentColor = currentAccent,
                        onClick = {
                            viewModel.exportWorkoutsToCsv(context) { errorMsg ->
                                toastManager.showToast("Export failed: $errorMsg", com.minimize.maximus.ui.components.ToastType.ERROR)
                            }
                        }
                    )
                }
            }

            // ── 8. Danger Zone ─────────────────────────────────────────────────
            item {
                Column {
                    Text(
                        stringResource(R.string.settings_danger_zone),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        SettingsItem(
                            icon = Icons.Default.DeleteForever,
                            title = stringResource(R.string.settings_reset_data),
                            subtitle = stringResource(R.string.settings_reset_data_subtitle),
                            titleColor = MaterialTheme.colorScheme.error,
                            accentColor = MaterialTheme.colorScheme.error,
                            onClick = { showResetDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                viewModel.fullReset("FIT-RESET")
                showResetDialog = false
                toastManager.showToast(
                    title = "Data Cleared",
                    message = "All records have been reset",
                    type = ToastType.INFO
                )
            }
        )
    }
}

// ── Accent Color Picker ────────────────────────────────────────────────────────
@Composable
private fun AccentColorPicker(currentColor: Color, isDarkMode: Boolean, onColorSelected: (Color) -> Unit) {
    val options = remember(isDarkMode) { getAccentOptions(isDarkMode) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        options.forEach { (_, color) ->
            val isSelected = currentColor == color
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(
                            3.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            CircleShape
                        ) else if (color == Color(0xFFFFFFFF) && isDarkMode) {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                        } else if (color == Color(0xFF18181B) && !isDarkMode) {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                        } else Modifier
                    )
                    .clickable { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (color.luminance() > 0.4f) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Pill Toggle Row (Segmented Capsule) ─────────────────────────────────────────
@Composable
private fun PillToggleRow(options: List<String>, selected: String, accentColor: Color, onSelect: (String) -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val effectiveAccent = when {
        isDark && accentColor.luminance() < 0.2f -> Color.White
        !isDark && accentColor.luminance() > 0.85f -> Color(0xFF18181B)
        else -> accentColor
    }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaximusShapes.Pill)
            .background(if (isDark) Color(0xFF27272A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            val bgCol by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) effectiveAccent else Color.Transparent,
                label = "pill_bg"
            )
            val txtCol by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) {
                    if (effectiveAccent.luminance() > 0.4f) Color(0xFF121214) else Color.White
                } else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "pill_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaximusShapes.Pill)
                    .background(bgCol)
                    .clickable {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                        onSelect(option)
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = txtCol
                )
            }
        }
    }
}
    

// ── Settings Section Container ─────────────────────────────────────────────────
@Composable
fun SettingsSection(
    title: String,
    accentColor: Color,
    isDebug: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        ) {
            if (isDebug) {
                Icon(
                    imageVector = Icons.Outlined.BugReport,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDebug) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            content = content
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun SettingIcon(icon: ImageVector, accentColor: Color) {
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
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            SettingIcon(icon, accentColor)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (accentColor.luminance() > 0.4f) Color.Black else Color.White,
                checkedTrackColor = accentColor
            )
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    val finalColor = titleColor ?: MaterialTheme.colorScheme.onSurface
    val isDestructive = titleColor == MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon, if (isDestructive) MaterialTheme.colorScheme.error else accentColor)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = finalColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (onClick != null && !isDestructive) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Reset Confirmation Dialog ──────────────────────────────────────────────────
@Composable
fun ResetConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reset_dialog_title), fontWeight = FontWeight.ExtraBold) },
        text = { Text(stringResource(R.string.settings_reset_dialog_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.settings_reset_dialog_confirm), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}
