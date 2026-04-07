// File: app/src/main/java/com/example/fitsync/ui/components/ExerciseCarouselItem.kt
package com.example.fitsync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.theme.ExerciseVisuals // Make sure this import is correct

@Composable
fun ExerciseCarouselItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val meta = ExerciseVisuals.getMetaData(name)
    val backgroundColor by animateColorAsState(if (isSelected) meta.accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    val contentColor by animateColorAsState(if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)

    Surface(
        modifier = Modifier
            .width(90.dp)
            .height(70.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        // Use a Box to center the entire content perfectly
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Fixed size icon container ensures different icon shapes don't shift the text
                Icon(
                    imageVector = meta.icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    fontSize = 10.sp
                )
            }
        }
    }
}