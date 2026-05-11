package com.example.fitsync.ui.screens.log

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitsync.ui.components.EmptyWorkoutState
import com.example.fitsync.ui.components.ExerciseCarouselItem
import com.example.fitsync.ui.components.ExerciseLogCard
import com.example.fitsync.ui.theme.ExerciseVisuals
import kotlinx.coroutines.launch

// ── IMPORT OUR GLOBAL PREFERENCES ──────────────────────────────────────────
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoggingScreen(
    viewModel: DailyLogViewModel,
    uiState: DailyLogUiState,
    onFinishWorkout: () -> Unit
) {
    // Note: If you want to make units dynamic later, you can add a LocalWeightUnit
    // to your Theme.kt just like we did with LocalAccentColor!
    val weightUnit = "kg"
    val coroutineScope = rememberCoroutineScope()

    // 1. Grab global settings
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    if (uiState.exercises.isNotEmpty()) {
        val pagerState = rememberPagerState(pageCount = { uiState.exercises.size })
        val carouselState = rememberLazyListState()

        // Sync Carousel scroll with Pager swipe
        LaunchedEffect(pagerState.currentPage) {
            carouselState.animateScrollToItem(maxOf(0, pagerState.currentPage - 1))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER SECTION ---
            Text(
                text = uiState.date,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Current Session",
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = currentAccent, // <-- Uses dynamic accent color
                fontWeight = FontWeight.Bold
            )

            // --- CAROUSEL + FINISH BUTTON ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    state = carouselState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.exercises) { index, exercise ->
                        ExerciseCarouselItem(
                            name = exercise.name,
                            isSelected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = {
                        viewModel.saveWorkout()
                        onFinishWorkout()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = currentAccent, // <-- Uses dynamic accent color
                    contentColor = Color.White,
                    modifier = Modifier.size(if (isCompact) 48.dp else 56.dp), // <-- Responds to compact cards
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, "Finish", modifier = Modifier.size(if (isCompact) 24.dp else 28.dp))
                    }
                }
            }

            // --- PAGER INDICATOR ---
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(uiState.exercises.size) { iteration ->
                    // Uses dynamic accent color
                    val color = if (pagerState.currentPage == iteration) currentAccent else currentAccent.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp)
                    )
                }
            }

            // --- PAGER ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                verticalAlignment = Alignment.Top
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
                                accentColor = meta.accentColor, // Keeps the exercise-specific color
                                onAddSet = { viewModel.addSet(currentExercise.name) },
                                onUpdateSet = { s, w, r -> viewModel.updateSet(currentExercise.name, s, w, r) },
                                onToggleSet = { s -> viewModel.toggleSetCompletion(currentExercise.name, s) },
                                onDeleteSet = { s -> viewModel.deleteSet(currentExercise.name, s) },
                                onDeleteExercise = { viewModel.removeExercise(currentExercise.name) }
                            )
                        }
                    }
                }
            }
        }
    } else {
        EmptyWorkoutState()
    }
}