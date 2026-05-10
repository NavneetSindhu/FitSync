package com.example.fitsync.ui.screens.profile

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
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

// Height of the floating nav bar pill (must match MainActivity.kt constant)
private val FLOATING_NAV_HEIGHT = 104.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isEditingPersonalDetails) {
        PersonalDetailsDialog(
            uiState = uiState,
            onDismiss = viewModel::closePersonalDetailsEditor,
            onSave = { age, weight, height, gender, goal ->
                viewModel.savePersonalDetails(age, weight, height, gender, goal)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.ExtraBold) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = FLOATING_NAV_HEIGHT + 16.dp)
        ) {
            // ── 1. Header ──────────────────────────────────────────────────────
            item { ProfileHeader(uiState = uiState) }

            // ── 2. Body metrics strip ──────────────────────────────────────────
            item { BodyMetricsStrip(uiState = uiState) }

            // ── 3. Quick stats ─────────────────────────────────────────────────
            item { QuickStatsGrid(uiState = uiState) }

            // ── 4. Achievements ────────────────────────────────────────────────
            item { AchievementsSection(achievements = uiState.achievements) }

            // ── 5. Account / settings menu ────────────────────────────────────
            item {
                SettingsSection(
                    uiState = uiState,
                    onNavigateToSettings = onNavigateToSettings,
                    onEditPersonalDetails = viewModel::openPersonalDetailsEditor,
                    onToggleNotifications = viewModel::toggleNotifications,
                    onToggleRestTimer = viewModel::toggleRestTimer,
                    onWeightUnitChange = viewModel::setWeightUnit,
                    onCloudSync = viewModel::triggerCloudSync
                )
            }

            // ── 6. Logout ──────────────────────────────────────────────────────
            item {
                LogoutButton(
                    isLoggingOut = uiState.isLoggingOut,
                    onLogout = { viewModel.logout(onLogout) }
                )
            }
        }
    }
}

// ── Profile Header ─────────────────────────────────────────────────────────────

@Composable
fun ProfileHeader(uiState: ProfileUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.avatarInitial,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = uiState.userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = uiState.planLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ── Body Metrics Strip ─────────────────────────────────────────────────────────

@Composable
fun BodyMetricsStrip(uiState: ProfileUiState) {
    val bmi = uiState.weightKg / ((uiState.heightCm / 100f) * (uiState.heightCm / 100f))
    val bmiLabel = when {
        bmi < 18.5f -> "Underweight"
        bmi < 25f   -> "Normal"
        bmi < 30f   -> "Overweight"
        else        -> "Obese"
    }
    val bmiColor = when {
        bmi < 18.5f -> MaterialTheme.colorScheme.tertiary
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
                .padding(vertical = 16.dp),
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
            MetricItem(
                label = "Height",
                value = "%.0f".format(uiState.heightCm),
                unit = "cm"
            )
            VerticalDivider()
            MetricItem(
                label = "BMI",
                value = "%.1f".format(bmi),
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Your Stats",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Workouts",
                value = "${uiState.totalWorkouts}",
                icon = Icons.Default.FitnessCenter
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Streak",
                value = "${uiState.currentStreak}d",
                icon = Icons.Default.LocalFireDepartment
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Volume",
                value = uiState.totalVolumeKg,
                icon = Icons.Default.BarChart
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Avg Session",
                value = "${uiState.avgSessionMin}m",
                icon = Icons.Default.Timer
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Achievements ───────────────────────────────────────────────────────────────

@Composable
fun AchievementsSection(achievements: List<Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                text = "${achievements.count { it.unlocked }}/${achievements.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(achievements) { achievement ->
                AchievementChip(achievement = achievement)
            }
        }
    }
}

@Composable
fun AchievementChip(achievement: Achievement) {
    val containerColor = if (achievement.unlocked)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val contentAlpha = if (achievement.unlocked) 1f else 0.4f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier.width(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = achievement.emoji,
                fontSize = 28.sp,
                modifier = Modifier.graphicsLayerAlpha(contentAlpha)
            )
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                lineHeight = 14.sp
            )
        }
    }
}

// Helper extension for alpha on Modifier (avoids extra import)
private fun Modifier.graphicsLayerAlpha(alpha: Float): Modifier =
    this.then(Modifier.graphicsLayer(alpha = alpha))

