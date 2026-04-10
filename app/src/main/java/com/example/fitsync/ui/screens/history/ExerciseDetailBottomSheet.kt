package com.example.fitsync.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitsync.domain.model.Exercise
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.fitsync.ui.theme.LocalAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailBottomSheet(
    exerciseName: String,
    history: List<Exercise>,
    onDismiss: () -> Unit
) {
    val points = remember(history) {
        history.mapIndexed { index, ex ->
            val volume = ex.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
            Point(index.toFloat(), volume)
        }
    }

    // 🔥 Using MaterialTheme colors to ensure Dark Mode compatibility
    val labelColor = MaterialTheme.colorScheme.onSurface
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    val xAxisData = AxisData.Builder()
        .axisStepSize(80.dp)
        .steps(if (points.size > 1) points.size - 1 else 1)
        .labelData { i -> "S${i + 1}" }
        .axisLabelColor(labelColor) // Use this instead of tint
        .axisLineColor(axisColor)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelData { i ->
            val maxVolume = points.maxOfOrNull { it.y } ?: 0f
            val labelValue = if (maxVolume > 0) (i * (maxVolume / 5)) else 0f
            String.format("%.0f", labelValue)
        }
        .axisLabelColor(labelColor)
        .axisLineColor(axisColor)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = points,
                    lineStyle = LineStyle(color = LocalAccentColor.current, width = 3f),
                    intersectionPoint = IntersectionPoint(color = LocalAccentColor.current, radius = 4.dp),
                    selectionHighlightPoint = SelectionHighlightPoint(color = LocalAccentColor.current),
                    shadowUnderLine = ShadowUnderLine(
                        alpha = 0.1f,
                        brush = Brush.verticalGradient(
                            listOf(LocalAccentColor.current, Color.Transparent)
                        )
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        backgroundColor = MaterialTheme.colorScheme.surface
        // 🔥 GridConfig is often causing issues in some 2.x sub-versions,
        // so we omit it to let the chart use its default stable background.
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Volume Progression (kg)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            if (points.isNotEmpty()) {
                LineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    lineChartData = lineChartData
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No history available yet", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val maxWeight = history.flatMap { it.sets }.maxOfOrNull { it.weight } ?: 0f
                StatItem("Personal Best", "$maxWeight kg")
                StatItem("Sessions", "${history.size}")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}