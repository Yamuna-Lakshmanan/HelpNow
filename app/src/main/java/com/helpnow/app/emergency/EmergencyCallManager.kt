package com.helpnow.app.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.lang.ref.WeakReference

class EmergencyCallManager(context: Context) {

    private val contextRef = WeakReference(context.applicationContext)

    fun call(phoneNumber: String) {
        val context = contextRef.get() ?: return
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}