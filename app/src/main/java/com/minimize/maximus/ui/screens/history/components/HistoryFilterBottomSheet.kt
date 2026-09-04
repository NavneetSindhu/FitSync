package com.minimize.maximus.ui.screens.history.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.LocalBottomSheetDismiss
import com.minimize.maximus.ui.screens.history.WorkoutSortOption
import com.minimize.maximus.ui.theme.LocalAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterBottomSheet(
    selectedMonth: String,
    availableMonths: List<String>,
    onMonthSelected: (String) -> Unit,
    selectedSplit: String,
    onSplitSelected: (String) -> Unit,
    selectedSort: WorkoutSortOption,
    onSortSelected: (WorkoutSortOption) -> Unit,
    onResetAll: () -> Unit,
    activeFilterCount: Int,
    onDismissRequest: () -> Unit
) {
    val currentAccent = LocalAccentColor.current

    var tempMonth by remember(selectedMonth) { mutableStateOf(selectedMonth) }
    var tempSplit by remember(selectedSplit) { mutableStateOf(selectedSplit) }
    var tempSort by remember(selectedSort) { mutableStateOf(selectedSort) }

    val splits = listOf("All", "Push", "Pull", "Legs", "Full Body", "Custom")

    MaximusModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        val dismissSheet = LocalBottomSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            // ── 1. Header Bar: Title + Reset All ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.history_filter_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = {
                        tempMonth = "All"
                        tempSplit = "All"
                        tempSort = WorkoutSortOption.NEWEST
                        onResetAll()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.history_filter_reset_all),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── 2. Scrollable Filter Form ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Section A: Sort By
                Text(
                    text = stringResource(R.string.history_filter_sort_by),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val sortOptions = WorkoutSortOption.entries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOptions.take(2).forEach { option ->
                        HistoryFilterPill(
                            text = option.displayName,
                            isSelected = tempSort == option,
                            onClick = { tempSort = option },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOptions.drop(2).forEach { option ->
                        HistoryFilterPill(
                            text = option.displayName,
                            isSelected = tempSort == option,
                            onClick = { tempSort = option },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Section B: Split / Category
                Text(
                    text = stringResource(R.string.history_filter_training_split),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    splits.take(3).forEach { split ->
                        HistoryFilterPill(
                            text = split,
                            isSelected = tempSplit == split,
                            onClick = { tempSplit = split },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    splits.drop(3).forEach { split ->
                        HistoryFilterPill(
                            text = split,
                            isSelected = tempSplit == split,
                            onClick = { tempSplit = split },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Section C: Month Filter
                Text(
                    text = stringResource(R.string.history_filter_month_timeline),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                availableMonths.chunked(3).forEach { chunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chunk.forEach { month ->
                            HistoryFilterPill(
                                text = month,
                                isSelected = tempMonth == month,
                                onClick = { tempMonth = month },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (chunk.size < 3) {
                            repeat(3 - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── 3. Apply Action Button ──
            Button(
                onClick = {
                    onMonthSelected(tempMonth)
                    onSplitSelected(tempSplit)
                    onSortSelected(tempSort)
                    dismissSheet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentAccent,
                    contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                )
            ) {
                Text(
                    text = if (activeFilterCount > 0) stringResource(R.string.history_filter_apply_count_format, activeFilterCount) else stringResource(R.string.history_filter_apply_btn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HistoryFilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current

    val bg by animateColorAsState(
        targetValue = if (isSelected) currentAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "HistoryFilterChipBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "HistoryFilterChipTextColor"
    )

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1
        )
    }
}
