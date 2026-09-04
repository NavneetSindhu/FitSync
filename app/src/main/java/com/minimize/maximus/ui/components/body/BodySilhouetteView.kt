package com.minimize.maximus.ui.components.body

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.minimize.maximus.domain.model.body.BodyViewMode
import com.minimize.maximus.domain.model.body.MuscleGroup
import com.minimize.maximus.domain.model.body.MuscleStat
import com.minimize.maximus.ui.theme.LocalAccentColor

/**
 * 3D Holographic Athletic Anatomical Model for FitSync
 * Features directional lighting gradients, multi-head muscle striations,
 * holographic blueprint joint grids, neon aura glows, and interactive callouts.
 */
@Composable
fun BodySilhouetteView(
    viewMode: BodyViewMode,
    muscleStats: Map<MuscleGroup, MuscleStat>,
    selectedMuscle: MuscleGroup?,
    onMuscleSelected: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalAccentColor.current
    val neutralBase = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val neutralShadow = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    val blueprintColor = accentColor.copy(alpha = 0.12f)
    val jointGlowColor = accentColor.copy(alpha = 0.6f)

    // Ambient pulsing glow for holographic effects
    val infiniteTransition = rememberInfiniteTransition(label = "HoloPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(viewMode) {
                        detectTapGestures { tapOffset ->
                            val canvasW = size.width
                            val canvasH = size.height
                            val cX = canvasW / 2f
                            val scale = minOf(canvasW / 300f, canvasH / 500f)
                            val topY = (canvasH - (500f * scale)) / 2f

                            val refX = 150f + ((tapOffset.x - cX) / scale)
                            val refY = (tapOffset.y - topY) / scale

                            val detectedMuscle = if (viewMode == BodyViewMode.FRONT) {
                                detectFrontMuscleTap(refX, refY)
                            } else {
                                detectBackMuscleTap(refX, refY)
                            }
                            detectedMuscle?.let { onMuscleSelected(it) }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val cX = canvasW / 2f
                val scale = minOf(canvasW / 300f, canvasH / 500f)
                val topY = (canvasH - (500f * scale)) / 2f

                fun sx(x: Float): Float = cX + ((x - 150f) * scale)
                fun sy(y: Float): Float = topY + (y * scale)

                // ── 1. Holographic Blueprint Background Grid & Joint Rings ──
                drawBlueprintGrid(
                    sx = ::sx,
                    sy = ::sy,
                    scale = scale,
                    blueprintColor = blueprintColor,
                    jointColor = jointGlowColor,
                    pulseAlpha = pulseAlpha
                )

                // ── 2. Head, Cranium & Neck Base ──
                val headPath = Path().apply {
                    moveTo(sx(150f), sy(14f))
                    cubicTo(sx(168f), sy(14f), sx(174f), sy(26f), sx(172f), sy(42f))
                    cubicTo(sx(170f), sy(54f), sx(162f), sy(60f), sx(150f), sy(62f))
                    cubicTo(sx(138f), sy(60f), sx(130f), sy(54f), sx(128f), sy(42f))
                    cubicTo(sx(126f), sy(26f), sx(132f), sy(14f), sx(150f), sy(14f))
                    close()
                }
                val headBrush = Brush.verticalGradient(
                    colors = listOf(neutralBase.copy(alpha = 0.5f), neutralShadow),
                    startY = sy(14f),
                    endY = sy(62f)
                )
                drawPath(headPath, brush = headBrush, style = Fill)
                drawPath(headPath, color = outlineColor, style = Stroke(width = 1.6f * scale))

                // Neck (Sternocleidomastoid Pillars)
                val neckPath = Path().apply {
                    moveTo(sx(139f), sy(56f))
                    cubicTo(sx(136f), sy(64f), sx(133f), sy(70f), sx(132f), sy(74f))
                    lineTo(sx(168f), sy(74f))
                    cubicTo(sx(167f), sy(70f), sx(164f), sy(64f), sx(161f), sy(56f))
                    close()
                }
                drawPath(neckPath, brush = headBrush, style = Fill)
                drawPath(neckPath, color = outlineColor, style = Stroke(width = 1.3f * scale))

                // ── 3. Anatomical Body Rendering (Front vs Back) ──
                if (viewMode == BodyViewMode.FRONT) {
                    drawChiseledFrontBody(
                        sx = ::sx,
                        sy = ::sy,
                        scale = scale,
                        muscleStats = muscleStats,
                        selectedMuscle = selectedMuscle,
                        accentColor = accentColor,
                        outlineColor = outlineColor,
                        neutralBase = neutralBase,
                        neutralShadow = neutralShadow,
                        pulseAlpha = pulseAlpha
                    )
                } else {
                    drawChiseledBackBody(
                        sx = ::sx,
                        sy = ::sy,
                        scale = scale,
                        muscleStats = muscleStats,
                        selectedMuscle = selectedMuscle,
                        accentColor = accentColor,
                        outlineColor = outlineColor,
                        neutralBase = neutralBase,
                        neutralShadow = neutralShadow,
                        pulseAlpha = pulseAlpha
                    )
                }
            }
        }
    }
}

