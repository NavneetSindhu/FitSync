package com.example.fitsync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.theme.LocalAccentColor

/**
 * A custom high-performance Filter Chip that reacts to the global Accent Color.
 * Better than standard Material 3 FilterChip because it handles luminance and
 * custom borders automatically for your specific branding.
 */
@Composable
fun FitSyncFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current

    // Animate background color transition
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) currentAccent else Color.Transparent,
        label = "chip_bg"
    )

    // Determine text color based on background luminance (Black text on bright colors, White on dark)
    val selectedContentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            selectedContentColor
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "chip_text"
    )

    // Border color matches the text for a clean "ghost button" look when unselected
    val borderColor = if (isSelected) {
        currentAccent
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}