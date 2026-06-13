package com.zenflow.brain.detox.di

import android.content.Context
import androidx.room.Room
import com.zenflow.brain.detox.data.local.AppDatabase
import com.zenflow.brain.detox.data.repository.BackupRepository
import com.zenflow.brain.detox.data.repository.BlockedAppRepository
import com.zenflow.brain.detox.data.repository.SettingsRepository
import com.zenflow.brain.detox.data.repository.UsageRepository
import com.zenflow.brain.detox.util.GoogleDriveHelper
import com.zenflow.brain.detox.util.UsageStatsHelper

class AppContainer(context: Context) {
    val context: Context = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "brain_detox.db").build()
    }

    val usageStatsHelper: UsageStatsHelper by lazy { UsageStatsHelper(context) }

    val blockedAppRepository: BlockedAppRepository by lazy {
        BlockedAppRepository(database.blockedAppDao(), database.usageLogDao())
    }

    val usageRepository: UsageRepository by lazy {
        UsageRepository(database.usageLogDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    val googleDriveHelper: GoogleDriveHelper by lazy {
        GoogleDriveHelper(context)
    }

    val backupRepository: BackupRepository by lazy {
        BackupRepository(
            database.settingsDao(),
            database.blockedAppDao(),
            database.usageLogDao(),
            googleDriveHelper
        )
    }
}
