package com.zenflow.brain.detox.ui.screens.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenflow.brain.detox.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimerSettingsUiState(
    val appName: String = "",
    val selectedMinutes: Int = 30,
)

class TimerSettingsViewModel(
    container: AppContainer,
    private val packageName: String,
) : ViewModel() {

    private val blockedAppRepository = container.blockedAppRepository

    private val _uiState = MutableStateFlow(TimerSettingsUiState())
    val uiState: StateFlow<TimerSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            blockedAppRepository.getApp(packageName)?.let { app ->
                _uiState.update {
                    it.copy(appName = app.displayName, selectedMinutes = app.dailyLimitMinutes)
                }
            }
        }
    }

    fun setLimit(minutes: Int) {
        viewModelScope.launch {
            blockedAppRepository.updateLimit(packageName, minutes)
            _uiState.update { it.copy(selectedMinutes = minutes) }
        }
    }
}
