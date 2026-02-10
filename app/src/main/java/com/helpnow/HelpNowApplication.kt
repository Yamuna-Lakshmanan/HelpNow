package com.helpnow

import android.app.Application
import com.helpnow.integration.SmsLocationModule
import com.helpnow.integration.SmsLocationModuleImpl
import com.helpnow.trackme.HelpNowApp

class HelpNowApplication : Application(), HelpNowApp {
    private val _smsLocationModule: SmsLocationModule by lazy { SmsLocationModuleImpl(this) }

    override fun getSmsLocationModule(): SmsLocationModule = _smsLocationModule
}
