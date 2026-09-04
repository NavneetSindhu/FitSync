package com.minimize.maximus.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.LocalAccentColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Toast classification types supporting semantic badge accents and icons.
 */
enum class ToastType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}

/**
 * Immutable payload describing a custom toast notification.
 */
data class MaximusToastData(
    val id: Long = System.currentTimeMillis(),
    val title: String? = null,
    val message: String,
    val type: ToastType = ToastType.SUCCESS,
    val durationMs: Long = 3200L,
    val isPersistent: Boolean = false
)

/**
 * State holder for managing toast presentation throughout the application.
 */
@Stable
class MaximusToastManager {
    var currentToast by mutableStateOf<MaximusToastData?>(null)
        private set

    fun showToast(
        message: String,
        type: ToastType = ToastType.SUCCESS,
        title: String? = null,
        durationMs: Long = 3200L,
        isPersistent: Boolean = false
    ) {
        val toast = MaximusToastData(
            id = System.currentTimeMillis(),
            title = title,
            message = message,
            type = type,
            durationMs = durationMs,
            isPersistent = isPersistent
        )
        currentToast = toast
    }

    fun dismiss() {
        currentToast = null
    }
}

val LocalMaximusToast = staticCompositionLocalOf { MaximusToastManager() }

/**
 * Unified Physical Drop-In Header Host.
 * Replaces floating overlays with a solid bottom-rounded header that physically
 * drops down from the top notch using spring physics, pushing the active screen content down.
 * Supports touch swipe-up dismiss, ✕ close button, and in-place content morphing.
 */
@Composable
fun MaximusToastHost(
    toastManager: MaximusToastManager,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val currentToast = toastManager.currentToast

    var activeToast by remember { mutableStateOf<MaximusToastData?>(null) }

    LaunchedEffect(currentToast?.id) {
        if (currentToast != null) {
            activeToast = currentToast
            when (currentToast.type) {
                ToastType.ERROR, ToastType.SUCCESS -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                else -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            if (!currentToast.isPersistent) {
                delay(currentToast.durationMs)
                if (toastManager.currentToast?.id == currentToast.id) {
                    toastManager.dismiss()
                }
            }
        }
    }

    val isVisible = currentToast != null

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)),
        modifier = modifier.fillMaxWidth()
    ) {
        val toastToRender = currentToast ?: activeToast

        if (toastToRender != null) {
            val (accentColor, icon, defaultTitle) = when (toastToRender.type) {
                ToastType.SUCCESS -> Triple(currentAccent, Icons.Default.CheckCircle, "Success")
                ToastType.INFO -> Triple(Color(0xFF2196F3), Icons.Outlined.Info, "Notice")
                ToastType.WARNING -> Triple(Color(0xFFFF9800), Icons.Outlined.WarningAmber, "Warning")
                ToastType.ERROR -> Triple(MaterialTheme.colorScheme.error, Icons.Outlined.ErrorOutline, "Error")
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 2.dp,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -12f) {
                                toastManager.dismiss()
                            }
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    AnimatedContent(
                        targetState = toastToRender,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.95f, animationSpec = tween(160))) togetherWith
                                    (fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing)) +
                                            scaleOut(targetScale = 0.95f, animationSpec = tween(120)))
                        },
                        label = "ToastContentMorph"
                    ) { toast ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Circular Semantic Icon Badge
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = toast.type.name,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // 2. Title + Message text block
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = toast.title ?: defaultTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = toast.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 3. Compact Dismiss Action Button
                            IconButton(
                                onClick = { toastManager.dismiss() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
