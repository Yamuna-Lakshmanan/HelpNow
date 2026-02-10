package com.helpnow.trackme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.helpnow.data.CheckIn
import com.helpnow.data.CheckInResponse
import com.helpnow.emergency.EmergencyCallManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TrackMeViewModel(application: Application) : AndroidViewModel(application) {
    private val manager by lazy { TrackMeServiceManager.getInstance(application) }
    private val prefs by lazy { manager.getPreferences() }
    private var checkInTimeoutJob: Job? = null
    private val emergencyCallManager by lazy { EmergencyCallManager(application) }

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _checkInCount = MutableStateFlow(0)
    val checkInCount: StateFlow<Int> = _checkInCount.asStateFlow()

    private val _showCheckInOverlay = MutableStateFlow<TrackMeServiceManager.CheckInOverlayEvent?>(null)
    val showCheckInOverlay: StateFlow<TrackMeServiceManager.CheckInOverlayEvent?> = _showCheckInOverlay.asStateFlow()

    private val _emergencyActive = MutableStateFlow(false)
    val emergencyActive: StateFlow<Boolean> = _emergencyActive.asStateFlow()

    private val _showWelcomeHome = MutableStateFlow(false)
    val showWelcomeHome: StateFlow<Boolean> = _showWelcomeHome.asStateFlow()

    init {
        manager.isTracking.onEach { _isTracking.value = it }.launchIn(viewModelScope)
        manager.checkInCount.onEach { _checkInCount.value = it }.launchIn(viewModelScope)
        manager.homeReached.onEach { _showWelcomeHome.value = true }.launchIn(viewModelScope)
        manager.emergencyTriggered.onEach { _emergencyActive.value = true }.launchIn(viewModelScope)
        viewModelScope.launch {
            manager.showCheckInOverlay.collect { event ->
                _showCheckInOverlay.value = event
                checkInTimeoutJob?.cancel()
                if (event != null) {
                    checkInTimeoutJob = viewModelScope.launch {
                        delay(120000) // 2 minutes
                        if (_showCheckInOverlay.value == event) {
                            onCheckInResponse(CheckInResponse.TIMEOUT, event.lat, event.lng, event.address)
                        }
                    }
                }
            }
        }
    }

    fun startTracking() {
        manager.startTracking()
    }

    fun stopTracking() {
        manager.stopTracking()
    }

    fun onCheckInResponse(response: CheckInResponse, lat: Double, lng: Double, address: String?) {
        checkInTimeoutJob?.cancel()
        _showCheckInOverlay.value = null
        manager.onCheckInResponse(response, lat, lng, address)
    }

    fun dismissWelcomeHome() {
        _showWelcomeHome.value = false
    }

    fun getCheckInHistory(): List<CheckIn> = manager.getCheckInHistory()

    fun getEmergencyContactCount(): Int = 0
}
