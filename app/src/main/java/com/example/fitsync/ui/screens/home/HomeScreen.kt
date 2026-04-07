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

//@Composable
//fun TodayTabContent(
//    viewModel: DailyLogViewModel,
//    uiState: DailyLogUiState // Assuming this is the class name
//) {
//    var selectedExerciseIndex by remember { mutableIntStateOf(0) }
//    val weightUnit = "kg"
//
//    LazyColumn(
//        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(bottom = 120.dp)
//    ) {
//        if (uiState.exercises.isNotEmpty()) {
//            item {
//                Text(
//                    "Current Session",
//                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp),
//                    style = MaterialTheme.typography.labelLarge,
//                    color = MaterialTheme.colorScheme.primary,
//                    fontWeight = FontWeight.Bold
//                )
//                LazyRow(
//                    modifier = Modifier.fillMaxWidth(),
//                    contentPadding = PaddingValues(horizontal = 16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    itemsIndexed(uiState.exercises) { index, exercise ->
//                        ExerciseCarouselItem(
//                            name = exercise.name,
//                            isSelected = selectedExerciseIndex == index,
//                            onClick = { selectedExerciseIndex = index }
//                        )
//                    }
//                }
//            }
//
//            item {
//                val currentExercise = uiState.exercises.getOrNull(selectedExerciseIndex)
//                if (currentExercise != null) {
//                    val meta = ExerciseVisuals.getMetaData(currentExercise.name)
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        ExerciseLogCard(
//                            exerciseName = currentExercise.name,
//                            sets = currentExercise.sets,
//                            unit = weightUnit,
//                            accentColor = meta.accentColor,
//                            exerciseIcon = meta.icon,
//                            onAddSet = { viewModel.addSet(currentExercise.name) },
//                            onUpdateSet = { s, w, r -> viewModel.updateSet(currentExercise.name, s, w, r) },
//                            onToggleSet = { s -> viewModel.toggleSetCompletion(currentExercise.name, s) },
//                            onDeleteSet = { s -> viewModel.deleteSet(currentExercise.name, s) },
//                            onDeleteExercise = { viewModel.removeExercise(currentExercise.name) }
//                        )
//                        Spacer(Modifier.height(24.dp))
//                        Button(
//                            onClick = { viewModel.saveWorkout() },
//                            modifier = Modifier.fillMaxWidth().height(52.dp),
//                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Text("Finish Workout", fontWeight = FontWeight.Bold)
//                        }
//                    }
//                }
//            }
//        } else {
//            item { EmptyWorkoutState() }
//        }
//    }
//}

// ----------------------------------------------------
// BOTTOM SHEETS
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseBottomSheet(
    onDismiss: () -> Unit,
    onAddExercise: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val availableExercises = listOf(
        "Barbell Squat", "Bench Press", "Deadlift", "Overhead Press",
        "Pull Ups", "Barbell Row", "Dips"
    ).filter { it.contains(searchQuery, ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text("Add Exercise", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(availableExercises) { name ->
                    ListItem(
                        headlineContent = { Text(name, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.clickable {
                            onAddExercise(name)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutBottomSheet(
    onDismiss: () -> Unit,
    onStartWorkout: (String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var expanded by remember { mutableStateOf(false) }
    val presetOptions = listOf("Push Day", "Pull Day", "Leg Day", "Cardio", "Custom")
    var selectedOption by remember { mutableStateOf(presetOptions[0]) }
    var customName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Start a Session", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Split") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    presetOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedOption = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (selectedOption == "Custom") {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Workout Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    val finalName = if (selectedOption == "Custom") customName.ifBlank { "Custom" } else selectedOption
                    onStartWorkout(finalName, selectedOption == "Custom")
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("Start Workout", fontWeight = FontWeight.Bold)
            }
        }
    }
}