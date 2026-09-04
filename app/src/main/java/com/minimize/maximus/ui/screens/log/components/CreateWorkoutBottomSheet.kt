package com.minimize.maximus.ui.screens.log.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.components.MaximusFilterChip
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.theme.LocalAccentColor

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutBottomSheet(
    onDismiss: () -> Unit,
    onStartWorkout: (String, Boolean) -> Unit
) {
    val presetOptions = listOf("Push Day", "Pull Day", "Leg Day", "Full Body", "Custom")
    var selectedOption by remember { mutableStateOf(presetOptions[0]) }
    var customName by remember { mutableStateOf("") }
    val currentAccent = LocalAccentColor.current

    MaximusModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        MaximusSheetHeader(
            title = stringResource(R.string.create_workout_title)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 10.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetOptions.forEach { option ->
                    MaximusFilterChip(
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
                    label = { Text(stringResource(R.string.create_workout_name_label)) },
                    placeholder = { Text(stringResource(R.string.create_workout_name_placeholder)) },
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
                            text = stringResource(R.string.create_workout_preset_info_format, selectedOption),
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
                Text(stringResource(R.string.create_workout_start_btn), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}
