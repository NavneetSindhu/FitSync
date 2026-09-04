package com.minimize.maximus.ui.screens.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.Exercise
import com.minimize.maximus.ui.components.ChartPoint
import com.minimize.maximus.ui.components.MaximusFilterChip
import com.minimize.maximus.ui.components.MaximusLineGraph
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.theme.LocalAccentColor
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailBottomSheet(
    exerciseName: String,
    historyWithDates: List<Pair<Long, Exercise>>,
    weightUnit: String,
    onDismiss: () -> Unit
) {
    val currentAccent = LocalAccentColor.current

    var selectedTimeline by remember { mutableStateOf("All") }
    val timelines = listOf("1W", "1M", "3M", "All")

    val isCalisthenicsHold = remember(exerciseName) {
        listOf("Planche", "Lever", "Handstand", "Hold", "Plank")
            .any { exerciseName.contains(it, ignoreCase = true) }
    }

    val metrics = if (isCalisthenicsHold) {
        listOf("Max Time", "Added Load", "Total Time")
    } else {
        listOf("Weight", "Reps", "Est. 1RM", "Volume")
    }
    var selectedMetric by remember(metrics) { mutableStateOf(metrics.first()) }

    val activeDisplayUnit = remember(selectedMetric, weightUnit) {
        when (selectedMetric) {
            "Reps" -> "reps"
            "Max Time", "Total Time" -> "secs"
            "Weight", "Added Load", "Est. 1RM", "Volume" -> weightUnit
            else -> weightUnit
        }
    }

    val filteredHistory = remember(historyWithDates, selectedTimeline) {
        if (selectedTimeline == "All") return@remember historyWithDates

        val cutoff = Calendar.getInstance().apply {
            when (selectedTimeline) {
                "1W" -> add(Calendar.DAY_OF_YEAR, -7)
                "1M" -> add(Calendar.MONTH, -1)
                "3M" -> add(Calendar.MONTH, -3)
            }
        }.timeInMillis

        historyWithDates.filter { it.first >= cutoff }
    }

    val chartPoints = remember(filteredHistory, selectedMetric) {
        filteredHistory.mapIndexed { index, pair ->
            val extractionValue = when (selectedMetric) {
                "Weight", "Added Load" -> {
                    pair.second.sets.maxOfOrNull { it.weight } ?: 0f
                }
                "Reps" -> {
                    pair.second.sets.maxOfOrNull { it.reps }?.toFloat() ?: 0f
                }
                "Max Time" -> {
                    pair.second.sets.maxOfOrNull { it.reps }?.toFloat() ?: 0f
                }
                "Total Time" -> {
                    pair.second.sets.sumOf { it.reps }.toFloat()
                }
                "Est. 1RM" -> {
                    pair.second.sets.maxOfOrNull { set ->
                        if (set.reps > 0) set.weight * (1f + set.reps / 30f) else set.weight
                    } ?: 0f
                }
                "Volume" -> {
                    pair.second.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                }
                else -> 0f
            }
            ChartPoint(label = "S${index + 1}", value = extractionValue)
        }
    }

    val maxPeakValue = remember(chartPoints) { chartPoints.maxOfOrNull { it.value } ?: 0f }

    MaximusModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        MaximusSheetHeader(
            title = exerciseName,
            subtitle = stringResource(R.string.exercise_analytics_title)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // Metric Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(metrics) { metric ->
                    MaximusFilterChip(
                        label = metric,
                        isSelected = selectedMetric == metric,
                        onClick = { selectedMetric = metric }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Timeline Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(timelines) { timeline ->
                    MaximusFilterChip(
                        label = timeline,
                        isSelected = selectedTimeline == timeline,
                        onClick = { selectedTimeline = timeline }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Native Smooth Canvas Chart
            MaximusLineGraph(
                points = chartPoints,
                unit = activeDisplayUnit,
                title = "$selectedMetric Progression"
            )

            Spacer(Modifier.height(16.dp))

            // Quick Stats Metric Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.exercise_all_time_peak), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${maxPeakValue.toInt()} $activeDisplayUnit",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = currentAccent
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = currentAccent.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = currentAccent, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
