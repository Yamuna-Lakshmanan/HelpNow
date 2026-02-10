package com.helpnow.trackme

import android.content.Context
import android.content.Intent
import android.os.Build
import com.helpnow.data.CheckIn
import com.helpnow.data.CheckInResponse
import com.helpnow.app.emergency.EmergencyCallManager
import com.helpnow.integration.SmsLocationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Singleton controller for Track Me Home: starts/stops service and coordinates check-ins and emergency.
 */
class TrackMeServiceManager private constructor(context: Context) {
    private val contextRef = WeakReference(context.applicationContext)

    companion object {
        @Volatile
        private var instance: TrackMeServiceManager? = null
        fun getInstance(context: Context): TrackMeServiceManager {
            return instance ?: synchronized(this) {
                instance ?: TrackMeServiceManager(context.applicationContext).also { instance = it }
            }
        }
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs by lazy { TrackMePreferences(contextRef.get()!!) }
    private val emergencyCallManager by lazy { EmergencyCallManager(contextRef.get()!!) }
    private val smsModule: SmsLocationModule by lazy {
        (contextRef.get()?.applicationContext as? HelpNowApp)?.getSmsLocationModule()
            ?: com.helpnow.integration.SmsLocationModuleImpl(contextRef.get()!!)
    }
    private var currentCheckInIndex = 0
    private val pendingCheckInResponded = AtomicBoolean(false)

    private val _isTracking = MutableStateFlow(prefs.isTrackingActive)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _checkInCount = MutableStateFlow(prefs.checkInCount)
    val checkInCount: StateFlow<Int> = _checkInCount.asStateFlow()

    private val _showCheckInOverlay = MutableSharedFlow<CheckInOverlayEvent>(replay = 0, extraBufferCapacity = 1)
    val showCheckInOverlay: SharedFlow<CheckInOverlayEvent> = _showCheckInOverlay.asSharedFlow()

    private val _emergencyTriggered = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val emergencyTriggered: SharedFlow<Unit> = _emergencyTriggered.asSharedFlow()

    private val _homeReached = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val homeReached: SharedFlow<Unit> = _homeReached.asSharedFlow()

    private val _welcomeHomeMessage = MutableStateFlow<String?>(null)
    val welcomeHomeMessage: StateFlow<String?> = _welcomeHomeMessage.asStateFlow()

    fun getCheckInHistory(): List<CheckIn> = prefs.getCheckInHistory()
    fun getPreferences(): TrackMePreferences = prefs

    fun startTracking() {
        val ctx = contextRef.get() ?: return
        try {
            if (_isTracking.value) return
            prefs.isTrackingActive = true
            prefs.trackingStartTime = System.currentTimeMillis()
            prefs.checkInCount = 0
            _checkInCount.value = 0
            _isTracking.value = true
            val intent = Intent(ctx, TrackMeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } catch (_: Exception) { }
    }

    fun stopTracking() {
        val ctx = contextRef.get() ?: return
        try {
            prefs.isTrackingActive = false
            prefs.resetTrackingState()
            _isTracking.value = false
            _welcomeHomeMessage.value = null
            ctx.stopService(Intent(ctx, TrackMeService::class.java))
        } catch (_: Exception) { }
    }

    fun onHomeReached() {
        prefs.isTrackingActive = false
        _isTracking.value = false
        _welcomeHomeMessage.value = try {
            contextRef.get()?.resources?.getString(com.helpnow.R.string.welcome_home)
        } catch (_: Exception) { null } ?: "Welcome home! Tracking stopped safely ✓"
        contextRef.get()?.stopService(Intent(contextRef.get(), TrackMeService::class.java))
        _homeReached.tryEmit(Unit)
    }

    fun scheduleCheckIn(index: Int, timeoutAt: Long, currentLat: Double, currentLng: Double, address: String?) {
        currentCheckInIndex = index
        pendingCheckInResponded.set(false)
        scope.launch {
            _showCheckInOverlay.emit(CheckInOverlayEvent(index, timeoutAt, currentLat, currentLng, address))
        }
    }

    fun onCheckInResponse(response: CheckInResponse, lat: Double, lng: Double, address: String?) {
        if (!pendingCheckInResponded.compareAndSet(false, true)) return
        val ctx = contextRef.get() ?: return
        val prefs = TrackMePreferences(ctx)
        val checkIn = CheckIn(
            timestamp = System.currentTimeMillis(),
            response = response,
            latitude = lat,
            longitude = lng,
            locationAddress = address
        )
        prefs.addCheckIn(checkIn)
        prefs.checkInCount = prefs.checkInCount + 1
        _checkInCount.value = prefs.checkInCount
        when (response) {
            CheckInResponse.YES -> { }
            CheckInResponse.NO, CheckInResponse.TIMEOUT -> triggerEmergency()
        }
    }

    fun triggerEmergency() {
        try {
            smsModule.sendEmergencyAlert()
            emergencyCallManager.call("8807659591")
            _emergencyTriggered.tryEmit(Unit)
        } catch (_: Exception) { }
    }

    fun markSafeFromNotification() {
        if (pendingCheckInResponded.get()) return
        val lastLat = TrackMeService.lastKnownLat
        val lastLng = TrackMeService.lastKnownLng
        val lastAddress = TrackMeService.lastKnownAddress
        onCheckInResponse(CheckInResponse.YES, lastLat, lastLng, lastAddress)
    }

    data class CheckInOverlayEvent(
        val index: Int,
        val timeoutAt: Long,
        val lat: Double,
        val lng: Double,
        val address: String?
    )
}

interface HelpNowApp {
    fun getSmsLocationModule(): com.helpnow.integration.SmsLocationModule?
}
