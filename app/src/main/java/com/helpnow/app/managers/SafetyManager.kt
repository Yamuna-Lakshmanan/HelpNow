package com.helpnow.app.managers

import android.content.Context
import com.helpnow.app.utils.TrackMePreferences

class SafetyManager private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var instance: SafetyManager? = null

        fun getInstance(context: Context): SafetyManager {
            return instance ?: synchronized(this) {
                instance ?: SafetyManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun markImSafe() {
        val prefs = TrackMePreferences(context)
        prefs.isTrackingActive = false
        TrackMeServiceManager.getInstance(context).stopTracking()
        // Also stop voice guard if necessary
    }
}
