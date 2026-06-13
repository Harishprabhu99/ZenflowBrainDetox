package com.zenflow.brain.detox.data.mapper

import com.zenflow.brain.detox.data.local.entity.BlockedAppEntity
import com.zenflow.brain.detox.data.local.entity.SettingsEntity
import com.zenflow.brain.detox.domain.model.AppSettings
import com.zenflow.brain.detox.domain.model.BlockedApp
import com.zenflow.brain.detox.domain.model.ResetSchedule

fun BlockedAppEntity.toDomain(usedTodayMs: Long = 0L, openCountToday: Int = 0) = BlockedApp(
    packageName = packageName,
    displayName = displayName,
    dailyLimitMinutes = dailyLimitMinutes,
    isEnabled = isEnabled,
    usedTodayMs = usedTodayMs,
    openCountToday = openCountToday,
)

fun BlockedApp.toEntity() = BlockedAppEntity(
    packageName = packageName,
    displayName = displayName,
    dailyLimitMinutes = dailyLimitMinutes,
    isEnabled = isEnabled,
)

fun SettingsEntity.toDomain() = AppSettings(
    strictModeEnabled = strictModeEnabled,
    strictModeCooldownMs = strictModeCooldownMs,
    lastStrictOverrideMs = lastStrictOverrideMs,
    monitoringEnabled = monitoringEnabled,
    resetSchedule = runCatching { ResetSchedule.valueOf(resetSchedule) }
        .getOrDefault(ResetSchedule.DAILY_MIDNIGHT),
    notifyAt50Percent = notifyAt50Percent,
    notifyAt90Percent = notifyAt90Percent,
    weeklyTargetMinutes = weeklyTargetMinutes,
    streakDays = streakDays,
    lastStreakDate = lastStreakDate,
)

fun AppSettings.toEntity() = SettingsEntity(
    strictModeEnabled = strictModeEnabled,
    strictModeCooldownMs = strictModeCooldownMs,
    lastStrictOverrideMs = lastStrictOverrideMs,
    monitoringEnabled = monitoringEnabled,
    resetSchedule = resetSchedule.name,
    notifyAt50Percent = notifyAt50Percent,
    notifyAt90Percent = notifyAt90Percent,
    weeklyTargetMinutes = weeklyTargetMinutes,
    streakDays = streakDays,
    lastStreakDate = lastStreakDate,
)
