package com.helpnow.integration

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
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
            Toast.makeText(ctx, "SMS or Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = SharedPreferencesManager.getInstance(ctx)
        val contacts = prefs.getEmergencyContacts()
        if (contacts.isEmpty()) {
            Log.w("SmsLocationModule", "No emergency contacts saved")
            Toast.makeText(ctx, "No emergency contacts saved", Toast.LENGTH_SHORT).show()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val message = if (location != null) {
                    "I need help! This is my current location: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                } else {
                    Log.w("SmsLocationModule", "Location is not available, sending last known location")
                    "I need help! I'm at my last known location: https://www.google.com/maps/search/?api=1&query=${TrackMeService.lastKnownLat},${TrackMeService.lastKnownLng}"
                }
                
                try {
                    val smsManager: SmsManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ctx.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }

                    if (smsManager != null) {
                        contacts.forEach { contact ->
                            val phone = contact?.phone
                            if (phone != null && phone.isNotBlank()) {
                                Log.d("SmsLocationModule", "Sending SMS to $phone")
                                smsManager.sendTextMessage(phone, null, message, null, null)
                            } else {
                                Log.w("SmsLocationModule", "Skipping contact with null or empty phone number")
                            }
                        }
                    } else {
                        Log.e("SmsLocationModule", "SmsManager is null - Device might not support SMS")
                        Toast.makeText(ctx, "SMS capability not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("SmsLocationModule", "Failed to send SMS", e)
                    Toast.makeText(ctx, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e("SmsLocationModule", "Failed to get location", e)
                Toast.makeText(ctx, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("SmsLocationModule", "Security exception getting location", e)
            Toast.makeText(ctx, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }
}
