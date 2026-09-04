package com.minimize.maximus.ui.theme

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
val DefaultAccent = Color(0xFF18181B) // Onyx Black default (Clean, modern fitness aesthetic)
val LocalAccentColor = compositionLocalOf { DefaultAccent }
val LocalCompactCards = compositionLocalOf { false }
val LocalNavStyle = compositionLocalOf { "Floating Pill" }
val LocalDebugShimmer = compositionLocalOf { false }

// ── COLOR SCHEME BUILDERS ──────────────────────────────────────────────────

fun createMaximusLightColorScheme(accentColor: Color) = lightColorScheme(
    primary = accentColor,
    onPrimary = Color.White,
    primaryContainer = accentColor.copy(alpha = 0.12f),
    onPrimaryContainer = accentColor,
    secondary = accentColor,
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutline
)

fun createMaximusDarkColorScheme(accentColor: Color) = darkColorScheme(
    primary = accentColor,
    onPrimary = Color.White,
    primaryContainer = accentColor.copy(alpha = 0.15f),
    onPrimaryContainer = Color.White,
    secondary = accentColor,
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutline
)

// ── ROOT THEME PROVIDER ────────────────────────────────────────────────────

@Composable
fun MaximusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = DefaultAccent,
    fontScale: Float = 1f,
    compactCards: Boolean = false,
    navStyle: String = "Floating Pill",
    forceShimmer: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> createMaximusDarkColorScheme(accentColor)
        else -> createMaximusLightColorScheme(accentColor)
    }

    val extendedColors = if (darkTheme) MaximusDarkExtendedColors else MaximusLightExtendedColors

    // Safe clamped font scale (avoids extreme text clipping in compact cards)
    val safeFontScale = fontScale.coerceIn(0.85f, 1.25f)

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

    // Dynamically scale Matter typography within safe bounds
    val scaledTypography = MaximusTypography.copy(
        displayLarge = MaximusTypography.displayLarge.copy(fontSize = MaximusTypography.displayLarge.fontSize * safeFontScale),
        displayMedium = MaximusTypography.displayMedium.copy(fontSize = MaximusTypography.displayMedium.fontSize * safeFontScale),
        displaySmall = MaximusTypography.displaySmall.copy(fontSize = MaximusTypography.displaySmall.fontSize * safeFontScale),
        headlineLarge = MaximusTypography.headlineLarge.copy(fontSize = MaximusTypography.headlineLarge.fontSize * safeFontScale),
        headlineMedium = MaximusTypography.headlineMedium.copy(fontSize = MaximusTypography.headlineMedium.fontSize * safeFontScale),
        headlineSmall = MaximusTypography.headlineSmall.copy(fontSize = MaximusTypography.headlineSmall.fontSize * safeFontScale),
        titleLarge = MaximusTypography.titleLarge.copy(fontSize = MaximusTypography.titleLarge.fontSize * safeFontScale),
        titleMedium = MaximusTypography.titleMedium.copy(fontSize = MaximusTypography.titleMedium.fontSize * safeFontScale),
        titleSmall = MaximusTypography.titleSmall.copy(fontSize = MaximusTypography.titleSmall.fontSize * safeFontScale),
        bodyLarge = MaximusTypography.bodyLarge.copy(fontSize = MaximusTypography.bodyLarge.fontSize * safeFontScale),
        bodyMedium = MaximusTypography.bodyMedium.copy(fontSize = MaximusTypography.bodyMedium.fontSize * safeFontScale),
        bodySmall = MaximusTypography.bodySmall.copy(fontSize = MaximusTypography.bodySmall.fontSize * safeFontScale),
        labelLarge = MaximusTypography.labelLarge.copy(fontSize = MaximusTypography.labelLarge.fontSize * safeFontScale),
        labelMedium = MaximusTypography.labelMedium.copy(fontSize = MaximusTypography.labelMedium.fontSize * safeFontScale),
        labelSmall = MaximusTypography.labelSmall.copy(fontSize = MaximusTypography.labelSmall.fontSize * safeFontScale)
    )

    // Inject custom settings & semantic extended tokens at root
    CompositionLocalProvider(
        LocalAccentColor provides accentColor,
        LocalCompactCards provides compactCards,
        LocalNavStyle provides navStyle,
        LocalDebugShimmer provides forceShimmer,
        LocalMaximusColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}