package com.example.fitsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitsync.ui.theme.* // Still import your theme for specific accent colors

@Composable
fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    FitSyncCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 16.dp else 24.dp)) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 40.dp else 48.dp)
                    .background(currentAccent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = currentAccent,
                    modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 20.dp))

            Text(
                text = title,
                style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}