package com.zenflow.brain.detox.data.repository

import com.google.gson.Gson
import com.zenflow.brain.detox.data.local.dao.BlockedAppDao
import com.zenflow.brain.detox.data.local.dao.SettingsDao
import com.zenflow.brain.detox.data.local.dao.UsageLogDao
import com.zenflow.brain.detox.data.model.BackupData
import com.zenflow.brain.detox.util.GoogleDriveHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupRepository(
    private val settingsDao: SettingsDao,
    private val blockedAppDao: BlockedAppDao,
    private val usageLogDao: UsageLogDao,
    private val googleDriveHelper: GoogleDriveHelper
) {
    private val gson = Gson()

    suspend fun createBackup(): String = withContext(Dispatchers.IO) {
        val data = BackupData(
            settings = settingsDao.get(),
            blockedApps = blockedAppDao.getAll(),
            usageLogs = usageLogDao.getAll()
        )
        gson.toJson(data)
    }

    suspend fun restoreBackup(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = gson.fromJson(json, BackupData::class.java)
            data.settings?.let { settingsDao.upsert(it) }
            blockedAppDao.upsertAll(data.blockedApps)
            // Usage logs could be many, maybe we want to merge instead of replace? 
            // For now, let's just upsert all as they have unique keys.
            data.usageLogs.forEach { usageLogDao.upsert(it) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun uploadToCloud(): Boolean {
        val json = createBackup()
        return googleDriveHelper.uploadBackup(json)
    }

    suspend fun downloadFromCloud(): Boolean {
        val json = googleDriveHelper.downloadBackup()
        return if (json != null) {
            restoreBackup(json)
        } else {
            false
        }
    }

    fun getGoogleDriveHelper() = googleDriveHelper
}
