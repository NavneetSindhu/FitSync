package com.minimize.maximus.ui.screens.history.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.LocalCompactCards
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.util.DateUtils
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import com.minimize.maximus.util.MuscleClassifier
import com.minimize.maximus.util.WorkoutMathUtils
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HistoryWorkoutCard(
    workout: WorkoutSession,
    modifier: Modifier = Modifier,
    unit: String = "kg",
    onClick: () -> Unit = {},
    onExerciseClick: (String) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val dateString = remember(workout.date) {
        DateUtils.formatEpoch(workout.date, "EEE, MMM dd")
    }

    val isMetric = unit.equals("kg", ignoreCase = true)

    // Compute Best Set / Top Lift in this session
    val topLift = remember(workout.exercise, isMetric) {
        var bestExName = ""
        var bestWeight = 0f
        var bestReps = 0
        var best1RM = 0f
        var isPureBodyweight = true

        workout.exercise.forEach { ex ->
            ex.sets.forEach { set ->
                if (set.weight > 0f) isPureBodyweight = false
                val est1RM = WorkoutMathUtils.calculate1RMEpley(set.weight, set.reps)
                if (est1RM > best1RM || (best1RM == 0f && set.reps > bestReps)) {
                    best1RM = est1RM
                    bestExName = ex.name
                    bestWeight = set.weight
                    bestReps = set.reps
                }
            }
        }

        if (bestExName.isNotBlank()) {
            val displayWeight = if (isMetric) bestWeight else (bestWeight * 2.20462f)
            if (bestWeight > 0f) {
                "${bestExName}: ${"%.1f".format(displayWeight).removeSuffix(".0")} $unit × $bestReps"
            } else {
                "${bestExName}: $bestReps reps"
            }
        } else null
    }

    // Compute Primary Target Muscle Split Focus
    val primaryMuscleGroup = remember(workout.exercise) {
        val groups = workout.exercise.flatMap { MuscleClassifier.getPrimaryMuscles(it.name) }.distinct()
        if (groups.isNotEmpty()) {
            groups.take(2).joinToString(" & ") { it.displayName }
        } else null
    }

    val rawVolume = remember(workout.exercise) {
        workout.exercise.sumOf { exercise ->
            exercise.sets.sumOf { (it.weight * it.reps).toDouble() }
        }
    }

    val displayVolume = remember(rawVolume, isMetric) {
        if (isMetric) rawVolume.toInt() else (rawVolume * 2.20462).toInt()
    }

    val totalSets = remember(workout.exercise) {
        workout.exercise.sumOf { it.sets.size }
    }

    // Gmail-style swipe actions width
    val actionsWidth = 144.dp
    val actionsWidthPx = with(density) { actionsWidth.toPx() }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // ── Hidden Action Buttons Revealed on Swipe Left ──
        if (offsetX.value < -1f) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(actionsWidth)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Button
                Surface(
                    onClick = {
                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                        onEdit()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = currentAccent.copy(alpha = 0.18f),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = MaximusIcons.Routines.Edit,
                            contentDescription = "Edit Workout",
                            tint = currentAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Edit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentAccent
                        )
                    }
                }

                // Delete Button
                Surface(
                    onClick = {
                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                        onDelete()
                    },
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 20.dp, bottomEnd = 20.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Icon(
                        imageVector = MaximusIcons.History.Delete,
                        contentDescription = "Delete Workout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Delete",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

        // ── Foreground Card with Snapping Drag Gesture ──
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target = if (offsetX.value < -actionsWidthPx * 0.22f) {
                                    MaximusHapticUtils.perform(haptic, MaximusEvent.SWIPE_REVEAL_THRESHOLD)
                                    -actionsWidthPx
                                } else {
                                    0f
                                }
                                offsetX.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount).coerceIn(-actionsWidthPx, 0f)
                            scope.launch { offsetX.snapTo(newOffset) }
                        }
                    )
                },
            shape = MaximusShapes.Card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 12.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 10.dp)
            ) {
                // --- 1. RESPONSIVE HEADER ROW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = workout.name.ifBlank { "Workout Session" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            primaryMuscleGroup?.let { split ->
                                Text(
                                    text = "•  $split",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = currentAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Top Lift PR Badge if present, otherwise clean Sets Pill
                    if (topLift != null) {
                        Surface(
                            shape = MaximusShapes.Pill,
                            color = Color(0xFFFFB300).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.35f)),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = MaximusIcons.History.TrophyMilestone,
                                    contentDescription = "Top Lift",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = topLift,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = MaximusShapes.Pill,
                            color = currentAccent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$totalSets Sets",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = currentAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // --- 2. SLEEK EXERCISE ICONS ROW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    workout.exercise.take(if (isCompact) 6 else 5).forEach { ex ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onExerciseClick(ex.name) }
                                .padding(4.dp)
                        ) {
                            ExerciseIcon(
                                name = ex.name,
                                size = if (isCompact) 26.dp else 28.dp
                            )
                        }
                    }

                    if (workout.exercise.size > (if (isCompact) 6 else 5)) {
                        Text(
                            text = "+${workout.exercise.size - (if (isCompact) 6 else 5)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- 3. RESPONSIVE COMPACT FOOTER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${workout.exercise.size} Movements • $totalSets Sets",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.width(8.dp))

                    if (displayVolume > 0) {
                        Text(
                            text = "${"%,d".format(displayVolume)} $unit Load",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = currentAccent,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "Bodyweight",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
