package com.helpnow.app.integration

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Default implementation. Replace with Yamuna's Module 3 (SMS/Location) when available.
 * When integrated: call smsLocationManager.sendEmergencyAlert() from Module 3.
 */
class SmsLocationModuleImpl(context: Context) : SmsLocationModule {
    private val contextRef = WeakReference(context.applicationContext)

    override fun sendEmergencyAlert() {
        val ctx = contextRef.get() ?: return
        // Integration point: when Module 3 is present, invoke its sendEmergencyAlert()
        // e.g. (application as HelpNowApplication).getSmsLocationManager()?.sendEmergencyAlert()
    }
}
