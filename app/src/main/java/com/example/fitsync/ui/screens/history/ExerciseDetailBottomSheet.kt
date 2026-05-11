package com.example.fitsync.ui.screens.history

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.domain.model.Exercise
import com.example.fitsync.ui.components.FitSyncFilterChip
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.fitsync.ui.theme.LocalAccentColor
import java.util.Calendar



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailBottomSheet(
    exerciseName: String,
    // 🔥 Changed to take Pair of Timestamp and Exercise to enable Timeline filtering
    historyWithDates: List<Pair<Long, Exercise>>,
    weightUnit: String,
    onDismiss: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    var selectedTimeline by remember { mutableStateOf("All") }
    val timelines = listOf("1W", "1M", "3M", "All")

    // 1. WORKING TIMELINE FILTER
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

    // 2. PROCESS POINTS
    val points = remember(filteredHistory) {
        filteredHistory.mapIndexed { index, pair ->
            val totalVolume = pair.second.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
            Point(index.toFloat(), totalVolume)
        }
    }

    if (points.isEmpty()) {
        // Handle empty period state
    }

    val maxVolume = remember(points) { points.maxOfOrNull { it.y } ?: 0f }
    val labelColor = MaterialTheme.colorScheme.onSurface
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    // 3. CHART CONFIG WITH AUTO-SCROLL LOGIC
    val xAxisData = AxisData.Builder()
        .axisStepSize(80.dp)
        .steps(if (points.isNotEmpty()) points.size - 1 else 0)
        // 🔥 This enables "Scroll to End" behavior so the most recent data is visible
        .labelData { i -> "S${i + 1}" }
        .axisLabelColor(labelColor)
        .axisLineColor(axisColor)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val labelValue = (i * (maxVolume / 5))
            if (labelValue >= 1000) "%.1fk".format(labelValue / 1000) else "%.0f".format(labelValue)
        }
        .axisLabelColor(labelColor)
        .axisLineColor(axisColor)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = points,
                    lineStyle = LineStyle(color = currentAccent, width = 3f),
                    intersectionPoint = IntersectionPoint(color = currentAccent, radius = 5.dp),
                    selectionHighlightPoint = SelectionHighlightPoint(color = currentAccent),
                    shadowUnderLine = ShadowUnderLine(
                        alpha = 0.2f,
                        brush = Brush.verticalGradient(listOf(currentAccent, Color.Transparent))
                    ),
                    // 🔥 FIX: Simplified PopUp to avoid label style errors
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        popUpLabel = { _, y -> "${y.toInt()} $weightUnit" }
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        backgroundColor = MaterialTheme.colorScheme.surface,
        // 🔥 Enables horizontal scrolling for the chart
        containerPaddingEnd = 50.dp
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 20.dp)
        ) {
            Text(exerciseName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Volume Progression", color = currentAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

            Spacer(Modifier.height(20.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(timelines) { timeline ->
                    FitSyncFilterChip(
                        label = timeline,
                        isSelected = selectedTimeline == timeline,
                        onClick = { selectedTimeline = timeline }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                if (points.size >= 2) {
                    LineChart(
                        modifier = Modifier.fillMaxSize(),
                        lineChartData = lineChartData
                    )
                } else if (points.size == 1) {
                    SinglePointMilestone(currentAccent, points.first().y.toInt(), weightUnit)
                } else {
                    Text("No data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val maxWeight = historyWithDates.flatMap { it.second.sets }.maxOfOrNull { it.weight } ?: 0f
                StatItem("Personal Best", "${maxWeight.toInt()} $weightUnit", currentAccent)
                StatItem("Sessions", "${filteredHistory.size}", currentAccent)
                StatItem("Avg. Volume", "${(points.map { it.y }.average().takeIf { !it.isNaN() } ?: 0.0).toInt()} $weightUnit", currentAccent)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SinglePointMilestone(accent: Color, volume: Int, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = CircleShape, color = accent.copy(alpha = 0.1f), modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, null, tint = accent, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("First Entry: $volume $unit", fontWeight = FontWeight.Bold)
        Text("Log more to see trend lines", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun StatItem(label: String, value: String, accentColor: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = accentColor)
    }
}