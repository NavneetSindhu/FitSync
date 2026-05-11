package com.example.fitsync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.theme.ExerciseVisuals
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards

@Composable
fun ExerciseCarouselItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val meta = ExerciseVisuals.getMetaData(name)
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) currentAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        label = "bg_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (currentAccent.luminance() > 0.4f) Color.Black else Color.White
        } else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content_color"
    )

    Surface(
        modifier = Modifier
            .width(if (isCompact) 80.dp else 90.dp)
            .height(if (isCompact) 65.dp else 75.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Image(
                painter = painterResource(id = meta.iconRes),
                contentDescription = null,
                modifier = Modifier.size(if (isCompact) 20.dp else 24.dp),
                colorFilter = ColorFilter.tint(contentColor)
            )
            Spacer(Modifier.height(if (isCompact) 4.dp else 6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
                maxLines = 1,
                fontSize = if (isCompact) 9.sp else 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}