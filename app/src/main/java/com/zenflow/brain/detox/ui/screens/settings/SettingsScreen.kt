package com.zenflow.brain.detox.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenflow.brain.detox.di.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenflow.brain.detox.util.PermissionHelper

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = appViewModel { SettingsViewModel(it) }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            SettingsToggle(
                title = "Strict Mode",
                subtitle = "24-hour cooldown before override is allowed",
                checked = uiState.strictModeEnabled,
                onCheckedChange = viewModel::setStrictMode,
            )
        }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            SettingsToggle(
                title = "50% Usage Warning",
                subtitle = "Notify when half your daily limit is used",
                checked = uiState.notifyAt50Percent,
                onCheckedChange = viewModel::setNotify50,
            )
        }
        item {
            SettingsToggle(
                title = "90% Usage Warning",
                subtitle = "Notify when 90% of your daily limit is used",
                checked = uiState.notifyAt90Percent,
                onCheckedChange = viewModel::setNotify90,
            )
        }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            SettingsToggle(
                title = "Usage Access Granted",
                subtitle = if (uiState.hasUsagePermission) "Permission enabled" else "Tap to open settings",
                checked = uiState.hasUsagePermission,
                onCheckedChange = {
                    context.startActivity(PermissionHelper.usageStatsSettingsIntent())
                },
            )
        }
        item {
            Text(
                "Battery Optimization",
                modifier = Modifier.padding(top = 16.dp),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Disable battery optimization for reliable monitoring on Xiaomi, Samsung, and OnePlus devices.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        item {
            Text(
                "Privacy",
                modifier = Modifier.padding(top = 16.dp),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "All usage data is stored locally on your device. No personal data is collected.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
