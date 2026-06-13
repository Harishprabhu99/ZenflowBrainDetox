package com.zenflow.brain.detox.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zenflow.brain.detox.BrainDetoxApp
import com.zenflow.brain.detox.MainActivity
import com.zenflow.brain.detox.R
import com.zenflow.brain.detox.data.repository.BlockedAppRepository
import com.zenflow.brain.detox.data.repository.SettingsRepository
import com.zenflow.brain.detox.domain.model.BlockedApp
import com.zenflow.brain.detox.ui.screens.blocking.BlockingActivity
import com.zenflow.brain.detox.util.PermissionHelper
import com.zenflow.brain.detox.util.UsageStatsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MonitoringService : Service() {

    private lateinit var blockedAppRepository: BlockedAppRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var usageStatsHelper: UsageStatsHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private var lastForegroundPackage: String? = null
    private val notifiedThresholds = mutableSetOf<String>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val container = (application as BrainDetoxApp).container
        blockedAppRepository = container.blockedAppRepository
        settingsRepository = container.settingsRepository
        usageStatsHelper = container.usageStatsHelper
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Monitoring social media usage"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        startMonitoringLoop()
        return START_STICKY
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                if (PermissionHelper.hasUsageStatsPermission(this@MonitoringService)) {
                    pollUsage()
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun pollUsage() {
        val enabledApps = blockedAppRepository.observeEnabledApps().first()
        if (enabledApps.isEmpty()) return

        for (app in enabledApps) {
            val usedMs = usageStatsHelper.getUsageForPackageToday(app.packageName)
            val openCount = if (lastForegroundPackage != app.packageName &&
                usageStatsHelper.getForegroundAppPackage() == app.packageName
            ) {
                app.openCountToday + 1
            } else {
                app.openCountToday
            }
            blockedAppRepository.updateUsage(app.packageName, usedMs, openCount)

            val updated = app.copy(usedTodayMs = usedMs, openCountToday = openCount)
            checkThresholdNotifications(updated)
        }

        val foreground = usageStatsHelper.getForegroundAppPackage()
        if (foreground != null && foreground != packageName) {
            val blocked = enabledApps.find { it.packageName == foreground }
            if (blocked != null) {
                val usedMs = usageStatsHelper.getUsageForPackageToday(foreground)
                if (usedMs >= blocked.dailyLimitMinutes * 60_000L) {
                    showBlockingScreen(blocked.displayName, blocked.packageName)
                }
            }
        }
        lastForegroundPackage = foreground
    }

    private suspend fun checkThresholdNotifications(app: BlockedApp) {
        val settings = settingsRepository.getSettings()
        val key50 = "${app.packageName}_50"
        val key90 = "${app.packageName}_90"
        if (settings.notifyAt50Percent && app.usagePercent >= 0.5f && key50 !in notifiedThresholds) {
            notifiedThresholds.add(key50)
            showThresholdNotification(app.displayName, 50)
        }
        if (settings.notifyAt90Percent && app.usagePercent >= 0.9f && key90 !in notifiedThresholds) {
            notifiedThresholds.add(key90)
            showThresholdNotification(app.displayName, 90)
        }
    }

    private fun showThresholdNotification(appName: String, percent: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Usage warning")
            .setContentText("$appName has reached $percent% of your daily limit")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(appName.hashCode() + percent, notification)
    }

    private fun showBlockingScreen(appName: String, packageName: String) {
        val intent = BlockingActivity.createIntent(this, appName, packageName).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun buildNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Usage Monitoring",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps Brain Rot Reducer monitoring your app usage"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_STOP = "com.zenflow.brain.detox.STOP_MONITORING"
        private const val CHANNEL_ID = "monitoring_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL_MS = 5_000L

        fun start(context: Context) {
            val intent = Intent(context, MonitoringService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitoringService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
