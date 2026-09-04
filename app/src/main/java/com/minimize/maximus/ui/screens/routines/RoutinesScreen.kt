package com.minimize.maximus.ui.screens.routines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.routine.DaySchedule
import com.minimize.maximus.domain.model.routine.SplitPresetType
import com.minimize.maximus.domain.model.routine.WorkoutRoutine
import com.minimize.maximus.ui.components.EmptyStateView
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.components.MaximusConfirmDialog
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.components.ToastType
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    onCreateRoutineClick: () -> Unit,
    onEditRoutineClick: (Long) -> Unit,
    onStartWorkout: () -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val toastManager = LocalMaximusToast.current

    var routineToDelete by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var selectedDayToAssign by remember { mutableStateOf<DaySchedule?>(null) }
    var previewTemplate by remember { mutableStateOf<WorkoutRoutine?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.routines_title),
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = MaximusIcons.Navigation.BackArrow,
                            contentDescription = stringResource(R.string.action_cancel)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                            onCreateRoutineClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentAccent,
                            contentColor = if (currentAccent.luminance() > 0.45f) Color(0xFF121214) else Color.White
                        ),
                        shape = MaximusShapes.Pill,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp)
                    ) {
                        Icon(MaximusIcons.Routines.CreateNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.routines_create_new), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented Tab Row
            val tabs = listOf(
                stringResource(R.string.routines_tab_my_routines),
                stringResource(R.string.routines_tab_split_planner),
                stringResource(R.string.routines_tab_explore)
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = uiState.selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) currentAccent else Color.Transparent)
                                .clickable {
                                    MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                                    viewModel.selectTab(index)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) {
                                    if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                                } else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                0 -> MyRoutinesTab(
                    routines = uiState.routines,
                    accentColor = currentAccent,
                    onPreview = { routine ->
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        previewTemplate = routine
                    },
                    onStart = { routine ->
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        viewModel.startWorkoutFromRoutine(routine, onStartWorkout)
                    },
                    onEdit = onEditRoutineClick,
                    onDuplicate = { routine ->
                        viewModel.duplicateRoutine(routine.id)
                    },
                    onDelete = { routine ->
                        routineToDelete = routine
                    },
                    onCreateNew = onCreateRoutineClick
                )
                1 -> WeeklyPlannerTab(
                    weeklyPlan = uiState.weeklyPlan,
                    routines = uiState.routines,
                    accentColor = currentAccent,
                    onPresetSelected = { preset ->
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        viewModel.applySplitPreset(preset)
                    },
                    onDayClick = { daySchedule ->
                        MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                        selectedDayToAssign = daySchedule
                    }
                )
                2 -> TemplatesExploreTab(
                    templates = uiState.defaultTemplates,
                    accentColor = currentAccent,
                    onPreview = { routine ->
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        previewTemplate = routine
                    },
                    onStart = { routine ->
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        viewModel.startWorkoutFromRoutine(routine, onStartWorkout)
                    },
                    onCustomize = { routine ->
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        viewModel.duplicateRoutine(routine.id)
                        toastManager.showToast("${routine.name} added to My Routines!", ToastType.SUCCESS)
                    }
                )
            }
        }
    }

    // Centralized Delete Confirmation Dialog
    routineToDelete?.let { routine ->
        MaximusConfirmDialog(
            title = stringResource(R.string.routines_delete_dialog_title),
            message = stringResource(R.string.routines_delete_dialog_msg, routine.name),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            icon = Icons.Default.DeleteOutline,
            isDestructive = true,
            onConfirm = {
                viewModel.deleteRoutine(routine.id)
                routineToDelete = null
            },
            onDismiss = { routineToDelete = null }
        )
    }

    // Template Preview Bottom Sheet
    previewTemplate?.let { template ->
        TemplatePreviewBottomSheet(
            template = template,
            accentColor = currentAccent,
            onDismiss = { previewTemplate = null },
            onStart = { routine ->
                viewModel.startWorkoutFromRoutine(routine, onStartWorkout)
            },
            onCustomize = { routine ->
                viewModel.duplicateRoutine(routine.id)
            }
        )
    }

    // Assign Routine to Day Bottom Sheet
    selectedDayToAssign?.let { daySchedule ->
        AssignDayBottomSheet(
            daySchedule = daySchedule,
            routines = uiState.routines,
            accentColor = currentAccent,
            onDismiss = { selectedDayToAssign = null },
            onAssignRoutine = { routine ->
                viewModel.assignRoutineToDay(daySchedule.dayOfWeek, routine?.id, routine?.name)
                selectedDayToAssign = null
            },
            onSetRestDay = {
                viewModel.assignRoutineToDay(daySchedule.dayOfWeek, null, null)
                selectedDayToAssign = null
            }
        )
    }
}

