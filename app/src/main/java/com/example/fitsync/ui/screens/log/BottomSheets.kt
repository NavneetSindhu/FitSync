package com.example.fitsync.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.fitsync.ui.components.FitSyncFilterChip // Use our custom chip

// ── IMPORT OUR GLOBAL PREFERENCES ──────────────────────────────────────────
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards
import kotlinx.coroutines.launch

data class ExerciseTemplate(val name: String, val category: String)

val MasterExerciseList = listOf(
    // PUSH
    ExerciseTemplate("Bench Press", "Push"),
    ExerciseTemplate("Overhead Press", "Push"),
    ExerciseTemplate("Incline DB Press", "Push"),
    ExerciseTemplate("Dips", "Push"),
    ExerciseTemplate("Tricep Pushdowns", "Push"),
    ExerciseTemplate("Lateral Raises", "Push"),
    ExerciseTemplate("Skull Crushers", "Push"),

    // PULL
    ExerciseTemplate("Deadlift", "Pull"),
    ExerciseTemplate("Barbell Row", "Pull"),
    ExerciseTemplate("Pull Ups", "Pull"),
    ExerciseTemplate("Lat Pulldown", "Pull"),
    ExerciseTemplate("Face Pulls", "Pull"),
    ExerciseTemplate("Bicep Curls", "Pull"),
    ExerciseTemplate("Hammer Curls", "Pull"),

    // LEGS
    ExerciseTemplate("Barbell Squat", "Legs"),
    ExerciseTemplate("Leg Press", "Legs"),
    ExerciseTemplate("Lunges", "Legs"),
    ExerciseTemplate("Leg Extensions", "Legs"),
    ExerciseTemplate("Hamstring Curls", "Legs"),
    ExerciseTemplate("Calf Raises", "Legs"),
    ExerciseTemplate("Bulgarian Split Squat", "Legs"),

    // CALISTHENICS
    ExerciseTemplate("Muscle Ups", "Calisthenics"),
    ExerciseTemplate("Push Ups", "Calisthenics"),
    ExerciseTemplate("Handstand Pushups", "Calisthenics"),
    ExerciseTemplate("Chin Ups", "Calisthenics"),
    ExerciseTemplate("L-Sit Hold", "Calisthenics"),
    ExerciseTemplate("Planche Lean", "Calisthenics"),
    ExerciseTemplate("Front Lever", "Calisthenics"),

    // CARDIO
    ExerciseTemplate("Running", "Cardio"),
    ExerciseTemplate("Cycling", "Cardio"),
    ExerciseTemplate("Jump Rope", "Cardio"),
    ExerciseTemplate("Swimming", "Cardio"),
    ExerciseTemplate("Rowing Machine", "Cardio"),
    ExerciseTemplate("Burpees", "Cardio")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExerciseBottomSheet(
    onDismiss: () -> Unit,
    onAddExercise: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Push", "Pull", "Legs", "Calisthenics", "Cardio")

    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    val filteredExercises = remember(searchQuery, selectedCategory) {
        MasterExerciseList.filter { exercise ->
            val matchesSearch = exercise.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || exercise.category == selectedCategory
            matchesSearch && matchesCategory
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
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Select Exercise",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = currentAccent) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentAccent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(12.dp))

            // 🔥 UPDATED: Using FitSyncFilterChip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    FitSyncFilterChip(
                        label = category,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredExercises) { exercise ->
                    Surface(
                        onClick = {
                            onAddExercise(exercise.name)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ExerciseIcon(name = exercise.name, size = if (isCompact) 40.dp else 48.dp)
                            Spacer(Modifier.height(if (isCompact) 8.dp else 12.dp))
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = exercise.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

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
    val currentAccent = LocalAccentColor.current

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
                    FitSyncFilterChip(
                        label = option,
                        isSelected = selectedOption == option,
                        onClick = { selectedOption = option }
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentAccent)
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
                        Box(Modifier.size(8.dp).background(currentAccent, CircleShape))
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
                colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text("Let's Go!", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}