package com.minimize.maximus.util.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.minimize.maximus.MainActivity
import com.minimize.maximus.R
import com.minimize.maximus.data.local.PreferenceManager
import com.minimize.maximus.data.remote.NotificationConfig
import com.minimize.maximus.data.remote.RemoteConfigManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.serialization.json.Json

private val notifJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

class WorkoutReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "maximus_workout_reminders"
        const val CHANNEL_NAME = "Daily Workout Reminders"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        val prefs = PreferenceManager(context)
        if (!prefs.isNotificationsEnabled()) {
            return Result.success()
        }

        createNotificationChannel()

        val name = prefs.getUserName().ifBlank { "Athlete" }
        val goal = prefs.getUserGoal().ifBlank { "Muscle & Strength" }

        // Read dynamic notification configuration with instant fallback
        val notifConfig: NotificationConfig = try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val rawJson = remoteConfig.getString(RemoteConfigManager.KEY_NOTIFICATION_CONFIG)
            if (rawJson.isNotBlank()) {
                notifJson.decodeFromString<NotificationConfig>(rawJson)
            } else {
                NotificationConfig()
            }
        } catch (_: Exception) {
            NotificationConfig()
        }

        // Daily rotating variation for fresh engagement
        val dayIndex = java.time.LocalDate.now().dayOfWeek.value - 1
        val rawTitle = if (notifConfig.title_variations.isNotEmpty()) {
            notifConfig.title_variations[dayIndex % notifConfig.title_variations.size]
        } else {
            notifConfig.title_template
        }
        val rawBody = if (notifConfig.body_variations.isNotEmpty()) {
            notifConfig.body_variations[dayIndex % notifConfig.body_variations.size]
        } else {
            notifConfig.body_template
        }

        val formattedTitle = rawTitle.replace("{name}", name).replace("{goal}", goal)
        val formattedBody = rawBody.replace("{name}", name).replace("{goal}", goal)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(formattedTitle)
            .setContentText(formattedBody)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(formattedBody)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS runtime permission fallback
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to keep your workout streak and fitness goals on track."
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
