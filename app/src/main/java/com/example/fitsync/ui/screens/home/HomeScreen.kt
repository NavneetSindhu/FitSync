package com.example.fitsync.ui.screens.home

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitsync.ui.components.ExerciseLogCard
import com.example.fitsync.ui.screens.log.DailyLogViewModel
import com.example.fitsync.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val weightUnit = "kg"

    var selectedExerciseIndex by remember { mutableIntStateOf(0) }

    // skipPartiallyExpanded = true ensures it opens to our custom height immediately
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val availableExercises = listOf(
        "Barbell Squat", "Bench Press", "Deadlift", "Overhead Press",
        "Pull Ups", "Barbell Row", "Dips"
    ).filter { it.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
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
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = AccentRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Hello, $userName! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniStatCard("Volume", "12.4k", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        MiniStatCard("Streak", "5 Days", SuccessGreen, Modifier.weight(1f))
                    }
                }
            }

            if (uiState.exercises.isNotEmpty()) {
                item {
                    Text(
                        "Current Session",
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(uiState.exercises) { index, exercise ->
                            ExerciseCarouselItem(
                                name = exercise.name,
                                isSelected = selectedExerciseIndex == index,
                                onClick = { selectedExerciseIndex = index }
                            )
                        }
                    }
                }

                item {
                    val currentExercise = uiState.exercises.getOrNull(selectedExerciseIndex)
                    if (currentExercise != null) {
                        val meta = ExerciseVisuals.getMetaData(currentExercise.name)
                        Column(modifier = Modifier.padding(16.dp)) {
                            ExerciseLogCard(
                                exerciseName = currentExercise.name,
                                sets = currentExercise.sets,
                                unit = weightUnit,
                                accentColor = meta.accentColor,
                                exerciseIcon = meta.icon,
                                onAddSet = { viewModel.addSet(currentExercise.name) },
                                onUpdateSet = { s, w, r -> viewModel.updateSet(currentExercise.name, s, w, r) },
                                onToggleSet = { s -> viewModel.toggleSetCompletion(currentExercise.name, s) },
                                onDeleteSet = { s -> viewModel.deleteSet(currentExercise.name, s) },
                                onDeleteExercise = { viewModel.removeExercise(currentExercise.name) }
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.saveWorkout() },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Finish Workout", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                item { EmptyWorkoutState() }
            }
        }

        // --- FIXED BOTTOM SHEET (70% Height from Bottom) ---
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    searchQuery = ""
                },
                sheetState = sheetState,
                // THE FIX: Remove height modifier here. Let content decide.
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                // Control height STRICTLY inside this Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f) // This creates the 70% look from bottom
                        .padding(horizontal = 20.dp)
                        .navigationBarsPadding()
                ) {
                    Text(
                        "Add Exercise",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

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

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(availableExercises) { name ->
                            val meta = ExerciseVisuals.getMetaData(name)
                            ListItem(
                                headlineContent = { Text(name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(meta.category) },
                                leadingContent = {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        color = meta.accentColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(meta.icon, null, Modifier.padding(10.dp), tint = meta.accentColor)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addExercise(name)
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) showSheet = false
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Support Components ---

@Composable
fun ExerciseCarouselItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val meta = ExerciseVisuals.getMetaData(name)
    val backgroundColor by animateColorAsState(if (isSelected) meta.accentColor else MaterialTheme.colorScheme.surface)
    val textColor by animateColorAsState(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.width(110.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(meta.icon, null, tint = if (isSelected) Color.White else meta.accentColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
        }
    }
}

@Composable
fun MiniStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun EmptyWorkoutState() {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.FitnessCenter, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f))
        Spacer(Modifier.height(16.dp))
        Text("No exercises added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tap the + button to start training!", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}