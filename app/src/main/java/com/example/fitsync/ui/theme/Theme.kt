package com.example.fitsync.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── CUSTOM GLOBALS (Accessible from any screen using LocalName.current) ────
val DefaultAccent = Color(0xFFE53935) // Coral Red default
val LocalAccentColor = compositionLocalOf { DefaultAccent }
val LocalCompactCards = compositionLocalOf { false }
val LocalNavStyle = compositionLocalOf { "Floating Pill" }

// ── COLOR PALETTES ─────────────────────────────────────────────────────────

private val FitSyncLightColorScheme = lightColorScheme(
    primary = NavyBlue, // Ensure NavyBlue is defined in Color.kt
    secondary = AccentRed, // Ensure AccentRed is defined in Color.kt
    tertiary = SuccessGreen, // Ensure SuccessGreen is defined in Color.kt
    background = BgLight, // Ensure BgLight is defined in Color.kt
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = NavyBlue,
    onSurface = NavyBlue,
    surfaceVariant = Color(0xFFF1F4F9)
)

val DarkBg = Color(0xFF0B0E14)
val DarkSurface = Color(0xFF161B22)
val DarkNavy = Color(0xFF91A7FF)

private val FitSyncDarkColorScheme = darkColorScheme(
    primary = DarkNavy,
    secondary = AccentRed,
    background = DarkBg,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF21262D)
)

// ── ROOT THEME PROVIDER ────────────────────────────────────────────────────

@Composable
fun FitSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = DefaultAccent,
    fontScale: Float = 1f,
    compactCards: Boolean = false,
    navStyle: String = "Floating Pill",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FitSyncDarkColorScheme
        else -> FitSyncLightColorScheme
    }

    // System Bar Styling
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // Dynamically scale all typography based on the user's setting
    // (Ensure 'Typography' is properly defined in your Type.kt file)
    val scaledTypography = Typography(
        displayLarge = Typography.displayLarge.copy(fontSize = Typography.displayLarge.fontSize * fontScale),
        displayMedium = Typography.displayMedium.copy(fontSize = Typography.displayMedium.fontSize * fontScale),
        displaySmall = Typography.displaySmall.copy(fontSize = Typography.displaySmall.fontSize * fontScale),
        headlineLarge = Typography.headlineLarge.copy(fontSize = Typography.headlineLarge.fontSize * fontScale),
        headlineMedium = Typography.headlineMedium.copy(fontSize = Typography.headlineMedium.fontSize * fontScale),
        headlineSmall = Typography.headlineSmall.copy(fontSize = Typography.headlineSmall.fontSize * fontScale),
        titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * fontScale),
        titleMedium = Typography.titleMedium.copy(fontSize = Typography.titleMedium.fontSize * fontScale),
        titleSmall = Typography.titleSmall.copy(fontSize = Typography.titleSmall.fontSize * fontScale),
        bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * fontScale),
        bodyMedium = Typography.bodyMedium.copy(fontSize = Typography.bodyMedium.fontSize * fontScale),
        bodySmall = Typography.bodySmall.copy(fontSize = Typography.bodySmall.fontSize * fontScale),
        labelLarge = Typography.labelLarge.copy(fontSize = Typography.labelLarge.fontSize * fontScale),
        labelMedium = Typography.labelMedium.copy(fontSize = Typography.labelMedium.fontSize * fontScale),
        labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * fontScale)
    )

    // Inject our custom settings into the Compose Tree!
    CompositionLocalProvider(
        LocalAccentColor provides accentColor,
        LocalCompactCards provides compactCards,
        LocalNavStyle provides navStyle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}