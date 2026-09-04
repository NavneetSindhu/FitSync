package com.minimize.maximus.ui.components.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.ui.components.getAvatarById
import java.text.SimpleDateFormat
import java.util.*

enum class ShareTheme(val displayName: String) {
    FROSTED_LIGHT("Clean Light"),
    DARK_ONYX("Dark Onyx"),
    ELECTRIC_GRADIENT("Electric")
}

enum class ShareAspectRatio(val displayName: String, val ratio: Float) {
    SQUARE("Square (1:1)", 1f),
    STORY("Story (9:16)", 9f / 16f)
}

private data class CardThemePalette(
    val bgBrush: Brush,
    val textColor: Color,
    val subTextColor: Color,
    val cardBg: Color,
    val cardBorder: Color
)

@Composable
fun WorkoutShareCard(
    workout: WorkoutSession,
    userName: String,
    avatarId: String,
    unit: String,
    theme: ShareTheme,
    aspectRatio: ShareAspectRatio,
    showPrs: Boolean = true,
    showVolume: Boolean = true,
    showAvatar: Boolean = true,
    modifier: Modifier = Modifier
) {
    val totalVolume = remember(workout) {
        workout.exercise.sumOf { ex -> ex.sets.sumOf { (it.weight * it.reps).toDouble() } }.toInt()
    }
    val totalSets = remember(workout) {
        workout.exercise.sumOf { it.sets.size }
    }
    val totalExercises = remember(workout) {
        workout.exercise.size
    }
    val prCount = remember(workout) {
        workout.exercise.sumOf { ex -> ex.sets.count { it.setType.equals("PR", ignoreCase = true) } }
    }
    val formattedDate = remember(workout.date) {
        SimpleDateFormat("EEE, MMM dd • yyyy", Locale.getDefault()).format(Date(workout.date))
    }
    val avatar = remember(avatarId) { getAvatarById(avatarId) }

    val palette = when (theme) {
        ShareTheme.DARK_ONYX -> CardThemePalette(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF18181B), Color(0xFF09090B))),
            textColor = Color.White,
            subTextColor = Color(0xFFA1A1AA),
            cardBg = Color(0xFF27272A).copy(alpha = 0.6f),
            cardBorder = Color(0xFF3F3F46)
        )
        ShareTheme.ELECTRIC_GRADIENT -> CardThemePalette(
            bgBrush = Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF1E3A8A))),
            textColor = Color.White,
            subTextColor = Color(0xFF93C5FD),
            cardBg = Color(0xFF1E293B).copy(alpha = 0.8f),
            cardBorder = Color(0xFF3B82F6).copy(alpha = 0.5f)
        )
        ShareTheme.FROSTED_LIGHT -> CardThemePalette(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))),
            textColor = Color(0xFF0F172A),
            subTextColor = Color(0xFF64748B),
            cardBg = Color.White.copy(alpha = 0.9f),
            cardBorder = Color(0xFFCBD5E1)
        )
    }

    val (bgBrush, textColor, subTextColor, cardBg, cardBorder) = palette

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        modifier = modifier
            .aspectRatio(aspectRatio.ratio)
            .clip(RoundedCornerShape(28.dp))
            .background(bgBrush)
            .border(2.dp, cardBorder, RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (aspectRatio == ShareAspectRatio.STORY) 20.dp else 16.dp)
        ) {
            // ── 1. Header: Branding & Athlete Profile ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showAvatar) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = avatar.drawableRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = userName.ifBlank { "Athlete" },
                            fontWeight = FontWeight.Black,
                            fontSize = 14.5.sp,
                            color = textColor,
                            maxLines = 1
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = subTextColor,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Maximus Brand Pill (Always single-line, never compressed or clipped)
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "MAXIMUS",
                        fontWeight = FontWeight.Black,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.6.sp,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(if (aspectRatio == ShareAspectRatio.STORY) 16.dp else 10.dp))

            // ── 2. Workout Title & Big Metric Hero ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = workout.name,
                    fontWeight = FontWeight.Black,
                    fontSize = if (aspectRatio == ShareAspectRatio.STORY) 24.sp else 20.sp,
                    color = textColor,
                    lineHeight = 28.sp,
                    maxLines = 2
                )

                // Metrics Grid (Always 3 Balanced, Uncluttered Cards)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showVolume) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("VOLUME", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = subTextColor, letterSpacing = 0.5.sp, maxLines = 1, softWrap = false)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "$totalVolume",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor,
                                    maxLines = 1
                                )
                                Text(unit, fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = subTextColor, maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SETS", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = subTextColor, letterSpacing = 0.5.sp, maxLines = 1, softWrap = false)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$totalSets",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                maxLines = 1
                            )
                            Text("Sets", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = subTextColor, maxLines = 1, softWrap = false)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (showPrs && prCount > 0) Color(0xFFF59E0B).copy(alpha = 0.18f) else cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showPrs && prCount > 0) Color(0xFFF59E0B).copy(alpha = 0.8f) else cardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isPr = showPrs && prCount > 0
                            val label = if (isPr) "RECORDS" else "EXERCISES"
                            val value = if (isPr) "$prCount" else "$totalExercises"
                            val subLabel = if (isPr) "New PRs 🔥" else if (totalExercises == 1) "Exercise" else "Exercises"
                            val valueColor = if (isPr) Color(0xFFF59E0B) else textColor
                            val labelColor = if (isPr) Color(0xFFF59E0B) else subTextColor

                            Text(label, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = labelColor, letterSpacing = 0.5.sp, maxLines = 1, softWrap = false)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = value,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = valueColor,
                                maxLines = 1
                            )
                            Text(subLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = labelColor, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            // ── Flexible Spacer: Pushes the exercise legend and footer to the bottom ──
            Spacer(Modifier.weight(1f))

            // ── 3. Exercise Highlights Strip (Legend pinned at the bottom) ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "EXERCISES COMPLETED",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = subTextColor,
                    letterSpacing = 0.5.sp
                )
                val maxVisible = if (aspectRatio == ShareAspectRatio.STORY) 4 else 2
                workout.exercise.take(maxVisible).forEach { ex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ex.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${ex.sets.size} sets",
                            fontSize = 11.5.sp,
                            color = subTextColor,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(if (aspectRatio == ShareAspectRatio.STORY) 14.dp else 10.dp))

            // ── 4. Bottom Footer Quote ──
            Text(
                text = "Logged with Maximus • Keep Showing Up",
                fontSize = if (aspectRatio == ShareAspectRatio.STORY) 8.5.sp else 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = subTextColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
