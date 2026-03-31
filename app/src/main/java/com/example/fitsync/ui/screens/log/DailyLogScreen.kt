package com.example.fitsync.ui.screens.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitsync.ui.components.ExerciseLogCard
import com.example.fitsync.ui.theme.*
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    onBackClick: () -> Unit,
    onFinishWorkout: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val weightUnit = "kg"

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val availableExercises = listOf(
        "Barbell Squat", "Bench Press", "Deadlift", "Overhead Press",
        "Pull Ups", "Barbell Row", "Dips"
    ).filter { it.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Workout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveWorkout()
                            onFinishWorkout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 4.dp) // Reduced padding
                    ) {
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = AccentRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp) // Tighter FAB
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        },
        containerColor = BgLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 12.dp), // Tighter horizontal margins
            verticalArrangement = Arrangement.spacedBy(8.dp), // Tighter vertical spacing
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) { // Reduced top padding
                    Text(
                        text = uiState.date,
                        style = MaterialTheme.typography.headlineSmall, // Smaller for better fit
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Current Session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftGray
                    )
                }
            }

            items(uiState.exercises) { exercise ->
                val meta = ExerciseVisuals.getMetaData(exercise.name)
                ExerciseLogCard(
                    exerciseName = exercise.name,
                    sets = exercise.sets,
                    unit = weightUnit,
                    accentColor = meta.accentColor,
                    exerciseIcon = meta.icon,
                    onAddSet = { viewModel.addSet(exercise.name) },
                    onUpdateSet = { setNo, w, r -> viewModel.updateSet(exercise.name, setNo, w, r) },
                    onToggleSet = { setNo -> viewModel.toggleSetCompletion(exercise.name, setNo) },
                    onDeleteSet = { setNo -> viewModel.deleteSet(exercise.name, setNo) },
                    onDeleteExercise = { viewModel.removeExercise(exercise.name) }
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    searchQuery = ""
                },
                sheetState = sheetState,
                containerColor = Color.White,
                // Removed windowInsets to fix the error
                modifier = Modifier.fillMaxHeight(1f),
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                // Point 1 & 2: Fix Overlap and Height
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // This pushes content above Bottom Nav
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp) // Extra space for the bottom handle
                ) {
                    Text(
                        "Add Exercise",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search exercises...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyBlue,
                            unfocusedBorderColor = SoftGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Use a fixed height or weight so the sheet stays stable
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 300.dp, max = 500.dp), // Keeps height consistent
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availableExercises) { name ->
                            val meta = ExerciseVisuals.getMetaData(name)
                            ListItem(
                                headlineContent = {
                                    Text(name, fontWeight = FontWeight.Medium)
                                },
                                supportingContent = {
                                    Text(meta.category, style = MaterialTheme.typography.bodySmall)
                                },
                                leadingContent = {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        color = meta.accentColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = meta.icon,
                                            contentDescription = null,
                                            modifier = Modifier.padding(10.dp),
                                            tint = meta.accentColor
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addExercise(name)
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) showSheet = false
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}