package com.helpnow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.helpnow.utils.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main Application class for HelpNow.
 * Handles background initialization to prevent startup ANRs.
 */
class HelpNowApplication : Application() {

    // Application-wide scope for background tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // 1. Immediate initialization (Main Thread)
        SharedPreferencesManager.init(this)

        // 2. Heavy initialization (Background Thread)
        applicationScope.launch(Dispatchers.IO) {
            initializeHeavyServices()
        }
    }

    private fun initializeHeavyServices() {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel("emergency_channel", "Emergency", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel("tracking_channel", "Tracking", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("check_in_channel", "Check-in", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel("voice_command_channel", "Voice Command", NotificationManager.IMPORTANCE_DEFAULT)
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.let { manager ->
                channels.forEach { manager.createNotificationChannel(it) }
            }
        }
    }
}
