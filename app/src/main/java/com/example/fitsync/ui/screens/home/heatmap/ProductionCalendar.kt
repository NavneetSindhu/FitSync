package com.example.fitsync.ui.screens.home.heatmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.domain.model.WorkoutSummary
import com.example.fitsync.ui.theme.SuccessGreen
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProductionCalendar(
    selectedDate: LocalDate,
    workoutMap: Map<LocalDate, WorkoutSummary>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    // Setup standard calendar boundaries (e.g., 10 years past, 1 year future)
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(120) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    // Trigger data fetch when the user swipes to a new month
    LaunchedEffect(state.firstVisibleMonth.yearMonth) {
        onMonthChanged(state.firstVisibleMonth.yearMonth)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header showing the Current Visible Month
            Text(
                text = state.firstVisibleMonth.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                        " " + state.firstVisibleMonth.yearMonth.year,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
            )

            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    DayBubble(
                        day = day,
                        isSelected = day.date == selectedDate,
                        workoutSummary = workoutMap[day.date],
                        onClick = { onDateSelected(it.date) }
                    )
                },
                monthHeader = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { dayOfWeek ->
                            Text(
                                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DayBubble(
    day: CalendarDay,
    isSelected: Boolean,
    workoutSummary: WorkoutSummary?,
    onClick: (CalendarDay) -> Unit
) {
    // Only show dates for the current month being viewed
    if (day.position != DayPosition.MonthDate) {
        Box(modifier = Modifier.aspectRatio(1f)) // Empty space for offset days
        return
    }

    val isToday = day.date == LocalDate.now()
    val hasWorkout = workoutSummary != null

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            // Border logic for selection
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
            )
            .clickable { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        // The Background Bubble
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        hasWorkout && workoutSummary!!.intensity >= 3 -> SuccessGreen
                        hasWorkout && workoutSummary!!.intensity == 2 -> SuccessGreen.copy(alpha = 0.6f)
                        hasWorkout && workoutSummary!!.intensity == 1 -> SuccessGreen.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasWorkout) {
                Text(text = "🔥", fontSize = 14.sp)
            } else {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}