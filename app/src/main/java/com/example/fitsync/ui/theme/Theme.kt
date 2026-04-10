package com.example.fitsync.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Mode Colors
val DarkBg = Color(0xFF0B0E14)      // Deep Charcoal
val DarkSurface = Color(0xFF161B22) // Lighter Charcoal for cards
val DarkNavy = Color(0xFF91A7FF)    // Desaturated Navy for Dark Mode

@Composable
fun FitSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 1. Grab the dynamic color HERE, inside the @Composable
    val currentAccent = LocalAccentColor.current

    // 2. Define schemes inside the function so they can use the currentAccent
    val FitSyncLightColorScheme = lightColorScheme(
        primary = NavyBlue,
        secondary = currentAccent,
        tertiary = SuccessGreen,
        background = BgLight,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = NavyBlue,
        onSurface = NavyBlue,
        surfaceVariant = Color(0xFFF1F4F9) // Light grey for input fields
    )

    val FitSyncDarkColorScheme = darkColorScheme(
        primary = DarkNavy,
        secondary = currentAccent,
        background = DarkBg,
        surface = DarkSurface,
        onBackground = Color.White,
        onSurface = Color.White,
        surfaceVariant = Color(0xFF21262D) // For input fields in dark mode
    )

    // 3. Determine which scheme to use
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FitSyncDarkColorScheme
        else -> FitSyncLightColorScheme
    }

    // --- PREMIUM TOUCH: System Bar Styling ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Light Mode (!darkTheme) -> Dark Icons
            // Dark Mode (darkTheme) -> Light Icons
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pulls the Poppins config from Type.kt
        content = content
    )
}