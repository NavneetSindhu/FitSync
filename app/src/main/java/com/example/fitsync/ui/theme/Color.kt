package com.example.fitsync.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val NavyBlue = Color(0xFF1A1C3D)
val SoftGray = Color(0xFF9EA1B0)
val SuccessGreen = Color(0xFF81C784)
val CardWhite = Color(0xFFFFFFFF)
val BgLight = Color(0xFFF1F4F9)

// 1. Define the default fallback color
val DefaultAccent = Color(0xFF0D6890)

// 2. Create the CompositionLocal
val LocalAccentColor = compositionLocalOf { DefaultAccent }