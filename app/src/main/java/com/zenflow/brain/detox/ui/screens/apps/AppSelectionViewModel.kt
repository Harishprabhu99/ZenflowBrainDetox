package com.zenflow.brain.detox.ui.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenflow.brain.detox.di.AppContainer
import com.zenflow.brain.detox.domain.model.BlockedApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppSelectionUiState(
    val apps: List<BlockedApp> = emptyList(),
    val searchQuery: String = "",
) {
    val filteredApps: List<BlockedApp>
        get() = if (searchQuery.isBlank()) apps
        else apps.filter { it.displayName.contains(searchQuery, ignoreCase = true) }
}

class AppSelectionViewModel(
    container: AppContainer,
) : ViewModel() {

    private val blockedAppRepository = container.blockedAppRepository

    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            blockedAppRepository.ensureDefaultsSeeded()
            blockedAppRepository.observeBlockedApps().collect { apps ->
                _uiState.update { it.copy(apps = apps) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            blockedAppRepository.toggleApp(packageName, enabled)
        }
    }
}
