package com.minimize.maximus.ui.screens.log.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.exercise.EquipmentType
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.components.LocalBottomSheetDismiss
import com.minimize.maximus.ui.components.MaximusFilterChip
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.LocalCompactCards
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseBottomSheet(
    exercises: List<ExerciseDefinition>,
    onDismiss: () -> Unit,
    onAddExercise: (String) -> Unit,
    onCreateCustomExercise: ((String, MuscleGroup, EquipmentType) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleFilter by remember { mutableStateOf<MuscleGroup?>(null) }
    var showCreateCustomSheet by remember { mutableStateOf(false) }

    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current
    val haptic = LocalHapticFeedback.current

    val filteredExercises = remember(exercises, searchQuery, selectedMuscleFilter) {
        exercises.filter { exercise ->
            val matchesSearch = searchQuery.isBlank() || exercise.name.contains(searchQuery, ignoreCase = true)
            val matchesMuscle = selectedMuscleFilter == null ||
                    exercise.primaryMuscle == selectedMuscleFilter ||
                    exercise.secondaryMuscles.contains(selectedMuscleFilter)
            matchesSearch && matchesMuscle
        }
    }

    MaximusModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        val dismissSheet = LocalBottomSheetDismiss.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.log_select_exercise_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (onCreateCustomExercise != null) {
                FilledTonalButton(
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        showCreateCustomSheet = true
                    },
                    shape = MaximusShapes.Pill,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = MaximusIcons.Routines.CreateNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.library_add_custom),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.log_search_exercises_placeholder)) },
                leadingIcon = { Icon(MaximusIcons.Navigation.Search, null, tint = currentAccent) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(MaximusIcons.Navigation.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentAccent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(12.dp))

            // Muscle Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    MaximusFilterChip(
                        label = stringResource(R.string.library_filter_all),
                        isSelected = selectedMuscleFilter == null,
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            selectedMuscleFilter = null
                        }
                    )
                }
                items(MuscleGroup.entries) { muscle ->
                    MaximusFilterChip(
                        label = muscle.displayName,
                        isSelected = selectedMuscleFilter == muscle,
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            selectedMuscleFilter = if (selectedMuscleFilter == muscle) null else muscle
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = filteredExercises.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ExerciseGridTransition",
                modifier = Modifier.weight(1f)
            ) { isEmpty ->
                if (isEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.log_no_exercises_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (searchQuery.isNotBlank() && onCreateCustomExercise != null) {
                                Button(
                                    onClick = {
                                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                        showCreateCustomSheet = true
                                    },
                                    shape = MaximusShapes.Pill,
                                    colors = ButtonDefaults.buttonColors(containerColor = currentAccent)
                                ) {
                                    Icon(MaximusIcons.Routines.CreateNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Create \"$searchQuery\"",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredExercises, key = { it.id.toString() + "_" + it.name }) { exercise ->
                            Surface(
                                onClick = {
                                    MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                                    onAddExercise(exercise.name)
                                    dismissSheet()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ExerciseIcon(
                                        name = exercise.name,
                                        muscleGroup = exercise.primaryMuscle,
                                        size = if (isCompact) 40.dp else 48.dp
                                    )
                                    Spacer(Modifier.height(if (isCompact) 8.dp else 10.dp))
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${exercise.primaryMuscle.displayName} • ${exercise.equipment.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = currentAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Modal to create custom movement directly into DB
    if (showCreateCustomSheet && onCreateCustomExercise != null) {
        var customName by remember { mutableStateOf(searchQuery.trim()) }
        var selectedMuscle by remember { mutableStateOf(MuscleGroup.CHEST) }
        var selectedEquipment by remember { mutableStateOf(EquipmentType.BARBELL) }

        MaximusModalBottomSheet(onDismissRequest = { showCreateCustomSheet = false }) {
            MaximusSheetHeader(title = stringResource(R.string.library_create_custom_title))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text(stringResource(R.string.library_custom_name_label)) },
                    placeholder = { Text(stringResource(R.string.library_custom_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.library_primary_muscle_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MuscleGroup.entries) { m ->
                            val isSelected = selectedMuscle == m
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMuscle = m },
                                label = { Text(m.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = MaximusShapes.Pill
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.library_equipment_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(EquipmentType.entries) { eq ->
                            val isSelected = selectedEquipment == eq
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEquipment = eq },
                                label = { Text(eq.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = MaximusShapes.Pill
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        onCreateCustomExercise(customName, selectedMuscle, selectedEquipment)
                        showCreateCustomSheet = false
                        onDismiss()
                    },
                    enabled = customName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                    shape = MaximusShapes.Pill,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.library_save_custom_btn), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
