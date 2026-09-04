package com.minimize.maximus.ui.screens.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.ui.screens.settings.getAccentOptions

@Composable
fun StepEnergyTheme(
    userName: String,
    selectedGoalKey: String,
    selectedAccent: Color,
    onAccentSelected: (Color) -> Unit
) {
    val isDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accentOptions = remember(isDarkMode) { getAccentOptions(isDarkMode) }
    Text(
        text = stringResource(R.string.onboarding_step4_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.onboarding_step4_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(24.dp))

    // Preview Card
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = selectedAccent.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                color = selectedAccent,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.onboarding_greeting_preview, userName),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = selectedGoalKey,
                            style = MaterialTheme.typography.labelSmall,
                            color = selectedAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = selectedAccent
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_badge_ready),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (selectedAccent.luminance() > 0.45f) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.onboarding_step4_palette_label),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = selectedAccent,
        letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(14.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        accentOptions.forEach { (name, color) ->
            val isSelected = selectedAccent == color
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAccentSelected(color) }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (color.luminance() > 0.45f) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) selectedAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
