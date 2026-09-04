package com.minimize.maximus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.R
import com.minimize.maximus.domain.manager.ActiveLiveWorkout
import com.minimize.maximus.ui.theme.MaximusDimens
import com.minimize.maximus.ui.theme.LocalAccentColor

@Composable
fun ActiveWorkoutMiniBar(
    activeWorkout: ActiveLiveWorkout?,
    onResumeClick: () -> Unit,
    onDiscardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current

    AnimatedVisibility(
        visible = activeWorkout != null && activeWorkout.isMinimized,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        if (activeWorkout != null) {
            val totalSecs = activeWorkout.elapsedSeconds
            val minutes = totalSecs / 60
            val seconds = totalSecs % 60
            val timeString = remember(totalSecs) { "%02d:%02d".format(minutes, seconds) }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, currentAccent.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = MaximusDimens.FloatingNavHeight + 16.dp)
                    .clickable { onResumeClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pulsing Active Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )

                        Column {
                            Text(
                                text = activeWorkout.sessionName.ifBlank { stringResource(R.string.active_workout_mini_title) },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "$timeString • ${activeWorkout.exercises.size} exercises",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onResumeClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentAccent,
                                contentColor = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(stringResource(R.string.active_workout_resume), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = onDiscardClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = MaximusIcons.WorkoutLog.CloseDiscard,
                                contentDescription = stringResource(R.string.active_workout_discard),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
