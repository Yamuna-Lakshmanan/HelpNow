package com.helpnow.app.emergency

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.helpnow.app.BuildConfig
import java.lang.ref.WeakReference

/**
 * Handles emergency call to 112 (India). Uses demo number 9176074517 when applicable.
 */
class EmergencyCallManager(private val context: Context) {
    private val contextRef = WeakReference(context.applicationContext)

    companion object {
        private const val EMERGENCY_NUMBER_INDIA = "112"
        private const val DEMO_NUMBER = "9176074517"
    }

    /**
     * Initiates emergency call. Uses DEMO_NUMBER for demo/test; use EMERGENCY_NUMBER_INDIA for production.
     */
    fun call112() {
        val ctx = contextRef.get() ?: return
        try {
            val number = if (BuildConfig.USE_DEMO_EMERGENCY_NUMBER) DEMO_NUMBER else EMERGENCY_NUMBER_INDIA
            if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${if (BuildConfig.USE_DEMO_EMERGENCY_NUMBER) DEMO_NUMBER else EMERGENCY_NUMBER_INDIA}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(fallback)
            } catch (_: Exception) { }
        }
    }

    fun useDemoNumber(): Boolean = BuildConfig.USE_DEMO_EMERGENCY_NUMBER
}
