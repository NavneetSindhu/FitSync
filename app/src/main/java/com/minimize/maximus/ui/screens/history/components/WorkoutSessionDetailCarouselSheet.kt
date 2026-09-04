package com.minimize.maximus.ui.screens.history.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.domain.model.Exercise
import com.minimize.maximus.domain.model.SetType
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.ui.components.ChartPoint
import com.minimize.maximus.ui.components.ExerciseCarouselItem
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.components.MaximusLineGraph
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.LocalBottomSheetDismiss
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.util.DateUtils
import com.minimize.maximus.util.WorkoutMathUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionDetailCarouselSheet(
    workout: WorkoutSession,
    allWorkouts: List<WorkoutSession>,
    unit: String,
    onEditWorkout: () -> Unit,
    onShareWorkout: () -> Unit = {},
    onDismissRequest: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val coroutineScope = rememberCoroutineScope()
    val exercises = workout.exercise

    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedSessionDate = remember(workout.date) { dateFormatter.format(Date(workout.date)) }

    MaximusModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        val dismissSheet = LocalBottomSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // ── 1. Header Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedSessionDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onShareWorkout,
                        shape = RoundedCornerShape(12.dp),
                        color = currentAccent.copy(alpha = 0.14f),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MaximusIcons.History.Share,
                                contentDescription = "Share",
                                tint = currentAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            dismissSheet()
                            onEditWorkout()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = currentAccent.copy(alpha = 0.14f),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaximusIcons.Routines.Edit,
                                contentDescription = null,
                                tint = currentAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Edit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = currentAccent
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No exercises recorded in this session.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { exercises.size })
                val tabListState = rememberLazyListState()

                LaunchedEffect(pagerState.currentPage) {
                    tabListState.animateScrollToItem(maxOf(0, pagerState.currentPage - 1))
                }

                // ── 2. Exercise Tabs Navigation Strip ──
                LazyRow(
                    state = tabListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(exercises) { index, exercise ->
                        val isSelected = pagerState.currentPage == index
                        ExerciseCarouselItem(
                            name = exercise.name,
                            isSelected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── 3. Swipeable Exercise Pages (Graph + Sets) ──
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    val exercise = exercises[pageIndex]

                    // Compute historical progression points for this exercise
                    val exerciseHistory = remember(exercise.name, allWorkouts) {
                        val simpleDateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
                        allWorkouts
                            .filter { it.exercise.any { ex -> ex.name.equals(exercise.name, ignoreCase = true) } }
                            .sortedBy { it.date }
                            .mapNotNull { session ->
                                val targetEx = session.exercise.firstOrNull { ex -> ex.name.equals(exercise.name, ignoreCase = true) }
                                val maxWeight = targetEx?.sets?.maxOfOrNull { it.weight } ?: 0f
                                if (maxWeight > 0f) {
                                    ChartPoint(
                                        label = simpleDateFmt.format(Date(session.date)),
                                        value = maxWeight
                                    )
                                } else null
                            }
                    }

                    val bestSet = remember(exercise.sets) {
                        exercise.sets.maxByOrNull { it.weight }
                    }
                    val est1RM = remember(bestSet) {
                        bestSet?.let { WorkoutMathUtils.calculateOneRepMax(it.weight, it.reps) } ?: 0
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header info card with 1RM pill
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ExerciseIcon(name = exercise.name, size = 42.dp)
                                        Spacer(Modifier.width(14.dp))
                                        Column {
                                            Text(
                                                text = exercise.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${exercise.sets.size} sets recorded",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (est1RM > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = currentAccent.copy(alpha = 0.12f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = "Est. 1RM",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "$est1RM $unit",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = currentAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Exercise Historical Progression Graph
                        item {
                            MaximusLineGraph(
                                points = exerciseHistory,
                                unit = unit,
                                title = "${exercise.name} Progression"
                            )
                        }

                        // Sets Breakdown
                        item {
                            Text(
                                text = "Session Sets",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        itemsIndexed(exercise.sets) { setIndex, set ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${setIndex + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = currentAccent
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = SetType.fromCode(set.setType).label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${set.weight.toInt()} $unit × ${set.reps} reps",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (set.isCompleted) {
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Completed",
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
