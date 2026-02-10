package com.helpnow.integration

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.helpnow.trackme.TrackMeService
import com.helpnow.utils.SharedPreferencesManager
import java.lang.ref.WeakReference

class SmsLocationModuleImpl(context: Context) : SmsLocationModule {
    private val contextRef = WeakReference(context.applicationContext)

    override fun sendEmergencyAlert() {
        Log.d("SmsLocationModule", "sendEmergencyAlert called")
        val ctx = contextRef.get() ?: return
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SmsLocationModule", "Permissions not granted")
            return
        }

        val prefs = SharedPreferencesManager.getInstance(ctx)
        val contacts = prefs.getEmergencyContacts()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val message = if (location != null) {
                    "I need help! This is my current location: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                } else {
                    "I need help! I'm at my last known location: https://www.google.com/maps/search/?api=1&query=${TrackMeService.lastKnownLat},${TrackMeService.lastKnownLng}"
                }
                try {
                    val smsManager = SmsManager.getDefault()
                    contacts.forEach { contact ->
                        Log.d("SmsLocationModule", "Sending SMS to ${contact.phone}")
                        smsManager.sendTextMessage(contact.phone, null, message, null, null)
                    }
                } catch (e: Exception) {
                    Log.e("SmsLocationModule", "Failed to send SMS", e)
                }
            }.addOnFailureListener { e ->
                Log.e("SmsLocationModule", "Failed to get location", e)
            }
        } catch (e: SecurityException) {
            Log.e("SmsLocationModule", "Security exception getting location", e)
        }
    }
}
