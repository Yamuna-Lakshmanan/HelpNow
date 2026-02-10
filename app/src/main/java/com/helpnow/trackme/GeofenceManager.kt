package com.helpnow.trackme

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Home arrival detection: checks if current location is within radius of home.
 * Used by TrackMeService on each location update (no separate Geofence API needed).
 */
object GeofenceManager {
    const val HOME_RADIUS_METERS = 100.0

    fun isWithinHomeRadius(currentLat: Double, currentLng: Double, homeLat: Double, homeLng: Double): Boolean {
        return distanceMeters(currentLat, currentLng, homeLat, homeLng) <= HOME_RADIUS_METERS
    }

    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
