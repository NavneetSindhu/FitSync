package com.minimize.maximus.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.components.LottieAnimationWrapper
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.MaximusIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailBottomSheet(
    currentStreak: Int,
    availableFreezes: Int = 2,
    maxFreezes: Int = 2,
    isTodayFrozen: Boolean = false,
    onDismissRequest: () -> Unit
) {
    val fireColor = Color(0xFFFF6D00)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iceColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    MaximusModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        MaximusSheetHeader(
            title = stringResource(R.string.home_streak_days_format, currentStreak),
            subtitle = if (currentStreak > 0) "Daily workout momentum active" else "Log a workout today to build your streak"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── 1. HERO FIRE CARD ──
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(fireColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        ) {
                            LottieAnimationWrapper(resId = R.raw.fire_streak_anim)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (currentStreak == 1) "1 Day Streak" else "$currentStreak Days Streak",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = fireColor
                        )
                        Text(
                            text = if (currentStreak > 0) "Keep the momentum burning strong" else "Complete today's session to begin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── 2. COMPACT FREEZE VAULT BADGE ──
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, iceColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(iceColor.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MaximusIcons.Home.CalendarFreeze,
                                contentDescription = "Freeze",
                                tint = iceColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Auto-Freeze Vault",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isTodayFrozen) "Protected for today"
                                else if (availableFreezes > 0) "$availableFreezes of $maxFreezes freezes available"
                                else "No freezes available",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTodayFrozen || availableFreezes > 0) iceColor else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..maxFreezes).forEach { index ->
                            val isArmed = index <= availableFreezes
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isArmed) iceColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isArmed) iceColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isArmed) {
                                    Icon(
                                        imageVector = MaximusIcons.Home.CalendarFreeze,
                                        contentDescription = null,
                                        tint = iceColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else {
                                    Text(
                                        text = "•",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
