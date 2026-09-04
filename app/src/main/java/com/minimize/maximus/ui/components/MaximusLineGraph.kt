package com.minimize.maximus.ui.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.ui.theme.MaximusIcons

data class ChartPoint(
    val label: String,
    val value: Float,
    val secondaryText: String = ""
)

@Composable
fun MaximusLineGraph(
    points: List<ChartPoint>,
    unit: String,
    modifier: Modifier = Modifier,
    title: String = "Weekly Volume Trend",
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    val currentAccent = LocalAccentColor.current
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }

    val animationProgress = remember(points) { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val isDatasetEmpty = points.isEmpty() || points.all { it.value == 0f }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (containerColor == Color.Transparent) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(if (containerColor == Color.Transparent) 0.dp else 18.dp)) {
            // Header with selected tooltip info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = onSurface
                    )
                    val activePoint = selectedIndex?.let { points.getOrNull(it) } ?: if (!isDatasetEmpty) points.lastOrNull() else null
                    Text(
                        text = if (activePoint != null && activePoint.value > 0)
                            "${activePoint.label}: ${"%,d".format(activePoint.value.toInt())} $unit"
                        else if (isDatasetEmpty)
                            "No volume logged yet"
                        else
                            "Tap points for exact details",
                        style = MaterialTheme.typography.labelSmall,
                        color = currentAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!isDatasetEmpty) {
                    val maxVal = points.maxOfOrNull { it.value } ?: 0f
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = currentAccent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Peak: ${"%,d".format(maxVal.toInt())} $unit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = currentAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isDatasetEmpty) {
                // ── Sleek Ghost Empty State with Ambient Dotted Curve ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient ghost curve in background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val paddingBottom = 20.dp.toPx()
                        val paddingTop = 12.dp.toPx()
                        val graphHeight = height - paddingBottom - paddingTop

                        // Draw subtle grid lines
                        for (i in 0..2) {
                            val y = paddingTop + (graphHeight * i / 2)
                            drawLine(
                                color = onSurfaceVariant.copy(alpha = 0.08f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Ghost bezier curve
                        val ghostPath = Path().apply {
                            moveTo(0f, height - paddingBottom - graphHeight * 0.2f)
                            cubicTo(
                                width * 0.3f, height - paddingBottom - graphHeight * 0.6f,
                                width * 0.7f, height - paddingBottom - graphHeight * 0.3f,
                                width, height - paddingBottom - graphHeight * 0.75f
                            )
                        }

                        drawPath(
                            path = ghostPath,
                            color = currentAccent.copy(alpha = 0.22f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                            )
                        )
                    }

                    // Centered Glassmorphic Prompt
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = currentAccent.copy(alpha = 0.15f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = MaximusIcons.Stats.Timeline,
                                        contentDescription = null,
                                        tint = currentAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Log your first session to unlock live volume trends",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val isScrollable = points.size > 5
                val scrollState = rememberScrollState()

                LaunchedEffect(points.size, scrollState.maxValue) {
                    if (isScrollable && scrollState.maxValue > 0) {
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .then(if (isScrollable) Modifier.horizontalScroll(scrollState) else Modifier)
                ) {
                    val chartWidthModifier = if (isScrollable) {
                        Modifier.width((points.size * 64).dp)
                    } else {
                        Modifier.fillMaxWidth()
                    }

                    Box(
                        modifier = chartWidthModifier.fillMaxHeight()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(points) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val change = event.changes.firstOrNull() ?: continue
                                            if (change.pressed) {
                                                val paddingHorizontalPx = 18.dp.toPx()
                                                val availableWidth = (size.width - paddingHorizontalPx * 2).coerceAtLeast(1f)
                                                val stepX = availableWidth / (points.size - 1).coerceAtLeast(1)
                                                val clampedX = (change.position.x - paddingHorizontalPx).coerceIn(0f, availableWidth)
                                                val targetIdx = ((clampedX + (stepX / 2)) / stepX).toInt().coerceIn(0, points.size - 1)
                                                if (selectedIndex != targetIdx) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedIndex = targetIdx
                                                }
                                            }
                                        }
                                    }
                                }
                                .pointerInput(points) {
                                    detectTapGestures { tapOffset ->
                                        val paddingHorizontalPx = 18.dp.toPx()
                                        val availableWidth = (size.width - paddingHorizontalPx * 2).coerceAtLeast(1f)
                                        val stepX = availableWidth / (points.size - 1).coerceAtLeast(1)
                                        val clampedX = (tapOffset.x - paddingHorizontalPx).coerceIn(0f, availableWidth)
                                        val tappedIndex = ((clampedX + (stepX / 2)) / stepX).toInt().coerceIn(0, points.size - 1)
                                        if (selectedIndex != tappedIndex) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedIndex = tappedIndex
                                        } else {
                                            selectedIndex = null
                                        }
                                    }
                                }
                        ) {
                        val width = size.width
                        val height = size.height
                        val paddingBottom = 28.dp.toPx()
                        val paddingTop = 24.dp.toPx()
                        val paddingHorizontal = 18.dp.toPx()
                        val graphHeight = height - paddingBottom - paddingTop
                        val availableWidth = (width - paddingHorizontal * 2).coerceAtLeast(1f)

                        val maxValue = (points.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
                        val stepX = availableWidth / (points.size - 1).coerceAtLeast(1)

                        // 1. Draw subtle horizontal grid lines
                        val gridLines = 3
                        for (i in 0..gridLines) {
                            val y = paddingTop + (graphHeight * i / gridLines)
                            drawLine(
                                color = onSurfaceVariant.copy(alpha = 0.1f),
                                start = Offset(paddingHorizontal, y),
                                end = Offset(width - paddingHorizontal, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Compute point coordinates with horizontal padding
                        val offsets = points.mapIndexed { index, pt ->
                            val x = paddingHorizontal + (stepX * index)
                            val normalizedY = (pt.value / maxValue) * animationProgress.value
                            val y = paddingTop + graphHeight * (1f - normalizedY)
                            Offset(x, y)
                        }

                        // 2. Build Smooth Cubic Bezier Curves
                        val strokePath = Path()
                        val fillPath = Path()

                        if (offsets.isNotEmpty()) {
                            strokePath.moveTo(offsets[0].x, offsets[0].y)
                            fillPath.moveTo(offsets[0].x, height - paddingBottom)
                            fillPath.lineTo(offsets[0].x, offsets[0].y)

                            for (i in 0 until offsets.size - 1) {
                                val p0 = offsets[i]
                                val p1 = offsets[i + 1]
                                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                                strokePath.cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    p1.x, p1.y
                                )
                                fillPath.cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    p1.x, p1.y
                                )
                            }

                            fillPath.lineTo(offsets.last().x, height - paddingBottom)
                            fillPath.close()

                            // Draw Gradient Area Under Curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        currentAccent.copy(alpha = 0.32f),
                                        currentAccent.copy(alpha = 0.01f)
                                    ),
                                    startY = paddingTop,
                                    endY = height - paddingBottom
                                )
                            )

                            // Draw Stroke Curve
                            drawPath(
                                path = strokePath,
                                color = currentAccent,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // 3. Draw Points & Selected Point Highlighter
                            offsets.forEachIndexed { index, offset ->
                                val isSelected = selectedIndex == index
                                if (isSelected) {
                                    // Highlight vertical guide line
                                    drawLine(
                                        color = currentAccent.copy(alpha = 0.45f),
                                        start = Offset(offset.x, paddingTop),
                                        end = Offset(offset.x, height - paddingBottom),
                                        strokeWidth = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                    // Outer ring
                                    drawCircle(
                                        color = currentAccent.copy(alpha = 0.25f),
                                        radius = 11.dp.toPx(),
                                        center = offset
                                    )
                                }

                                // Solid point center
                                drawCircle(
                                    color = if (isSelected) currentAccent else onSurface,
                                    radius = if (isSelected) 5.5.dp.toPx() else 3.5.dp.toPx(),
                                    center = offset
                                )
                            }

                            // 4. Draw Clamped Non-Clipping Tooltip for selected point
                            selectedIndex?.let { index ->
                                val point = points.getOrNull(index)
                                val offset = offsets.getOrNull(index)
                                if (point != null && offset != null) {
                                    val tooltipText = "${point.label} • ${"%,d".format(point.value.toInt())} $unit"
                                    val textPaint = Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 11.sp.toPx()
                                        isAntiAlias = true
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    }
                                    val textBounds = Rect()
                                    textPaint.getTextBounds(tooltipText, 0, tooltipText.length, textBounds)

                                    val tooltipPaddingHorizontal = 12.dp.toPx()
                                    val tooltipPaddingVertical = 6.dp.toPx()
                                    val tooltipWidth = textBounds.width() + (tooltipPaddingHorizontal * 2)
                                    val tooltipHeight = textBounds.height() + (tooltipPaddingVertical * 2)

                                    // Clamp X so it NEVER clips out of screen bounds
                                    val clampedX = offset.x.coerceIn(
                                        tooltipWidth / 2 + 8.dp.toPx(),
                                        width - tooltipWidth / 2 - 8.dp.toPx()
                                    )
                                    // Position above point if space permits, otherwise below point
                                    val tooltipY = if (offset.y - tooltipHeight - 12.dp.toPx() >= 0f) {
                                        offset.y - tooltipHeight - 12.dp.toPx()
                                    } else {
                                        offset.y + 12.dp.toPx()
                                    }

                                    // Draw Tooltip Capsule
                                    drawRoundRect(
                                        color = Color(0xFF1E1E22),
                                        topLeft = Offset(clampedX - tooltipWidth / 2, tooltipY),
                                        size = Size(tooltipWidth, tooltipHeight),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                    )
                                    // Accent Outline on Tooltip
                                    drawRoundRect(
                                        color = currentAccent.copy(alpha = 0.6f),
                                        topLeft = Offset(clampedX - tooltipWidth / 2, tooltipY),
                                        size = Size(tooltipWidth, tooltipHeight),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                        style = Stroke(width = 1.dp.toPx())
                                    )

                                    // Draw text centered inside tooltip
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawText(
                                            tooltipText,
                                            clampedX - (textBounds.width() / 2f),
                                            tooltipY + tooltipHeight - tooltipPaddingVertical + 1.dp.toPx(),
                                            textPaint
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. X-Axis Day Labels at the bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, bottom = 2.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        points.forEachIndexed { index, pt ->
                            val isSelected = selectedIndex == index
                            Text(
                                text = pt.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) currentAccent else onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
}
