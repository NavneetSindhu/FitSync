package com.minimize.maximus.util.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.minimize.maximus.data.local.PreferenceManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderSchedulerUtil {

    private const val WORK_NAME = "maximus_daily_workout_reminder"

    /**
     * Schedules a repeating 24-hour reminder at the targeted hour and minute.
     */
    fun scheduleDailyReminder(
        context: Context,
        hourOfDay: Int? = null,
        minute: Int? = null
    ) {
        val prefs = PreferenceManager(context)
        if (!prefs.isNotificationsEnabled()) {
            cancelDailyReminder(context)
            return
        }

        val targetHour = hourOfDay ?: prefs.getReminderHour()
        val targetMinute = minute ?: prefs.getReminderMinute()

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If targeted time has already passed today, target tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelayMillis = target.timeInMillis - now.timeInMillis

        val reminderWorkRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }

    /**
     * Cancels any scheduled recurring reminder worker.
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
