package com.minimize.maximus.ui.screens.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R

@Composable
fun StepBodyMetrics(
    age: Int,
    onAgeChange: (Int) -> Unit,
    weightInput: String,
    onWeightChange: (String) -> Unit,
    isMetric: Boolean,
    onMetricToggle: (Boolean) -> Unit,
    heightInput: String,
    onHeightChange: (String) -> Unit,
    isHeightMetric: Boolean,
    onHeightMetricToggle: (Boolean) -> Unit,
    accentColor: Color
) {
    Text(
        text = stringResource(R.string.onboarding_step2_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.onboarding_step2_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(24.dp))

    // Age Selector
    Text(
        text = stringResource(R.string.onboarding_step2_age_label),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(8.dp))
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (age > 12) onAgeChange(age - 1) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Remove, null, tint = accentColor)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$age",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.onboarding_years_old),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { if (age < 99) onAgeChange(age + 1) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Add, null, tint = accentColor)
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    // Weight Input with KG/LBS toggle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.onboarding_step2_weight_label),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isMetric) accentColor else Color.Transparent)
                    .clickable { onMetricToggle(true) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.unit_kg).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isMetric) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (!isMetric) accentColor else Color.Transparent)
                    .clickable { onMetricToggle(false) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.unit_lbs).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (!isMetric) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = weightInput,
        onValueChange = onWeightChange,
        placeholder = { Text(if (isMetric) "75.0" else "165.0") },
        trailingIcon = {
            Text(
                text = if (isMetric) stringResource(R.string.unit_kg) else stringResource(R.string.unit_lbs),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    // Height Input with cm / ft+in toggle
    val currentCm = heightInput.toFloatOrNull() ?: 175f
    val initialFtIn = remember(currentCm) { com.minimize.maximus.util.UnitConverter.cmToFtIn(currentCm) }
    var heightFeet by remember { mutableStateOf(initialFtIn.first.toString()) }
    var heightInches by remember { mutableStateOf(initialFtIn.second.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.onboarding_step2_height_label),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isHeightMetric) accentColor else Color.Transparent)
                    .clickable {
                        if (!isHeightMetric) {
                            onHeightMetricToggle(true)
                            val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(
                                heightFeet.toIntOrNull() ?: 5,
                                heightInches.toIntOrNull() ?: 9
                            )
                            onHeightChange("%.0f".format(cm))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "CM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isHeightMetric) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (!isHeightMetric) accentColor else Color.Transparent)
                    .clickable {
                        if (isHeightMetric) {
                            onHeightMetricToggle(false)
                            val (f, i) = com.minimize.maximus.util.UnitConverter.cmToFtIn(heightInput.toFloatOrNull() ?: 175f)
                            heightFeet = f.toString()
                            heightInches = i.toString()
                            val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(f, i)
                            onHeightChange("%.0f".format(cm))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "FT / IN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (!isHeightMetric) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    if (isHeightMetric) {
        OutlinedTextField(
            value = heightInput,
            onValueChange = onHeightChange,
            placeholder = { Text("175") },
            trailingIcon = {
                Text(
                    text = stringResource(R.string.unit_cm),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = heightFeet,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() }.take(1)
                    heightFeet = filtered
                    val f = filtered.toIntOrNull() ?: 5
                    val i = heightInches.toIntOrNull() ?: 0
                    val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(f, i)
                    onHeightChange("%.0f".format(cm))
                },
                placeholder = { Text("5") },
                trailingIcon = {
                    Text("ft", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = heightInches,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() }.take(2)
                    heightInches = filtered
                    val f = heightFeet.toIntOrNull() ?: 5
                    val i = filtered.toIntOrNull() ?: 0
                    val cm = com.minimize.maximus.util.UnitConverter.ftInToCm(f, i)
                    onHeightChange("%.0f".format(cm))
                },
                placeholder = { Text("10") },
                trailingIcon = {
                    Text("in", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
