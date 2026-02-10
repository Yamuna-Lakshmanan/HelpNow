package com.helpnow.voice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.helpnow.R
import com.helpnow.utils.SharedPreferencesManager

/**
 * Emergency trigger - POLICE CALL is primary. HARDCODED police number 8807659591.
 * NOT user configurable - security + safety.
 */
class EmergencyTriggerManager(private val context: Context) {

    companion object {
        private const val TAG = "EmergencyTrigger"
        const val ACTION_EMERGENCY_TRIGGERED = "com.helpnow.EMERGENCY_TRIGGERED"
        /** HARDCODED POLICE NUMBER - NEVER CHANGE - NOT USER CONFIGURABLE */
        private const val POLICE_NUMBER = "8807659591"
    }

    private val prefsManager = SharedPreferencesManager.getInstance(context)

    /**
     * Full emergency sequence: 1) Call police 2) Send SMS+location 3) Navigate 4) Stop service 5) Log.
     */
    fun triggerEmergency(detectedPhrase: String, voiceConfidence: Float) {
        val timestamp = System.currentTimeMillis()
        try {
            Log.w(TAG, "⚠️⚠️⚠️ EMERGENCY TRIGGERED ⚠️⚠️⚠️")
            prefsManager.setLastDetectionTime(timestamp)

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "❌ CALL_PHONE permission not granted")
                showError(context.getString(R.string.permission_call_denied))
                return
            }

            callPolice()
            sendEmergencyAlert()
            navigateToEmergencyScreen()
            stopVoiceService()
            logIncident(detectedPhrase, voiceConfidence, timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error triggering emergency", e)
            showError(context.getString(R.string.emergency_trigger_failed))
        }
    }

    private fun callPolice() {
        try {
            Log.w(TAG, "📞 CALLING POLICE: $POLICE_NUMBER")
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$POLICE_NUMBER")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.i(TAG, "✓ Police call initiated: $POLICE_NUMBER")
            showMessage(context.getString(R.string.calling_police))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to call police", e)
            showError("${context.getString(R.string.police_call_failed)}: ${e.message}")
        }
    }

    private fun sendEmergencyAlert() {
        try {
            Log.i(TAG, "📱 Sending emergency SMS + location")
            com.helpnow.emergency.SMSLocationManager(context).sendEmergencyAlert()
            val intent = Intent(ACTION_EMERGENCY_TRIGGERED).apply {
                setPackage(context.packageName)
                putExtra("source", "voice")
            }
            context.sendBroadcast(intent)
            Log.i(TAG, "✓ Emergency SMS + location sent")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send emergency alert", e)
        }
    }

    private fun navigateToEmergencyScreen() {
        try {
            Log.i(TAG, "📺 Navigating to EmergencyActiveScreen")
            val intent = Intent(context, com.helpnow.screens.EmergencyActiveScreen::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
            Log.i(TAG, "✓ Navigated to EmergencyActiveScreen")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to navigate", e)
            try {
                val mainIntent = Intent(context, Class.forName("com.helpnow.MainActivity")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("show_emergency_active", true)
                }
                context.startActivity(mainIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "❌ Failed to navigate to MainActivity", e2)
            }
        }
    }

    private fun stopVoiceService() {
        try {
            context.stopService(Intent(context, VoiceListenerService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "stopVoiceService error", e)
        }
    }

    private fun logIncident(detectedPhrase: String, voiceConfidence: Float, timestamp: Long) {
        val log = """
            ╔════════════════════════════════════╗
            ║        INCIDENT LOG                ║
            ╠════════════════════════════════════╣
            ║ Timestamp: $timestamp
            ║ Detected Phrase: $detectedPhrase
            ║ Voice Confidence: $voiceConfidence (80%+ required)
            ║ Police Number: $POLICE_NUMBER
            ║ Police Call: INITIATED ✓
            ║ SMS + Location: SENT ✓
            ║ Emergency Screen: SHOWN ✓
            ╚════════════════════════════════════╝
        """
        .trimIndent()
        Log.i("IncidentLog", log)
    }

    private fun showMessage(msg: String) {
        try {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) { }
    }

    private fun showError(msg: String) {
        try {
            Toast.makeText(context, "❌ $msg", Toast.LENGTH_LONG).show()
        } catch (e: Exception) { }
    }
}
