package com.example.fitsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.data.local.entity.WorkoutEntity
import com.example.fitsync.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryWorkoutCard(
    workout: WorkoutEntity,
    modifier: Modifier = Modifier
) {
    // 1. Logic remains the same (Safe Long-to-Date conversion)
    val dateString = remember(workout.date) {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(workout.date))
    }

    val workoutVolume = workout.exerciseList.sumOf { exercise ->
        exercise.sets.sumOf { (it.weight * it.reps).toDouble() }
    }.toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        // THE FIX: Uses Surface color (White in Light, Dark Grey in Dark)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER: Date & Sync Status ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, // Dynamic Blue
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface // Dynamic Text
                    )
                }

                // Sync Status Icon
                Icon(
                    imageVector = if (workout.isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = if (workout.isSynced) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // --- EXERCISE CHIPS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                workout.exerciseList.take(4).forEach { exercise ->
                    val meta = ExerciseVisuals.getMetaData(exercise.name)
                    Surface(
                        modifier = Modifier.size(32.dp),
                        color = meta.accentColor.copy(alpha = 0.12f), // Tinted background
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = meta.icon,
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp),
                            tint = meta.accentColor
                        )
                    }
                }
                if (workout.exerciseList.size > 4) {
                    Text(
                        "+${workout.exerciseList.size - 4}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // THE FIX: Adaptive Divider color
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // --- FOOTER: Summary Stats ---
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
                        "${workoutVolume}kg",
                        style = MaterialTheme.typography.bodyMedium,
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
                        "${workout.exerciseList.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}