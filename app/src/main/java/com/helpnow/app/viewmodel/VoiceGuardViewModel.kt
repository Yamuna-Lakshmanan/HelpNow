package com.helpnow.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceGuardViewModel : ViewModel() {
    private val _isVoiceGuardEnabled = MutableStateFlow(true)
    val isVoiceGuardEnabled: StateFlow<Boolean> = _isVoiceGuardEnabled.asStateFlow()

    private val _isSpeechRecognitionAvailable = MutableStateFlow(true)
    val isSpeechRecognitionAvailable: StateFlow<Boolean> = _isSpeechRecognitionAvailable.asStateFlow()

    private val _serviceStatus = MutableStateFlow("stopped")
    val serviceStatus: StateFlow<String> = _serviceStatus.asStateFlow()

    private val _falseAlarmsToday = MutableStateFlow(0)
    val falseAlarmsToday: StateFlow<Int> = _falseAlarmsToday.asStateFlow()

    private val _isTestListening = MutableStateFlow(false)
    val isTestListening: StateFlow<Boolean> = _isTestListening.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _testError = MutableStateFlow<String?>(null)
    val testError: StateFlow<String?> = _testError.asStateFlow()

    fun toggleVoiceGuard(enabled: Boolean) {
        _isVoiceGuardEnabled.value = enabled
    }

    fun refreshStatus() {
        // Implementation
    }

    fun triggerTestPhrase() {
        // Implementation
    }
}
