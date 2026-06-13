package com.zenflow.brain.detox.domain.model

data class BlockedApp(
    val packageName: String,
    val displayName: String,
    val dailyLimitMinutes: Int,
    val isEnabled: Boolean,
    val usedTodayMs: Long = 0L,
    val openCountToday: Int = 0,
) {
    val remainingMs: Long
        get() = (dailyLimitMinutes * 60_000L - usedTodayMs).coerceAtLeast(0L)

    val isLimitExceeded: Boolean
        get() = usedTodayMs >= dailyLimitMinutes * 60_000L

    val usagePercent: Float
        get() = if (dailyLimitMinutes <= 0) 0f
        else (usedTodayMs.toFloat() / (dailyLimitMinutes * 60_000f)).coerceIn(0f, 1f)
}
