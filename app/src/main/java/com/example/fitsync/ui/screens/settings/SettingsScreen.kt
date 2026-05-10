package com.example.fitsync.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.theme.LocalAccentColor

// ── Accent palette (label to Color) ───────────────────────────────────────────
val AccentOptions = listOf(
    "Coral Red"    to Color(0xFFE53935),
    "Violet"       to Color(0xFF7C4DFF),
    "Teal"         to Color(0xFF00897B),
    "Amber"        to Color(0xFFFFB300),
    "Cobalt"       to Color(0xFF1E88E5),
    "Rose"         to Color(0xFFD81B60),
    "Lime"         to Color(0xFF43A047),
    "Deep Orange"  to Color(0xFFF4511E),
)

// ── Nav bar style options ──────────────────────────────────────────────────────
val NavStyleOptions = listOf("Floating Pill", "Classic Bar")

// ── Font scale options ─────────────────────────────────────────────────────────
val FontScaleOptions = listOf("Small" to 0.85f, "Default" to 1f, "Large" to 1.15f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel
) {
    val name         by viewModel.userName.collectAsState()
    val goal         by viewModel.userGoal.collectAsState()
    val isMetric     by viewModel.isMetric.collectAsState()
    val isDarkMode   by viewModel.isDarkMode.collectAsState()
    val accentInt    by viewModel.accentColor.collectAsState()
    val navStyle     by viewModel.navStyle.collectAsState()
    val fontScale    by viewModel.fontScale.collectAsState()
    val compactCards by viewModel.compactCards.collectAsState()
    val showVolume   by viewModel.showVolumeInLog.collectAsState()
    val restSeconds  by viewModel.defaultRestSeconds.collectAsState()
    val haptics      by viewModel.hapticsEnabled.collectAsState()

    val currentAccent = Color(accentInt.toLong() and 0xFFFFFFFF)

    var showEditDialog  by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {

            // ── 1. Profile card ────────────────────────────────────────────────
            item {
                ProfileCard(
                    name = name,
                    goal = goal,
                    onEditClick = { showEditDialog = true }
                )
            }

            // ── 2. Appearance ──────────────────────────────────────────────────
            item {
                SettingsSection(title = "Appearance") {

                    // Dark mode
                    ToggleRow(
                        icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        title = "Dark Mode",
                        subtitle = if (isDarkMode) "Dark theme active" else "Light theme active",
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )

                    SectionDivider()

                    // Accent colour
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Palette)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Accent Color",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    AccentOptions.find { it.second == currentAccent }?.first
                                        ?: "Custom",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Colour swatch row
                        AccentColorPicker(
                            currentColor = currentAccent,
                            onColorSelected = { viewModel.setAccentColor(it) }
                        )
                    }

                    SectionDivider()

                    // Font scale
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.TextFields)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Text Size",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    FontScaleOptions.find { it.second == fontScale }?.first
                                        ?: "Custom",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        PillToggleRow(
                            options = FontScaleOptions.map { it.first },
                            selected = FontScaleOptions.find { it.second == fontScale }?.first
                                ?: "Default",
                            accentColor = currentAccent,
                            onSelect = { label ->
                                FontScaleOptions.find { it.first == label }?.second
                                    ?.let { viewModel.setFontScale(it) }
                            }
                        )
                    }

                    SectionDivider()

                    // Nav bar style
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Settings)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Navigation Style",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    navStyle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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

                    // Compact cards
                    ToggleRow(
                        icon = Icons.Default.ViewCompact,
                        title = "Compact Cards",
                        subtitle = "Reduce card height in lists",
                        checked = compactCards,
                        onCheckedChange = { viewModel.setCompactCards(it) }
                    )
                }
            }

            // ── 3. Units & Measurements ────────────────────────────────────────
            item {
                SettingsSection(title = "Units & Measurements") {
                    // Weight unit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Scale)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Weight Unit",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = isMetric,
                                onClick = { viewModel.toggleUnits(true) },
                                shape = SegmentedButtonDefaults.itemShape(0, 2)
                            ) { Text("KG", fontSize = 12.sp) }
                            SegmentedButton(
                                selected = !isMetric,
                                onClick = { viewModel.toggleUnits(false) },
                                shape = SegmentedButtonDefaults.itemShape(1, 2)
                            ) { Text("LBS", fontSize = 12.sp) }
                        }
                    }
                }
            }

            // ── 4. Workout Defaults ────────────────────────────────────────────
            item {
                SettingsSection(title = "Workout Defaults") {

                    // Show volume in log
                    ToggleRow(
                        icon = Icons.Default.BarChart,
                        title = "Show Volume in Log",
                        subtitle = "Display total kg lifted per exercise",
                        checked = showVolume,
                        onCheckedChange = { viewModel.setShowVolumeInLog(it) }
                    )

                    SectionDivider()

                    // Default rest timer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Default.Timer)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Default Rest Timer",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${restSeconds}s between sets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Slider 30 – 300 s
                        Slider(
                            value = restSeconds.toFloat(),
                            onValueChange = { viewModel.setDefaultRestSeconds(it.toInt()) },
                            valueRange = 30f..300f,
                            steps = 8,                         // 30 s increments
                            colors = SliderDefaults.colors(
                                thumbColor = currentAccent,
                                activeTrackColor = currentAccent,
                                inactiveTrackColor = currentAccent.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "30s",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "5 min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── 5. Notifications & Feedback ────────────────────────────────────
            item {
                SettingsSection(title = "Notifications & Feedback") {
                    ToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on set completion",
                        checked = haptics,
                        onCheckedChange = { viewModel.setHapticsEnabled(it) }
                    )
                }
            }

            // ── 6. About ───────────────────────────────────────────────────────
            item {
                SettingsSection(title = "About FitSync") {
                    SettingsItem(Icons.Default.Info, "App Version", "1.0.0 (2026 Build)")
                    SectionDivider()
                    SettingsItem(Icons.Default.Shield, "Privacy Policy", onClick = {})
                }
            }

            // ── 7. Danger Zone ─────────────────────────────────────────────────
            item {
                Column {
                    Text(
                        "Danger Zone",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        )
                    ) {
                        SettingsItem(
                            icon = Icons.Default.DeleteForever,
                            title = "Reset All Data",
                            subtitle = "Permanently delete local & cloud data",
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = { showResetDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = name,
            currentGoal = goal,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newGoal ->
                viewModel.updateProfile(newName, newGoal)
                showEditDialog = false
            }
        )
    }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                viewModel.fullReset("FIT-${name.uppercase()}")
                showResetDialog = false
            }
        )
    }
}

