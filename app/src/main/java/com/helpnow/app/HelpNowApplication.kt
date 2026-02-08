package com.helpnow.app

import com.helpnow.app.integration.SmsLocationModule
import com.helpnow.app.integration.SmsLocationModuleImpl
import com.helpnow.app.trackme.HelpNowApp

class HelpNowApplication : android.app.Application(), HelpNowApp {
    private val _smsLocationModule: SmsLocationModule by lazy { SmsLocationModuleImpl(this) }

    override fun getSmsLocationModule(): SmsLocationModule = _smsLocationModule
}
