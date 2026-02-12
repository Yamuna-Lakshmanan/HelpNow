package com.helpnow.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.helpnow.app.MainActivity
import com.helpnow.app.R
import com.helpnow.app.utils.TrackMePreferences
import com.helpnow.app.managers.GeofenceManager
import com.helpnow.app.managers.TrackMeServiceManager
import java.util.concurrent.atomic.AtomicLong

class TrackMeService : Service() {
    private var fusedClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var manager: TrackMeServiceManager? = null
    private var prefs: TrackMePreferences? = null
    private val serviceStartTime = AtomicLong(0L)
    private var nextCheckInAt = 0L
    private var checkInIndex = 0

    companion object {
        private const val NOTIFICATION_ID = 4001
        private const val CHANNEL_ID = "track_me_channel"
        private const val LOCATION_INTERVAL_MS = 30_000L
        private const val CHECK_IN_INTERVAL_MS = 5 * 60 * 1000L
        private const val CHECK_IN_TIMEOUT_MS = 2 * 60 * 1000L
        const val ACTION_STOP = "com.helpnow.trackme.STOP"
        const val ACTION_SAFE = "com.helpnow.trackme.SAFE"

        @Volatile
        var lastKnownLat: Double = 0.0
            private set
        @Volatile
        var lastKnownLng: Double = 0.0
            private set
        @Volatile
        var lastKnownAddress: String? = null
            private set
    }

    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): TrackMeService = this@TrackMeService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        val ctx = applicationContext
        manager = TrackMeServiceManager.getInstance(ctx)
        prefs = TrackMePreferences(ctx)
        serviceStartTime.set(System.currentTimeMillis())
        nextCheckInAt = System.currentTimeMillis() + CHECK_IN_INTERVAL_MS
        checkInIndex = 0
        fusedClient = LocationServices.getFusedLocationProviderClient(ctx)
        createChannel(ctx)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                // Notification "STOP" is often intended as "I'm not safe" in emergency contexts,
                // but usually the UI "Stop Track" is for safe ending.
                // We'll keep the emergency trigger for notification STOP as a safeguard.
                manager?.triggerEmergency()
                stopTrackingAndStopSelf()
                return START_NOT_STICKY
            }
            ACTION_SAFE -> {
                manager?.markSafeFromNotification()
                return START_NOT_STICKY
            }
        }
        val count = prefs?.checkInCount ?: 0
        startForeground(NOTIFICATION_ID, buildNotification(count, checkInIndex))
        startLocationUpdates()
        return START_STICKY
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.track_me_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { setShowBadge(true) }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL_MS / 2)
            .setMaxUpdates(Integer.MAX_VALUE)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                lastKnownLat = loc.latitude
                lastKnownLng = loc.longitude
                lastKnownAddress = null
                checkHomeAndCheckIn()
            }
        }
        try {
            fusedClient?.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
        } catch (_: Exception) { }
    }

    private fun checkHomeAndCheckIn() {
        val p = prefs ?: return
        val homeLat = p.homeLat
        val homeLng = p.homeLng
        if (homeLat != 0.0 && homeLng != 0.0 && GeofenceManager.isWithinHomeRadius(lastKnownLat, lastKnownLng, homeLat, homeLng)) {
            manager?.onHomeReached()
            stopTrackingAndStopSelf()
            return
        }
        val now = System.currentTimeMillis()
        if (now >= nextCheckInAt) {
            checkInIndex++
            nextCheckInAt = now + CHECK_IN_INTERVAL_MS
            val timeoutAt = now + CHECK_IN_TIMEOUT_MS
            manager?.scheduleCheckIn(checkInIndex, timeoutAt, lastKnownLat, lastKnownLng, lastKnownAddress)
            p.setNextCheckInTime(nextCheckInAt)
        }
        val totalCheckIns = checkInIndex
        val count = p.checkInCount
        try {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(NOTIFICATION_ID, buildNotification(count, kotlin.math.max(1, totalCheckIns)))
        } catch (_: Exception) { }
    }

    private fun buildNotification(checkInDone: Int, totalCheckIns: Int): Notification {
        val ctx = applicationContext
        val title = ctx.getString(R.string.track_me_notification_title)
        val text = ctx.getString(R.string.track_me_notification_text, checkInDone, totalCheckIns)
        val stopIntent = Intent(this, TrackMeService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val safeIntent = Intent(this, TrackMeService::class.java).apply { action = ACTION_SAFE }
        val safePending = PendingIntent.getService(
            this, 1, safeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val openPending = PendingIntent.getActivity(
            this, 2, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(openPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, getString(R.string.notification_action_safe), safePending)
            .addAction(0, getString(R.string.notification_action_stop), stopPending)
            .setProgress(0, 0, true)
            .build()
    }

    private fun stopTrackingAndStopSelf() {
        try {
            locationCallback?.let { fusedClient?.removeLocationUpdates(it) }
        } catch (_: Exception) { }
        locationCallback = null
        manager?.stopTracking()
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) { }
        stopSelf()
    }

    override fun onDestroy() {
        try {
            locationCallback?.let { fusedClient?.removeLocationUpdates(it) }
        } catch (_: Exception) { }
        super.onDestroy()
    }
}
