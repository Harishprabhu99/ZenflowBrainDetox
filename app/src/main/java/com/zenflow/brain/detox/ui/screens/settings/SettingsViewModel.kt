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
    val isGoogleSignedIn: Boolean = false,
    val googleAccountName: String? = null,
    val isBackupLoading: Boolean = false,
    val backupMessage: String? = null,
)

class SettingsViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val settingsRepository = container.settingsRepository
    private val backupRepository = container.backupRepository
    private val userRepository = container.userRepository
    private val context = container.context

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshGoogleAccount()
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

    fun refreshGoogleAccount() {
        val account = backupRepository.getGoogleDriveHelper().getSignedInAccount()
        _uiState.update {
            it.copy(
                isGoogleSignedIn = account != null,
                googleAccountName = account?.email
            )
        }
    }

    fun getGoogleSignInIntent() = backupRepository.getGoogleDriveHelper().getSignInIntent()

    fun uploadBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupLoading = true, backupMessage = "Uploading...") }
            val account = backupRepository.getGoogleDriveHelper().getSignedInAccount()
            if (account == null) {
                _uiState.update {
                    it.copy(
                        isBackupLoading = false,
                        backupMessage = "Not signed in to Google",
                        isGoogleSignedIn = false
                    )
                }
                return@launch
            }
            val success = backupRepository.uploadToCloud()
            _uiState.update {
                it.copy(
                    isBackupLoading = false,
                    backupMessage = if (success) "Backup uploaded successfully" else "Failed to upload backup. Check permissions."
                )
            }
        }
    }

    fun downloadBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupLoading = true, backupMessage = "Downloading...") }
            val success = backupRepository.downloadFromCloud()
            _uiState.update {
                it.copy(
                    isBackupLoading = false,
                    backupMessage = if (success) "Backup restored successfully" else "Failed to restore backup"
                )
            }
        }
    }

    fun signOutGoogle() {
        backupRepository.getGoogleDriveHelper().signOut()
        refreshGoogleAccount()
    }

    fun clearBackupMessage() {
        _uiState.update { it.copy(backupMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
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
