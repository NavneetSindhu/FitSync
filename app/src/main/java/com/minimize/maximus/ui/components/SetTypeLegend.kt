package com.minimize.maximus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

/**
 * Shared App-Wide Component: Set Type Legend
 * Displays colored badge chips for set classifications: Warmup (W), Normal (N), Drop (D), Failure (F).
 */
@Composable
fun SetTypeLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetTypeLegendItem(code = "W", label = "Warmup", color = Color(0xFFFF9800))
        SetTypeLegendItem(code = "N", label = "Normal", color = Color.Transparent)
        SetTypeLegendItem(code = "D", label = "Drop", color = Color(0xFF9C27B0))
        SetTypeLegendItem(code = "F", label = "Failure", color = Color(0xFFE91E63))
    }
}

@Composable
fun SetTypeLegendItem(
    code: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (color == Color.Transparent) MaterialTheme.colorScheme.surfaceVariant
                    else color.copy(alpha = 0.2f)
                )
                .border(
                    0.5.dp,
                    if (color == Color.Transparent) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    else color.copy(alpha = 0.5f),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                ),
                color = if (color == Color.Transparent) MaterialTheme.colorScheme.onSurfaceVariant else color
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
