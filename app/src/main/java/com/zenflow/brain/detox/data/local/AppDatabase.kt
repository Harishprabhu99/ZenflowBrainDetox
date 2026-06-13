package com.zenflow.brain.detox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zenflow.brain.detox.data.local.dao.BlockedAppDao
import com.zenflow.brain.detox.data.local.dao.SettingsDao
import com.zenflow.brain.detox.data.local.dao.UsageLogDao
import com.zenflow.brain.detox.data.local.entity.BlockedAppEntity
import com.zenflow.brain.detox.data.local.entity.SettingsEntity
import com.zenflow.brain.detox.data.local.entity.UsageLogEntity

@Database(
    entities = [
        BlockedAppEntity::class,
        UsageLogEntity::class,
        SettingsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun usageLogDao(): UsageLogDao
    abstract fun settingsDao(): SettingsDao
}
