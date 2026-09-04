package com.minimize.maximus.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.domain.model.Exercise
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.ui.components.EmptyStateView
import com.minimize.maximus.ui.components.MaximusConfirmDialog
import com.minimize.maximus.ui.components.HistoryListSkeletonView
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.components.ToastType
import com.minimize.maximus.ui.screens.history.components.ExerciseDetailBottomSheet
import com.minimize.maximus.ui.screens.history.components.HistoryBackupBottomSheet
import com.minimize.maximus.ui.screens.history.components.HistoryFilterBottomSheet
import com.minimize.maximus.ui.screens.history.components.HistoryWorkoutCard
import com.minimize.maximus.ui.screens.history.components.WorkoutSessionDetailCarouselSheet
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.LocalCompactCards
import com.minimize.maximus.util.DateUtils
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import java.text.SimpleDateFormat
import java.util.*

private enum class HistoryScreenState {
    LOADING,
    EMPTY,
    POPULATED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onEditWorkout: (WorkoutSession) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workouts = uiState.workouts
    val weightUnit = uiState.weightUnit

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedSplit by viewModel.selectedSplit.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    val listState = rememberLazyListState()

    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current
    val toastManager = LocalMaximusToast.current
    val hapticFeedback = LocalHapticFeedback.current

    var showFilterSheet by remember { mutableStateOf(false) }
    var showBackupSheet by remember { mutableStateOf(false) }
    var selectedSessionForDetail by remember { mutableStateOf<WorkoutSession?>(null) }
    var workoutToShare by remember { mutableStateOf<WorkoutSession?>(null) }
    var workoutToDelete by remember { mutableStateOf<WorkoutSession?>(null) }
    var selectedExerciseDetails by remember { mutableStateOf<Pair<String, List<Pair<Long, Exercise>>>?>(null) }

    val activeFilterCount = (if (selectedMonth != "All") 1 else 0) + (if (selectedSplit != "All") 1 else 0)

    val availableMonths = remember(workouts) {
        listOf("All") + workouts.map { DateUtils.formatEpoch(it.date, "MMMM yyyy") }.distinct()
    }

    // Automatically scroll to top whenever filters or search change
    LaunchedEffect(activeFilterCount, searchQuery, selectedMonth, selectedSplit, selectedSort) {
        listState.animateScrollToItem(0)
    }

    // Filter and sort workouts dynamically
    val filteredWorkouts = remember(workouts, searchQuery, selectedMonth, selectedSplit, selectedSort) {
        workouts.filter { session ->
            val matchesSearch = searchQuery.isBlank() ||
                    session.name.contains(searchQuery, ignoreCase = true) ||
                    session.exercise.any { it.name.contains(searchQuery, ignoreCase = true) }

            val sessionMonth = DateUtils.formatEpoch(session.date, "MMMM yyyy")
            val matchesMonth = selectedMonth == "All" || sessionMonth == selectedMonth

            val matchesSplit = selectedSplit == "All" ||
                    session.exercise.any { ex ->
                        com.minimize.maximus.util.MuscleClassifier.getPrimaryMuscles(ex.name)
                            .any { it.displayName.equals(selectedSplit, ignoreCase = true) }
                    }

            matchesSearch && matchesMonth && matchesSplit
        }.let { list ->
            when (selectedSort) {
                WorkoutSortOption.NEWEST -> list.sortedByDescending { it.date }
                WorkoutSortOption.OLDEST -> list.sortedBy { it.date }
                WorkoutSortOption.HIGHEST_VOLUME -> list.sortedByDescending { it.exercise.sumOf { ex -> ex.sets.sumOf { s -> (s.weight * s.reps).toDouble() } } }
                WorkoutSortOption.MOST_EXERCISES -> list.sortedByDescending { it.exercise.size }
            }
        }
    }

    // Group workouts by relative date headers (Today, Yesterday, This Week, Earlier)
    val groupedWorkouts = remember(filteredWorkouts) {
        filteredWorkouts.groupBy { session ->
            DateUtils.getRelativeDateHeader(session.date)
        }
    }

