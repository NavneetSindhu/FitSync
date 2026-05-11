package com.example.fitsync.ui.screens.home

import android.widget.Toast
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
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

// ── IMPORT GLOBAL PREFERENCES ──────────────────────────────────────────────
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards
import kotlinx.coroutines.launch
import java.time.LocalDate

private val FLOATING_NAV_HEIGHT = 104.dp

// ... (imports remain the same)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStartWorkout: (String) -> Unit, // ── 1. ADD NAVIGATION CALLBACK ──
    viewModel: DailyLogViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userName by homeViewModel.userName.collectAsState()
    val workoutHistory by homeViewModel.workoutHistory.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var showCreateWorkoutSheet by remember { mutableStateOf(false) }

    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    LaunchedEffect(Unit) {
        homeViewModel.refreshUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FitSync",
                            fontWeight = FontWeight.ExtraBold,
                            color = currentAccent,
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
                    containerColor = currentAccent,
                    // 🔥 SMART TEXT COLOR: Flips to black if the accent is too bright
                    contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = FLOATING_NAV_HEIGHT),
                    icon = { Icon(if (targetPage == 0) Icons.Default.PlayArrow else Icons.Default.Add, null) },
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
                onTabSelected = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                },
                accentColor = currentAccent
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = false // Pager strictly controlled by buttons
            ) { page ->
                when (page) {
                    0 -> StatsTabContent(userName = userName, workoutMap = workoutHistory)
                    1 -> Column(modifier = Modifier.fillMaxSize()) {
                        // 2. EDITABLE HEADER FOR THE TODAY TAB
                        WorkoutNameHeader(
                            workoutName = uiState.workoutName,
                            onNameChange = { newName -> viewModel.startWorkoutSession(newName) },
                            accentColor = currentAccent
                        )

                        LoggingScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onFinishWorkout = {
                                viewModel.saveWorkout()
                                Toast.makeText(context, "Workout Saved to History! 💪", Toast.LENGTH_SHORT).show()

                            }
                        )
                    }
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
                onStartWorkout = { finalName, isCustom ->
                    // ── 2. LOGIC SYNC ──
                    // Update the session name in the ViewModel
                    onStartWorkout(finalName)
                    showCreateWorkoutSheet = false
                    // Swipe the pager to the "Today" logging tab
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                }
            )
        }
    }
}


@Composable
fun WorkoutNameHeader(
    workoutName: String,
    onNameChange: (String) -> Unit,
    accentColor: Color
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(workoutName) { mutableStateOf(workoutName) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isEditing) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        onNameChange(textValue)
                        isEditing = false
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = accentColor)
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { isEditing = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workoutName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    modifier = Modifier.size(16.dp),
                    tint = accentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
@Composable
fun PillTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit, accentColor: Color) {
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
                    targetValue = if (isSelected) accentColor else Color.Transparent, // Uses dynamic accent
                    label = "TabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (accentColor.luminance() > 0.4f) Color.Black else Color.White
                    } else MaterialTheme.colorScheme.onSurfaceVariant,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsTabContent(userName: String, workoutMap: Map<LocalDate, WorkoutSummary>) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Read global settings
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 120.dp
        )
    ) {
        // --- 1. THE HEADER & HERO STATS (The Progress Grid) ---
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Hello, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = currentAccent // Dynamic Accent
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
                        color = currentAccent, // Uses dynamic accent
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        label = "Total Sets",
                        value = "$allTimeSets",
                        color = Color(0xFF4CAF50), // Green (Kept distinct for visual variety)
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
                        value = "3 Days",
                        color = Color(0xFFFF9800), // Orange
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 2. CONSISTENCY HEATMAP (The Calendar) ---
        item {
            Spacer(Modifier.height(if (isCompact) 24.dp else 32.dp))
            SectionHeader(title = "Consistency")
            Spacer(Modifier.height(if (isCompact) 8.dp else 16.dp))

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
                                if (!clickedDate.isAfter(LocalDate.now())) {
                                    selectedDate = clickedDate
                                }
                            },
                            onMonthChanged = { }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Text(
                            text = if (selectedDate == LocalDate.now()) "Showing: Today"
                            else "Showing: ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = currentAccent, // Dynamic Accent
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
            Spacer(Modifier.height(if (isCompact) 24.dp else 32.dp))
            SectionHeader(title = "Trends & Analytics")
            Spacer(Modifier.height(if (isCompact) 8.dp else 16.dp))

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
            Spacer(Modifier.height(if (isCompact) 24.dp else 32.dp))
            SectionHeader(title = "Personal Records")
            Spacer(Modifier.height(if (isCompact) 8.dp else 16.dp))

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
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 140.dp else 180.dp), // Height responds to compact setting
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isCompact) 36.dp else 48.dp),
                    tint = currentAccent.copy(alpha = 0.5f) // Uses dynamic accent color
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
                val currentAccent = LocalAccentColor.current
                Text(text = weight, fontWeight = FontWeight.Black, color = currentAccent)
                Text(text = reps, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WorkoutDetailsCard(date: LocalDate, summary: WorkoutSummary) {
    val currentAccent = LocalAccentColor.current

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
                color = currentAccent // Uses dynamic accent
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