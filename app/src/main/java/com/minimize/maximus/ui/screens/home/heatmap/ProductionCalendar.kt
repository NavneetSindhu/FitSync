package com.minimize.maximus.ui.screens.home.heatmap

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.domain.model.WorkoutSummary
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.SuccessGreen
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProductionCalendar(
    selectedDate: LocalDate,
    workoutMap: Map<LocalDate, WorkoutSummary>,
    frozenDates: Set<LocalDate> = emptySet(),
    skippedDates: Set<LocalDate> = emptySet(),
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Default to Weekly View as requested
    var isExpandedToMonth by remember { mutableStateOf(false) }

    // Week navigation anchor date
    var weekAnchorDate by remember { mutableStateOf(selectedDate) }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(120) }
    val endMonth = remember { currentMonth }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    // outDateStyle = OutDateStyle.EndOfGrid ensures constant 6 rows (zero height jump for 28/30/31 days)
    val monthCalendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfGrid
    )

    LaunchedEffect(monthCalendarState.firstVisibleMonth.yearMonth) {
        onMonthChanged(monthCalendarState.firstVisibleMonth.yearMonth)
    }

    val today = remember { LocalDate.now() }
    val currentWeekMonday = remember(today) { today.minusDays((today.dayOfWeek.value - 1).toLong()) }
    val activeWeekMonday = remember(weekAnchorDate) { weekAnchorDate.minusDays((weekAnchorDate.dayOfWeek.value - 1).toLong()) }

    val isAwayFromToday = if (isExpandedToMonth) {
        monthCalendarState.firstVisibleMonth.yearMonth != currentMonth
    } else {
        currentWeekMonday != activeWeekMonday
    }

    val canGoNextMonth = monthCalendarState.firstVisibleMonth.yearMonth < currentMonth

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        // ── Top Bar: Month/Week Title, Scroll to Today, Navigation Arrows, Expand/Collapse Toggle ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isExpandedToMonth) {
                        monthCalendarState.firstVisibleMonth.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                                " " + monthCalendarState.firstVisibleMonth.yearMonth.year
                    } else {
                        weekAnchorDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                                " " + weekAnchorDate.year
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Animated "Today" quick-jump pill when scrolled into the past
                AnimatedVisibility(
                    visible = isAwayFromToday,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Surface(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                            if (isExpandedToMonth) {
                                coroutineScope.launch {
                                    monthCalendarState.animateScrollToMonth(currentMonth)
                                }
                            } else {
                                weekAnchorDate = today
                            }
                            onDateSelected(today)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = currentAccent.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentAccent
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardDoubleArrowRight,
                                contentDescription = "Jump to Today",
                                tint = currentAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Navigation Left (<)
                IconButton(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                        if (isExpandedToMonth) {
                            coroutineScope.launch {
                                monthCalendarState.animateScrollToMonth(monthCalendarState.firstVisibleMonth.yearMonth.minusMonths(1))
                            }
                        } else {
                            weekAnchorDate = weekAnchorDate.minusWeeks(1)
                        }
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Navigation Right (>)
                IconButton(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                        if (isExpandedToMonth) {
                            if (canGoNextMonth) {
                                coroutineScope.launch {
                                    monthCalendarState.animateScrollToMonth(monthCalendarState.firstVisibleMonth.yearMonth.plusMonths(1))
                                }
                            }
                        } else {
                            val nextWeek = weekAnchorDate.plusWeeks(1)
                            if (!nextWeek.isAfter(LocalDate.now().plusWeeks(1))) {
                                weekAnchorDate = nextWeek
                            }
                        }
                    },
                    enabled = if (isExpandedToMonth) canGoNextMonth else !weekAnchorDate.isAfter(LocalDate.now()),
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next",
                        tint = if (if (isExpandedToMonth) canGoNextMonth else !weekAnchorDate.isAfter(LocalDate.now())) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Expand / Collapse Toggle Arrow
                Surface(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.TAB_SWITCH)
                        isExpandedToMonth = !isExpandedToMonth
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = currentAccent.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isExpandedToMonth) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpandedToMonth) "Collapse to Week" else "Expand to Full Month",
                            tint = currentAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── Day of Week Headers (M T W T F S S) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Calendar Body: Week View vs Constant-Height Month View ──
        if (isExpandedToMonth) {
            HorizontalCalendar(
                state = monthCalendarState,
                dayContent = { day ->
                    DayBubble(
                        dayDate = day.date,
                        isCurrentMonth = day.position == DayPosition.MonthDate,
                        isSelected = day.date == selectedDate,
                        workoutSummary = workoutMap[day.date],
                        isFrozen = frozenDates.contains(day.date),
                        isSkipped = skippedDates.contains(day.date),
                        accentColor = currentAccent,
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            onDateSelected(it)
                        }
                    )
                }
            )
        } else {
            // Smooth Weekly View (7 Days of the active week)
            val mondayOfWeek = weekAnchorDate.minusDays((weekAnchorDate.dayOfWeek.value - 1).toLong())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                (0..6).forEach { dayOffset ->
                    val date = mondayOfWeek.plusDays(dayOffset.toLong())
                    Box(modifier = Modifier.weight(1f)) {
                        DayBubble(
                            dayDate = date,
                            isCurrentMonth = true,
                            isSelected = date == selectedDate,
                            workoutSummary = workoutMap[date],
                            isFrozen = frozenDates.contains(date),
                            isSkipped = skippedDates.contains(date),
                            accentColor = currentAccent,
                            onClick = {
                                MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                onDateSelected(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayBubble(
    dayDate: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    workoutSummary: WorkoutSummary?,
    isFrozen: Boolean,
    isSkipped: Boolean,
    accentColor: Color,
    onClick: (LocalDate) -> Unit
) {
    val isToday = dayDate == LocalDate.now()
    val isFuture = dayDate.isAfter(LocalDate.now())
    val hasWorkout = workoutSummary != null
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iceColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val skipColor = Color(0xFFFF9800)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "DayBubbleScale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.5.dp)
            .scale(scale)
            .clip(CircleShape)
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, accentColor, CircleShape)
                    isFrozen -> Modifier.border(1.5.dp, iceColor, CircleShape)
                    isSkipped -> Modifier.border(1.5.dp, skipColor, CircleShape)
                    isToday -> Modifier.border(1.5.dp, accentColor, CircleShape)
                    else -> Modifier
                }
            )
            .clickable(enabled = !isFuture && isCurrentMonth) { onClick(dayDate) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !isCurrentMonth -> Color.Transparent
                        isFrozen -> iceColor.copy(alpha = 0.2f)
                        hasWorkout && workoutSummary!!.intensity >= 3 -> SuccessGreen
                        hasWorkout && workoutSummary!!.intensity == 2 -> SuccessGreen.copy(alpha = 0.85f)
                        hasWorkout && workoutSummary!!.intensity == 1 -> SuccessGreen.copy(alpha = 0.55f)
                        isSkipped -> skipColor.copy(alpha = 0.2f)
                        isSelected -> accentColor.copy(alpha = 0.18f)
                        isFuture -> Color.Transparent
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!isCurrentMonth) {
                Text(
                    text = dayDate.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    fontSize = 11.sp
                )
            } else if (isFrozen) {
                Icon(
                    imageVector = MaximusIcons.Home.CalendarFreeze,
                    contentDescription = "Streak Frozen",
                    tint = iceColor,
                    modifier = Modifier.size(14.dp)
                )
            } else if (hasWorkout) {
                Icon(
                    imageVector = MaximusIcons.Home.StreakFlame,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else if (isSkipped) {
                Text(text = "!", fontSize = 12.sp, fontWeight = FontWeight.Black, color = skipColor)
            } else {
                Text(
                    text = dayDate.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isToday -> accentColor
                        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}