// ── Profile Card ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(name: String, goal: String, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    goal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    CircleShape
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Accent color picker ────────────────────────────────────────────────────────

@Composable
private fun AccentColorPicker(currentColor: Color, onColorSelected: (Color) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AccentOptions.forEach { (_, color) ->
            val isSelected = currentColor == color
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(
                            3.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            CircleShape
                        ) else Modifier
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

// ── Pill toggle row (shared) ───────────────────────────────────────────────────

@Composable
private fun PillToggleRow(
    options: List<String>,
    selected: String,
    accentColor: Color,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) accentColor else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        if (accentColor.luminance() > 0.4f) Color.Black else Color.White
                    } else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Shared sub-components ──────────────────────────────────────────────────────

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            content = content
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(36.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.padding(8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ToggleRow(
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            SettingIcon(icon)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
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
        Surface(
            modifier = Modifier.size(36.dp),
            color = if (isDestructive)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = finalColor)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

// ── Dialogs ────────────────────────────────────────────────────────────────────

@Composable
fun ResetConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wipe all data?", fontWeight = FontWeight.Bold) },
        text = {
            Text("This will permanently delete your local history and cloud backup. This cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Delete Everything") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentGoal: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var goal by remember { mutableStateOf(currentGoal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Update Profile",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Fitness Goal") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, goal) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}