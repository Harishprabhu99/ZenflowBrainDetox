package com.zenflow.brain.detox.data.repository

import com.zenflow.brain.detox.data.local.dao.SettingsDao
import com.zenflow.brain.detox.data.mapper.toDomain
import com.zenflow.brain.detox.data.mapper.toEntity
import com.zenflow.brain.detox.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDao: SettingsDao,
) {
    fun observeSettings(): Flow<AppSettings> =
        settingsDao.observe().map { it?.toDomain() ?: AppSettings() }

    suspend fun getSettings(): AppSettings =
        settingsDao.get()?.toDomain() ?: AppSettings()

    suspend fun updateSettings(settings: AppSettings) {
        settingsDao.upsert(settings.toEntity())
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        updateSettings(getSettings().copy(monitoringEnabled = enabled))
    }

    suspend fun setStrictMode(enabled: Boolean) {
        updateSettings(getSettings().copy(strictModeEnabled = enabled))
    }

    suspend fun recordStrictOverride() {
        val current = getSettings()
        updateSettings(current.copy(lastStrictOverrideMs = System.currentTimeMillis()))
    }

    suspend fun canOverrideStrictMode(): Boolean {
        val settings = getSettings()
        if (!settings.strictModeEnabled) return true
        val elapsed = System.currentTimeMillis() - settings.lastStrictOverrideMs
        return settings.lastStrictOverrideMs == 0L || elapsed >= settings.strictModeCooldownMs
    }

    suspend fun ensureDefaults() {
        if (settingsDao.get() == null) {
            settingsDao.upsert(AppSettings().toEntity())
        }
    }
}
