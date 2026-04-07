package com.example.fitsync.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.fitsync.ui.components.EmptyWorkoutState
import com.example.fitsync.ui.components.ExerciseCarouselItem
import com.example.fitsync.ui.components.ExerciseLogCard
import com.example.fitsync.ui.components.MiniStatCard
import com.example.fitsync.ui.screens.log.AddExerciseBottomSheet
import com.example.fitsync.ui.screens.log.CreateWorkoutBottomSheet
import com.example.fitsync.ui.screens.log.DailyLogUiState
import com.example.fitsync.ui.screens.log.DailyLogViewModel
import com.example.fitsync.ui.screens.log.TodayTabContent
import com.example.fitsync.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSyncClick: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by homeViewModel.userName.collectAsState()

    // Pager State (0 = Stats, 1 = Today) - Start on Today (1)
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 1)
    val coroutineScope = rememberCoroutineScope()

    // Bottom Sheet States
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
                            fontSize = 20.sp
                        )
                        Text(
                            text = uiState.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            // Animated FAB that swaps based on the current page
            AnimatedContent(
                targetState = pagerState.currentPage,
                transitionSpec = {
                    scaleIn(tween(200)) togetherWith scaleOut(tween(200))
                },
                label = "FAB_Animation"
            ) { targetPage ->
                FloatingActionButton(
                    onClick = {
                        if (targetPage == 0) showCreateWorkoutSheet = true
                        else showAddExerciseSheet = true
                    },
                    containerColor = AccentRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = if (targetPage == 0) Icons.Default.PlayArrow else Icons.Default.Add,
                        contentDescription = if (targetPage == 0) "Start Workout" else "Add Exercise"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Custom Pill Segmented Control
            PillTabRow(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )

            // Smooth Sliding Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> StatsTabContent(userName = userName)
                    1 -> TodayTabContent(viewModel = viewModel, uiState = uiState)
                }
            }
        }

        // --- BOTTOM SHEETS ---
        if (showAddExerciseSheet) {
            AddExerciseBottomSheet(
                onDismiss = { showAddExerciseSheet = false },
                onAddExercise = { name -> viewModel.addExercise(name) }
            )
        }

        if (showCreateWorkoutSheet) {
            CreateWorkoutBottomSheet(
                onDismiss = { showCreateWorkoutSheet = false },
                onStartWorkout = { presetName, isCustom ->
                    // Handle passing to ViewModel here
                    showCreateWorkoutSheet = false
                    coroutineScope.launch { pagerState.animateScrollToPage(1) } // Slide to "Today" tab
                }
            )
        }
    }
}

// ----------------------------------------------------
// UI COMPONENTS
// ----------------------------------------------------

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
                    MiniStatCard("Streak", "5 Days", SuccessGreen, Modifier.weight(1f))
                }

                Spacer(Modifier.height(40.dp))

                // Placeholder for future graphs
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Activity Heatmap Coming Soon", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
