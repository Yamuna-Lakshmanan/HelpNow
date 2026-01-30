package com.helpnow.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

object LocationUtils {
    fun getCurrentLocation(context: Context): Task<Location>? {
        return try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
            
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasPermission) {
                fusedLocationClient.lastLocation
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun formatCoordinates(latitude: Double, longitude: Double): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }
}
