package com.minimize.maximus.ui.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.exercise.EquipmentType
import com.minimize.maximus.domain.model.exercise.ExerciseDefinition
import com.minimize.maximus.ui.components.ExerciseIcon
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onBackClick: () -> Unit,
    onExerciseSelected: (ExerciseDefinition) -> Unit,
    viewModel: ExerciseLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current

    var showCreateCustomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library_title), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                            showCreateCustomSheet = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp)
                    ) {
                        Icon(MaximusIcons.Routines.CreateNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.library_add_custom), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.library_search_placeholder)) },
                leadingIcon = { Icon(MaximusIcons.Navigation.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(MaximusIcons.Navigation.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Muscle Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedMuscleFilter == null,
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            viewModel.selectMuscleFilter(null)
                        },
                        label = { Text(stringResource(R.string.library_filter_all), fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                items(MuscleGroup.entries) { muscle ->
                    val isSelected = uiState.selectedMuscleFilter == muscle
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            viewModel.selectMuscleFilter(muscle)
                        },
                        label = { Text(muscle.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = currentAccent.copy(alpha = 0.2f),
                            selectedLabelColor = currentAccent
                        )
                    )
                }
            }

            // Equipment Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items(EquipmentType.entries) { eq ->
                    val isSelected = uiState.selectedEquipmentFilter == eq
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            viewModel.selectEquipmentFilter(eq)
                        },
                        label = { Text(eq.displayName, fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Exercise List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.filteredExercises, key = { index, ex -> "lib_${index}_${ex.name}" }) { _, ex ->
                    Surface(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                            onExerciseSelected(ex)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                ExerciseIcon(name = ex.name, muscleGroup = ex.primaryMuscle, size = 38.dp)
                                Column {
                                    Text(
                                        text = ex.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ex.primaryMuscle.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = currentAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = ex.equipment.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            FilledIconButton(
                                onClick = {
                                    MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                                    onExerciseSelected(ex)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = currentAccent.copy(alpha = 0.15f),
                                    contentColor = currentAccent
                                ),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(MaximusIcons.Routines.CreateNew, contentDescription = "Add", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick-Add Custom Movement Bottom Sheet
    if (showCreateCustomSheet) {
        CreateCustomExerciseBottomSheet(
            accentColor = currentAccent,
            onDismiss = { showCreateCustomSheet = false },
            onSave = { name, muscle, equipment ->
                viewModel.createCustomExercise(name, muscle, equipment) { newDef ->
                    showCreateCustomSheet = false
                    onExerciseSelected(newDef)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCustomExerciseBottomSheet(
    accentColor: Color,
    onDismiss: () -> Unit,
    onSave: (String, MuscleGroup, EquipmentType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedEquipment by remember { mutableStateOf(EquipmentType.BARBELL) }

    com.minimize.maximus.ui.components.MaximusModalBottomSheet(onDismissRequest = onDismiss) {
        com.minimize.maximus.ui.components.MaximusSheetHeader(
            title = stringResource(R.string.library_create_custom_title)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.library_custom_name_label)) },
                placeholder = { Text(stringResource(R.string.library_custom_name_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Muscle Group Selector (2-Row Layout)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.library_primary_muscle_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val halfSize = (MuscleGroup.entries.size + 1) / 2
                val row1 = MuscleGroup.entries.take(halfSize)
                val row2 = MuscleGroup.entries.drop(halfSize)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(row1) { m ->
                            val isSelected = selectedMuscle == m
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMuscle = m },
                                label = { Text(m.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = com.minimize.maximus.ui.theme.MaximusShapes.Pill
                            )
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(row2) { m ->
                            val isSelected = selectedMuscle == m
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMuscle = m },
                                label = { Text(m.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = com.minimize.maximus.ui.theme.MaximusShapes.Pill
                            )
                        }
                    }
                }
            }

            // Equipment Selector
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
                            shape = com.minimize.maximus.ui.theme.MaximusShapes.Pill
                        )
                    }
                }
            }

            Button(
                onClick = { onSave(name, selectedMuscle, selectedEquipment) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = com.minimize.maximus.ui.theme.MaximusShapes.Pill,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.library_save_custom_btn), fontWeight = FontWeight.Bold)
            }
        }
    }
}
