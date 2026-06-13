package com.zenflow.brain.detox.data.repository

import com.zenflow.brain.detox.data.local.dao.UsageLogDao
import com.zenflow.brain.detox.data.local.entity.UsageLogEntity
import com.zenflow.brain.detox.util.DateKeys
import kotlinx.coroutines.flow.Flow

data class DailyUsageSummary(
    val dateKey: String,
    val totalUsedMs: Long,
    val appBreakdown: Map<String, Long>,
)

class UsageRepository(
    private val usageLogDao: UsageLogDao,
) {
    fun observeTodayUsage(): Flow<List<UsageLogEntity>> =
        usageLogDao.observeForDate(DateKeys.today())

    suspend fun getWeeklySummary(): List<DailyUsageSummary> {
        val start = DateKeys.daysAgo(6)
        val end = DateKeys.today()
        val logs = usageLogDao.getBetweenDates(start, end)
        return logs.groupBy { it.dateKey }
            .map { (date, entries) ->
                DailyUsageSummary(
                    dateKey = date,
                    totalUsedMs = entries.sumOf { it.usedMs },
                    appBreakdown = entries.associate { it.packageName to it.usedMs },
                )
            }
            .sortedBy { it.dateKey }
    }

    suspend fun getTotalSavedMs(): Long {
        val logs = usageLogDao.getBetweenDates(DateKeys.daysAgo(6), DateKeys.today())
        val baselinePerDay = 2 * 60 * 60 * 1000L
        return logs.groupBy { it.dateKey }.values.sumOf { entries ->
            val used = entries.sumOf { it.usedMs }
            (baselinePerDay - used).coerceAtLeast(0L)
        }
    }
}
