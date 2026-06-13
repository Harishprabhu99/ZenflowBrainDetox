package com.zenflow.brain.detox.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenflow.brain.detox.data.repository.DailyUsageSummary
import com.zenflow.brain.detox.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val weeklySummary: List<DailyUsageSummary> = emptyList(),
    val timeSavedMs: Long = 0L,
    val streakDays: Int = 0,
)

class DashboardViewModel(
    container: AppContainer,
) : ViewModel() {

    private val usageRepository = container.usageRepository
    private val settingsRepository = container.settingsRepository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                _uiState.update { it.copy(streakDays = settings.streakDays) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val weekly = usageRepository.getWeeklySummary()
            val saved = usageRepository.getTotalSavedMs()
            _uiState.update {
                it.copy(weeklySummary = weekly, timeSavedMs = saved)
            }
        }
    }
}
