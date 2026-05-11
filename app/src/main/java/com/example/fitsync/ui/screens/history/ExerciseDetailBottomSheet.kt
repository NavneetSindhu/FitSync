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
    history: List<Exercise>, // Assumed list with date/timestamp or sorted by date
    weightUnit: String,
    onDismiss: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    var selectedTimeline by remember { mutableStateOf("All") }
    val timelines = listOf("1W", "1M", "3M", "All")

    // 1. FILTER HISTORY BY TIMELINE
    val filteredHistory = remember(history, selectedTimeline) {
        if (selectedTimeline == "All") return@remember history

        val calendar = Calendar.getInstance()
        when (selectedTimeline) {
            "1W" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "1M" -> calendar.add(Calendar.MONTH, -1)
            "3M" -> calendar.add(Calendar.MONTH, -3)
        }
        // Note: This requires your Exercise model to have a date,
        // or for the history list to be filtered by the Session date before passing.
        history // Fallback to full history if date comparison logic is in Session level
    }

    // 2. PROCESS POINTS
    val points = remember(filteredHistory) {
        filteredHistory.mapIndexed { index, ex ->
            val totalVolume = ex.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
            Point(index.toFloat(), totalVolume)
        }
    }

    val maxVolume = remember(points) { points.maxOfOrNull { it.y } ?: 0f }
    val labelColor = MaterialTheme.colorScheme.onSurface
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    // 3. THEMED POPUP & CHART CONFIG
    val xAxisData = AxisData.Builder()
        .axisStepSize(if (points.size > 5) 70.dp else 100.dp)
        .steps(if (points.isNotEmpty()) points.size - 1 else 0)
        .labelData { i -> "S${i + 1}" }
        .axisLabelColor(labelColor)
        .axisLineColor(axisColor)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
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
                    // 🔥 CUSTOM THEMED POPUP
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        popUpLabel = { x, y -> "${y.toInt()} $weightUnit" },
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        draw = { offset, _ ->
                            // Custom drawing logic if needed, but styling parameters above cover most theme needs
                        }
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        backgroundColor = MaterialTheme.colorScheme.surface
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
            // Header
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Volume Progression",
                style = MaterialTheme.typography.labelMedium,
                color = currentAccent,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            // 🔥 TIMELINE SELECTOR
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(timelines) { timeline ->
                    FitSyncFilterChip(
                        label = timeline,
                        isSelected = selectedTimeline == timeline,
                        onClick = { selectedTimeline = timeline }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // GRAPH BOX
            Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                if (points.size >= 2) {
                    LineChart(modifier = Modifier.fillMaxSize(), lineChartData = lineChartData)
                } else if (points.size == 1) {
                    SinglePointMilestone(currentAccent, points.first().y.toInt(), weightUnit)
                } else {
                    Text("No data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(32.dp))

            // STATS FOOTER
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val maxWeight = history.flatMap { it.sets }.maxOfOrNull { it.weight } ?: 0f
                StatItem("Personal Best", "${maxWeight.toInt()} $weightUnit", currentAccent)
                StatItem("Sessions", "${history.size}", currentAccent)
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