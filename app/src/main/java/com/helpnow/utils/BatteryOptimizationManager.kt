package com.helpnow.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles the logic for checking and requesting battery optimization status.
 * This class is designed to be called from a coroutine and contains no UI code.
 */
class BatteryOptimizationManager(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    /**
     * Checks if the app is currently ignoring battery optimizations.
     * This is a suspend function that must be called from a coroutine on a background thread.
     *
     * @return true if battery optimizations are off, false otherwise.
     */
    suspend fun isIgnoringBatteryOptimizations(): Boolean {
        // This check can be slow and should not run on the main thread.
        return withContext(Dispatchers.IO) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
    }

    /**
     * Creates an intent to navigate the user to the battery optimization settings screen.
     * It is the caller's responsibility to show any UI prompts.
     */
    fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fallback for devices that do not support the primary intent.
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: ActivityNotFoundException) {
                // Could not open any settings screen. It's best to log this.
            }
        }
    }
}
