package com.example.fitsync.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitsync.domain.model.Exercise
import com.example.fitsync.domain.model.WorkoutSession
import com.example.fitsync.ui.components.HistoryWorkoutCard
import com.example.fitsync.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onEditWorkout: (WorkoutSession) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val workouts by viewModel.workoutHistory.collectAsState(initial = emptyList())

    // --- STATE FOR BOTTOM SHEET ---
    var selectedExerciseDetails by remember { mutableStateOf<Pair<String, List<Exercise>>?>(null) }

    var selectedMonth by remember { mutableStateOf("All") }
    var selectedExercise by remember { mutableStateOf("All") }

    val monthFormatter = remember { SimpleDateFormat("MMMM", Locale.getDefault()) }

    val availableMonths = remember(workouts) {
        listOf("All") + workouts.map { monthFormatter.format(Date(it.date)) }.distinct()
    }

    val availableExercises = remember(workouts) {
        listOf("All") + workouts.flatMap { it.exercise.map { ex -> ex.name } }.distinct()
    }

    val filteredWorkouts = remember(workouts, selectedMonth, selectedExercise) {
        workouts.filter { workout ->
            val workoutMonth = monthFormatter.format(Date(workout.date))
            val monthMatch = selectedMonth == "All" || workoutMonth == selectedMonth
            val exerciseMatch = selectedExercise == "All" || workout.exercise.any { it.name == selectedExercise }
            monthMatch && exerciseMatch
        }
    }

    val totalVolume = remember(filteredWorkouts) {
        filteredWorkouts.sumOf { workout ->
            workout.exercise.sumOf { ex ->
                ex.sets.sumOf { (it.weight * it.reps).toDouble() }
            }
        }.toInt()
    }

    // Helper function to fetch and prepare history for the graph
    fun openExerciseGraph(exerciseName: String) {
        val history = workouts
            .filter { session -> session.exercise.any { it.name == exerciseName } }
            .sortedBy { it.date }
            .map { session -> session.exercise.first { it.name == exerciseName } }

        selectedExerciseDetails = Pair(exerciseName, history)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                FilterChipRow(availableMonths, selectedMonth) { selectedMonth = it }
                FilterChipRow(availableExercises, selectedExercise) { selectedExercise = it }
            }

            if (filteredWorkouts.isEmpty()) {
                EmptyHistoryState(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { HistorySummaryHeader(filteredWorkouts.size, totalVolume) }

                    items(items = filteredWorkouts, key = { it.id }) { workout ->
                        SwipeActionContainer(
                            onDelete = { viewModel.deleteWorkout(workout) },
                            onEdit = { onEditWorkout(workout) }
                        ) {
                            HistoryWorkoutCard(
                                workout = workout,
                                // 🔥 CLICK ANYWHERE ON CARD
                                modifier = Modifier.clickable {
                                    if (workout.exercise.isNotEmpty()) {
                                        openExerciseGraph(workout.exercise.first().name)
                                    }
                                },
                                // 🔥 CLICK SPECIFIC ICON
                                onExerciseClick = { exerciseName ->
                                    openExerciseGraph(exerciseName)
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- BOTTOM SHEET TRIGGER ---
        selectedExerciseDetails?.let { (name, history) ->
            ExerciseDetailBottomSheet(
                exerciseName = name,
                history = history,
                onDismiss = { selectedExerciseDetails = null }
            )
        }
    }
}

@Composable
fun SwipeActionContainer(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val actionsWidth = 120.dp
    val actionsWidthPx = with(density) { actionsWidth.toPx() }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(actionsWidth)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                scope.launch { offsetX.animateTo(0f) }
                onEdit()
            }) {
                Icon(Icons.Default.Edit, "Edit", tint = SuccessGreen)
            }
            IconButton(onClick = {
                scope.launch { offsetX.animateTo(0f) }
                onDelete()
            }) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount).coerceIn(-actionsWidthPx, 0f)
                            scope.launch { offsetX.snapTo(newOffset) }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -actionsWidthPx / 2) {
                                    offsetX.animateTo(-actionsWidthPx)
                                } else {
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun HistorySummaryHeader(count: Int, volume: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Volume", color = Color.White.copy(0.7f), fontSize = 11.sp)
                Text("${"%,d".format(volume)} kg", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Sessions", color = Color.White.copy(0.7f), fontSize = 11.sp)
                Text("$count", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilterChipRow(items: List<String>, selectedItem: String, onSelected: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = selectedItem == item,
                onClick = { onSelected(item) },
                label = { Text(item, fontSize = 12.sp) },
                shape = CircleShape
            )
        }
    }
}

@Composable
fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.History,
                null,
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
            )
            Text("No workouts found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}