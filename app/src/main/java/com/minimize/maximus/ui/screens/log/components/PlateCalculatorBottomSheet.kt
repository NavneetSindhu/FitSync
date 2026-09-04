package com.minimize.maximus.ui.screens.log.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.PlatePair
import com.minimize.maximus.util.WorkoutMathUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateCalculatorBottomSheet(
    initialTargetWeight: Float,
    isMetric: Boolean = true,
    onDismiss: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    var targetWeight by remember { mutableStateOf(if (initialTargetWeight > 0f) initialTargetWeight else if (isMetric) 60f else 135f) }
    var barWeight by remember { mutableStateOf(if (isMetric) 20f else 45f) }

    val unit = if (isMetric) "kg" else "lbs"
    val calculation = remember(targetWeight, barWeight, isMetric) {
        WorkoutMathUtils.calculateBarbellPlates(targetWeight, barWeight, isMetric)
    }

    MaximusModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        MaximusSheetHeader(
            title = stringResource(R.string.plate_calc_title),
            subtitle = stringResource(R.string.plate_calc_subtitle)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp)
        ) {

            // ── Target Weight Selector ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledIconButton(
                        onClick = {
                            val step = if (isMetric) 2.5f else 5f
                            targetWeight = (targetWeight - step).coerceAtLeast(barWeight)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.Remove, "Decrease Weight")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${if (targetWeight % 1 == 0f) targetWeight.toInt() else targetWeight} $unit",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = currentAccent
                        )
                        Text(
                            text = stringResource(R.string.plate_calc_target_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            val step = if (isMetric) 2.5f else 5f
                            targetWeight = (targetWeight + step).coerceAtMost(400f)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.Add, "Increase Weight")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Bar Tare Selector ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.plate_calc_bar_weight_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                val barOptions = if (isMetric) listOf(20f, 15f, 10f) else listOf(45f, 35f, 25f)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    barOptions.forEach { opt ->
                        val isSelected = barWeight == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) currentAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { barWeight = opt }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${opt.toInt()} $unit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Visual Barbell Sleeve ──
            Text(
                text = stringResource(R.string.plate_calc_each_side_header),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(8.dp))

            if (calculation.platesPerSide.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.plate_calc_just_bar_message),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bar Collar Indicator
                    item {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(56.dp)
                                .background(Color(0xFF64748B), RoundedCornerShape(2.dp))
                        )
                    }

                    items(calculation.platesPerSide) { pair ->
                        repeat(pair.countPerSide) {
                            PlateVisualPill(weight = pair.weight, isMetric = isMetric)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = currentAccent)
            ) {
                Text(stringResource(R.string.plate_calc_got_it_btn), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun PlateVisualPill(weight: Float, isMetric: Boolean) {
    val plateColor = when {
        weight >= 25f || weight >= 55f -> Color(0xFFE53935)
        weight >= 20f || weight >= 45f -> Color(0xFF1E88E5)
        weight >= 15f || weight >= 35f -> Color(0xFFFDD835)
        weight >= 10f || weight >= 25f -> Color(0xFF43A047)
        weight >= 5f || weight >= 10f -> Color(0xFFFFFFFF)
        else -> Color(0xFF78909C)
    }

    val plateHeight = when {
        weight >= 20f || weight >= 45f -> 64.dp
        weight >= 15f || weight >= 35f -> 56.dp
        weight >= 10f || weight >= 25f -> 48.dp
        else -> 38.dp
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(plateHeight)
                .background(plateColor, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (weight % 1 == 0f) weight.toInt().toString() else weight.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (plateColor == Color.White || plateColor == Color(0xFFFDD835)) Color.Black else Color.White
            )
        }
    }
}
