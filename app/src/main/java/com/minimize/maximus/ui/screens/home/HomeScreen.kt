package com.minimize.maximus.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.WorkoutSummary
import com.minimize.maximus.domain.model.routine.DaySchedule
import com.minimize.maximus.domain.model.routine.RoutineExercise
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.ui.components.ActiveWorkoutMiniBar
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.components.HomeScreenFullSkeletonView
import com.minimize.maximus.ui.screens.home.components.StreakDetailBottomSheet
import com.minimize.maximus.ui.screens.settings.SettingsViewModel
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onRoutinesClick: () -> Unit,
    onStatsClick: () -> Unit = {},
    onStartWorkout: () -> Unit,
    onResumeWorkout: () -> Unit = {},
    onAICoachClick: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val userName by homeViewModel.userName.collectAsState()
    val dashboardState by homeViewModel.dashboardState.collectAsState()
    val activeWorkout by homeViewModel.activeWorkout.collectAsState()
    val isAiCoachEnabled by homeViewModel.isAiCoachEnabled.collectAsState()

    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    var showStreakDetailSheet by remember { mutableStateOf(false) }
    var showSkipWorkoutSheet by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }

    LaunchedEffect(Unit) {
        homeViewModel.refreshUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = stringResource(R.string.app_name),
                            colorFilter = ColorFilter.tint(currentAccent),
                            modifier = Modifier
                                .height(26.dp)
                                .wrapContentWidth(Alignment.Start)
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // AI Coach Quick Button (remotely toggleable)
                    AnimatedVisibility(
                        visible = isAiCoachEnabled,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        Surface(
                            onClick = {
                                MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                onAICoachClick()
                            },
                            shape = MaximusShapes.Pill,
                            color = currentAccent.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, currentAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = MaximusIcons.Home.AiSparkleDrawable),
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "AI Coach",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = currentAccent
                                )
                            }
                        }
                    }

                    // Streak Flame Pill with Freeze Indicator
                    if (dashboardState.currentStreakDays > 0 || dashboardState.isStreakFrozenToday) {
                        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                        val freezeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        val flameColor = Color(0xFFFF9800)
                        val pillColor = if (dashboardState.isStreakFrozenToday) freezeColor else flameColor

                        Surface(
                            onClick = {
                                MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                showStreakDetailSheet = true
                            },
                            shape = MaximusShapes.Pill,
                            color = pillColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (dashboardState.isStreakFrozenToday) MaximusIcons.Home.CalendarFreeze else MaximusIcons.Home.StreakFlame,
                                    contentDescription = null,
                                    tint = pillColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = if (dashboardState.isStreakFrozenToday) "Frozen" else stringResource(R.string.home_streak_days_format, dashboardState.currentStreakDays),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = pillColor
                                )
                            }
                        }
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = MaximusIcons.Home.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val isMiniBarVisible = activeWorkout?.isMinimized == true
        val homeBottomPadding by androidx.compose.animation.core.animateDpAsState(
            targetValue = if (isMiniBarVisible) MaximusDimens.FloatingNavHeight + 120.dp else MaximusDimens.FloatingNavHeight + 48.dp,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
            ),
            label = "HomeBottomPadding"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Crossfade(
                targetState = dashboardState.isLoading,
                label = "HomeDashboardLoadingCrossfade"
            ) { isLoading ->
                if (isLoading) {
                    HomeScreenFullSkeletonView(modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = homeBottomPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── 1. Greeting & Interactive Day-Bubble Week Strip ──
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_greeting_format, userName),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black
                                )

                                InteractiveWeeklyBubbleRow(
                                    weekDays = dashboardState.weekDaysStatus,
                                    accentColor = currentAccent,
                                    onManageRoutines = onRoutinesClick
                                )
                            }
                        }

                        // ── 2. Skipped Workout Makeup Alert (If any) ──
                        dashboardState.skippedWorkoutAlert?.let { skipped ->
                            item {
                                Surface(
                                    shape = MaximusShapes.Card,
                                    color = Color(0xFFFF9800).copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(MaximusIcons.Home.SkippedAlert, contentDescription = null, tint = Color(0xFFFF9800))
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.home_skipped_yesterday_alert, skipped.routineName ?: "Workout", skipped.dayOfWeek.name.take(3)),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF9800)
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            TextButton(onClick = {
                                                MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                                homeViewModel.startEmptyWorkout(onStartWorkout)
                                            }) {
                                                Text(stringResource(R.string.home_make_up_workout_btn), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFF9800))
                                            }
                                            IconButton(
                                                onClick = {
                                                    MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                                    homeViewModel.dismissSkippedAlert()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(MaximusIcons.Home.Close, contentDescription = "Dismiss", tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── 3. TODAY'S SESSION HERO CARD ──
                        item {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                TodaySessionHeroCard(
                                    todayRoutine = dashboardState.todayRoutine,
                                    todaySchedule = dashboardState.todaySchedule,
                                    accentColor = currentAccent,
                                    onStartToday = {
                                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                        homeViewModel.startTodayWorkout(onStartWorkout)
                                    },
                                    onStartFreestyle = {
                                        homeViewModel.startEmptyWorkout(onStartWorkout)
                                    },
                                    onSkipToday = {
                                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                        showSkipWorkoutSheet = true
                                    },
                                    onManageRoutines = onRoutinesClick
                                )
                            }
                        }

                        // ── 4. QUICK ACTIONS: Custom Routine vs Freestyle Log ──
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                        homeViewModel.startEmptyWorkout(onStartWorkout)
                                    },
                                    shape = MaximusShapes.Pill,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(MaximusIcons.Home.EmptyWorkout, contentDescription = null, tint = currentAccent, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.home_empty_workout_btn), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Surface(
                                    onClick = onRoutinesClick,
                                    shape = MaximusShapes.Pill,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(MaximusIcons.Home.Routines, contentDescription = null, tint = currentAccent, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.nav_routines), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Floating Active Workout Mini-Bar (if minimized) ──
            ActiveWorkoutMiniBar(
                activeWorkout = activeWorkout,
                onResumeClick = onResumeWorkout,
                onDiscardClick = { homeViewModel.discardActiveWorkout() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showStreakDetailSheet) {
        StreakDetailBottomSheet(
            currentStreak = dashboardState.currentStreakDays,
            availableFreezes = dashboardState.availableFreezes,
            maxFreezes = dashboardState.maxFreezes,
            isTodayFrozen = dashboardState.isStreakFrozenToday,
            onDismissRequest = { showStreakDetailSheet = false }
        )
    }

    // ── Explicit Skip Workout Bottom Sheet ──
    if (showSkipWorkoutSheet) {
        SkipWorkoutBottomSheet(
            routineName = dashboardState.todayRoutine?.name ?: dashboardState.todaySchedule?.routineName ?: "Workout",
            accentColor = currentAccent,
            onDismiss = { showSkipWorkoutSheet = false },
            onMarkRestDay = {
                homeViewModel.markTodayAsRestDay {
                    showSkipWorkoutSheet = false
                }
            },
            onShiftForward = {
                homeViewModel.shiftSplitForward {
                    showSkipWorkoutSheet = false
                }
            },
            onSkipForNow = {
                showSkipWorkoutSheet = false
            }
        )
    }
}

// ── Skip Workout Bottom Sheet ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkipWorkoutBottomSheet(
    routineName: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onMarkRestDay: () -> Unit,
    onShiftForward: () -> Unit,
    onSkipForNow: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.skip_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(R.string.skip_dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Option 1: Mark as Rest Day
            Surface(
                onClick = {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                    onMarkRestDay()
                },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF10B981).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌿", fontSize = 24.sp)
                    Column {
                        Text(
                            text = stringResource(R.string.skip_option_rest_title),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = stringResource(R.string.skip_option_rest_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Option 2: Shift Split Forward (+1 Day)
            Surface(
                onClick = {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                    onShiftForward()
                },
                shape = RoundedCornerShape(16.dp),
                color = accentColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏩", fontSize = 24.sp)
                    Column {
                        Text(
                            text = stringResource(R.string.skip_option_shift_title),
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = stringResource(R.string.skip_option_shift_desc, routineName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Option 3: Skip for now
            Surface(
                onClick = {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                    onSkipForNow()
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏳", fontSize = 24.sp)
                    Column {
                        Text(
                            text = stringResource(R.string.skip_option_dismiss_title),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.skip_option_dismiss_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Interactive Day-by-Day Bubble Row ─────────────────────────────────────
@Composable
private fun InteractiveWeeklyBubbleRow(
    weekDays: List<com.minimize.maximus.ui.screens.home.WeekDayStatus>,
    accentColor: Color,
    onManageRoutines: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedDayOfWeek by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iceColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    val activeSelectedDay = weekDays.find { it.dayOfWeek == selectedDayOfWeek } ?: weekDays.firstOrNull()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { dayStatus ->
                    val isBubbleSelected = dayStatus.dayOfWeek == selectedDayOfWeek

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                selectedDayOfWeek = dayStatus.dayOfWeek
                            }
                    ) {
                        Text(
                            text = dayStatus.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            fontSize = 12.sp,
                            fontWeight = if (dayStatus.isToday) FontWeight.Black else FontWeight.SemiBold,
                            color = if (dayStatus.isToday) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isBubbleSelected -> accentColor.copy(alpha = 0.25f)
                                        dayStatus.isFrozen -> iceColor.copy(alpha = 0.2f)
                                        dayStatus.isCompleted -> accentColor
                                        dayStatus.isToday -> accentColor.copy(alpha = 0.15f)
                                        dayStatus.isRestDay -> Color(0xFF10B981).copy(alpha = 0.15f)
                                        dayStatus.isSkipped -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .border(
                                    width = if (isBubbleSelected) 2.dp else if (dayStatus.isToday || dayStatus.isFrozen) 1.5.dp else 1.dp,
                                    color = when {
                                        isBubbleSelected -> accentColor
                                        dayStatus.isFrozen -> iceColor
                                        dayStatus.isToday -> accentColor.copy(alpha = 0.6f)
                                        dayStatus.isCompleted -> accentColor
                                        dayStatus.isRestDay -> Color(0xFF10B981).copy(alpha = 0.4f)
                                        dayStatus.isSkipped -> Color(0xFFFF9800).copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                dayStatus.isFrozen -> Icon(
                                    imageVector = MaximusIcons.Home.CalendarFreeze,
                                    contentDescription = "Streak Frozen",
                                    tint = iceColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                dayStatus.isCompleted -> Icon(
                                    imageVector = MaximusIcons.Home.CalendarCompleted,
                                    contentDescription = "Completed",
                                    tint = if (accentColor.luminance() > 0.4f) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                dayStatus.isRestDay -> Icon(
                                    imageVector = MaximusIcons.Home.CalendarRestDay,
                                    contentDescription = "Rest Day",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(15.dp)
                                )
                                dayStatus.isSkipped -> Icon(
                                    imageVector = MaximusIcons.Home.CalendarSkipped,
                                    contentDescription = "Skipped",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                                dayStatus.isToday -> Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(accentColor)
                                )
                                else -> Text(
                                    text = dayStatus.date.dayOfMonth.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            activeSelectedDay?.let { selected ->
                Surface(
                    onClick = onManageRoutines,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selected.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = when {
                                    selected.isFrozen -> stringResource(R.string.streak_freeze_auto_protected_day)
                                    selected.isRestDay -> "Rest & Recovery 🌿"
                                    else -> (selected.routineName ?: "Planned Split")
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    selected.isFrozen -> iceColor
                                    selected.isRestDay -> Color(0xFF10B981)
                                    else -> accentColor
                                }
                            )
                        }

                        Icon(MaximusIcons.Home.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ── Today's Session Hero Card ─────────────────────────────────────────────
@Composable
private fun TodaySessionHeroCard(
    todayRoutine: WorkoutRoutine?,
    todaySchedule: DaySchedule?,
    accentColor: Color,
    onStartToday: () -> Unit,
    onStartFreestyle: () -> Unit,
    onSkipToday: () -> Unit,
    onManageRoutines: () -> Unit
) {
    val isRest = todaySchedule?.isRestDay == true && todayRoutine == null
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isRest) Color(0xFF10B981).copy(alpha = 0.35f) else accentColor.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isRest) Color(0xFF10B981).copy(alpha = 0.15f) else accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isRest) "REST & RECOVERY" else stringResource(R.string.home_today_session_title),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (isRest) Color(0xFF10B981) else accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isRest) {
                        TextButton(
                            onClick = onSkipToday,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(stringResource(R.string.skip_workout_btn), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = onManageRoutines, modifier = Modifier.size(24.dp)) {
                        Icon(MaximusIcons.Home.EditSchedule, contentDescription = "Edit Schedule", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (isRest) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.home_rest_today_hero_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Muscles repair and grow during rest periods. Hydrate, eat protein, and prepare for your next split.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onStartFreestyle,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(MaximusIcons.Home.StartWorkout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.home_start_unscheduled_btn), fontWeight = FontWeight.Bold)
                }
            } else {
                val exerciseList = remember(todayRoutine, todaySchedule) {
                    if (!todayRoutine?.exercises.isNullOrEmpty()) {
                        todayRoutine!!.exercises
                    } else {
                        val name = (todayRoutine?.name ?: todaySchedule?.routineName ?: "").lowercase()
                        when {
                            name.contains("push") -> listOf(
                                RoutineExercise(exerciseName = "Bench Press", targetSets = 4, targetReps = 8, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 0),
                                RoutineExercise(exerciseName = "Incline Dumbbell Press", targetSets = 3, targetReps = 10, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                                RoutineExercise(exerciseName = "Overhead Shoulder Press", targetSets = 3, targetReps = 10, targetRepsRange = "8-12", defaultWeight = 0f, restSeconds = 90, orderIndex = 2),
                                RoutineExercise(exerciseName = "Tricep Rope Pushdown", targetSets = 3, targetReps = 12, targetRepsRange = "12-15", defaultWeight = 0f, restSeconds = 60, orderIndex = 3)
                            )
                            name.contains("pull") -> listOf(
                                RoutineExercise(exerciseName = "Barbell Deadlift", targetSets = 4, targetReps = 6, targetRepsRange = "6-8", defaultWeight = 0f, restSeconds = 120, orderIndex = 0),
                                RoutineExercise(exerciseName = "Barbell Bent-Over Row", targetSets = 4, targetReps = 8, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                                RoutineExercise(exerciseName = "Lat Pulldown", targetSets = 3, targetReps = 10, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 90, orderIndex = 2),
                                RoutineExercise(exerciseName = "Barbell Bicep Curl", targetSets = 3, targetReps = 12, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 60, orderIndex = 3)
                            )
                            name.contains("leg") -> listOf(
                                RoutineExercise(exerciseName = "Barbell Back Squat", targetSets = 4, targetReps = 8, targetRepsRange = "6-8", defaultWeight = 0f, restSeconds = 120, orderIndex = 0),
                                RoutineExercise(exerciseName = "Romanian Deadlift", targetSets = 3, targetReps = 10, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                                RoutineExercise(exerciseName = "Leg Press", targetSets = 3, targetReps = 12, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 90, orderIndex = 2),
                                RoutineExercise(exerciseName = "Calf Raises", targetSets = 4, targetReps = 15, targetRepsRange = "12-15", defaultWeight = 0f, restSeconds = 60, orderIndex = 3)
                            )
                            name.contains("upper") -> listOf(
                                RoutineExercise(exerciseName = "Bench Press", targetSets = 4, targetReps = 8, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 0),
                                RoutineExercise(exerciseName = "Barbell Row", targetSets = 4, targetReps = 8, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                                RoutineExercise(exerciseName = "Overhead Press", targetSets = 3, targetReps = 10, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 2),
                                RoutineExercise(exerciseName = "Pull Ups", targetSets = 3, targetReps = 10, targetRepsRange = "8-12", defaultWeight = 0f, restSeconds = 90, orderIndex = 3)
                            )
                            name.contains("lower") -> listOf(
                                RoutineExercise(exerciseName = "Barbell Squat", targetSets = 4, targetReps = 8, targetRepsRange = "6-8", defaultWeight = 0f, restSeconds = 120, orderIndex = 0),
                                RoutineExercise(exerciseName = "Romanian Deadlift", targetSets = 3, targetReps = 10, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                                RoutineExercise(exerciseName = "Walking Lunges", targetSets = 3, targetReps = 12, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 60, orderIndex = 2),
                                RoutineExercise(exerciseName = "Hamstring Curls", targetSets = 3, targetReps = 12, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 60, orderIndex = 3)
                            )
                            else -> listOf(
                                RoutineExercise(exerciseName = "Primary Compound", targetSets = 4, targetReps = 8, targetRepsRange = "8-10", defaultWeight = 0f, restSeconds = 90, orderIndex = 0),
                                RoutineExercise(exerciseName = "Secondary Compound", targetSets = 3, targetReps = 10, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 90, orderIndex = 1),
                                RoutineExercise(exerciseName = "Isolation Accessory", targetSets = 3, targetReps = 12, targetRepsRange = "10-12", defaultWeight = 0f, restSeconds = 60, orderIndex = 2)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = todayRoutine?.name ?: todaySchedule?.routineName ?: "Scheduled Workout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = todayRoutine?.description?.ifBlank { "Planned from your weekly split schedule." }
                            ?: "${exerciseList.size} exercises targeted for hypertrophy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                var isExpanded by remember { mutableStateOf(false) }
                val displayedExercises = if (isExpanded) exerciseList else exerciseList.take(3)

                // ── Rich Planned Movements Glimpse ──
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (exerciseList.size > 3) {
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                                isExpanded = !isExpanded
                                            }
                                    } else Modifier
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.home_planned_movements_format, exerciseList.size),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            if (exerciseList.size > 3) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (isExpanded) {
                                            stringResource(R.string.home_show_less)
                                        } else {
                                            stringResource(R.string.home_more_exercises_format, exerciseList.size - 3)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) MaximusIcons.Home.ExpandUp else MaximusIcons.Home.ExpandDown,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        displayedExercises.forEachIndexed { index, ex ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ExerciseIcon(name = ex.exerciseName, size = 26.dp)
                                    Text(
                                        text = ex.exerciseName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${ex.targetSets}×${ex.targetReps}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (index < displayedExercises.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }

                Button(
                    onClick = onStartToday,
                    shape = MaximusShapes.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (accentColor.luminance() > 0.45f) Color.Black else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(MaximusIcons.Home.StartWorkout, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.home_start_today_workout), fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PRItemCard(pr: com.minimize.maximus.ui.screens.home.PersonalRecord, weightUnit: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                ExerciseIcon(name = pr.exerciseName, size = 42.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = pr.exerciseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.home_pr_best_format, pr.bestWeight.toInt(), weightUnit, pr.bestReps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(R.string.home_est_1rm),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${pr.estimatedOneRepMax.toInt()} $weightUnit",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutDetailsCard(date: LocalDate, summary: WorkoutSummary) {
    val currentAccent = LocalAccentColor.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_summary_title_format, date.dayOfMonth, date.month.name.take(3)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.home_summary_sets_format, summary.sets),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = summary.volume,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = currentAccent
            )
        }
    }
}