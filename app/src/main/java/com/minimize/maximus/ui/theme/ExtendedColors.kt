package com.minimize.maximus.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended semantic color tokens for Maximus fitness elements.
 */
data class MaximusExtendedColors(
    val badgeNormal: Color,
    val badgeWarmup: Color,
    val onBadgeWarmup: Color,
    val badgeDrop: Color,
    val badgeFailure: Color,
    val chartGradientStart: Color,
    val chartGradientEnd: Color,
    val statCardBackground: Color,
    val cardBorderSubtle: Color,
    val sleeveBarColor: Color
)

val MaximusLightExtendedColors = MaximusExtendedColors(
    badgeNormal = Color(0xFF64748B),
    badgeWarmup = Color(0xFFFF9800),
    onBadgeWarmup = Color(0xFF111827),
    badgeDrop = Color(0xFF9C27B0),
    badgeFailure = Color(0xFF991B1B),
    chartGradientStart = Color(0xFFE53935),
    chartGradientEnd = Color.Transparent,
    statCardBackground = Color(0xFFF8FAFC),
    cardBorderSubtle = Color(0xFFE2E8F0),
    sleeveBarColor = Color(0xFF94A3B8)
)

val MaximusDarkExtendedColors = MaximusExtendedColors(
    badgeNormal = Color(0xFF94A3B8),
    badgeWarmup = Color(0xFFFFB74D),
    onBadgeWarmup = Color(0xFF111827),
    badgeDrop = Color(0xFFBA68C8),
    badgeFailure = Color(0xFF991B1B),
    chartGradientStart = Color(0xFFE53935),
    chartGradientEnd = Color.Transparent,
    statCardBackground = Color(0xFF1E293B),
    cardBorderSubtle = Color(0xFF334155),
    sleeveBarColor = Color(0xFF64748B)
)

val LocalMaximusColors = staticCompositionLocalOf { MaximusLightExtendedColors }
// Compatibility alias
val LocalFitSyncColors = LocalMaximusColors
