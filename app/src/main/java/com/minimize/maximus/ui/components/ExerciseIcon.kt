package com.minimize.maximus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.ui.theme.ExerciseVisuals
import com.minimize.maximus.ui.theme.LocalAccentColor

@Composable
fun ExerciseIcon(
    name: String,
    modifier: Modifier = Modifier,
    muscleGroup: MuscleGroup? = null,
    size: Dp = 40.dp
) {
    val meta = ExerciseVisuals.getMetaData(name, muscleGroup)
    val currentAccent = LocalAccentColor.current
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
    val effectiveAccent = if (isDark && currentAccent.luminance() < 0.2f) Color.White else currentAccent

    Box(
        modifier = modifier
            .size(size)
            .background(
                color = effectiveAccent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(size * 0.18f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = meta.iconRes),
            contentDescription = name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = if (isDark) ColorFilter.tint(androidx.compose.material3.MaterialTheme.colorScheme.onSurface) else null
        )
    }
}