    val openExerciseGraph: (String) -> Unit = { exerciseName ->
        val historyWithDates = workouts
            .filter { session -> session.exercise.any { it.name == exerciseName } }
            .sortedBy { it.date }
            .map { session ->
                Pair(session.date, session.exercise.first { it.name == exerciseName })
            }
        selectedExerciseDetails = Pair(exerciseName, historyWithDates)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(MaximusIcons.Navigation.BackArrow, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    // Backup & Export Button
                    IconButton(onClick = { showBackupSheet = true }) {
                        Icon(
                            MaximusIcons.History.CloudBackup,
                            contentDescription = "Backup & Export",
                            tint = currentAccent
                        )
                    }

                    // Filter Sheet Button with Active Count Badge
                    IconButton(onClick = { showFilterSheet = true }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                MaximusIcons.Stats.Filter,
                                contentDescription = "Filter",
                                tint = currentAccent
                            )
                            if (activeFilterCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .background(currentAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeFilterCount.toString(),
                                        color = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Search Bar ──
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search by workout or exercise...") },
                leadingIcon = { Icon(MaximusIcons.Navigation.Search, null, tint = currentAccent) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(MaximusIcons.Navigation.Close, "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentAccent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // ── Athletic Summary Metrics Bar (Meaningful Sets & Frequency) ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = MaximusShapes.Card,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WORKING SETS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.totalSetsCount} Sets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = currentAccent
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SESSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${filteredWorkouts.size} logged",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "MILESTONES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.totalPRsCount} PRs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }
            }

            // ── Workouts List with Animated Content ──
            val forceShimmer = com.minimize.maximus.ui.theme.LocalDebugShimmer.current
            val isDataLoading = uiState.isLoading || forceShimmer
            val screenState = remember(isDataLoading, filteredWorkouts.isEmpty()) {
                when {
                    isDataLoading -> HistoryScreenState.LOADING
                    filteredWorkouts.isEmpty() -> HistoryScreenState.EMPTY
                    else -> HistoryScreenState.POPULATED
                }
            }

            AnimatedContent(
                targetState = screenState,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "HistoryListTransition",
                modifier = Modifier.weight(1f)
            ) { state ->
                when (state) {
                    HistoryScreenState.LOADING -> {
                        HistoryListSkeletonView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    HistoryScreenState.EMPTY -> {
                        EmptyStateView(
                            title = if (searchQuery.isNotBlank() || activeFilterCount > 0) "No Matching Workouts" else "No Workouts Logged",
                            subtitle = if (searchQuery.isNotBlank() || activeFilterCount > 0) "Try clearing filters or adjusting your search query." else "Complete and save your workouts to see your full progression history here.",
                            ctaText = if (activeFilterCount > 0) "Reset Filters" else "Start First Workout",
                            onCtaClick = {
                                if (activeFilterCount > 0) viewModel.resetFilters() else onBackClick()
                            },
                            lottieRes = com.minimize.maximus.R.raw.anim_cat_relaxing,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = MaximusDimens.FloatingNavHeight + 16.dp)
                        )
                    }

                    HistoryScreenState.POPULATED -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = MaximusDimens.FloatingNavHeight + 24.dp)
                        ) {
                            groupedWorkouts.forEach { (dateGroup, sessions) ->
                                item(key = "header_$dateGroup") {
                                    Text(
                                        text = dateGroup,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            horizontal = 20.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }

                                items(items = sessions, key = { it.id }) { workout ->
                                    Box(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        HistoryWorkoutCard(
                                            workout = workout,
                                            unit = weightUnit,
                                            onClick = { selectedSessionForDetail = workout },
                                            onExerciseClick = { openExerciseGraph(it) },
                                            onEdit = { onEditWorkout(workout) },
                                            onDelete = { workoutToDelete = workout }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Workout Session Detail Multi-Exercise Carousel Sheet ──
        selectedSessionForDetail?.let { session ->
            WorkoutSessionDetailCarouselSheet(
                workout = session,
                allWorkouts = workouts,
                unit = weightUnit,
                onEditWorkout = {
                    selectedSessionForDetail = null
                    onEditWorkout(session)
                },
                onShareWorkout = {
                    selectedSessionForDetail = null
                    workoutToShare = session
                },
                onDismissRequest = { selectedSessionForDetail = null }
            )
        }

        // ── Workout Social Share Bottom Sheet ──
        workoutToShare?.let { session ->
            com.minimize.maximus.ui.components.share.WorkoutShareBottomSheet(
                workout = session,
                userName = viewModel.userName,
                avatarId = viewModel.userAvatar,
                unit = weightUnit,
                onDismissRequest = { workoutToShare = null }
            )
        }

        // ── UniSwap Pattern Filter Bottom Sheet ──
        if (showFilterSheet) {
            HistoryFilterBottomSheet(
                selectedMonth = selectedMonth,
                availableMonths = availableMonths,
                onMonthSelected = { viewModel.setSelectedMonth(it) },
                selectedSplit = selectedSplit,
                onSplitSelected = { viewModel.setSelectedSplit(it) },
                selectedSort = selectedSort,
                onSortSelected = { viewModel.setSelectedSort(it) },
                onResetAll = { viewModel.resetFilters() },
                activeFilterCount = activeFilterCount,
                onDismissRequest = { showFilterSheet = false }
            )
        }

        // ── Backup & Export Bottom Sheet ──
        if (showBackupSheet) {
            HistoryBackupBottomSheet(
                workouts = workouts,
                onRestoreWorkouts = { viewModel.restoreWorkouts(it) },
                onDismissRequest = { showBackupSheet = false }
            )
        }

        // ── Exercise Progression Details Bottom Sheet ──
        selectedExerciseDetails?.let { (exerciseName, historyWithDates) ->
            ExerciseDetailBottomSheet(
                exerciseName = exerciseName,
                historyWithDates = historyWithDates,
                weightUnit = weightUnit,
                onDismiss = { selectedExerciseDetails = null }
            )
        }

        // ── Centralized Delete Workout Confirmation Dialog ──
        workoutToDelete?.let { session ->
            MaximusConfirmDialog(
                title = "Delete Workout?",
                message = "Are you sure you want to delete \"${session.name}\"? This session's volume and history records will be permanently removed.",
                confirmText = "Delete Workout",
                dismissText = "Cancel",
                icon = Icons.Default.DeleteOutline,
                isDestructive = true,
                onConfirm = {
                    MaximusHapticUtils.perform(
                        hapticFeedback,
                        MaximusEvent.SWIPE_REVEAL_THRESHOLD
                    )
                    viewModel.deleteWorkout(session)
                    toastManager.showToast("Workout deleted from history", ToastType.INFO)
                    workoutToDelete = null
                },
                onDismiss = { workoutToDelete = null }
            )
        }
    }
}