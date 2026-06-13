package com.zenflow.brain.detox.data.model

import com.zenflow.brain.detox.data.local.entity.BlockedAppEntity
import com.zenflow.brain.detox.data.local.entity.SettingsEntity
import com.zenflow.brain.detox.data.local.entity.UsageLogEntity

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val settings: SettingsEntity?,
    val blockedApps: List<BlockedAppEntity>,
    val usageLogs: List<UsageLogEntity>
)
