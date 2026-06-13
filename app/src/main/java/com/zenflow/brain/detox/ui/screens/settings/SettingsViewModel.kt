package com.zenflow.brain.detox.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenflow.brain.detox.di.AppContainer
import com.zenflow.brain.detox.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val strictModeEnabled: Boolean = false,
    val notifyAt50Percent: Boolean = true,
    val notifyAt90Percent: Boolean = true,
    val hasUsagePermission: Boolean = false,
)

class SettingsViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val settingsRepository = container.settingsRepository
    private val context = container.context

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        strictModeEnabled = settings.strictModeEnabled,
                        notifyAt50Percent = settings.notifyAt50Percent,
                        notifyAt90Percent = settings.notifyAt90Percent,
                        hasUsagePermission = PermissionHelper.hasUsageStatsPermission(context),
                    )
                }
            }
        }
    }

    fun setStrictMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setStrictMode(enabled) }
    }

    fun setNotify50(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.getSettings()
            settingsRepository.updateSettings(current.copy(notifyAt50Percent = enabled))
        }
    }

    fun setNotify90(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.getSettings()
            settingsRepository.updateSettings(current.copy(notifyAt90Percent = enabled))
        }
    }
}
