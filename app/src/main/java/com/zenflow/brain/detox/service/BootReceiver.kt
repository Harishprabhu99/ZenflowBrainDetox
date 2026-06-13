package com.zenflow.brain.detox.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenflow.brain.detox.BrainDetoxApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val container = (context.applicationContext as BrainDetoxApp).container
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = container.settingsRepository.getSettings()
                if (settings.monitoringEnabled) {
                    MonitoringService.start(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
