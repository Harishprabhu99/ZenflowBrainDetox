package com.zenflow.brain.detox.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    val snackbarHostState = remember { SnackbarHostState() }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshGoogleAccount()
    }

    LaunchedEffect(uiState.backupMessage) {
        uiState.backupMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearBackupMessage()
        }
    }

    Column {
        LazyColumn(
            modifier = Modifier.weight(1f),
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
                Text(
                    "Backup & Restore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )

                if (!uiState.isGoogleSignedIn) {
                    Button(
                        onClick = { signInLauncher.launch(viewModel.getGoogleSignInIntent()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect Google Drive")
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "Connected as: ${uiState.googleAccountName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = viewModel::uploadBackup,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isBackupLoading
                            ) {
                                if (uiState.isBackupLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.CloudUpload, null)
                                    Spacer(Modifier.size(8.dp))
                                    Text("Backup")
                                }
                            }
                            Button(
                                onClick = viewModel::downloadBackup,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isBackupLoading
                            ) {
                                if (uiState.isBackupLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.CloudDownload, null)
                                    Spacer(Modifier.size(8.dp))
                                    Text("Restore")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.signOutGoogle() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                "Disconnect",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
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
        SnackbarHost(hostState = snackbarHostState)
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
