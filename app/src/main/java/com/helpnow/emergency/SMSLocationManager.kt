package com.helpnow.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.helpnow.utils.SharedPreferencesManager

/**
 * Module 3 - Sends emergency SMS + GPS location to emergency contacts.
 */
class SMSLocationManager(private val context: Context) {

    companion object {
        private const val TAG = "SMSLocationManager"
    }

    private val prefsManager = SharedPreferencesManager.getInstance(context)

    fun sendEmergencyAlert() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "SEND_SMS permission not granted")
                return
            }
            val contacts = prefsManager.getEmergencyContacts()
            val user = prefsManager.getUserData()
            val location = getLastKnownLocation()
            val msg = buildEmergencyMessage(user, location)
            for (contact in contacts) {
                try {
                    sendSms(contact.phone, msg)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send SMS to ${contact.phone}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendEmergencyAlert error", e)
        }
    }

    private fun buildEmergencyMessage(user: com.helpnow.models.User?, location: String): String {
        val name = user?.userName ?: "HelpNow User"
        return "EMERGENCY from $name! Need help immediately. Location: $location"
    }

    private fun getLastKnownLocation(): String {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                return "Location unavailable"
            }
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            val loc = lm?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            loc?.let { "https://maps.google.com/?q=${it.latitude},${it.longitude}" } ?: "Location unavailable"
        } catch (e: Exception) {
            "Location unavailable"
        }
    }

    private fun sendSms(phone: String, message: String) {
        try {
            @Suppress("DEPRECATION")
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            Log.i(TAG, "SMS sent to $phone")
        } catch (e: Exception) {
            Log.e(TAG, "sendSms error", e)
            throw e
        }
    }
}
