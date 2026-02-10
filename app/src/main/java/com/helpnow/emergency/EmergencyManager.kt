package com.helpnow.emergency

import android.content.Context
import com.helpnow.integration.SmsLocationModuleImpl

class EmergencyManager(private val context: Context) {

    private val smsLocationModule = SmsLocationModuleImpl(context)
    private val emergencyCallManager = EmergencyCallManager(context)

    fun triggerEmergency() {
        smsLocationModule.sendEmergencyAlert()
        emergencyCallManager.callEmergencyNumber()
    }
}
