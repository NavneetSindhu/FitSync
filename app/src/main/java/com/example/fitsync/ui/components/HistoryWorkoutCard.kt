package com.example.fitsync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitsync.domain.model.WorkoutSession
import com.example.fitsync.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryWorkoutCard(
    workout: WorkoutSession,
    modifier: Modifier = Modifier
) {
    // 1. Format date
    val dateString = remember(workout.date) {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(workout.date))
    }

    // 2. Calculate volume based on the correct field name 'exercise'
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
        Column(modifier = Modifier.padding(16.dp)) {
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
                    tint = if (workout.isSynced) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- EXERCISE ICONS (Using new ExerciseIcon component) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                workout.exercise.take(5).forEach { ex ->
                    // 🔥 REPLACED: Using your brand new reusable component here
                    ExerciseIcon(
                        name = ex.name,
                        size = 36.dp
                    )
                }

                if (workout.exercise.size > 5) {
                    Text(
                        text = "+${workout.exercise.size - 5}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // --- FOOTER STATS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${"%,d".format(workoutVolume)}kg",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Exercises",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${workout.exercise.size}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}