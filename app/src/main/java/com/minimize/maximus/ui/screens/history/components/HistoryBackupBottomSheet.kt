package com.minimize.maximus.ui.screens.history.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.R
import com.minimize.maximus.domain.model.WorkoutSession
import com.minimize.maximus.ui.components.MaximusModalBottomSheet
import com.minimize.maximus.ui.components.MaximusSheetHeader
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.components.ToastType
import com.minimize.maximus.ui.theme.LocalAccentColor
import com.minimize.maximus.util.BackupUtils
import com.minimize.maximus.util.MaximusEvent
import com.minimize.maximus.util.MaximusHapticUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBackupBottomSheet(
    workouts: List<WorkoutSession>,
    onRestoreWorkouts: (List<WorkoutSession>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val toastManager = LocalMaximusToast.current
    val hapticFeedback = LocalHapticFeedback.current
    val currentAccent = LocalAccentColor.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }

    // Native JSON File Picker for seamless 1-click restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                try {
                    val restoredWorkouts = BackupUtils.importFromJsonUri(context, selectedUri)
                    if (restoredWorkouts.isNotEmpty()) {
                        onRestoreWorkouts(restoredWorkouts)
                        MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.PR_RECORD_UNLOCKED)
                        toastManager.showToast(context.getString(R.string.backup_restored_toast, restoredWorkouts.size), ToastType.SUCCESS)
                        onDismissRequest()
                    } else {
                        toastManager.showToast(context.getString(R.string.backup_no_valid_json), ToastType.ERROR)
                    }
                } catch (e: Exception) {
                    toastManager.showToast("Import failed: ${e.localizedMessage}", ToastType.ERROR)
                }
            }
        }
    }

    MaximusModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        MaximusSheetHeader(
            title = stringResource(R.string.backup_title),
            subtitle = "Export actual files or restore database from backup"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Action 1: Export CSV Spreadsheet File ──
            ExportActionRow(
                icon = Icons.Default.TableChart,
                iconColor = Color(0xFF10B981),
                title = "Export CSV Spreadsheet",
                subtitle = "Share as .csv file for Excel, Google Sheets, & Numbers",
                onClick = {
                    coroutineScope.launch {
                        try {
                            isProcessing = true
                            val fileUri = BackupUtils.exportCsvFile(context, workouts)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                putExtra(Intent.EXTRA_SUBJECT, "Maximus Workout History (CSV)")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share CSV File"))
                        } catch (e: Exception) {
                            toastManager.showToast("Export failed: ${e.localizedMessage}", ToastType.ERROR)
                        } finally {
                            isProcessing = false
                        }
                    }
                }
            )

            // ── Action 2: Export PDF Workout Logbook Report ──
            ExportActionRow(
                icon = Icons.Default.PictureAsPdf,
                iconColor = Color(0xFFEF4444),
                title = "Export PDF Report",
                subtitle = "Formatted workout logbook for personal trainers & coaches",
                onClick = {
                    coroutineScope.launch {
                        try {
                            isProcessing = true
                            val fileUri = BackupUtils.exportPdfReport(context, workouts)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                putExtra(Intent.EXTRA_SUBJECT, "Maximus Workout Report (PDF)")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Report"))
                        } catch (e: Exception) {
                            toastManager.showToast("PDF generation failed: ${e.localizedMessage}", ToastType.ERROR)
                        } finally {
                            isProcessing = false
                        }
                    }
                }
            )

            // ── Action 3: Export Full JSON Database Backup ──
            ExportActionRow(
                icon = Icons.Default.FolderZip,
                iconColor = currentAccent,
                title = "Export JSON Backup File",
                subtitle = "Complete database backup file for device migration",
                onClick = {
                    coroutineScope.launch {
                        try {
                            isProcessing = true
                            val fileUri = BackupUtils.exportJsonFile(context, workouts)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                putExtra(Intent.EXTRA_SUBJECT, "Maximus Database Backup (JSON)")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share JSON Backup File"))
                        } catch (e: Exception) {
                            toastManager.showToast("Export failed: ${e.localizedMessage}", ToastType.ERROR)
                        } finally {
                            isProcessing = false
                        }
                    }
                }
            )

            // ── Action 4: Restore from Backup File ──
            ExportActionRow(
                icon = Icons.Default.Restore,
                iconColor = Color(0xFFF59E0B),
                title = "Restore from JSON File",
                subtitle = "Select a .json backup file from device storage",
                onClick = {
                    MaximusHapticUtils.perform(hapticFeedback, MaximusEvent.SELECTION_TAP)
                    filePickerLauncher.launch("*/*")
                }
            )
        }
    }
}

@Composable
private fun ExportActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
        }
    }
}
