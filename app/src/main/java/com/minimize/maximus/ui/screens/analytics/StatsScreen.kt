package com.minimize.maximus.ui.screens.analytics

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.WorkoutSummary
import com.minimize.maximus.domain.model.body.BodyViewMode
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.body.MuscleStat
import com.minimize.maximus.ui.components.EmptyStateView
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.components.MaximusLineGraph
import com.minimize.maximus.ui.components.MaximusSelectDropdown
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.components.body.BodySilhouetteView
import com.minimize.maximus.ui.screens.home.PRItemCard
import com.minimize.maximus.ui.screens.home.WorkoutDetailsCard
import com.minimize.maximus.ui.screens.home.components.MiniStatCard
import com.minimize.maximus.ui.screens.home.heatmap.ProductionCalendar
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import java.time.LocalDate

enum class StatsTab {
    OVERVIEW,
    OVERLOAD,
    ANATOMY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBackClick: () -> Unit,
    onStartTodayWorkout: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember { mutableStateOf(StatsTab.OVERVIEW) }
    var selectedCalendarDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.stats_title),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(MaximusIcons.Navigation.BackArrow, contentDescription = "Back")
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
        ) {
            // ── Primary 3-Tab Segmented Row ──
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = currentAccent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = currentAccent
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == StatsTab.OVERVIEW,
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.TAB_SWITCH)
                        selectedTab = StatsTab.OVERVIEW
                    },
                    text = {
                        Text(
                            stringResource(R.string.stats_tab_overview),
                            fontWeight = if (selectedTab == StatsTab.OVERVIEW) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == StatsTab.OVERLOAD,
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.TAB_SWITCH)
                        selectedTab = StatsTab.OVERLOAD
                    },
                    text = {
                        Text(
                            stringResource(R.string.stats_tab_overload),
                            fontWeight = if (selectedTab == StatsTab.OVERLOAD) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == StatsTab.ANATOMY,
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.TAB_SWITCH)
                        selectedTab = StatsTab.ANATOMY
                    },
                    text = {
                        Text(
                            stringResource(R.string.stats_tab_anatomy),
                            fontWeight = if (selectedTab == StatsTab.ANATOMY) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Tab Content ──
            AnimatedContent(
                targetState = selectedTab,
                label = "StatsTabTransition",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    StatsTab.OVERVIEW -> {
                        StatsOverviewTab(
                            uiState = uiState,
                            selectedDate = selectedCalendarDate,
                            accentColor = currentAccent,
                            onDateSelected = { selectedCalendarDate = it },
                            onStartTodayWorkout = onStartTodayWorkout,
                            onSelectMetric = { viewModel.selectGraphMetric(it) }
                        )
                    }
                    StatsTab.OVERLOAD -> {
                        ProgressiveOverloadTab(
                            uiState = uiState,
                            accentColor = currentAccent,
                            onSelectExercise = { viewModel.selectProgressiveExercise(it) }
                        )
                    }
                    StatsTab.ANATOMY -> {
                        StatsAnatomyTab(
                            uiState = uiState,
                            accentColor = currentAccent,
                            onToggleViewMode = {
                                val next = if (uiState.viewMode == BodyViewMode.FRONT) BodyViewMode.BACK else BodyViewMode.FRONT
                                viewModel.setViewMode(next)
                            },
                            onSelectMuscle = { viewModel.selectMuscle(it) }
                        )
                    }
                }
            }
        }
    }
}

// ── Tab 1: Overview Tab (With Integrated Calendar & Metrics) ──────────────
@Composable
private fun StatsOverviewTab(
    uiState: StatsScreenUiState,
    selectedDate: LocalDate,
    accentColor: Color,
    onDateSelected: (LocalDate) -> Unit,
    onStartTodayWorkout: () -> Unit,
    onSelectMetric: (StatsGraphMetric) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Metric Trio Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatCard(
                    label = stringResource(R.string.home_total_volume),
                    value = "${uiState.totalVolume.toInt()} ${uiState.weightUnit}",
                    color = accentColor,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                MiniStatCard(
                    label = stringResource(R.string.home_total_sets),
                    value = "${uiState.totalSets}",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                MiniStatCard(
                    label = stringResource(R.string.home_total_workouts),
                    value = "${uiState.totalWorkouts}",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }

        // 2. Integrated Calendar Heatmap Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    ProductionCalendar(
                        selectedDate = selectedDate,
                        workoutMap = uiState.workoutMap,
                        frozenDates = uiState.frozenDates,
                        onDateSelected = { clickedDate ->
                            if (!clickedDate.isAfter(LocalDate.now())) {
                                MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                onDateSelected(clickedDate)
                            }
                        },
                        onMonthChanged = { }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Text(
                        text = if (selectedDate == LocalDate.now()) stringResource(R.string.home_showing_today)
                        else stringResource(R.string.home_showing_date_format, selectedDate.dayOfMonth, selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp)
                    )
                }
            }
        }

        // 3. Selected Day Details Card
        item {
            val currentSummary = uiState.workoutMap[selectedDate]
            AnimatedContent(
                targetState = selectedDate to currentSummary,
                label = "OverviewCalendarDetailsCardAnim"
            ) { (currentDate, summaryState) ->
                if (summaryState != null) {
                    WorkoutDetailsCard(date = currentDate, summary = summaryState)
                } else {
                    val isToday = currentDate == LocalDate.now()
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isToday) "No Workout Logged Yet Today" else stringResource(R.string.home_no_workout_logged),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isToday) "Ready to train? Start your session now." else stringResource(R.string.home_missed_log_format, currentDate.dayOfMonth, currentDate.month.name.take(3)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isToday) {
                                TextButton(
                                    onClick = onStartTodayWorkout,
                                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                                ) {
                                    Text("Log Workout", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Multi-Metric Trend Graph with Dropdown Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaximusShapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEEKLY TRENDS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.selectedGraphMetric.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        MaximusSelectDropdown(
                            items = StatsGraphMetric.entries,
                            selectedItem = uiState.selectedGraphMetric,
                            onItemSelected = { onSelectMetric(it) },
                            labelProvider = { it.displayName }
                        )
                    }

                    val graphUnit = when (uiState.selectedGraphMetric) {
                        StatsGraphMetric.VOLUME -> uiState.weightUnit
                        StatsGraphMetric.SETS -> "Sets"
                        StatsGraphMetric.WORKOUTS -> "Workouts"
                        StatsGraphMetric.INTENSITY -> "Reps"
                    }

                    AnimatedContent(
                        targetState = uiState.selectedGraphMetric,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                        label = "StatsGraphTransition"
                    ) { metric ->
                        MaximusLineGraph(
                            points = uiState.currentMetricChart,
                            unit = graphUnit,
                            title = "",
                            containerColor = Color.Transparent
                        )
                    }
                }
            }
        }

        // 5. Personal Records (1RM Bests)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.home_personal_records_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.personalRecords.isEmpty()) {
                    EmptyStateView(
                        title = stringResource(R.string.home_no_prs_title),
                        subtitle = stringResource(R.string.home_no_prs_desc),
                        fallbackIcon = Icons.Default.EmojiEvents
                    )
                } else {
                    uiState.personalRecords.forEach { pr ->
                        PRItemCard(pr = pr, weightUnit = uiState.weightUnit, accentColor = accentColor)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(MaximusDimens.FloatingNavHeight + 48.dp)) }
    }
}

