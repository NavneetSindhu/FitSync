package com.minimize.maximus.ui.screens.log.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.theme.LocalAccentColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroactiveLogBottomSheet(
    date: LocalDate,
    onDismiss: () -> Unit,
    onStartRetroactiveWorkout: (String, Long) -> Unit
) {
    val currentAccent = LocalAccentColor.current
    var workoutName by remember { mutableStateOf("Retroactive Workout") }
    val formattedDate = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))

    MaximusModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        MaximusSheetHeader(
            title = stringResource(R.string.retroactive_log_title),
            subtitle = formattedDate
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 10.dp, bottom = 32.dp)
        ) {
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text(stringResource(R.string.create_workout_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentAccent
                )
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val epochMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onStartRetroactiveWorkout(workoutName.ifBlank { "Past Workout" }, epochMillis)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentAccent,
                    contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.retroactive_log_btn_format, date.dayOfMonth, date.month.name.take(3)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
