package com.minimize.maximus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.LocalAccentColor

/**
 * A custom high-performance Filter Chip that reacts dynamically to the global Accent Color.
 * Unselected chips use high-contrast elevated surface capsules with crisp text.
 * Selected chips pop with the vibrant accent color and bold contrast.
 */
@Composable
fun MaximusFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current

    val unselectedBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    val unselectedText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    val unselectedBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val selectedText = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) currentAccent else unselectedBg,
        label = "chip_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedText else unselectedText,
        label = "chip_text"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) currentAccent else unselectedBorder,
        label = "chip_border"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}