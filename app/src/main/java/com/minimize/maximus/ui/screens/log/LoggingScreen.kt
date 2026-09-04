package com.minimize.maximus.ui.screens.log

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.components.ExerciseCarouselItem
import com.minimize.maximus.ui.components.MaximusConfirmDialog
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.components.ToastType
import com.minimize.maximus.ui.screens.log.components.WorkoutNameHeader
import com.minimize.maximus.ui.screens.log.components.AddExerciseBottomSheet
import com.minimize.maximus.ui.screens.log.components.EmptyWorkoutState
import com.minimize.maximus.ui.screens.log.components.ExerciseLogCard
import com.minimize.maximus.ui.screens.log.components.PlateCalculatorBottomSheet
import com.minimize.maximus.ui.screens.log.components.RestTimerBar
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.LocalCompactCards
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import kotlinx.coroutines.launch

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoggingScreen(
    viewModel: DailyLogViewModel,
    uiState: DailyLogUiState,
    onBackClick: () -> Unit = {},
    onFinishWorkout: () -> Unit
) {
    val weightUnit by viewModel.weightUnit.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current
    val hapticFeedback = LocalHapticFeedback.current
    val toastManager = LocalMaximusToast.current

    BackHandler {
        viewModel.minimizeWorkout(onBackClick)
    }

    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var showFinishConfirmation by remember { mutableStateOf(false) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }
    var plateCalcTargetWeight by remember { mutableStateOf<Float?>(null) }

    val elapsedHours = uiState.elapsedWorkoutSeconds / 3600
    val elapsedMins = (uiState.elapsedWorkoutSeconds % 3600) / 60
    val elapsedSecs = uiState.elapsedWorkoutSeconds % 60
    val formattedStopwatch = if (elapsedHours > 0) {
        "%02d:%02d:%02d".format(elapsedHours, elapsedMins, elapsedSecs)
    } else {
        "%02d:%02d".format(elapsedMins, elapsedSecs)
    }

    // Dynamic FAB bottom clearance to avoid colliding with floating RestTimer HUD
    val fabBottomPadding by animateDpAsState(
        targetValue = if (uiState.isRestTimerVisible) 90.dp else 24.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "LogFabPadding"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.minimizeWorkout(onBackClick)
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize Workout",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    WorkoutNameHeader(
                        workoutName = uiState.workoutName,
                        onNameChange = { newName -> viewModel.updateWorkoutName(newName) },
                        accentColor = currentAccent
                    )
                },
                actions = {
                    if (uiState.isEditingExistingWorkout) {
                        Surface(
                            shape = MaximusShapes.Pill,
                            color = currentAccent.copy(alpha = 0.16f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = MaximusIcons.WorkoutLog.EditMode,
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "Editing Mode",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = currentAccent
                                )
                            }
                        }
                    } else {
                        // Stopwatch Pill with Interactive Pause/Resume
                        Surface(
                            onClick = { viewModel.toggleWorkoutTimerPause() },
                            shape = MaximusShapes.Pill,
                            color = if (uiState.isWorkoutTimerRunning) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            } else {
                                Color(0xFFFF9800).copy(alpha = 0.2f)
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isWorkoutTimerRunning) MaximusIcons.WorkoutLog.Timer else MaximusIcons.WorkoutLog.Play,
                                    contentDescription = if (uiState.isWorkoutTimerRunning) "Pause Workout Timer" else "Resume Workout Timer",
                                    tint = if (uiState.isWorkoutTimerRunning) currentAccent else Color(0xFFFF9800),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = if (uiState.isWorkoutTimerRunning) formattedStopwatch else "Paused ($formattedStopwatch)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isWorkoutTimerRunning) MaterialTheme.colorScheme.onSurface else Color(0xFFFF9800)
                                )
                            }
                        }
                    }

                    // Finish / Save Changes Action Button
                    if (uiState.exercises.isNotEmpty()) {
                        Button(
                            onClick = { showFinishConfirmation = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentAccent,
                                contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                            ),
                            shape = MaximusShapes.Pill,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 4.dp).height(36.dp)
                        ) {
                            Text(
                                text = if (uiState.isEditingExistingWorkout) "Save Changes" else stringResource(R.string.log_finish_workout),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Discard Session Icon Button
                        IconButton(
                            onClick = { showDiscardConfirmation = true },
                            modifier = Modifier.size(36.dp).padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = MaximusIcons.WorkoutLog.CloseDiscard,
                                contentDescription = "Discard Session",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExerciseSheet = true },
                containerColor = currentAccent,
                contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White,
                shape = MaximusShapes.Pill,
                modifier = Modifier.padding(bottom = fabBottomPadding),
                icon = { Icon(MaximusIcons.WorkoutLog.AddExercise, null) },
                text = { Text(stringResource(R.string.log_add_exercise), fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.exercises.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { uiState.exercises.size })
                val carouselState = rememberLazyListState()

                LaunchedEffect(pagerState.currentPage) {
                    carouselState.animateScrollToItem(maxOf(0, pagerState.currentPage - 1))
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // ── Carousel Tabs ──
                    LazyRow(
                        state = carouselState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.exercises) { index, exercise ->
                            val isDone = exercise.sets.isNotEmpty() && exercise.sets.all { it.isCompleted }

                            Box {
                                ExerciseCarouselItem(
                                    name = exercise.name,
                                    isSelected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                )

                                if (isDone) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Exercise Swipeable Pager ──
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { pageIndex ->
                        val exercise = uiState.exercises.getOrNull(pageIndex)
                        if (exercise != null) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                item {
                                    ExerciseLogCard(
                                        exerciseName = exercise.name,
                                        sets = exercise.sets,
                                        unit = weightUnit,
                                        accentColor = currentAccent,
                                        onAddSet = {
                                            MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                                            viewModel.addSet(exercise.name)
                                        },
                                        onUpdateSet = { setNum, weight, reps ->
                                            viewModel.updateSet(exercise.name, setNum, weight, reps)
                                        },
                                        onUpdateSetType = { setNum, newType ->
                                            MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.FILTER_SWITCH)
                                            viewModel.updateSetType(exercise.name, setNum, newType)
                                        },
                                        onToggleSet = { setNum ->
                                            MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SET_COMPLETED)
                                            viewModel.toggleSetComplete(exercise.name, setNum)
                                        },
                                        onDeleteSet = { setNum ->
                                            MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SWIPE_REVEAL_THRESHOLD)
                                            viewModel.deleteSet(exercise.name, setNum)
                                        },
                                        onDeleteExercise = {
                                            MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SWIPE_REVEAL_THRESHOLD)
                                            viewModel.deleteExercise(exercise.name)
                                            toastManager.showToast("${exercise.name} removed", ToastType.INFO)
                                        },
                                        onOpenPlateCalculator = { targetWeight ->
                                            plateCalcTargetWeight = targetWeight
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                EmptyWorkoutState(
                    onAddExerciseClick = { showAddExerciseSheet = true },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── Floating Rest Timer Bar (Anchored Above Screen Bottom) ──
            AnimatedVisibility(
                visible = uiState.isRestTimerVisible,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                RestTimerBar(
                    totalSeconds = uiState.restTimerTotalSeconds,
                    remainingSeconds = uiState.restTimerRemainingSeconds,
                    isRunning = uiState.isRestTimerRunning,
                    onAddSeconds = { delta -> viewModel.addRestSeconds(delta) },
                    onTogglePause = { viewModel.toggleRestTimerPause() },
                    onSkip = { viewModel.dismissRestTimer() }
                )
            }
        }

        // ── Add Exercise Bottom Sheet ──
        if (showAddExerciseSheet) {
            AddExerciseBottomSheet(
                exercises = allExercises,
                onDismiss = { showAddExerciseSheet = false },
                onAddExercise = { exerciseName ->
                    viewModel.addExercise(exerciseName)
                    showAddExerciseSheet = false
                },
                onCreateCustomExercise = { name, muscle, equipment ->
                    viewModel.createCustomExercise(name, muscle, equipment) { createdName ->
                        toastManager.showToast("$createdName added to workout", ToastType.SUCCESS)
                        showAddExerciseSheet = false
                    }
                }
            )
        }

        // ── Standard Left-Aligned Finish Confirmation Dialog ──
        if (showFinishConfirmation) {
            val totalSets = uiState.exercises.sumOf { it.sets.size }
            val completedSets = uiState.exercises.sumOf { it.sets.count { s -> s.isCompleted } }
            val hasIncomplete = completedSets < totalSets

            MaximusConfirmDialog(
                title = if (hasIncomplete) stringResource(R.string.log_incomplete_dialog_title) else stringResource(R.string.log_finish_dialog_title),
                message = if (hasIncomplete) {
                    stringResource(R.string.log_incomplete_dialog_message, completedSets, totalSets)
                } else {
                    stringResource(R.string.log_finish_dialog_message, completedSets)
                },
                confirmText = stringResource(R.string.log_save_workout_btn),
                dismissText = stringResource(R.string.log_keep_training_btn),
                icon = Icons.Default.CheckCircleOutline,
                isDestructive = false,
                onConfirm = {
                    showFinishConfirmation = false
                    MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.PR_RECORD_UNLOCKED)
                    viewModel.finishWorkout {
                        toastManager.showToast("Workout Saved! Great work 💪", ToastType.SUCCESS)
                        onFinishWorkout()
                    }
                },
                onDismiss = { showFinishConfirmation = false }
            )
        }

        // ── Centralized Discard Confirmation Dialog ──
        if (showDiscardConfirmation) {
            MaximusConfirmDialog(
                title = "Discard Workout?",
                message = "All recorded sets and active workout timer progress will be permanently cleared. This cannot be undone.",
                confirmText = "Discard Workout",
                dismissText = "Keep Training",
                icon = Icons.Default.WarningAmber,
                isDestructive = true,
                onConfirm = {
                    showDiscardConfirmation = false
                    viewModel.discardWorkout {
                        toastManager.showToast("Workout session discarded", ToastType.INFO)
                        onFinishWorkout()
                    }
                },
                onDismiss = { showDiscardConfirmation = false }
            )
        }

        // ── Plate Calculator Modal Sheet ──
        plateCalcTargetWeight?.let { weight ->
            PlateCalculatorBottomSheet(
                initialTargetWeight = weight,
                isMetric = true,
                onDismiss = { plateCalcTargetWeight = null }
            )
        }
    }
}