package com.zenflow.brain.detox.data.repository

import com.zenflow.brain.detox.data.local.dao.BlockedAppDao
import com.zenflow.brain.detox.data.local.dao.UsageLogDao
import com.zenflow.brain.detox.data.local.entity.BlockedAppEntity
import com.zenflow.brain.detox.data.local.entity.UsageLogEntity
import com.zenflow.brain.detox.data.mapper.toDomain
import com.zenflow.brain.detox.data.mapper.toEntity
import com.zenflow.brain.detox.domain.model.BlockedApp
import com.zenflow.brain.detox.domain.model.SocialMediaCatalog
import com.zenflow.brain.detox.util.DateKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BlockedAppRepository(
    private val blockedAppDao: BlockedAppDao,
    private val usageLogDao: UsageLogDao,
) {
    fun observeBlockedApps(): Flow<List<BlockedApp>> {
        val dateKey = DateKeys.today()
        return combine(
            blockedAppDao.observeAll(),
            usageLogDao.observeForDate(dateKey),
        ) { apps, logs ->
            val logMap = logs.associateBy { it.packageName }
            apps.map { entity ->
                val log = logMap[entity.packageName]
                entity.toDomain(
                    usedTodayMs = log?.usedMs ?: 0L,
                    openCountToday = log?.openCount ?: 0,
                )
            }
        }
    }

    fun observeEnabledApps(): Flow<List<BlockedApp>> {
        val dateKey = DateKeys.today()
        return combine(
            blockedAppDao.observeEnabled(),
            usageLogDao.observeForDate(dateKey),
        ) { apps, logs ->
            val logMap = logs.associateBy { it.packageName }
            apps.map { entity ->
                val log = logMap[entity.packageName]
                entity.toDomain(
                    usedTodayMs = log?.usedMs ?: 0L,
                    openCountToday = log?.openCount ?: 0,
                )
            }
        }
    }

    suspend fun ensureDefaultsSeeded() {
        val existing = blockedAppDao.getAll()
        if (existing.isNotEmpty()) return

        val defaults = SocialMediaCatalog.defaultApps.map {
            BlockedAppEntity(
                packageName = it.packageName,
                displayName = it.displayName,
                dailyLimitMinutes = 30,
                isEnabled = false,
            )
        }
        blockedAppDao.upsertAll(defaults)
    }

    suspend fun toggleApp(packageName: String, enabled: Boolean) {
        val existing = blockedAppDao.getByPackage(packageName)
        if (existing != null) {
            blockedAppDao.upsert(existing.copy(isEnabled = enabled))
        } else {
            // If it doesn't exist in DB, look it up in catalog and create it
            val catalogApp = SocialMediaCatalog.defaultApps.find { it.packageName == packageName }
            if (catalogApp != null) {
                blockedAppDao.upsert(
                    BlockedAppEntity(
                        packageName = catalogApp.packageName,
                        displayName = catalogApp.displayName,
                        dailyLimitMinutes = 30,
                        isEnabled = enabled
                    )
                )
            }
        }
    }

    suspend fun updateLimit(packageName: String, minutes: Int) {
        blockedAppDao.getByPackage(packageName)?.let {
            blockedAppDao.upsert(it.copy(dailyLimitMinutes = minutes))
        }
    }

    suspend fun updateUsage(packageName: String, usedMs: Long, openCount: Int) {
        val dateKey = DateKeys.today()
        val existing = usageLogDao.getForPackageAndDate(packageName, dateKey)
        usageLogDao.upsert(
            UsageLogEntity(
                id = existing?.id ?: 0,
                packageName = packageName,
                dateKey = dateKey,
                usedMs = usedMs,
                openCount = openCount,
            ),
        )
    }

    suspend fun getApp(packageName: String): BlockedApp? {
        val entity = blockedAppDao.getByPackage(packageName) ?: return null
        val log = usageLogDao.getForPackageAndDate(packageName, DateKeys.today())
        return entity.toDomain(log?.usedMs ?: 0L, log?.openCount ?: 0)
    }

    suspend fun saveApp(app: BlockedApp) {
        blockedAppDao.upsert(app.toEntity())
    }
}
