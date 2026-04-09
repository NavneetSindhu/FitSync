package com.example.fitsync.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitsync.domain.model.WorkoutSummary
import com.example.fitsync.ui.components.MiniStatCard
import com.example.fitsync.ui.screens.home.heatmap.ProductionCalendar
import com.example.fitsync.ui.screens.home.heatmap.StreakCard
import com.example.fitsync.ui.screens.log.AddExerciseBottomSheet
import com.example.fitsync.ui.screens.log.CreateWorkoutBottomSheet
import com.example.fitsync.ui.screens.log.DailyLogViewModel
import com.example.fitsync.ui.screens.log.LoggingScreen
import com.example.fitsync.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by homeViewModel.userName.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var showCreateWorkoutSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FitSync",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp
                        )
                        Text(
                            text = uiState.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, "History", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = pagerState.currentPage,
                transitionSpec = {
                    scaleIn(tween(200)) togetherWith scaleOut(tween(200))
                },
                label = "FAB_Animation"
            ) { targetPage ->
                ExtendedFloatingActionButton(
                    onClick = {
                        if (targetPage == 0) showCreateWorkoutSheet = true
                        else showAddExerciseSheet = true
                    },
                    containerColor = AccentRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp),
                    icon = {
                        Icon(
                            imageVector = if (targetPage == 0) Icons.Default.PlayArrow else Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(if (targetPage == 0) "Start Workout" else "Add Exercise", fontWeight = FontWeight.Bold)
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PillTabRow(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> StatsTabContent(userName = userName)
                    1 -> LoggingScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        onFinishWorkout = { }
                    )
                }
            }
        }

        if (showAddExerciseSheet) {
            AddExerciseBottomSheet(
                onDismiss = { showAddExerciseSheet = false },
                onAddExercise = { name ->
                    viewModel.addExercise(name)
                    showAddExerciseSheet = false
                }
            )
        }

        if (showCreateWorkoutSheet) {
            CreateWorkoutBottomSheet(
                onDismiss = { showCreateWorkoutSheet = false },
                onStartWorkout = { presetName, isCustom ->
                    showCreateWorkoutSheet = false
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                }
            )
        }
    }
}

@Composable
fun PillTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Stats", "Today")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val bgColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "TabBg"
                )
                val textColor by animateColorAsState(
                    if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "TabTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun StatsTabContent(userName: String) {
    // 1. State Hoisting (Normally this lives in the ViewModel)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Mock Data for the Map
    val mockWorkoutMap = remember {
        val map = mutableMapOf<LocalDate, WorkoutSummary>()
        map[LocalDate.now().minusDays(1)] = WorkoutSummary(3, "14.2k", 48)
        map[LocalDate.now().minusDays(3)] = WorkoutSummary(1, "8.1k", 24)
        map
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Hello, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard("Volume", "12.4k", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    MiniStatCard("Sets", "42", SuccessGreen, Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            StreakCard(streakCount = 5)
        }

        item {
            ProductionCalendar(
                selectedDate = selectedDate,
                workoutMap = mockWorkoutMap,
                onDateSelected = { selectedDate = it },
                onMonthChanged = { newMonth ->
                    // Trigger ViewModel to fetch new month data here
                }
            )
        }

        // 2. Dynamic Details Card below the calendar
        item {
            val summary = mockWorkoutMap[selectedDate]
            val isFuture = selectedDate.isAfter(LocalDate.now())

            AnimatedContent(
                targetState = Triple(summary != null, isFuture, selectedDate),
                label = "DetailsCardAnimation"
            ) { (hasData, future, date) ->
                when {
                    hasData -> {
                        WorkoutDetailsCard(date = date, summary = summary!!)
                    }
                    future -> {
                        FutureDateCard(date = date)
                    }
                    else -> {
                        EmptyPastWorkoutCard(
                            date = date,
                            onLogClick = {
                                // Later: Open logging sheet with this date passed in
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutDetailsCard(date: LocalDate, summary: WorkoutSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Workout on ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Volume: ${summary.volume}", style = MaterialTheme.typography.bodyMedium)
                Text("Sets: ${summary.sets}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ... existing imports ...

@Composable
fun EmptyPastWorkoutCard(date: LocalDate, onLogClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rest Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No workout logged on ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onLogClick) {
                Text("Log Retroactively")
            }
        }
    }
}

@Composable
fun FutureDateCard(date: LocalDate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📅",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Future Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