// ── Blueprint Grid & Joint Nodes ──────────────────────────────────────────
private fun DrawScope.drawBlueprintGrid(
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    scale: Float,
    blueprintColor: Color,
    jointColor: Color,
    pulseAlpha: Float
) {
    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f * scale, 6f * scale), 0f)

    // Center Vertical Linea Alba / Spine Plumb Axis
    drawLine(
        color = blueprintColor,
        start = Offset(sx(150f), sy(10f)),
        end = Offset(sx(150f), sy(480f)),
        strokeWidth = 1f * scale,
        pathEffect = dashedEffect
    )

    // Horizontal Guideline at Shoulder Clavicle & Pelvis
    drawLine(
        color = blueprintColor.copy(alpha = 0.5f),
        start = Offset(sx(60f), sy(74f)),
        end = Offset(sx(240f), sy(74f)),
        strokeWidth = 1f * scale,
        pathEffect = dashedEffect
    )
    drawLine(
        color = blueprintColor.copy(alpha = 0.5f),
        start = Offset(sx(80f), sy(192f)),
        end = Offset(sx(220f), sy(192f)),
        strokeWidth = 1f * scale,
        pathEffect = dashedEffect
    )

    // Major Joint Target Holographic Nodes
    val joints = listOf(
        Offset(sx(74f), sy(84f)),   // Left Shoulder Acromion
        Offset(sx(226f), sy(84f)),  // Right Shoulder Acromion
        Offset(sx(68f), sy(154f)),  // Left Elbow
        Offset(sx(232f), sy(154f)), // Right Elbow
        Offset(sx(118f), sy(318f)), // Left Knee Patella
        Offset(sx(182f), sy(318f)), // Right Knee Patella
        Offset(sx(118f), sy(432f)), // Left Ankle
        Offset(sx(182f), sy(432f))  // Right Ankle
    )

    joints.forEach { pos ->
        drawCircle(
            color = jointColor.copy(alpha = pulseAlpha * 0.4f),
            radius = 6f * scale,
            center = pos,
            style = Fill
        )
        drawCircle(
            color = jointColor.copy(alpha = pulseAlpha * 0.9f),
            radius = 2.5f * scale,
            center = pos,
            style = Fill
        )
    }
}

