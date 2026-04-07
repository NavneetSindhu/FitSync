package com.example.fitsync.ui.screens.log


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodayTabContent(
    viewModel: DailyLogViewModel,
    uiState: DailyLogUiState
) {
    val weightUnit = "kg"
    val coroutineScope = rememberCoroutineScope()

    if (uiState.exercises.isNotEmpty()) {
        val pagerState = rememberPagerState(pageCount = { uiState.exercises.size })
        val carouselState = rememberLazyListState()

        LaunchedEffect(pagerState.currentPage) {
            carouselState.animateScrollToItem(maxOf(0, pagerState.currentPage - 1))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER SECTION
            Text(
                "Current Session",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // CAROUSEL + TICK ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // This 16.dp is our anchor width
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
                    onClick = { viewModel.saveWorkout() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // --- ADDED: PAGER INDICATOR (DOTS) ---
            Row(
                Modifier
                    .height(24.dp)
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(uiState.exercises.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FULL WIDTH PAGER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp), // Matches carousel row padding
                pageSpacing = 16.dp, // Space between cards during swipe
                verticalAlignment = Alignment.Top
            ) { page ->
                val currentExercise = uiState.exercises.getOrNull(page)
                if (currentExercise != null) {
                    val meta = ExerciseVisuals.getMetaData(currentExercise.name)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        item {
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
                        }
                    }
                }
            }
        }
    } else {
        EmptyWorkoutState()
    }
}