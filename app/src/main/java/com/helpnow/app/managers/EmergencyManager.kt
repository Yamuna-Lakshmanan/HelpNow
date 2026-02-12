package com.helpnow.app.managers

import android.content.Context
import com.helpnow.app.integration.SmsLocationModuleImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EmergencyManager(private val context: Context, private val scope: CoroutineScope) {

    private val smsLocationModule = SmsLocationModuleImpl(context)
    private val emergencyCallManager = EmergencyCallManager(context)

    fun triggerEmergency() {
        scope.launch {
            launch { smsLocationModule.sendEmergencyAlert() }
            launch { emergencyCallManager.callEmergencyNumber() }
        }
    }
}
