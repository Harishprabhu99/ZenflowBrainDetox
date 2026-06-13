package com.zenflow.brain.detox.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateKeys {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(formatter)

    fun daysAgo(days: Long): String = LocalDate.now().minusDays(days).format(formatter)
}

object TimeFormatter {
    fun formatDurationMs(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%dh %02dm", hours, minutes)
        } else if (minutes > 0) {
            String.format("%dm %02ds", minutes, seconds)
        } else {
            String.format("%ds", seconds)
        }
    }

    fun formatMinutes(minutes: Int): String {
        return when {
            minutes >= 60 -> {
                val h = minutes / 60
                val m = minutes % 60
                if (m == 0) "${h}h" else "${h}h ${m}m"
            }
            else -> "${minutes}m"
        }
    }
}
