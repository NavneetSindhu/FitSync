package com.example.fitsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitsync.domain.model.WorkoutSession
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryWorkoutCard(
    workout: WorkoutSession,
    modifier: Modifier = Modifier,
    unit: String = "kg", // Pass this from HistoryScreen (ViewModel)
    onExerciseClick: (String) -> Unit = {}
) {
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    val dateString = remember(workout.date) {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(workout.date))
    }

    val workoutVolume = remember(workout.exercise) {
        workout.exercise.sumOf { exercise ->
            exercise.sets.sumOf { (it.weight * it.reps).toDouble() }
        }.toInt()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp)) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = if (workout.isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                    contentDescription = "Sync Status",
                    tint = if (workout.isSynced) currentAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                )
            }

            Spacer(Modifier.height(if (isCompact) 12.dp else 16.dp))

            // --- EXERCISE ICONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                workout.exercise.take(if (isCompact) 6 else 5).forEach { ex ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onExerciseClick(ex.name) }
                    ) {
                        ExerciseIcon(
                            name = ex.name,
                            size = if (isCompact) 32.dp else 36.dp
                        )
                    }
                }

                if (workout.exercise.size > (if (isCompact) 6 else 5)) {
                    Text(
                        text = "+${workout.exercise.size - (if (isCompact) 6 else 5)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = if (isCompact) 12.dp else 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // --- FOOTER STATS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%,d".format(workoutVolume)} $unit", fontWeight = FontWeight.Bold, color = currentAccent)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Exercises", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${workout.exercise.size}", fontWeight = FontWeight.Bold, color = currentAccent)
                }
            }
        }
    }
}