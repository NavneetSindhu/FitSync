package com.minimize.maximus.ui.screens.routines

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.routine.RoutineExercise
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBuilderScreen(
    onBackClick: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RoutineBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    var draggedExerciseName by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val itemThresholdPx = with(density) { 90.dp.toPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.routineId > 0) stringResource(R.string.builder_edit_title)
                        else stringResource(R.string.builder_create_title),
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(MaximusIcons.Navigation.BackArrow, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                            viewModel.saveRoutine(onSaved)
                        },
                        enabled = uiState.name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentAccent,
                            contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                        ),
                        shape = MaximusShapes.Pill,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp)
                    ) {
                        Icon(MaximusIcons.Navigation.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Routine Name Input Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.builder_name_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            placeholder = { Text(stringResource(R.string.builder_name_placeholder)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Target Muscles Multi-Select (2 Rows)
            item {
                val muscleChunks = remember { MuscleGroup.entries.chunked((MuscleGroup.entries.size + 1) / 2) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.builder_target_muscles),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    muscleChunks.forEach { rowMuscles ->
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(rowMuscles) { muscle ->
                                val isSelected = uiState.targetMuscleGroups.contains(muscle.name)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                        viewModel.toggleMuscleGroup(muscle)
                                    },
                                    label = { Text(muscle.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    shape = MaximusShapes.Pill,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = currentAccent.copy(alpha = 0.2f),
                                        selectedLabelColor = currentAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Exercises Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.builder_exercises_section, uiState.exercises.size),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                            onOpenExerciseLibrary()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(MaximusIcons.Routines.LibraryBooks, contentDescription = "Full Library", tint = currentAccent, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Exercise Items with Drag-and-Drop Reorder & Target Set Controls
            itemsIndexed(
                items = uiState.exercises,
                key = { _, ex -> ex.exerciseName }
            ) { index, ex ->
                val isItemDragging = draggedExerciseName == ex.exerciseName
                RoutineExerciseItemCard(
                    modifier = if (isItemDragging) Modifier else Modifier.animateItem(),
                    exercise = ex,
                    index = index,
                    totalCount = uiState.exercises.size,
                    accentColor = currentAccent,
                    isDragging = isItemDragging,
                    dragOffsetY = if (isItemDragging) dragOffsetY else 0f,
                    onDragStart = {
                        draggedExerciseName = ex.exerciseName
                        dragOffsetY = 0f
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                    },
                    onDragEnd = {
                        draggedExerciseName = null
                        dragOffsetY = 0f
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                    },
                    onDragCancel = {
                        draggedExerciseName = null
                        dragOffsetY = 0f
                    },
                    onDragDelta = { delta ->
                        dragOffsetY += delta
                        val currentName = draggedExerciseName ?: return@RoutineExerciseItemCard
                        val currentIdx = uiState.exercises.indexOfFirst { it.exerciseName == currentName }
                        if (currentIdx != -1) {
                            if (dragOffsetY > itemThresholdPx && currentIdx < uiState.exercises.size - 1) {
                                viewModel.moveExercise(currentIdx, currentIdx + 1)
                                dragOffsetY -= itemThresholdPx
                                MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            } else if (dragOffsetY < -itemThresholdPx && currentIdx > 0) {
                                viewModel.moveExercise(currentIdx, currentIdx - 1)
                                dragOffsetY += itemThresholdPx
                                MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            }
                        }
                    },
                    onMoveUp = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                        viewModel.moveExercise(index, index - 1)
                    },
                    onMoveDown = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                        viewModel.moveExercise(index, index + 1)
                    },
                    onSetsChange = { viewModel.updateExerciseSets(index, it) },
                    onRepsChange = { viewModel.updateExerciseReps(index, it) },
                    onWeightChange = { viewModel.updateExerciseDefaultWeight(index, it) },
                    onRestChange = { viewModel.updateExerciseRestSeconds(index, it) },
                    onDelete = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        viewModel.removeExercise(index)
                    }
                )
            }

            // Add Exercise Big Card (Opens Exercise Library)
            item {
                Surface(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        onOpenExerciseLibrary()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(MaximusIcons.WorkoutLog.AddCircle, contentDescription = null, tint = currentAccent)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.builder_add_exercise_btn),
                            fontWeight = FontWeight.Bold,
                            color = currentAccent
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun RoutineExerciseItemCard(
    exercise: RoutineExercise,
    index: Int,
    totalCount: Int,
    accentColor: Color,
    isDragging: Boolean,
    dragOffsetY: Float,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Float) -> Unit,
    onRestChange: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardElevation by animateDpAsState(if (isDragging) 12.dp else 2.dp, label = "cardElevation")
    val cardScale by animateFloatAsState(if (isDragging) 1.03f else 1f, label = "cardScale")

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = cardElevation,
        shadowElevation = cardElevation,
        border = androidx.compose.foundation.BorderStroke(
            if (isDragging) 2.dp else 1.dp,
            if (isDragging) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 10f else 1f)
            .graphicsLayer {
                translationY = if (isDragging) dragOffsetY else 0f
                scaleX = cardScale
                scaleY = cardScale
            }
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Step Index Badge, Icon, Name, Drag Handle, Reorder Arrows, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Index Badge (Order in Routine)
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                    }

                    ExerciseIcon(name = exercise.exerciseName, size = 34.dp)

                    Column {
                        Text(
                            text = exercise.exerciseName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${exercise.targetSets} sets × ${exercise.targetReps} reps • ${exercise.restSeconds}s rest",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Drag Handle, Reordering & Delete Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Dedicated Drag Handle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDragging) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(30.dp)
                            .pointerInput(exercise.exerciseName) {
                                detectVerticalDragGestures(
                                    onDragStart = { onDragStart() },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragCancel() },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        onDragDelta(dragAmount)
                                    }
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Drag to reorder",
                                tint = if (isDragging) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        shape = RoundedCornerShape(8.dp),
                        color = if (index > 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = MaximusIcons.WorkoutLog.ReorderUp,
                                contentDescription = "Move Up",
                                tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        shape = RoundedCornerShape(8.dp),
                        color = if (index < totalCount - 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = MaximusIcons.WorkoutLog.ReorderDown,
                                contentDescription = "Move Down",
                                tint = if (index < totalCount - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(MaximusIcons.Navigation.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 3-Column Stepper Controls: Target Sets, Target Reps, Default Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sets Stepper
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SETS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { onSetsChange(exercise.targetSets - 1) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text("${exercise.targetSets}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        IconButton(onClick = { onSetsChange(exercise.targetSets + 1) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Reps Stepper
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { onRepsChange(exercise.targetReps - 1) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text("${exercise.targetReps}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        IconButton(onClick = { onRepsChange(exercise.targetReps + 1) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Weight Stepper
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { onWeightChange((exercise.defaultWeight - 2.5f).coerceAtLeast(0f)) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text("${exercise.defaultWeight.toInt()}kg", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        IconButton(onClick = { onWeightChange(exercise.defaultWeight + 2.5f) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
