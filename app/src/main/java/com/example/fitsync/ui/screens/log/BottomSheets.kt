package com.example.fitsync.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.components.ExerciseIcon
import com.example.fitsync.ui.theme.AccentRed
import kotlinx.coroutines.launch

// Simple data class to hold exercise info
data class ExerciseDef(val name: String, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseBottomSheet(
    onDismiss: () -> Unit,
    onAddExercise: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Push", "Pull", "Legs", "Core", "Cardio", "Calisthenics")

    // Expanded exercise database mapped to categories
    val allExercises = remember {
        listOf(
            // Push
            ExerciseDef("Bench Press", "Push"), ExerciseDef("Overhead Press", "Push"),
            ExerciseDef("Push Ups", "Push"), ExerciseDef("Dips", "Push"),
            ExerciseDef("Incline DB Press", "Push"), ExerciseDef("Tricep Extension", "Push"),
            // Pull
            ExerciseDef("Deadlift", "Pull"), ExerciseDef("Pull Ups", "Pull"),
            ExerciseDef("Barbell Row", "Pull"), ExerciseDef("Lat Pulldown", "Pull"),
            ExerciseDef("Bicep Curl", "Pull"), ExerciseDef("Face Pulls", "Pull"),
            // Legs
            ExerciseDef("Barbell Squat", "Legs"), ExerciseDef("Leg Press", "Legs"),
            ExerciseDef("Romanian Deadlift", "Legs"), ExerciseDef("Lunges", "Legs"),
            ExerciseDef("Calf Raises", "Legs"), ExerciseDef("Bulgarian Split Squat", "Legs"),
            // Core
            ExerciseDef("Plank", "Core"), ExerciseDef("Cable Crunches", "Core"),
            ExerciseDef("Hanging Leg Raises", "Core"), ExerciseDef("Russian Twists", "Core"),
            // Cardio
            ExerciseDef("Treadmill Running", "Cardio"), ExerciseDef("Rowing Machine", "Cardio"),
            ExerciseDef("Jump Rope", "Cardio"), ExerciseDef("Cycling", "Cardio"),
            // Calisthenics
            ExerciseDef("Muscle Ups", "Calisthenics"), ExerciseDef("Front Lever", "Calisthenics"),
            ExerciseDef("Handstand Pushup", "Calisthenics"), ExerciseDef("Pistol Squat", "Calisthenics")
        ).sortedBy { it.name }
    }

    // Logic: If search is active, ignore categories and search ALL. Otherwise, filter by category.
    val filteredExercises = remember(searchQuery, selectedCategory) {
        allExercises.filter { exercise ->
            if (searchQuery.isNotBlank()) {
                exercise.name.contains(searchQuery, ignoreCase = true)
            } else {
                selectedCategory == "All" || exercise.category == selectedCategory
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // Keeps it tall for easy scrolling
                .navigationBarsPadding()
        ) {
            // Header
            Text(
                text = "Select Exercise",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search any exercise...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentRed) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentRed,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(16.dp))

            // Category Filter Chips (Scrollable Horizontally)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            searchQuery = "" // Clear search when picking a category
                        },
                        label = { Text(category, fontWeight = FontWeight.Bold) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentRed,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = null
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Exercise List (Rectangular Cards)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExercises) { exercise ->
                    Surface(
                        onClick = {
                            onAddExercise(exercise.name)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon container
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                ExerciseIcon(name = exercise.name, size = 32.dp)
                            }

                            Spacer(Modifier.width(16.dp))

                            // Text Content
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = exercise.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Empty state if search yields nothing
                if (filteredExercises.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No exercises found 🕵️‍♂️", style = MaterialTheme.typography.titleMedium)
                            Text("Try a different search term.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// Keeping CreateWorkoutBottomSheet exactly as you had it below:
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateWorkoutBottomSheet(
    onDismiss: () -> Unit,
    onStartWorkout: (String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val presetOptions = listOf("Push Day", "Pull Day", "Leg Day", "Full Body", "Custom")
    var selectedOption by remember { mutableStateOf(presetOptions[0]) }
    var customName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Start New Session",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetOptions.forEach { option ->
                    FilterChip(
                        selected = selectedOption == option,
                        onClick = { selectedOption = option },
                        label = { Text(option) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (selectedOption == "Custom") {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Workout Name") },
                    placeholder = { Text("e.g. Weekend Blast") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentRed
                    )
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).background(AccentRed, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "You are starting a $selectedOption session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val finalName = if (selectedOption == "Custom") customName.ifBlank { "Custom" } else selectedOption
                    onStartWorkout(finalName, selectedOption == "Custom")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentRed
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("Let's Go!", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}