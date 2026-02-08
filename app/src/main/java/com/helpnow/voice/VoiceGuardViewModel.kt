package com.helpnow.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.helpnow.utils.SharedPreferencesManager
import com.helpnow.voice.models.VoiceServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Voice Guard UI state.
 */
class VoiceGuardViewModel(private val context: Context) : ViewModel() {

    private val prefsManager = SharedPreferencesManager.getInstance(context)

    private val _isVoiceGuardEnabled = MutableStateFlow(prefsManager.isVoiceGuardEnabled())
    val isVoiceGuardEnabled: StateFlow<Boolean> = _isVoiceGuardEnabled.asStateFlow()

    private val _serviceStatus = MutableStateFlow(prefsManager.getVoiceServiceStatus())
    val serviceStatus: StateFlow<String> = _serviceStatus.asStateFlow()

    private val _falseAlarmsToday = MutableStateFlow(prefsManager.getFalseAlarmsToday())
    val falseAlarmsToday: StateFlow<Int> = _falseAlarmsToday.asStateFlow()

    fun toggleVoiceGuard(enabled: Boolean) {
        viewModelScope.launch {
            try {
                prefsManager.setVoiceGuardEnabled(enabled)
                _isVoiceGuardEnabled.value = enabled
                if (enabled) {
                    VoiceListenerService.start(context)
                } else {
                    VoiceListenerService.stop(context)
                }
                refreshStatus()
            } catch (e: Exception) {
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            try {
                _serviceStatus.value = prefsManager.getVoiceServiceStatus()
                _falseAlarmsToday.value = prefsManager.getFalseAlarmsToday()
                _isVoiceGuardEnabled.value = prefsManager.isVoiceGuardEnabled()
            } catch (e: Exception) {
            }
        }
    }

    fun triggerTestPhrase() {
        viewModelScope.launch {
            try {
                val phrase = prefsManager.getCustomDangerPhrase()
                val testPhrase = if (phrase.any { it.isDigit() }) phrase else "$phrase 5"
                val intent = android.content.Intent(context, VoiceSosCancelActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(VoiceSosCancelActivity.EXTRA_PHRASE, testPhrase)
                    putExtra(VoiceSosCancelActivity.EXTRA_CONFIDENCE, 0.9f)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VoiceGuardViewModel(context) as T
        }
    }
}
