package com.minimize.maximus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Centralized Design System Dimensions for FitSync
 */
object MaximusDimens {
    val FloatingNavHeight = 68.dp
    val FloatingNavPadding = 20.dp
    val RestTimerBarHeight = 64.dp
    val FabWithRestTimerBottomPadding = 180.dp
    val CardCornerRadius = 20.dp
    val PillCornerRadius = 50.dp
    val ScreenHorizontalPadding = 20.dp
}

/**
 * Centralized Shapes for FitSync UI Consistency
 */
object MaximusShapes {
    val Pill = RoundedCornerShape(MaximusDimens.PillCornerRadius)
    val Card = RoundedCornerShape(MaximusDimens.CardCornerRadius)
    val SmallPill = RoundedCornerShape(12.dp)
}
