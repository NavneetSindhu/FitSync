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
    // Defaulting iconColor to Primary ensures it adapts to Light/Dark automatically
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FitSyncCard(
        modifier = modifier,
        onClick = onClick,
        // Ensure FitSyncCard internal container uses MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Icon with circular tinted background
            // The 0.1f alpha works great on both light and dark backgrounds
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                // THE FIX: Use onSurface so it turns White in Dark Mode and Navy in Light Mode
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                // THE FIX: Use onSurfaceVariant for that "Muted" look in both modes
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}