package com.zenflow.brain.detox.ui.screens.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenflow.brain.detox.di.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenflow.brain.detox.domain.model.SocialMediaCatalog
import com.zenflow.brain.detox.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimerSettingsScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: TimerSettingsViewModel = appViewModel { TimerSettingsViewModel(it, packageName) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var customMinutes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.appName.ifBlank { "Timer Settings" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                "Daily Time Limit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SocialMediaCatalog.presetTimerMinutes.forEach { minutes ->
                    FilterChip(
                        selected = uiState.selectedMinutes == minutes,
                        onClick = { viewModel.setLimit(minutes) },
                        label = { Text(TimeFormatter.formatMinutes(minutes)) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Custom (minutes)")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customMinutes,
                onValueChange = { customMinutes = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 45") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    customMinutes.toIntOrNull()?.let { viewModel.setLimit(it) }
                },
                enabled = customMinutes.toIntOrNull()?.let { it > 0 } == true,
            ) {
                Text("Apply Custom Limit")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Current limit: ${TimeFormatter.formatMinutes(uiState.selectedMinutes)}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "Resets daily at midnight",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
