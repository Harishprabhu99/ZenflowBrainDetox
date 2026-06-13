package com.zenflow.brain.detox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenflow.brain.detox.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(container: AppContainer) : ViewModel() {
    private val userRepository = container.userRepository
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val authenticated = userRepository.isAuthenticated()
            _startDestination.value = if (authenticated) "home" else "auth"
            _isReady.value = true
        }
    }
}
