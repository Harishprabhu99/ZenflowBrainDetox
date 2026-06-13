package com.zenflow.brain.detox.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

class UsageStatsHelper(private val context: Context) {

    private val usageStatsManager: UsageStatsManager
        get() = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getForegroundAppPackage(): String? {
        val end = System.currentTimeMillis()
        val start = end - TimeUnit.SECONDS.toMillis(10)
        val events = usageStatsManager.queryEvents(start, end)
        var lastPackage: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }

    fun getUsageForPackageToday(packageName: String): Long {
        val end = System.currentTimeMillis()
        val start = startOfDayMs()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end,
        ) ?: return 0L
        return stats.firstOrNull { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }

    private fun startOfDayMs(): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
