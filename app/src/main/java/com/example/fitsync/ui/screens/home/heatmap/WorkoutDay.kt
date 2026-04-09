package com.example.fitsync.ui.screens.home.heatmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.theme.SuccessGreen
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class WorkoutDay(
    val date: LocalDate,
    val intensity: Int // 0 to 4
)

@Composable
fun WorkoutCalendarHeatmap(
    workoutDays: List<WorkoutDay>,
    modifier: Modifier = Modifier
) {
    val currentMonth = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Month Name
            Text(
                text = currentMonth,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Days of Week Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            // We use a fixed height or wrapContent. 7 columns for 7 days.
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(workoutDays) { day ->
                    CalendarBubble(day)
                }
            }
        }
    }
}

@Composable
fun CalendarBubble(day: WorkoutDay) {
    val isToday = day.date == LocalDate.now()
    val hasWorkout = day.intensity > 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    when {
                        day.intensity == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        day.intensity == 1 -> SuccessGreen.copy(alpha = 0.2f)
                        day.intensity == 2 -> SuccessGreen.copy(alpha = 0.5f)
                        day.intensity == 3 -> SuccessGreen.copy(alpha = 0.8f)
                        else -> SuccessGreen
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasWorkout) {
                Text(text = "🔥", fontSize = 14.sp)
            } else {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Small dot indicator for "Today" if no workout yet
        if (isToday && !hasWorkout) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun StreakCard(streakCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "$streakCount Day Streak!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Don't break the chain!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}