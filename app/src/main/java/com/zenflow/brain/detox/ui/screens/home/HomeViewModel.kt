package com.zenflow.brain.detox.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenflow.brain.detox.di.AppContainer
import com.zenflow.brain.detox.domain.model.BlockedApp
import com.zenflow.brain.detox.service.MonitoringService
import com.zenflow.brain.detox.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val enabledApps: List<BlockedApp> = emptyList(),
    val totalUsedMs: Long = 0L,
    val timeSavedMs: Long = 0L,
    val dailyGoalPercent: Float = 0f,
    val isMonitoring: Boolean = false,
    val hasUsagePermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
)

class HomeViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val blockedAppRepository = container.blockedAppRepository
    private val settingsRepository = container.settingsRepository
    private val usageRepository = container.usageRepository
    private val context = container.context

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            blockedAppRepository.ensureDefaultsSeeded()
            settingsRepository.ensureDefaults()
        }
        viewModelScope.launch {
            combine(
                blockedAppRepository.observeBlockedApps(),
                settingsRepository.observeSettings(),
            ) { apps, settings ->
                val enabled = apps.filter { it.isEnabled }
                val totalUsed = enabled.sumOf { it.usedTodayMs }
                val totalLimit = enabled.sumOf { it.dailyLimitMinutes * 60_000L }
                val goalPercent = if (totalLimit > 0) totalUsed.toFloat() / totalLimit else 0f
                _uiState.update {
                    it.copy(
                        enabledApps = enabled,
                        totalUsedMs = totalUsed,
                        dailyGoalPercent = goalPercent,
                        isMonitoring = settings.monitoringEnabled,
                        hasUsagePermission = PermissionHelper.hasUsageStatsPermission(context),
                        hasOverlayPermission = PermissionHelper.hasOverlayPermission(context),
                    )
                }
            }.collect {}
        }
        refreshSavedTime()
    }

    fun toggleMonitoring() {
        viewModelScope.launch {
            val current = settingsRepository.getSettings()
            val newEnabled = !current.monitoringEnabled
            settingsRepository.setMonitoringEnabled(newEnabled)
            if (newEnabled) {
                MonitoringService.start(context)
            } else {
                MonitoringService.stop(context)
            }
            _uiState.update { it.copy(isMonitoring = newEnabled) }
        }
    }

    private fun refreshSavedTime() {
        viewModelScope.launch {
            val saved = usageRepository.getTotalSavedMs()
            _uiState.update { it.copy(timeSavedMs = saved) }
        }
    }
}
