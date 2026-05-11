package com.example.fitsync.ui.screens.log

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.components.EmptyWorkoutState
import com.example.fitsync.ui.components.ExerciseCarouselItem
import com.example.fitsync.ui.components.ExerciseLogCard
import com.example.fitsync.ui.theme.ExerciseVisuals
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoggingScreen(
    viewModel: DailyLogViewModel,
    uiState: DailyLogUiState,
    onFinishWorkout: () -> Unit
) {
    val weightUnit = "kg" // Should ideally come from PreferenceManager via ViewModel
    val coroutineScope = rememberCoroutineScope()
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    // Safety state to prevent accidental finish
    var showFinishConfirmation by remember { mutableStateOf(false) }

    if (uiState.exercises.isNotEmpty()) {
        val pagerState = rememberPagerState(pageCount = { uiState.exercises.size })
        val carouselState = rememberLazyListState()

        LaunchedEffect(pagerState.currentPage) {
            carouselState.animateScrollToItem(maxOf(0, pagerState.currentPage - 1))
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // --- CAROUSEL + FINISH BUTTON ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    state = carouselState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.exercises) { index, exercise ->
                        val isDone = exercise.sets.isNotEmpty() && exercise.sets.all { it.isCompleted }

                        Box {
                            ExerciseCarouselItem(
                                name = exercise.name,
                                isSelected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                }
                            )
                            // Subtle "Completed" indicator on the carousel item
                            if (isDone) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                        .padding(2.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Robust Finish Button
                Surface(
                    onClick = { showFinishConfirmation = true },
                    shape = RoundedCornerShape(16.dp),
                    color = currentAccent,
                    contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White,
                    modifier = Modifier.size(if (isCompact) 48.dp else 56.dp),
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, "Finish", modifier = Modifier.size(if (isCompact) 24.dp else 28.dp))
                    }
                }
            }

            // --- PAGER INDICATOR (Improved UI) ---
            Row(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(uiState.exercises.size) { iteration ->
                    val isActive = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(if (isActive) currentAccent else currentAccent.copy(alpha = 0.2f))
                            .width(if (isActive) 16.dp else 6.dp) // Bar-style indicator
                            .height(6.dp)
                    )
                }
            }

            // --- PAGER ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                verticalAlignment = Alignment.Top,
                userScrollEnabled = true // Allow swiping for better UX
            ) { page ->
                val currentExercise = uiState.exercises.getOrNull(page)
                if (currentExercise != null) {
                    val meta = ExerciseVisuals.getMetaData(currentExercise.name)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ExerciseLogCard(
                                exerciseName = currentExercise.name,
                                sets = currentExercise.sets,
                                unit = weightUnit,
                                accentColor = meta.accentColor,
                                onAddSet = { viewModel.addSet(currentExercise.name) },
                                onUpdateSet = { s, w, r -> viewModel.updateSet(currentExercise.name, s, w, r) },
                                onToggleSet = { s -> viewModel.toggleSetCompletion(currentExercise.name, s) },
                                onDeleteSet = { s -> viewModel.deleteSet(currentExercise.name, s) },
                                onDeleteExercise = { viewModel.removeExercise(currentExercise.name) }
                            )
                        }

                        // Robustness: Warning if logging a set without data
                        val hasEmptySets = currentExercise.sets.any { it.weight == 0f || it.reps == 0 }
                        if (hasEmptySets) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Some sets are missing weight or reps",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        EmptyWorkoutState()
    }

    // --- FINISH CONFIRMATION DIALOG ---
    if (showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            icon = { Icon(Icons.Default.Check, contentDescription = null, tint = currentAccent) },
            title = { Text("Finish Session?") },
            text = { Text("Ensure all your sets are logged. Unfinished exercises will be saved as is.") },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmation = false
                        viewModel.saveWorkout()
                        onFinishWorkout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = currentAccent)
                ) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) {
                    Text("Keep Training")
                }
            }
        )
    }
}