package com.example.fitsync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.theme.ExerciseVisuals

@Composable
fun ExerciseCarouselItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val meta = ExerciseVisuals.getMetaData(name)

    // Animate colors for a smooth selection feel
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) meta.accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        label = "bg_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content_color"
    )

    Surface(
        modifier = Modifier
            .width(90.dp)
            .height(75.dp) // Increased slightly for better breathability
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp), // Matched your new 16dp rounded UI style
        color = backgroundColor,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                // 🔥 UPDATED: Using Image with painterResource for your Flaticon PNGs
                Image(
                    painter = painterResource(id = meta.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    // Tints the PNG white when selected, or LocalAccentColor.current when unselected
                    colorFilter = ColorFilter.tint(contentColor)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                    maxLines = 1,
                    fontSize = 10.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}