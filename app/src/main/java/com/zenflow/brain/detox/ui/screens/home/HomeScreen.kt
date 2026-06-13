package com.zenflow.brain.detox.ui.screens.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenflow.brain.detox.di.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenflow.brain.detox.domain.model.BlockedApp
import com.zenflow.brain.detox.ui.theme.BlockRed
import com.zenflow.brain.detox.ui.theme.PrimaryBlue
import com.zenflow.brain.detox.ui.theme.SuccessGreen
import com.zenflow.brain.detox.util.PermissionHelper
import com.zenflow.brain.detox.util.TimeFormatter

@Composable
fun HomeScreen(
    onNavigateToApps: () -> Unit,
    onNavigateToTimer: (String) -> Unit,
    viewModel: HomeViewModel = appViewModel { HomeViewModel(it) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh permissions whenever the app comes back to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Zenflow Brain Detox",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Take back control of your screen time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }

        item {
            StatsRow(
                totalUsedMs = uiState.totalUsedMs,
                timeSavedMs = uiState.timeSavedMs,
                goalPercent = uiState.dailyGoalPercent,
            )
        }

        item {
            if (!uiState.hasUsagePermission) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(PermissionHelper.usageStatsSettingsIntent())
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Grant Usage Access Permission")
                }
            }

            if (!uiState.hasOverlayPermission) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(PermissionHelper.overlaySettingsIntent(context))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Grant Overlay Permission")
                }
            }

            Button(
                onClick = { viewModel.toggleMonitoring() },
                enabled = uiState.hasUsagePermission && uiState.hasOverlayPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isMonitoring) BlockRed else PrimaryBlue,
                ),
            ) {
                Icon(
                    imageVector = if (uiState.isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(if (uiState.isMonitoring) "Stop Monitoring" else "Start Monitoring")
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Blocked Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedButton(onClick = onNavigateToApps) {
                    Text("Manage")
                }
            }
        }

        if (uiState.enabledApps.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("No apps selected yet")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Select social media apps to start limiting your usage",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onNavigateToApps) {
                            Text("Select Apps")
                        }
                    }
                }
            }
        } else {
            items(uiState.enabledApps, key = { it.packageName }) { app ->
                BlockedAppCard(
                    app = app,
                    onClick = { onNavigateToTimer(app.packageName) },
                )
            }
        }
    }
}

@Composable
private fun StatsRow(totalUsedMs: Long, timeSavedMs: Long, goalPercent: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = "Today's Usage",
            value = TimeFormatter.formatDurationMs(totalUsedMs),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Time Saved",
            value = TimeFormatter.formatDurationMs(timeSavedMs),
            modifier = Modifier.weight(1f),
            valueColor = SuccessGreen,
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Daily Goal")
                Text("${(goalPercent * 100).toInt()}%")
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { goalPercent.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun BlockedAppCard(app: BlockedApp, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isLimitExceeded) {
                BlockRed.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(app.displayName, fontWeight = FontWeight.SemiBold)
                if (app.isLimitExceeded) {
                    Text("BLOCKED", color = BlockRed, fontWeight = FontWeight.Bold)
                } else {
                    Text(TimeFormatter.formatDurationMs(app.remainingMs) + " left")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { app.usagePercent },
                modifier = Modifier.fillMaxWidth(),
                color = if (app.usagePercent >= 0.9f) BlockRed else PrimaryBlue,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${TimeFormatter.formatDurationMs(app.usedTodayMs)} / ${TimeFormatter.formatMinutes(app.dailyLimitMinutes)}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
