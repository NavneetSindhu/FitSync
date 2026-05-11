package com.example.fitsync.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitsync.domain.model.Exercise
import com.example.fitsync.domain.model.WorkoutSession
import com.example.fitsync.ui.components.HistoryWorkoutCard
import com.example.fitsync.ui.theme.*

// ── IMPORT GLOBAL PREFERENCES ──────────────────────────────────────────────
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onEditWorkout: (WorkoutSession) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val weightUnit by viewModel.weightUnit.collectAsState()
    val workouts by viewModel.workoutHistory.collectAsState(initial = emptyList())

    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    var selectedExerciseDetails by remember { mutableStateOf<Pair<String, List<Pair<Long, Exercise>>>?>(null) }

    // --- DELETE CONFIRMATION STATE ---
    var workoutToDelete by remember { mutableStateOf<WorkoutSession?>(null) }

    var selectedMonth by remember { mutableStateOf("All") }
    var selectedWorkoutType by remember { mutableStateOf("All") }

    val monthFormatter = remember { SimpleDateFormat("MMMM", Locale.getDefault()) }
    val dayFormatter = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }

    // Derive filter options
    val availableMonths = remember(workouts) {
        listOf("All") + workouts.map { monthFormatter.format(Date(it.date)) }.distinct()
    }
    val workoutTypes = listOf("All", "Push", "Pull", "Legs", "Full Body", "Custom")

    val groupedWorkouts = remember(workouts, selectedMonth, selectedWorkoutType) {
        workouts
            .filter { workout ->
                val monthMatch = selectedMonth == "All" || monthFormatter.format(Date(workout.date)) == selectedMonth
                val typeMatch = selectedWorkoutType == "All" || workout.name.contains(selectedWorkoutType, ignoreCase = true)
                monthMatch && typeMatch
            }
            .sortedByDescending { it.date }
            .groupBy { dayFormatter.format(Date(it.date)) }
    }

    val totalVolume = remember(groupedWorkouts) {
        groupedWorkouts.values.flatten().sumOf { session ->
            session.exercise.sumOf { ex ->
                ex.sets.sumOf { (it.weight * it.reps).toDouble() }
            }
        }.toInt()
    }

    fun openExerciseGraph(exerciseName: String) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                FilterChipRow(availableMonths, selectedMonth, currentAccent) { selectedMonth = it }
                FilterChipRow(workoutTypes, selectedWorkoutType, currentAccent) { selectedWorkoutType = it }
            }

            if (groupedWorkouts.isEmpty()) {
                EmptyHistoryState(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        HistorySummaryHeader(
                            count = groupedWorkouts.values.flatten().size,
                            volume = totalVolume,
                            accentColor = currentAccent,
                            unit = weightUnit,
                            isCompact = isCompact
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    groupedWorkouts.forEach { (date, sessions) ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = date.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = currentAccent.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        items(sessions, key = { it.id }) { workout ->
                            SwipeActionContainer(
                                onDelete = { workoutToDelete = workout }, // Trigger Dialog
                                onEdit = { onEditWorkout(workout) }
                            ) {
                                HistoryWorkoutCard(
                                    workout = workout,
                                    unit = weightUnit,
                                    onExerciseClick = { exerciseName -> openExerciseGraph(exerciseName) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- DELETE DIALOG ---
        workoutToDelete?.let { session ->
            AlertDialog(
                onDismissRequest = { workoutToDelete = null },
                // M3 AlertDialog centers the icon slot by default
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Delete Workout?",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete this ${session.name} session? This action cannot be undone.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteWorkout(session)
                            workoutToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { workoutToDelete = null }) {
                        Text("Cancel", color = currentAccent)
                    }
                }
            )
        }

        selectedExerciseDetails?.let { (name, historyWithDates) ->
            ExerciseDetailBottomSheet(
                exerciseName = name,
                historyWithDates = historyWithDates,
                weightUnit = weightUnit,
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
fun HistorySummaryHeader(
    count: Int,
    volume: Int,
    accentColor: Color,
    unit: String,
    isCompact: Boolean
) {
    val contentColor = if (accentColor.luminance() > 0.4f) Color.Black else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentColor),
        shape = RoundedCornerShape(if (isCompact) 12.dp else 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(if (isCompact) 16.dp else 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Volume", color = contentColor.copy(0.7f), fontSize = 11.sp)
                Text("${"%,d".format(volume)} $unit", color = contentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Sessions", color = contentColor.copy(0.7f), fontSize = 11.sp)
                Text("$count", color = contentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilterChipRow(items: List<String>, selectedItem: String, accentColor: Color, onSelected: (String) -> Unit) {
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
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = if (accentColor.luminance() > 0.4f) Color.Black else Color.White
                )
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