// ── Settings Section ───────────────────────────────────────────────────────────

@Composable
fun SettingsSection(
    uiState: ProfileUiState,
    onNavigateToSettings: () -> Unit,
    onEditPersonalDetails: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleRestTimer: (Boolean) -> Unit,
    onWeightUnitChange: (String) -> Unit,
    onCloudSync: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Account group ──────────────────────────────────────────────────────
        SectionLabel("Account")
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.PersonOutline,
                    title = "Personal Details",
                    subtitle = "Age, weight, height & goal",
                    onClick = onEditPersonalDetails
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    title = "Fitness Goal",
                    subtitle = uiState.fitnessGoal,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onEditPersonalDetails
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Preferences group ──────────────────────────────────────────────────
        SectionLabel("Preferences")
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                // Notifications toggle
                ToggleMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Workout Reminders",
                    subtitle = "Daily push notifications",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )
                MenuDivider()
                // Rest timer toggle
                ToggleMenuItem(
                    icon = Icons.Default.Timer,
                    title = "Rest Timer",
                    subtitle = "Auto-start after each set",
                    checked = uiState.restTimerEnabled,
                    onCheckedChange = onToggleRestTimer
                )
                MenuDivider()
                // Weight unit picker
                WeightUnitMenuItem(
                    currentUnit = uiState.weightUnit,
                    onUnitChange = onWeightUnitChange
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Data group ─────────────────────────────────────────────────────────
        SectionLabel("Data")
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                CloudSyncMenuItem(
                    syncStatus = uiState.syncStatus,
                    onClick = onCloudSync
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "App Settings",
                    subtitle = "Theme and display",
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
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
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun WeightUnitMenuItem(currentUnit: String, onUnitChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Default.Scale,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Weight Unit",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Used across the app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Segmented-style pill toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(2.dp)
        ) {
            listOf("kg", "lbs").forEach { unit ->
                val selected = currentUnit == unit
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onUnitChange(unit) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CloudSyncMenuItem(syncStatus: SyncStatus, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = syncStatus == SyncStatus.Idle, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Cloud Sync",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            AnimatedContent(targetState = syncStatus, label = "SyncStatus") { status ->
                Text(
                    text = when (status) {
                        SyncStatus.Idle    -> "Back up data to FitSync servers"
                        SyncStatus.Syncing -> "Syncing…"
                        SyncStatus.Success -> "✓ Sync complete"
                        SyncStatus.Error   -> "Sync failed — tap to retry"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (status) {
                        SyncStatus.Success -> Color(0xFF4CAF50)
                        SyncStatus.Error   -> MaterialTheme.colorScheme.error
                        else               -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        AnimatedContent(targetState = syncStatus, label = "SyncIcon") { status ->
            when (status) {
                SyncStatus.Syncing ->
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                else ->
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
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

// ── Personal Details Dialog ────────────────────────────────────────────────────

@Composable
fun PersonalDetailsDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onSave: (Int, Float, Float, String, String) -> Unit
) {
    var age     by remember { mutableStateOf(uiState.age.toString()) }
    var weight  by remember { mutableStateOf(uiState.weightKg.toString()) }
    var height  by remember { mutableStateOf(uiState.heightCm.toString()) }
    var gender  by remember { mutableStateOf(uiState.gender) }
    var goal    by remember { mutableStateOf(uiState.fitnessGoal) }

    val genders = listOf("Male", "Female", "Other")
    val goals   = listOf("Lose Weight", "Build Muscle", "Stay Active")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Personal Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Age
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Weight
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (${uiState.weightUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Height
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender pill selector
                Text(
                    "Gender",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PillSelector(options = genders, selected = gender, onSelect = { gender = it })

                // Goal pill selector
                Text(
                    "Fitness Goal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PillSelector(options = goals, selected = goal, onSelect = { goal = it })

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }

                    Button(
                        onClick = {
                            onSave(
                                age.toIntOrNull() ?: uiState.age,
                                weight.toFloatOrNull() ?: uiState.weightKg,
                                height.toFloatOrNull() ?: uiState.heightCm,
                                gender,
                                goal
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save") }
                }
            }
        }
    }
}

@Composable
private fun PillSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(50)
                    )
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}