package com.example.fitsync.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitsync.ui.theme.ExerciseVisuals

@Composable
fun ExerciseIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val meta = ExerciseVisuals.getMetaData(name)

    Box(
        modifier = modifier
            .size(size)
            .background(
                color = meta.accentColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = meta.iconRes),
            contentDescription = name,
            modifier = Modifier.size(size * 0.65f),
            // 🔥 This tints your PNG to match your app's theme colors
            colorFilter = ColorFilter.tint(meta.accentColor)
        )
    }
}