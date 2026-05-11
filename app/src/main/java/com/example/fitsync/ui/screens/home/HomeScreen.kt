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

private val FLOATING_NAV_HEIGHT = 104.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- REAL DATA PIPELINE ---
    val userName by homeViewModel.userName.collectAsState()
    val workoutHistory by homeViewModel.workoutHistory.collectAsState()

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
                        Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = pagerState.currentPage,
                transitionSpec = { scaleIn(tween(200)) togetherWith scaleOut(tween(200)) },
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
                    modifier = Modifier.padding(bottom = FLOATING_NAV_HEIGHT),
                    icon = { Icon(if (targetPage == 0) Icons.Default.PlayArrow else Icons.Default.Add, contentDescription = null) },
                    text = { Text(if (targetPage == 0) "Start Workout" else "Add Exercise", fontWeight = FontWeight.Bold) }
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
                onTabSelected = { index -> coroutineScope.launch { pagerState.animateScrollToPage(index) } }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    // Pass the real data map down to the Stats Tab!
                    0 -> StatsTabContent(userName = userName, workoutMap = workoutHistory)
                    1 -> LoggingScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        onFinishWorkout = {}
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
                onStartWorkout = { _, _ ->
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
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "TabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(text = title, color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
// --- REPLACEMENT FOR StatsTabContent IN HomeScreen.kt ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsTabContent(userName: String, workoutMap: Map<LocalDate, WorkoutSummary>) {
    // Restrict selection to today or earlier
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 120.dp // Floating Nav clearance
        )
    ) {
        // --- 1. THE HEADER & HERO STATS (The Progress Grid) ---
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Hello, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                val allTimeVolume = workoutMap.values.sumOf { it.volume.replace(" kg", "").toDoubleOrNull() ?: 0.0 }
                val allTimeSets = workoutMap.values.sumOf { it.sets }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(
                        label = "Total Volume",
                        value = "${allTimeVolume.toInt()}kg",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        label = "Total Sets",
                        value = "$allTimeSets",
                        color = Color(0xFF4CAF50), // Green
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(
                        label = "Total Workouts",
                        value = "${workoutMap.size}",
                        color = Color(0xFF9C27B0), // Purple
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        label = "Current Streak",
                        value = "3 Days", // Replace with real streak logic later
                        color = Color(0xFFFF9800), // Orange
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 2. CONSISTENCY HEATMAP (The Calendar) ---
        item {
            Spacer(Modifier.height(32.dp))
            SectionHeader(title = "Consistency")
            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        ProductionCalendar(
                            selectedDate = selectedDate,
                            workoutMap = workoutMap,
                            onDateSelected = { clickedDate ->
                                // Restrict selection to today or earlier
                                if (!clickedDate.isAfter(LocalDate.now())) {
                                    selectedDate = clickedDate
                                }
                            },
                            onMonthChanged = { }
                        )

                        // Small indicator of what is currently selected
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Text(
                            text = if (selectedDate == LocalDate.now()) "Showing: Today"
                            else "Showing: ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().capitalize()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // --- 3. SELECTED DAY DETAILS (Grouped with Calendar) ---
        item {
            Spacer(Modifier.height(12.dp))
            val summary = workoutMap[selectedDate]

            AnimatedContent(
                targetState = summary != null,
                label = "DetailsCardAnimation"
            ) { hasData ->
                if (hasData) {
                    WorkoutDetailsCard(date = selectedDate, summary = summary!!)
                } else {
                    EmptyPastWorkoutCard(
                        date = selectedDate,
                        onLogClick = { /* Logic for retroactive logging */ }
                    )
                }
            }
        }

        // --- 4. TRENDS & ANALYTICS ---
        item {
            Spacer(Modifier.height(32.dp))
            SectionHeader(title = "Trends & Analytics")
            Spacer(Modifier.height(16.dp))

            val pagerState = rememberPagerState(pageCount = { 2 })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 16.dp
            ) { page ->
                when (page) {
                    0 -> GraphPlaceholderCard(title = "Volume Trend", icon = Icons.Default.Timeline)
                    1 -> GraphPlaceholderCard(title = "Muscle Split", icon = Icons.Default.PieChart)
                }
            }
        }

        // --- 5. PERSONAL RECORDS (PRs) ---
        item {
            Spacer(Modifier.height(32.dp))
            SectionHeader(title = "Personal Records")
            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                PRPlaceholderCard(exercise = "Barbell Bench Press", weight = "100 kg", reps = "5 reps")
                Spacer(Modifier.height(8.dp))
                PRPlaceholderCard(exercise = "Squat", weight = "140 kg", reps = "3 reps")
            }
        }
    }
}

// ─── HELPER UI COMPONENTS ──────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
fun GraphPlaceholderCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "$title Chart",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "(Coming Soon via Vico or MPAndroidChart)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun PRPlaceholderCard(exercise: String, weight: String, reps: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD700).copy(alpha = 0.2f), // Gold tint
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents, // Trophy icon
                        contentDescription = "PR",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = exercise, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "New 1RM Estimate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = weight, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(text = reps, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WorkoutDetailsCard(date: LocalDate, summary: WorkoutSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
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
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Volume: ${summary.volume}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Sets: ${summary.sets}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EmptyPastWorkoutCard(date: LocalDate, onLogClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Rest Day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No workout logged on ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FutureDateCard(date: LocalDate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📅", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Future Date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}