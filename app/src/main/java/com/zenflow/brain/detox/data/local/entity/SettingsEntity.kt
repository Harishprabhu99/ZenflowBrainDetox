package com.zenflow.brain.detox.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val strictModeEnabled: Boolean,
    val strictModeCooldownMs: Long,
    val lastStrictOverrideMs: Long,
    val monitoringEnabled: Boolean,
    val resetSchedule: String,
    val notifyAt50Percent: Boolean,
    val notifyAt90Percent: Boolean,
    val weeklyTargetMinutes: Int,
    val streakDays: Int,
    val lastStreakDate: String,
)
