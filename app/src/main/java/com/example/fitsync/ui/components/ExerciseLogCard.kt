package com.example.fitsync.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.domain.model.WorkoutSet
import com.example.fitsync.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ExerciseLogCard(
    exerciseName: String,
    sets: List<WorkoutSet>,
    unit: String,
    accentColor: Color,
    exerciseIcon: ImageVector, // Note: ExerciseIcon component handles this internally now
    onAddSet: () -> Unit,
    onUpdateSet: (Int, Float, Int) -> Unit,
    onToggleSet: (Int) -> Unit,
    onDeleteSet: (Int) -> Unit,
    onDeleteExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 🔥 REPLACED: Using the new ExerciseIcon component
                    ExerciseIcon(name = exerciseName, size = 40.dp)

                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreHoriz, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Exercise", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDeleteExercise() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- LABELS ---
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SET", Modifier.width(40.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text(unit.uppercase(), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text("REPS", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            Spacer(Modifier.height(8.dp))

            // --- SETS LIST ---
            sets.forEach { set ->
                key(set.setNumber) {
                    SwipeToRevealDelete(onDelete = { onDeleteSet(set.setNumber) }) {
                        SetLogRow(
                            set = set,
                            onValueChange = { w, r -> onUpdateSet(set.setNumber, w, r) },
                            onToggle = { onToggleSet(set.setNumber) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- ADD SET BUTTON ---
            TextButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Set", fontWeight = FontWeight.ExtraBold)
            }
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
        // DELETE BACKGROUND (Revealed behind)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(buttonWidth)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable {
                    scope.launch {
                        offsetX.animateTo(-500f, tween(200)) // Slide out fully
                        onDelete()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
        }

        // CONTENT LAYER (Sliding part)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetX.value + dragAmount).coerceIn(-buttonWidthPx, 0f)
                        scope.launch { offsetX.snapTo(newOffset) }
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
fun SetLogRow(set: WorkoutSet, onValueChange: (Float, Int) -> Unit, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set Number Badge
        Box(
            modifier = Modifier
                .size(32.dp)
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

        GymInputField(
            value = if (set.weight == 0f) "" else (if (set.weight % 1 == 0f) set.weight.toInt().toString() else set.weight.toString()),
            onValueChange = { onValueChange(it.toFloatOrNull() ?: 0f, set.reps) },
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )

        GymInputField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            onValueChange = { onValueChange(set.weight, it.toIntOrNull() ?: 0) },
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )

        IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = if (set.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle Set",
                tint = if (set.isCompleted) SuccessGreen else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun GymInputField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
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
                    Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), fontSize = 16.sp)
                }
                inner()
            }
        }
    )
}