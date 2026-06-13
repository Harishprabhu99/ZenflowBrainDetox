package com.zenflow.brain.detox.ui.screens.blocking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenflow.brain.detox.BrainDetoxApp
import com.zenflow.brain.detox.data.repository.SettingsRepository
import com.zenflow.brain.detox.service.MonitoringService
import com.zenflow.brain.detox.ui.theme.BlockRed
import com.zenflow.brain.detox.ui.theme.ZenflowBrainDetoxTheme
import kotlinx.coroutines.launch

class BlockingActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = (application as BrainDetoxApp).container.settingsRepository
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "App"
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

        setContent {
            ZenflowBrainDetoxTheme {
                BlockingScreen(
                    appName = appName,
                    onGoBack = { finishAndRemoveTask() },
                    onOverride = {
                        // Send override intent to service
                        val overrideIntent = Intent(this, MonitoringService::class.java).apply {
                            action = MonitoringService.ACTION_OVERRIDE
                            putExtra(MonitoringService.EXTRA_PACKAGE_NAME, packageName)
                        }
                        startService(overrideIntent)
                        finishAndRemoveTask()
                    },
                    canOverride = { settingsRepository.canOverrideStrictMode() },
                    recordOverride = { settingsRepository.recordStrictOverride() },
                )
            }
        }
    }

    companion object {
        private const val EXTRA_APP_NAME = "extra_app_name"
        private const val EXTRA_PACKAGE_NAME = "extra_package_name"

        fun createIntent(context: Context, appName: String, packageName: String): Intent =
            Intent(context, BlockingActivity::class.java).apply {
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
    }
}

@Composable
private fun BlockingScreen(
    appName: String,
    onGoBack: () -> Unit,
    onOverride: () -> Unit,
    canOverride: suspend () -> Boolean,
    recordOverride: suspend () -> Unit,
) {
    var showOverrideDialog by remember { mutableStateOf(false) }
    var strictBlocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockRed)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("⛔", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onError,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Daily limit reached",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Take a break. Your brain will thank you.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGoBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onError,
                contentColor = BlockRed,
            ),
        ) {
            Text("Go Home", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                scope.launch {
                    if (canOverride()) {
                        showOverrideDialog = true
                    } else {
                        strictBlocked = true
                    }
                }
            },
        ) {
            Text("Override", color = MaterialTheme.colorScheme.onError)
        }
    }

    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("Override block?") },
            text = {
                Text("Bypassing your limit can increase compulsive scrolling and reduce focus. Are you sure?")
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        recordOverride()
                        onOverride()
                    }
                }) {
                    Text("Yes, override")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showOverrideDialog = false }) {
                    Text("Stay focused")
                }
            },
        )
    }

    if (strictBlocked) {
        AlertDialog(
            onDismissRequest = { strictBlocked = false },
            title = { Text("Strict Mode Active") },
            text = { Text("Override is locked for 24 hours. Stay strong!") },
            confirmButton = {
                Button(onClick = { strictBlocked = false }) { Text("OK") }
            },
        )
    }
}
