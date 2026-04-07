package com.example.fitsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitsync.domain.model.WorkoutSession
import com.example.fitsync.ui.theme.ExerciseVisuals
import com.example.fitsync.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryWorkoutCard(
    workout: WorkoutSession,
    modifier: Modifier = Modifier
) {
    val dateString = remember(workout.date) {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(workout.date))
    }

    // UPDATED: Changed .exercises to .exercise to match your model
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (workout.isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = if (workout.isSynced) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // UPDATED: Changed .exercises to .exercise
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                workout.exercise.take(5).forEach { ex ->
                    val meta = ExerciseVisuals.getMetaData(ex.name)
                    Surface(modifier = Modifier.size(32.dp), color = meta.accentColor.copy(0.12f), shape = CircleShape) {
                        Icon(meta.icon, null, Modifier.padding(6.dp), tint = meta.accentColor)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Volume", style = MaterialTheme.typography.labelSmall)
                    Text("${workoutVolume}kg", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Exercises", style = MaterialTheme.typography.labelSmall)
                    Text("${workout.exercise.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}