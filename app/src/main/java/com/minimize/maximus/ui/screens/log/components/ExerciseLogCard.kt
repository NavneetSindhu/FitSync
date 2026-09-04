package com.minimize.maximus.ui.screens.log.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.domain.model.SetType
import com.minimize.maximus.domain.model.WorkoutSet
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.components.SetTypeLegend
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.LocalCompactCards
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ExerciseLogCard(
    exerciseName: String,
    sets: List<WorkoutSet>,
    unit: String,
    accentColor: Color,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, Float, Int) -> Unit,
    onUpdateSetType: (Int, String) -> Unit,
    onToggleSet: (Int) -> Unit,
    onDeleteSet: (Int) -> Unit,
    onDeleteExercise: () -> Unit,
    onOpenPlateCalculator: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    val isBarbellMovement = remember(exerciseName) {
        listOf("Barbell", "Bench Press", "Squat", "Deadlift", "Overhead Press", "Incline")
            .any { exerciseName.contains(it, ignoreCase = true) }
    }

    val isCalisthenicsHold = remember(exerciseName) {
        listOf("Planche", "Lever", "Handstand", "Hold", "Plank")
            .any { exerciseName.contains(it, ignoreCase = true) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    ExerciseIcon(name = exerciseName, size = if (isCompact) 36.dp else 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = exerciseName,
                            style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isBarbellMovement) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_barbell_exercise_tag),
                                style = MaterialTheme.typography.labelSmall,
                                color = currentAccent
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isBarbellMovement) {
                        val maxWeight = sets.maxOfOrNull { it.weight } ?: 60f
                        IconButton(
                            onClick = { onOpenPlateCalculator(maxWeight) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = MaximusIcons.WorkoutLog.PlateCalculator,
                                contentDescription = androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_plate_calc_desc),
                                tint = currentAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreHoriz, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_delete_exercise_menu), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(MaximusIcons.WorkoutLog.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDeleteExercise()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(if (isCompact) 10.dp else 16.dp))

            // ── Table Column Headers (Strictly matching SetLogRow columns) ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                    Text(androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_set_header), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(6.dp))
                Box(Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                    Text(androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_type_header), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(6.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(unit.uppercase(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(6.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isCalisthenicsHold) androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_time_header) else androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_reps_header),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.width(44.dp))
            }

            sets.forEach { set ->
                key(set.setNumber) {
                    SwipeToRevealDelete(onDelete = { onDeleteSet(set.setNumber) }) {
                        SetLogRow(
                            set = set,
                            isDurationType = isCalisthenicsHold,
                            onValueChange = { w, r -> onUpdateSet(set.setNumber, w, r) },
                            onTypeCycle = {
                                val currentType = SetType.fromCode(set.setType)
                                val nextType = when (currentType) {
                                    SetType.NORMAL -> SetType.WARMUP
                                    SetType.WARMUP -> SetType.DROP
                                    SetType.DROP -> SetType.FAILURE
                                    SetType.FAILURE -> SetType.NORMAL
                                }
                                onUpdateSetType(set.setNumber, nextType.code)
                            },
                            onToggle = { onToggleSet(set.setNumber) }
                        )
                    }
                }
            }

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = currentAccent)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(androidx.compose.ui.res.stringResource(com.minimize.maximus.R.string.log_add_set), fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(4.dp))

            // ── Set Type Legend Bar (Bottom Centered Shared Component) ──
            SetTypeLegend()
        }
    }
}

@Composable
fun SwipeToRevealDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val buttonWidth = 72.dp
    val buttonWidthPx = with(density) { buttonWidth.toPx() }
    val offsetX = remember { Animatable(0f) }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(buttonWidth)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable {
                    scope.launch {
                        offsetX.animateTo(-500f, tween(200))
                        onDelete()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target = if (offsetX.value < -buttonWidthPx * 0.22f) -buttonWidthPx else 0f
                                offsetX.animateTo(
                                    targetValue = target,
                                    animationSpec = androidx.compose.animation.core.spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = androidx.compose.animation.core.spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount).coerceIn(-buttonWidthPx, 0f)
                            scope.launch { offsetX.snapTo(newOffset) }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun SetLogRow(
    set: WorkoutSet,
    isDurationType: Boolean,
    onValueChange: (Float, Int) -> Unit,
    onTypeCycle: () -> Unit,
    onToggle: () -> Unit
) {
    val type = SetType.fromCode(set.setType)
    val typeColor = when (type) {
        SetType.NORMAL -> Color.Transparent
        SetType.WARMUP -> Color(0xFFFF9800) // Orange
        SetType.DROP -> Color(0xFF9C27B0) // Purple
        SetType.FAILURE -> Color(0xFFE91E63) // Pink/Red
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set Number Badge
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (set.isCompleted) SuccessGreen.copy(0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = set.setNumber.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (set.isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.width(6.dp))

        // Set Type Tag Chip (Tappable to cycle: N -> W -> D -> F)
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (type == SetType.NORMAL) MaterialTheme.colorScheme.surfaceVariant.copy(0.4f) else typeColor.copy(0.2f))
                    .clickable { onTypeCycle() }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.code,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (type == SetType.NORMAL) MaterialTheme.colorScheme.onSurfaceVariant else typeColor
                )
            }
        }

        Spacer(Modifier.width(6.dp))

        // Weight Input Box
        GymInputField(
            value = if (set.weight == 0f) "" else (if (set.weight % 1 == 0f) set.weight.toInt().toString() else set.weight.toString()),
            hintText = "0",
            onValueChange = { onValueChange(it.toFloatOrNull() ?: 0f, set.reps) },
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(6.dp))

        // Reps / Duration Input Box
        GymInputField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            hintText = if (isDurationType) "0s" else "0",
            onValueChange = { onValueChange(set.weight, it.toIntOrNull() ?: 0) },
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(4.dp))

        IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = if (set.isCompleted) MaximusIcons.WorkoutLog.CheckCompleted else MaximusIcons.WorkoutLog.Unchecked,
                contentDescription = "Toggle Set",
                tint = if (set.isCompleted) SuccessGreen else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun GymInputField(
    value: String,
    hintText: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text(hintText, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), fontSize = 16.sp)
                }
                inner()
            }
        }
    )
}
