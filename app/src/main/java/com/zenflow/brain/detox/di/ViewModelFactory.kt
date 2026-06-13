package com.zenflow.brain.detox.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenflow.brain.detox.BrainDetoxApp

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    crossinline creator: (AppContainer) -> VM,
): VM {
    val container = (LocalContext.current.applicationContext as BrainDetoxApp).container
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = creator(container) as T
        },
    )
}
