// File: app/src/main/java/com/example/fitsync/ui/components/MiniStatCard.kt
package com.example.fitsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsync.ui.theme.LocalCompactCards

@Composable
fun MiniStatCard(
    label: String,
    value: String,
    color: Color, // Still passed from Dashboard to allow for green/orange/purple variety
    modifier: Modifier = Modifier
) {
    val isCompact = LocalCompactCards.current

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 6.dp else 8.dp)
                    .background(color, CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = label,
                    fontSize = if (isCompact) 9.sp else 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = if (isCompact) 12.sp else 14.sp
                )
            }
        }
    }
}