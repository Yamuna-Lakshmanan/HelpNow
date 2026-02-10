package com.helpnow.emergency

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

class EmergencyCallManager(private val context: Context) {
    private val contextRef = WeakReference(context.applicationContext)

    companion object {
        private const val EMERGENCY_NUMBER = "8807659591"
    }

    fun callEmergencyNumber() {
        val ctx = contextRef.get() ?: return
        try {
            if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$EMERGENCY_NUMBER")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$EMERGENCY_NUMBER")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(fallback)
            } catch (_: Exception) { }
        }
    }
}
