package com.minimize.maximus.ui.components.share

import android.graphics.Bitmap
import android.view.View
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.components.LocalBottomSheetDismiss
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import com.minimize.maximus.util.ShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutShareBottomSheet(
    workout: WorkoutSession,
    userName: String,
    avatarId: String,
    unit: String,
    onDismissRequest: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val initialTheme = if (isDark) ShareTheme.DARK_ONYX else ShareTheme.FROSTED_LIGHT
    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var selectedRatio by remember { mutableStateOf(ShareAspectRatio.SQUARE) }
    var showPrs by remember { mutableStateOf(true) }
    var showVolume by remember { mutableStateOf(true) }
    var showAvatar by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }

    val graphicsLayer = rememberGraphicsLayer()

    val toastManager = com.minimize.maximus.ui.components.LocalMaximusToast.current

    MaximusModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        val dismissSheet = LocalBottomSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MaximusSheetHeader(
                title = "Share Workout Summary",
                subtitle = "Generate a social-ready graphic of your workout"
            )

            // ── Live Interactive Card Preview ──
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (selectedRatio == ShareAspectRatio.STORY) 0.72f else 0.88f)
                    .drawWithCache {
                        onDrawWithContent {
                            graphicsLayer.record {
                                this@onDrawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                WorkoutShareCard(
                    workout = workout,
                    userName = userName,
                    avatarId = avatarId,
                    unit = unit,
                    theme = selectedTheme,
                    aspectRatio = selectedRatio,
                    showPrs = showPrs,
                    showVolume = showVolume,
                    showAvatar = showAvatar,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Aspect Ratio Selector ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShareAspectRatio.entries.forEach { ratio ->
                    val isSelected = selectedRatio == ratio
                    Surface(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            selectedRatio = ratio
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) currentAccent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, currentAccent) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = ratio.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) currentAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // ── Theme Selector ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShareTheme.entries.forEach { theme ->
                    val isSelected = selectedTheme == theme
                    Surface(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.FILTER_SWITCH)
                            selectedTheme = theme
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) currentAccent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, currentAccent) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = theme.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) currentAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // ── Action Share Button ──
            Button(
                onClick = {
                    MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                    isExporting = true
                    coroutineScope.launch {
                        try {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            ShareHelper.saveBitmapAndShare(context, bitmap, "Workout - ${workout.name}")
                            dismissSheet()
                        } catch (e: Exception) {
                            toastManager.showToast("Export failed: ${e.localizedMessage}", com.minimize.maximus.ui.components.ToastType.ERROR)
                        } finally {
                            isExporting = false
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Share Workout Image",
                        fontWeight = FontWeight.Bold,
                        color = if (currentAccent.luminance() > 0.45f) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
