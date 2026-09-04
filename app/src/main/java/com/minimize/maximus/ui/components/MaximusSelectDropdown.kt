package com.minimize.maximus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

/**
 * Reusable, sleek Glassmorphic Dropdown Selector for FitSync.
 * Supports generic items, icons, animated chevron rotation, and tactile feedback.
 */
@Composable
fun <T> MaximusSelectDropdown(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    iconProvider: ((T) -> ImageVector?)? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "DropdownChevron"
    )

    Box(modifier = modifier) {
        Surface(
            onClick = {
                if (enabled) {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.SELECTION_TAP)
                    expanded = !expanded
                }
            },
            shape = MaximusShapes.Pill,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                iconProvider?.invoke(selectedItem)?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = labelProvider(selectedItem),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = MaximusIcons.Stats.DropdownChevron,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaximusShapes.Card,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            iconProvider?.invoke(item)?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) currentAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = labelProvider(item),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) currentAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = MaximusIcons.Navigation.Check,
                                contentDescription = "Selected",
                                tint = currentAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