@Composable
private fun MyRoutinesTab(
    routines: List<WorkoutRoutine>,
    accentColor: Color,
    onPreview: (WorkoutRoutine) -> Unit,
    onStart: (WorkoutRoutine) -> Unit,
    onEdit: (Long) -> Unit,
    onDuplicate: (WorkoutRoutine) -> Unit,
    onDelete: (WorkoutRoutine) -> Unit,
    onCreateNew: () -> Unit
) {
    if (routines.isEmpty()) {
        EmptyStateView(
            title = stringResource(R.string.routines_empty_title),
            subtitle = stringResource(R.string.routines_empty_desc),
            lottieRes = R.raw.anim_cat_relaxing,
            fallbackIcon = Icons.Default.FormatListBulleted,
            ctaText = stringResource(R.string.routines_create_new),
            onCtaClick = onCreateNew
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(routines, key = { index, r -> if (r.id > 0L) "routine_${r.id}" else "routine_${index}_${r.name}" }) { _, routine ->
                RoutineCard(
                    routine = routine,
                    accentColor = accentColor,
                    onPreview = { onPreview(routine) },
                    onStart = { onStart(routine) },
                    onEdit = { onEdit(routine.id) },
                    onDuplicate = { onDuplicate(routine) },
                    onDelete = { onDelete(routine) }
                )
            }
            item { Spacer(Modifier.height(MaximusDimens.FloatingNavHeight + 32.dp)) }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: WorkoutRoutine,
    accentColor: Color,
    onPreview: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = {
            MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
            onPreview()
        },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    if (routine.description.isNotBlank()) {
                        Text(
                            text = routine.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.routines_edit_routine_btn)) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.routines_duplicate_btn)) },
                            onClick = { showMenu = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            // Exercise Tags Preview
            if (routine.exercises.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(routine.exercises.take(4), key = { index, ex -> "card_ex_${index}_${ex.exerciseName}" }) { _, ex ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${ex.targetSets}× ${ex.exerciseName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (routine.exercises.size > 4) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = "+${routine.exercises.size - 4}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${routine.estimatedDurationMinutes} mins • ${routine.exercises.size} exercises",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (accentColor.luminance() > 0.45f) Color.Black else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.routines_start_routine_btn), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun WeeklyPlannerTab(
    weeklyPlan: com.minimize.maximus.domain.model.routine.WeeklyPlan,
    routines: List<WorkoutRoutine>,
    accentColor: Color,
    onPresetSelected: (SplitPresetType) -> Unit,
    onDayClick: (DaySchedule) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Split Selector
        item {
            Text(
                text = stringResource(R.string.split_presets_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SplitPresetType.entries) { preset ->
                    val isSelected = weeklyPlan.presetType == preset
                    Surface(
                        onClick = { onPresetSelected(preset) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null,
                        tonalElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = preset.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 7-Day Day-by-Day Schedule List
        item {
            Text(
                text = stringResource(R.string.split_custom_schedule_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val daysOrder = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )

        items(daysOrder) { day ->
            val schedule = weeklyPlan.days[day] ?: DaySchedule(day, isRestDay = true)
            val dayName = day.getDisplayName(TextStyle.FULL, Locale.getDefault())

            Surface(
                onClick = { onDayClick(schedule) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (schedule.isRestDay) MaterialTheme.colorScheme.surfaceVariant
                                    else accentColor.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.name.take(3),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (schedule.isRestDay) MaterialTheme.colorScheme.onSurfaceVariant else accentColor
                            )
                        }

                        Column {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (schedule.isRestDay) stringResource(R.string.split_rest_day) else (schedule.routineName ?: "Assigned Workout"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (schedule.isRestDay) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Edit Day",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(Modifier.height(MaximusDimens.FloatingNavHeight + 32.dp)) }
    }
}

@Composable
private fun TemplatesExploreTab(
    templates: List<WorkoutRoutine>,
    accentColor: Color,
    onPreview: (WorkoutRoutine) -> Unit,
    onStart: (WorkoutRoutine) -> Unit,
    onCustomize: (WorkoutRoutine) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(templates, key = { index, t -> "template_${index}_${t.name}" }) { _, template ->
            Surface(
                onClick = {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                    onPreview(template)
                },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Surface(
                            shape = MaximusShapes.Pill,
                            color = accentColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${template.exercises.size} Exercises",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onCustomize(template) },
                            shape = MaximusShapes.Pill,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Save as Mine", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { onStart(template) },
                            shape = MaximusShapes.Pill,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = if (accentColor.luminance() > 0.4f) Color(0xFF121214) else Color.White
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(stringResource(R.string.routines_start_routine_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(MaximusDimens.FloatingNavHeight + 32.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatePreviewBottomSheet(
    template: WorkoutRoutine,
    accentColor: Color,
    onDismiss: () -> Unit,
    onStart: (WorkoutRoutine) -> Unit,
    onCustomize: (WorkoutRoutine) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    MaximusModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = MaximusShapes.Pill,
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${template.exercises.size} Movements",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Informational Notice Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (template.isCustom && !template.isDefaultTemplate) {
                            "Launch this routine immediately, or duplicate to make a customized variation."
                        } else {
                            "You can customize, edit, reorder, delete, or add exercises after saving."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "EXERCISES IN THIS ROUTINE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Exercise List (scrollable body)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(template.exercises) { ex ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExerciseIcon(name = ex.exerciseName, size = 36.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ex.exerciseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ex.targetSets} sets × ${ex.targetRepsRange} reps • ${ex.restSeconds}s rest",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Pinned Bottom CTAs with 50.dp Pill Shapes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        onCustomize(template)
                        onDismiss()
                    },
                    shape = MaximusShapes.Pill,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(
                        text = if (template.isCustom && !template.isDefaultTemplate) "Duplicate" else "Save as Mine",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        onStart(template)
                        onDismiss()
                    },
                    shape = MaximusShapes.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (accentColor.luminance() > 0.4f) Color(0xFF121214) else Color.White
                    ),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text("Start Workout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignDayBottomSheet(
    daySchedule: DaySchedule,
    routines: List<WorkoutRoutine>,
    accentColor: Color,
    onDismiss: () -> Unit,
    onAssignRoutine: (WorkoutRoutine?) -> Unit,
    onSetRestDay: () -> Unit
) {
    val dayName = daySchedule.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

    MaximusModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MaximusSheetHeader(
                title = stringResource(R.string.split_assign_day, dayName)
            )

            // Rest Day Option
            Surface(
                onClick = onSetRestDay,
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF10B981).copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(MaximusIcons.Routines.RestDaySpa, contentDescription = null, tint = Color(0xFF10B981))
                    Text(
                        text = stringResource(R.string.split_set_rest),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "OR CHOOSE A ROUTINE:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            routines.forEach { routine ->
                Surface(
                    onClick = { onAssignRoutine(routine) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(routine.name, fontWeight = FontWeight.Bold)
                            Text("${routine.exercises.size} exercises • ${routine.estimatedDurationMinutes} mins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                    }
                }
            }
        }
    }
}
