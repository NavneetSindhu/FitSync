package com.minimize.maximus.ui.screens.analytics

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.domain.model.body.BodyViewMode
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.body.MuscleStat
import com.minimize.maximus.domain.model.body.RecoveryState
import com.minimize.maximus.ui.components.body.BodySilhouetteView
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyAnalyticsScreen(
    onBackClick: () -> Unit,
    viewModel: BodyAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current

    val activeMuscles = remember(uiState.viewMode) {
        if (uiState.viewMode == BodyViewMode.FRONT) {
            MuscleGroup.entries.filter { it.isFront }
        } else {
            MuscleGroup.entries.filter { it.isBack }
        }
    }

    val selectedStat = uiState.selectedMuscle?.let { uiState.muscleStats[it] }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muscle Recovery & Volume", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = MaximusDimens.FloatingNavHeight + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Overall Recovery Metric Card ────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "OVERALL RECOVERY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.overallRecoveryScore}% Ready",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = if (uiState.overallRecoveryScore > 75) Color(0xFF10B981) else currentAccent
                        )
                    }

                    // View Mode Pill Switcher (FRONT vs BACK)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            listOf(BodyViewMode.FRONT to "Front", BodyViewMode.BACK to "Back").forEach { (mode, label) ->
                                val isSelected = uiState.viewMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) currentAccent else Color.Transparent)
                                        .clickable {
                                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                                            viewModel.setViewMode(mode)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) {
                                            if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                                        } else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 2. 3D Holographic Vector Canvas Body Silhouette with Smooth Flip Transitions ──
            AnimatedContent(
                targetState = uiState.viewMode,
                transitionSpec = {
                    if (targetState == BodyViewMode.BACK) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(250)))
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(250)))
                    }
                },
                label = "BodyViewModeTransition"
            ) { mode ->
                BodySilhouetteView(
                    viewMode = mode,
                    muscleStats = uiState.muscleStats,
                    selectedMuscle = uiState.selectedMuscle,
                    onMuscleSelected = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                        viewModel.selectMuscle(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                )
            }

            // ── 3. Quick Muscle Selector Chips ─────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activeMuscles) { muscle ->
                    val isSelected = uiState.selectedMuscle == muscle
                    val stat = uiState.muscleStats[muscle]
                    Surface(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            viewModel.selectMuscle(muscle)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) currentAccent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, currentAccent) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(stat?.recoveryState?.color ?: Color.Gray)
                            )
                            Text(
                                text = muscle.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) currentAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // ── 4. Selected Muscle Detail Card ─────────────────────────────────
            selectedStat?.let { stat ->
                MuscleDetailCard(stat = stat, unit = uiState.weightUnit, accentColor = currentAccent)
            }
        }
    }
}

@Composable
fun MuscleDetailCard(
    stat: MuscleStat,
    unit: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stat.muscleGroup.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stat.muscleGroup.anatomicalName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = stat.recoveryState.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stat.recoveryState.label,
                        fontWeight = FontWeight.Bold,
                        color = stat.recoveryState.color,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Stats 2-Column Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("WEEKLY SETS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${stat.weeklySets} sets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = accentColor)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL VOLUME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${stat.weeklyVolume.toInt()} $unit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Contributing Exercises
            if (stat.contributingExercises.isNotEmpty()) {
                Text(
                    text = "Contributing Exercises",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    stat.contributingExercises.take(3).forEach { exName ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = exName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
