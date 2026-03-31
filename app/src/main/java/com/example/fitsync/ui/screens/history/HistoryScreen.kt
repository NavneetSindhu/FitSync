package com.example.fitsync.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitsync.data.local.entity.WorkoutEntity
import com.example.fitsync.ui.components.HistoryWorkoutCard
import com.example.fitsync.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val workouts by viewModel.workoutHistory.collectAsState()

    var selectedMonth by remember { mutableStateOf("All") }
    var selectedExercise by remember { mutableStateOf("All") }

    val monthFormatter = remember { SimpleDateFormat("MMMM", Locale.getDefault()) }

    val availableMonths = remember(workouts) {
        listOf("All") + workouts.map { monthFormatter.format(Date(it.date)) }.distinct()
    }
    val availableExercises = remember(workouts) {
        listOf("All") + workouts.flatMap { it.exerciseList.map { ex -> ex.name } }.distinct()
    }

    val filteredWorkouts = remember(workouts, selectedMonth, selectedExercise) {
        workouts.filter { workout ->
            val workoutMonth = monthFormatter.format(Date(workout.date))
            val monthMatch = selectedMonth == "All" || workoutMonth == selectedMonth
            val exerciseMatch = selectedExercise == "All" || workout.exerciseList.any { it.name == selectedExercise }
            monthMatch && exerciseMatch
        }
    }

    val totalVolume = remember(filteredWorkouts) {
        filteredWorkouts.sumOf { workout ->
            workout.exerciseList.sumOf { exercise ->
                exercise.sets.sumOf { (it.weight * it.reps).toDouble() }
            }
        }.toInt()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        "Workout History",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(top = padding.calculateTopPadding())) {

            // --- FILTER CHIPS ---
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                FilterChipRow(availableMonths, selectedMonth) { selectedMonth = it }
                FilterChipRow(availableExercises, selectedExercise) { selectedExercise = it }
            }

            if (filteredWorkouts.isEmpty()) {
                EmptyHistoryState(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        HistorySummaryHeader(filteredWorkouts.size, totalVolume)
                    }

                    items(items = filteredWorkouts, key = { it.id }) { workout ->
                        Box(modifier = Modifier.animateItem()) {
                            SwipeToDeleteContainer(
                                item = workout,
                                onDelete = { viewModel.deleteWorkout(it) }
                            ) {
                                HistoryWorkoutCard(workout = workout)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedItem == item,
                    borderColor = Color.Transparent,
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun HistorySummaryHeader(count: Int, volume: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Keep the brand color for the header background
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Total Volume",
                    color = MaterialTheme.colorScheme.onPrimary.copy(0.7f),
                    fontSize = 11.sp
                )
                Text(
                    "${"%,d".format(volume)} kg",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Sessions",
                    color = MaterialTheme.colorScheme.onPrimary.copy(0.7f),
                    fontSize = 11.sp
                )
                Text(
                    "$count",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(item: WorkoutEntity, onDelete: (WorkoutEntity) -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item)
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                MaterialTheme.colorScheme.error.copy(0.8f) else Color.Transparent
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(color, RoundedCornerShape(12.dp)),
                Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        },
        content = { content() }
    )
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
            Text(
                "No workouts found.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}