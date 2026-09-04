package com.minimize.maximus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.minimize.maximus.ui.theme.MaximusShapes
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils

/**
 * FitSync Centralized Confirmation & Alert Dialog
 * Standardizes design, typography, spacing, and button pill symmetry across the entire app.
 */
@Composable
fun MaximusConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: ImageVector? = null,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false)
) {
    val haptic = LocalHapticFeedback.current
    val currentAccent = LocalAccentColor.current
    val actionColor = if (isDestructive) MaterialTheme.colorScheme.error else currentAccent

    Dialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Icon
                val displayIcon = icon ?: if (isDestructive) Icons.Default.WarningAmber else null
                if (displayIcon != null) {
                    Surface(
                        shape = MaximusShapes.Pill,
                        color = actionColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = displayIcon,
                                contentDescription = null,
                                tint = actionColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Title & Message Content
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Full Width Stadium Pill Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, if (isDestructive) MaximusEvent.ERROR_ALERT else MaximusEvent.ACTION_TAP)
                            onConfirm()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = MaximusShapes.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionColor,
                            contentColor = if (isDestructive) Color.White else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            MaximusHapticUtils.perform(haptic, MaximusEvent.ACTION_TAP)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = MaximusShapes.Pill
                    ) {
                        Text(
                            text = dismissText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
