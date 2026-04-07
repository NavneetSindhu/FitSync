package com.example.fitsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitsync.ui.theme.ExerciseVisuals

/**
 * A standardized icon component for exercises.
 * Automatically pulls the correct icon and accent color from ExerciseVisuals.
 */
@Composable
fun ExerciseIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    // Pulls the icon and color based on the exercise name
    val meta = ExerciseVisuals.getMetaData(name)

    Box(
        modifier = modifier
            .size(size)
            .background(
                // 0.15f creates a subtle "Pastel" version of the exercise's color
                color = meta.accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = meta.icon,
            contentDescription = name,
            tint = meta.accentColor, // The icon itself uses the full-vibrancy color
            modifier = Modifier.size(size * 0.6f) // Scales icon to 60% of the container
        )
    }
}