// ── 3D Chiseled Front Anatomy ─────────────────────────────────────────────
private fun DrawScope.drawChiseledFrontBody(
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    scale: Float,
    muscleStats: Map<MuscleGroup, MuscleStat>,
    selectedMuscle: MuscleGroup?,
    accentColor: Color,
    outlineColor: Color,
    neutralBase: Color,
    neutralShadow: Color,
    pulseAlpha: Float
) {
    fun createMuscleBrush(group: MuscleGroup, topY: Float, botY: Float): Pair<Brush, Color> {
        val stat = muscleStats[group]
        val isSelected = group == selectedMuscle
        val baseColor = when {
            isSelected -> accentColor
            stat != null && stat.weeklySets > 0 -> stat.recoveryState.color
            else -> neutralBase
        }
        val topHighlight = baseColor.copy(alpha = if (isSelected) 1f else 0.85f)
        val botShade = when {
            isSelected -> accentColor.copy(alpha = 0.5f)
            stat != null && stat.weeklySets > 0 -> stat.recoveryState.color.copy(alpha = 0.45f)
            else -> neutralShadow
        }
        val brush = Brush.verticalGradient(listOf(topHighlight, botShade), topY, botY)
        return brush to baseColor
    }

    // ── 1. PECTORALIS MAJOR (Clavicular Upper & Sternal Lower Heads) ──
    val (chestBrush, chestColor) = createMuscleBrush(MuscleGroup.CHEST, sy(74f), sy(120f))
    val leftPec = Path().apply {
        moveTo(sx(148f), sy(74f))
        cubicTo(sx(132f), sy(73f), sx(114f), sy(74f), sx(106f), sy(76f))
        cubicTo(sx(102f), sy(90f), sx(104f), sy(104f), sx(106f), sy(108f))
        cubicTo(sx(118f), sy(120f), sx(136f), sy(120f), sx(148f), sy(112f))
        close()
    }
    val rightPec = Path().apply {
        moveTo(sx(152f), sy(74f))
        cubicTo(sx(168f), sy(73f), sx(186f), sy(74f), sx(194f), sy(76f))
        cubicTo(sx(198f), sy(90f), sx(196f), sy(104f), sx(194f), sy(108f))
        cubicTo(sx(182f), sy(120f), sx(164f), sy(120f), sx(152f), sy(112f))
        close()
    }
    // Neon Glow if selected
    if (selectedMuscle == MuscleGroup.CHEST) {
        drawPath(leftPec, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightPec, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftPec, brush = chestBrush, style = Fill)
    drawPath(leftPec, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightPec, brush = chestBrush, style = Fill)
    drawPath(rightPec, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // Pectoral Clavicular Striation Divider Lines
    drawLine(color = outlineColor.copy(alpha = 0.5f), start = Offset(sx(108f), sy(88f)), end = Offset(sx(146f), sy(94f)), strokeWidth = 1f * scale)
    drawLine(color = outlineColor.copy(alpha = 0.5f), start = Offset(sx(192f), sy(88f)), end = Offset(sx(154f), sy(94f)), strokeWidth = 1f * scale)

    // ── 2. ANTERIOR & LATERAL DELTOIDS ──
    val (deltBrush, deltColor) = createMuscleBrush(MuscleGroup.FRONT_DELTS, sy(75f), sy(118f))
    val leftDelt = Path().apply {
        moveTo(sx(104f), sy(75f))
        cubicTo(sx(88f), sy(77f), sx(76f), sy(86f), sx(74f), sy(98f))
        cubicTo(sx(74f), sy(108f), sx(82f), sy(116f), sx(96f), sy(118f))
        cubicTo(sx(100f), sy(110f), sx(102f), sy(96f), sx(104f), sy(75f))
        close()
    }
    val rightDelt = Path().apply {
        moveTo(sx(196f), sy(75f))
        cubicTo(sx(212f), sy(77f), sx(224f), sy(86f), sx(226f), sy(98f))
        cubicTo(sx(226f), sy(108f), sx(218f), sy(116f), sx(204f), sy(118f))
        cubicTo(sx(200f), sy(110f), sx(198f), sy(96f), sx(196f), sy(75f))
        close()
    }
    if (selectedMuscle == MuscleGroup.FRONT_DELTS) {
        drawPath(leftDelt, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightDelt, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftDelt, brush = deltBrush, style = Fill)
    drawPath(leftDelt, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightDelt, brush = deltBrush, style = Fill)
    drawPath(rightDelt, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 3. BICEPS BRACHII (3D Muscle Peak Curve) ──
    val (bicepBrush, bicepColor) = createMuscleBrush(MuscleGroup.BICEPS, sy(118f), sy(158f))
    val leftBicep = Path().apply {
        moveTo(sx(94f), sy(118f))
        cubicTo(sx(82f), sy(122f), sx(76f), sy(134f), sx(74f), sy(148f))
        cubicTo(sx(78f), sy(156f), sx(88f), sy(158f), sx(94f), sy(154f))
        cubicTo(sx(98f), sy(144f), sx(98f), sy(128f), sx(94f), sy(118f))
        close()
    }
    val rightBicep = Path().apply {
        moveTo(sx(206f), sy(118f))
        cubicTo(sx(218f), sy(122f), sx(224f), sy(134f), sx(226f), sy(148f))
        cubicTo(sx(222f), sy(156f), sx(212f), sy(158f), sx(206f), sy(154f))
        cubicTo(sx(202f), sy(144f), sx(202f), sy(128f), sx(206f), sy(118f))
        close()
    }
    if (selectedMuscle == MuscleGroup.BICEPS) {
        drawPath(leftBicep, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightBicep, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftBicep, brush = bicepBrush, style = Fill)
    drawPath(leftBicep, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightBicep, brush = bicepBrush, style = Fill)
    drawPath(rightBicep, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 4. FOREARMS (Brachioradialis Curve) ──
    val (forearmBrush, forearmColor) = createMuscleBrush(MuscleGroup.FOREARMS, sy(156f), sy(220f))
    val leftForearm = Path().apply {
        moveTo(sx(74f), sy(156f))
        cubicTo(sx(68f), sy(170f), sx(60f), sy(192f), sx(58f), sy(218f))
        cubicTo(sx(64f), sy(220f), sx(70f), sy(220f), sx(74f), sy(216f))
        cubicTo(sx(80f), sy(196f), sx(88f), sy(176f), sx(92f), sy(158f))
        close()
    }
    val rightForearm = Path().apply {
        moveTo(sx(226f), sy(156f))
        cubicTo(sx(232f), sy(170f), sx(240f), sy(192f), sx(242f), sy(218f))
        cubicTo(sx(236f), sy(220f), sx(230f), sy(220f), sx(226f), sy(216f))
        cubicTo(sx(220f), sy(196f), sx(212f), sy(176f), sx(208f), sy(158f))
        close()
    }
    if (selectedMuscle == MuscleGroup.FOREARMS) {
        drawPath(leftForearm, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightForearm, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftForearm, brush = forearmBrush, style = Fill)
    drawPath(leftForearm, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightForearm, brush = forearmBrush, style = Fill)
    drawPath(rightForearm, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 5. RECTUS ABDOMINIS & SERRATUS / OBLIQUES (6-Pack with Inscriptions) ──
    val (absBrush, absColor) = createMuscleBrush(MuscleGroup.ABS, sy(118f), sy(190f))
    val absUpperLeft = Path().apply { moveTo(sx(147f), sy(118f)); lineTo(sx(128f), sy(118f)); lineTo(sx(129f), sy(138f)); lineTo(sx(147f), sy(138f)); close() }
    val absUpperRight = Path().apply { moveTo(sx(153f), sy(118f)); lineTo(sx(172f), sy(118f)); lineTo(sx(171f), sy(138f)); lineTo(sx(153f), sy(138f)); close() }
    val absMidLeft = Path().apply { moveTo(sx(147f), sy(141f)); lineTo(sx(130f), sy(141f)); lineTo(sx(131f), sy(161f)); lineTo(sx(147f), sy(161f)); close() }
    val absMidRight = Path().apply { moveTo(sx(153f), sy(141f)); lineTo(sx(170f), sy(141f)); lineTo(sx(169f), sy(161f)); lineTo(sx(153f), sy(161f)); close() }
    val absLowerLeft = Path().apply { moveTo(sx(147f), sy(164f)); lineTo(sx(132f), sy(164f)); lineTo(sx(136f), sy(188f)); lineTo(sx(147f), sy(190f)); close() }
    val absLowerRight = Path().apply { moveTo(sx(153f), sy(164f)); lineTo(sx(168f), sy(164f)); lineTo(sx(164f), sy(188f)); lineTo(sx(153f), sy(190f)); close() }

    listOf(absUpperLeft, absUpperRight, absMidLeft, absMidRight, absLowerLeft, absLowerRight).forEach { path ->
        if (selectedMuscle == MuscleGroup.ABS) {
            drawPath(path, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 5f * scale))
        }
        drawPath(path, brush = absBrush, style = Fill)
        drawPath(path, color = outlineColor, style = Stroke(width = 1.3f * scale))
    }

    // Serratus & External Obliques
    val leftObliques = Path().apply { moveTo(sx(118f), sy(122f)); lineTo(sx(108f), sy(134f)); lineTo(sx(112f), sy(176f)); lineTo(sx(128f), sy(188f)); lineTo(sx(124f), sy(138f)); close() }
    val rightObliques = Path().apply { moveTo(sx(182f), sy(122f)); lineTo(sx(192f), sy(134f)); lineTo(sx(188f), sy(176f)); lineTo(sx(172f), sy(188f)); lineTo(sx(176f), sy(138f)); close() }
    drawPath(leftObliques, brush = absBrush, style = Fill)
    drawPath(leftObliques, color = outlineColor, style = Stroke(width = 1.3f * scale))
    drawPath(rightObliques, brush = absBrush, style = Fill)
    drawPath(rightObliques, color = outlineColor, style = Stroke(width = 1.3f * scale))

    // ── 6. QUADRICEPS (Vastus Lateralis & Medialis Teardrop Sweeps) ──
    val (quadBrush, quadColor) = createMuscleBrush(MuscleGroup.QUADS, sy(196f), sy(316f))
    val leftQuad = Path().apply {
        moveTo(sx(116f), sy(196f))
        cubicTo(sx(98f), sy(214f), sx(94f), sy(250f), sx(98f), sy(294f))
        cubicTo(sx(106f), sy(316f), sx(130f), sy(316f), sx(138f), sy(306f))
        cubicTo(sx(144f), sy(276f), sx(144f), sy(232f), sx(144f), sy(198f))
        close()
    }
    val rightQuad = Path().apply {
        moveTo(sx(184f), sy(196f))
        cubicTo(sx(202f), sy(214f), sx(206f), sy(250f), sx(202f), sy(294f))
        cubicTo(sx(194f), sy(316f), sx(170f), sy(316f), sx(162f), sy(306f))
        cubicTo(sx(156f), sy(276f), sx(156f), sy(232f), sx(156f), sy(198f))
        close()
    }
    if (selectedMuscle == MuscleGroup.QUADS) {
        drawPath(leftQuad, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightQuad, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftQuad, brush = quadBrush, style = Fill)
    drawPath(leftQuad, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightQuad, brush = quadBrush, style = Fill)
    drawPath(rightQuad, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // Vastus Medialis (Teardrop above knee) Contour Lines
    val leftTeardrop = Path().apply { moveTo(sx(124f), sy(280f)); cubicTo(sx(128f), sy(298f), sx(138f), sy(304f), sx(136f), sy(294f)) }
    val rightTeardrop = Path().apply { moveTo(sx(176f), sy(280f)); cubicTo(sx(172f), sy(298f), sx(162f), sy(304f), sx(164f), sy(294f)) }
    drawPath(leftTeardrop, color = outlineColor.copy(alpha = 0.6f), style = Stroke(width = 1.2f * scale))
    drawPath(rightTeardrop, color = outlineColor.copy(alpha = 0.6f), style = Stroke(width = 1.2f * scale))

    // ── 7. CALVES & TIBIALIS ANTERIOR ──
    val (calfBrush, calfColor) = createMuscleBrush(MuscleGroup.CALVES, sy(324f), sy(436f))
    val leftCalf = Path().apply {
        moveTo(sx(104f), sy(324f))
        cubicTo(sx(94f), sy(346f), sx(94f), sy(372f), sx(104f), sy(410f))
        cubicTo(sx(112f), sy(436f), sx(126f), sy(436f), sx(132f), sy(412f))
        cubicTo(sx(138f), sy(376f), sx(136f), sy(346f), sx(134f), sy(324f))
        close()
    }
    val rightCalf = Path().apply {
        moveTo(sx(196f), sy(324f))
        cubicTo(sx(206f), sy(346f), sx(206f), sy(372f), sx(196f), sy(410f))
        cubicTo(sx(188f), sy(436f), sx(174f), sy(436f), sx(168f), sy(412f))
        cubicTo(sx(162f), sy(376f), sx(164f), sy(346f), sx(166f), sy(324f))
        close()
    }
    if (selectedMuscle == MuscleGroup.CALVES) {
        drawPath(leftCalf, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightCalf, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftCalf, brush = calfBrush, style = Fill)
    drawPath(leftCalf, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightCalf, brush = calfBrush, style = Fill)
    drawPath(rightCalf, color = outlineColor, style = Stroke(width = 1.5f * scale))
}

// ── 3D Chiseled Back Anatomy ──────────────────────────────────────────────
private fun DrawScope.drawChiseledBackBody(
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    scale: Float,
    muscleStats: Map<MuscleGroup, MuscleStat>,
    selectedMuscle: MuscleGroup?,
    accentColor: Color,
    outlineColor: Color,
    neutralBase: Color,
    neutralShadow: Color,
    pulseAlpha: Float
) {
    fun createMuscleBrush(group: MuscleGroup, topY: Float, botY: Float): Pair<Brush, Color> {
        val stat = muscleStats[group]
        val isSelected = group == selectedMuscle
        val baseColor = when {
            isSelected -> accentColor
            stat != null && stat.weeklySets > 0 -> stat.recoveryState.color
            else -> neutralBase
        }
        val topHighlight = baseColor.copy(alpha = if (isSelected) 1f else 0.85f)
        val botShade = when {
            isSelected -> accentColor.copy(alpha = 0.5f)
            stat != null && stat.weeklySets > 0 -> stat.recoveryState.color.copy(alpha = 0.45f)
            else -> neutralShadow
        }
        val brush = Brush.verticalGradient(listOf(topHighlight, botShade), topY, botY)
        return brush to baseColor
    }

    // ── 1. TRAPEZIUS (Diamond Cervical & Thoracic Spine Complex) ──
    val (trapsBrush, trapsColor) = createMuscleBrush(MuscleGroup.TRAPS, sy(58f), sy(146f))
    val trapsDiamond = Path().apply {
        moveTo(sx(150f), sy(58f))
        cubicTo(sx(132f), sy(64f), sx(114f), sy(72f), sx(104f), sy(76f))
        cubicTo(sx(118f), sy(94f), sx(136f), sy(126f), sx(150f), sy(146f))
        cubicTo(sx(164f), sy(126f), sx(182f), sy(94f), sx(196f), sy(76f))
        cubicTo(sx(186f), sy(72f), sx(168f), sy(64f), sx(150f), sy(58f))
        close()
    }
    if (selectedMuscle == MuscleGroup.TRAPS) {
        drawPath(trapsDiamond, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(trapsDiamond, brush = trapsBrush, style = Fill)
    drawPath(trapsDiamond, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 2. LATISSIMUS DORSI (Sweeping V-Taper Wings) ──
    val (latsBrush, latsColor) = createMuscleBrush(MuscleGroup.LATS, sy(82f), sy(176f))
    val leftLat = Path().apply {
        moveTo(sx(108f), sy(82f))
        cubicTo(sx(88f), sy(106f), sx(84f), sy(136f), sx(96f), sy(164f))
        cubicTo(sx(116f), sy(176f), sx(136f), sy(174f), sx(144f), sy(166f))
        cubicTo(sx(134f), sy(138f), sx(122f), sy(108f), sx(108f), sy(82f))
        close()
    }
    val rightLat = Path().apply {
        moveTo(sx(192f), sy(82f))
        cubicTo(sx(212f), sy(106f), sx(216f), sy(136f), sx(204f), sy(164f))
        cubicTo(sx(184f), sy(176f), sx(164f), sy(174f), sx(156f), sy(166f))
        cubicTo(sx(166f), sy(138f), sx(178f), sy(108f), sx(192f), sy(82f))
        close()
    }
    if (selectedMuscle == MuscleGroup.LATS) {
        drawPath(leftLat, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightLat, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftLat, brush = latsBrush, style = Fill)
    drawPath(leftLat, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightLat, brush = latsBrush, style = Fill)
    drawPath(rightLat, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 3. TRICEPS BRACHII (Horseshoe Lateral & Long Heads) ──
    val (tricepBrush, tricepColor) = createMuscleBrush(MuscleGroup.TRICEPS, sy(98f), sy(158f))
    val leftTricep = Path().apply {
        moveTo(sx(74f), sy(98f))
        cubicTo(sx(64f), sy(114f), sx(64f), sy(136f), sx(72f), sy(154f))
        cubicTo(sx(82f), sy(158f), sx(92f), sy(154f), sx(94f), sy(146f))
        cubicTo(sx(92f), sy(126f), sx(86f), sy(108f), sx(74f), sy(98f))
        close()
    }
    val rightTricep = Path().apply {
        moveTo(sx(226f), sy(98f))
        cubicTo(sx(236f), sy(114f), sx(236f), sy(136f), sx(228f), sy(154f))
        cubicTo(sx(218f), sy(158f), sx(208f), sy(154f), sx(206f), sy(146f))
        cubicTo(sx(208f), sy(126f), sx(214f), sy(108f), sx(226f), sy(98f))
        close()
    }
    if (selectedMuscle == MuscleGroup.TRICEPS) {
        drawPath(leftTricep, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightTricep, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftTricep, brush = tricepBrush, style = Fill)
    drawPath(leftTricep, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightTricep, brush = tricepBrush, style = Fill)
    drawPath(rightTricep, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 4. GLUTEUS MAXIMUS (Anatomical Heart/Shield Curvature) ──
    val (glutesBrush, glutesColor) = createMuscleBrush(MuscleGroup.GLUTES, sy(186f), sy(252f))
    val leftGlute = Path().apply {
        moveTo(sx(118f), sy(186f))
        cubicTo(sx(104f), sy(200f), sx(102f), sy(226f), sx(114f), sy(244f))
        cubicTo(sx(128f), sy(252f), sx(144f), sy(248f), sx(148f), sy(232f))
        cubicTo(sx(148f), sy(206f), sx(140f), sy(188f), sx(118f), sy(186f))
        close()
    }
    val rightGlute = Path().apply {
        moveTo(sx(182f), sy(186f))
        cubicTo(sx(196f), sy(200f), sx(198f), sy(226f), sx(186f), sy(244f))
        cubicTo(sx(172f), sy(252f), sx(156f), sy(248f), sx(152f), sy(232f))
        cubicTo(sx(152f), sy(206f), sx(160f), sy(188f), sx(182f), sy(186f))
        close()
    }
    if (selectedMuscle == MuscleGroup.GLUTES) {
        drawPath(leftGlute, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightGlute, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftGlute, brush = glutesBrush, style = Fill)
    drawPath(leftGlute, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightGlute, brush = glutesBrush, style = Fill)
    drawPath(rightGlute, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 5. HAMSTRINGS (Biceps Femoris & Semitendinosus) ──
    val (hamBrush, hamColor) = createMuscleBrush(MuscleGroup.HAMSTRINGS, sy(246f), sy(322f))
    val leftHam = Path().apply {
        moveTo(sx(114f), sy(248f))
        cubicTo(sx(102f), sy(266f), sx(102f), sy(294f), sx(108f), sy(316f))
        cubicTo(sx(124f), sy(322f), sx(136f), sy(320f), sx(142f), sy(306f))
        cubicTo(sx(146f), sy(284f), sx(146f), sy(258f), sx(146f), sy(246f))
        close()
    }
    val rightHam = Path().apply {
        moveTo(sx(186f), sy(248f))
        cubicTo(sx(198f), sy(266f), sx(198f), sy(294f), sx(192f), sy(316f))
        cubicTo(sx(176f), sy(322f), sx(164f), sy(320f), sx(158f), sy(306f))
        cubicTo(sx(154f), sy(284f), sx(154f), sy(258f), sx(154f), sy(246f))
        close()
    }
    if (selectedMuscle == MuscleGroup.HAMSTRINGS) {
        drawPath(leftHam, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightHam, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftHam, brush = hamBrush, style = Fill)
    drawPath(leftHam, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightHam, brush = hamBrush, style = Fill)
    drawPath(rightHam, color = outlineColor, style = Stroke(width = 1.5f * scale))

    // ── 6. CALVES (Gastrocnemius Medial/Lateral Bellies & Soleus) ──
    val (calfBrush, calfColor) = createMuscleBrush(MuscleGroup.CALVES, sy(326f), sy(434f))
    val leftCalf = Path().apply {
        moveTo(sx(106f), sy(326f))
        cubicTo(sx(94f), sy(346f), sx(94f), sy(372f), sx(106f), sy(410f))
        cubicTo(sx(114f), sy(434f), sx(126f), sy(434f), sx(132f), sy(410f))
        cubicTo(sx(138f), sy(376f), sx(138f), sy(346f), sx(136f), sy(326f))
        close()
    }
    val rightCalf = Path().apply {
        moveTo(sx(194f), sy(326f))
        cubicTo(sx(206f), sy(346f), sx(206f), sy(372f), sx(194f), sy(410f))
        cubicTo(sx(186f), sy(434f), sx(174f), sy(434f), sx(168f), sy(410f))
        cubicTo(sx(162f), sy(376f), sx(162f), sy(346f), sx(164f), sy(326f))
        close()
    }
    if (selectedMuscle == MuscleGroup.CALVES) {
        drawPath(leftCalf, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
        drawPath(rightCalf, color = accentColor.copy(alpha = 0.35f * pulseAlpha), style = Stroke(width = 6f * scale))
    }
    drawPath(leftCalf, brush = calfBrush, style = Fill)
    drawPath(leftCalf, color = outlineColor, style = Stroke(width = 1.5f * scale))
    drawPath(rightCalf, brush = calfBrush, style = Fill)
    drawPath(rightCalf, color = outlineColor, style = Stroke(width = 1.5f * scale))
}

// ── Interactive Touch Detection on Canvas ─────────────────────────────────
private fun detectFrontMuscleTap(refX: Float, refY: Float): MuscleGroup? {
    return when {
        refY in 70f..120f && refX in 100f..200f -> MuscleGroup.CHEST
        refY in 70f..120f && (refX < 100f || refX > 200f) -> MuscleGroup.FRONT_DELTS
        refY in 118f..160f && (refX < 100f || refX > 200f) -> MuscleGroup.BICEPS
        refY in 158f..230f && (refX < 100f || refX > 200f) -> MuscleGroup.FOREARMS
        refY in 118f..195f && refX in 105f..195f -> MuscleGroup.ABS
        refY in 195f..320f -> MuscleGroup.QUADS
        refY in 320f..440f -> MuscleGroup.CALVES
        else -> null
    }
}

private fun detectBackMuscleTap(refX: Float, refY: Float): MuscleGroup? {
    return when {
        refY in 55f..140f && refX in 110f..190f -> MuscleGroup.TRAPS
        refY in 80f..180f && (refX in 80f..140f || refX in 160f..220f) -> MuscleGroup.LATS
        refY in 95f..160f && (refX < 95f || refX > 205f) -> MuscleGroup.TRICEPS
        refY in 180f..250f && refX in 100f..200f -> MuscleGroup.GLUTES
        refY in 245f..325f -> MuscleGroup.HAMSTRINGS
        refY in 320f..440f -> MuscleGroup.CALVES
        else -> null
    }
}