// ── Tab 2: Progressive Overload Tab ───────────────────────────────────────
@Composable
private fun ProgressiveOverloadTab(
    uiState: StatsScreenUiState,
    accentColor: Color,
    onSelectExercise: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Exercise Selector Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT EXERCISE TO TRACK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.progressiveExercises.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Log workout sessions to unlock progressive overload charts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.progressiveExercises) { exProg ->
                            val isSelected = exProg.exerciseName == uiState.selectedExerciseProgression?.exerciseName
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                    onSelectExercise(exProg.exerciseName)
                                },
                                label = {
                                    Text(
                                        text = exProg.exerciseName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = accentColor
                                )
                            )
                        }
                    }
                }
            }
        }

        // Progression Details Card
        uiState.selectedExerciseProgression?.let { prog ->
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ExerciseIcon(name = prog.exerciseName, size = 36.dp)
                                Column {
                                    Text(
                                        text = prog.exerciseName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${prog.sessionCount} sessions logged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (prog.percentageGain != 0f) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (prog.percentageGain >= 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${if (prog.percentageGain >= 0) "+" else ""}${String.format("%.1f", prog.percentageGain)}%",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = if (prog.percentageGain >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "INITIAL 1RM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${prog.initial1RM.toInt()} ${uiState.weightUnit}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "PEAK 1RM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${prog.peak1RM.toInt()} ${uiState.weightUnit}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor
                                )
                            }
                        }

                        if (prog.historyPoints.size >= 2) {
                            MaximusLineGraph(
                                points = prog.historyPoints,
                                unit = uiState.weightUnit,
                                title = stringResource(R.string.stats_1rm_progression_chart_title)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Session Progression Logs",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(prog.sessions.reversed(), key = { index, s -> "sess_log_${index}_${s.date}" }) { _, sess ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${sess.date.dayOfMonth} ${sess.date.month.name.take(3)} ${sess.date.year}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Best: ${sess.bestWeight.toInt()} ${uiState.weightUnit} × ${sess.bestReps} reps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "1RM: ${sess.estimated1RM.toInt()} ${uiState.weightUnit}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(MaximusDimens.FloatingNavHeight + 16.dp)) }
    }
}

// ── Tab 3: 3D Holographic Silhouette & Anatomy Tab ────────────────────────
@Composable
private fun StatsAnatomyTab(
    uiState: StatsScreenUiState,
    accentColor: Color,
    onToggleViewMode: () -> Unit,
    onSelectMuscle: (MuscleGroup) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // View Mode Flip Switch & Readiness Score
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Readiness: ${uiState.overallRecoveryScore}%",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                FilledTonalButton(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        onToggleViewMode()
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (uiState.viewMode == BodyViewMode.FRONT) "Flip to Back 🔄" else "Flip to Front 🔄",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 3D Body Silhouette Interactive View
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BodySilhouetteView(
                        viewMode = uiState.viewMode,
                        muscleStats = uiState.muscleStats,
                        selectedMuscle = uiState.selectedMuscle,
                        onMuscleSelected = onSelectMuscle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Selected Muscle Breakdown Card
        item {
            uiState.selectedMuscle?.let { muscle ->
                val stat = uiState.muscleStats[muscle]
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = muscle.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = stat?.recoveryState?.color?.copy(alpha = 0.15f) ?: Color.Transparent
                            ) {
                                Text(
                                    text = stat?.recoveryState?.label ?: "Fresh",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = stat?.recoveryState?.color ?: accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Weekly Sets: ${stat?.weeklySets ?: 0}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Weekly Volume: ${stat?.weeklyVolume?.toInt() ?: 0} ${uiState.weightUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(MaximusDimens.FloatingNavHeight + 16.dp)) }
    }
}
