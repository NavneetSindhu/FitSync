package com.minimize.maximus.ui.screens.home.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.LocalCompactCards

@Composable
fun MiniStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isCompact = LocalCompactCards.current

    Surface(
        modifier = modifier.defaultMinSize(minHeight = if (isCompact) 56.dp else 64.dp),
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(if (isCompact) 10.dp else 14.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = if (isCompact) 8.dp else 10.dp, vertical = if (isCompact) 8.dp else 10.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 6.dp else 8.dp)
                    .background(color, CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = label,
                    fontSize = if (isCompact) 9.sp else 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    color = color,
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
