package com.example.fitsync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ── IMPORT GLOBAL PREFERENCES ──────────────────────────────────────────────
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards

@Composable
fun EmptyWorkoutState() {
    // 1. Grab Global Settings
    val currentAccent = LocalAccentColor.current
    val isCompact = LocalCompactCards.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (isCompact) 24.dp else 40.dp), // Snugger padding if compact
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(if (isCompact) 48.dp else 64.dp), // Dynamic size
            tint = currentAccent.copy(alpha = 0.25f) // Matches user's accent color
        )

        Spacer(Modifier.height(if (isCompact) 12.dp else 16.dp))

        Text(
            text = "No exercises added yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Tap the + button to start training!",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}