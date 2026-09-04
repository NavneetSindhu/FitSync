package com.minimize.maximus.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

object DateUtils {

    fun toLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun toEpochMilli(localDate: LocalDate): Long {
        return localDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun formatEpoch(epochMillis: Long, pattern: String = "EEE, MMM dd"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(epochMillis))
    }

    fun formatRelativeDate(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        val daysDiff = ChronoUnit.DAYS.between(date, today)
        return when (daysDiff) {
            0L -> "Today"
            1L -> "Yesterday"
            in 2..6 -> "$daysDiff days ago"
            else -> "${date.dayOfMonth} ${date.month.name.take(3)}"
        }
    }

    fun getRelativeDateHeader(epochMillis: Long): String {
        return formatRelativeDate(toLocalDate(epochMillis))
    }
}
