package com.zenflow.brain.detox.domain.model

enum class ResetSchedule {
    DAILY_MIDNIGHT,
    WEEKDAYS,
    CUSTOM,
}

data class AppSettings(
    val strictModeEnabled: Boolean = false,
    val strictModeCooldownMs: Long = 24 * 60 * 60 * 1000L,
    val lastStrictOverrideMs: Long = 0L,
    val monitoringEnabled: Boolean = false,
    val resetSchedule: ResetSchedule = ResetSchedule.DAILY_MIDNIGHT,
    val notifyAt50Percent: Boolean = true,
    val notifyAt90Percent: Boolean = true,
    val weeklyTargetMinutes: Int = 420,
    val streakDays: Int = 0,
    val lastStreakDate: String = "",
)
