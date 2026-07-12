package com.zenflow.brain.detox.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenflow.brain.detox.data.remote.LoginRequest
import com.zenflow.brain.detox.data.remote.SignupRequest
import com.zenflow.brain.detox.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val isLoginMode: Boolean = true
)

class AuthViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val userRepository = container.userRepository
    private val api = container.api
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun toggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.login(LoginRequest(email, password))
                userRepository.saveAuthToken(response.token, email)
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            } catch (e: Exception) {
                val message = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            401 -> "Invalid email or password"
                            400 -> "Bad request – please check your input"
                            500 -> "Server error – try again later"
                            else -> "Unexpected error: ${e.code()}"
                        }
                    }
                    else -> e.localizedMessage ?: "Login failed"
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                api.signup(SignupRequest(email, password, email.split("@")[0]))
                // After signup, auto login
                login(email, password)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Signup failed") }
            }
        }
    }

    fun enterAsGuest() {
        viewModelScope.launch {
            userRepository.setGuestMode(true)
            _uiState.update { it.copy(isAuthenticated = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
