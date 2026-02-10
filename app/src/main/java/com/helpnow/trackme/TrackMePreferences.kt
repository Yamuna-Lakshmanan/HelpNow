package com.helpnow.trackme

import android.content.Context
import android.content.SharedPreferences
import com.helpnow.data.CheckIn
import com.helpnow.data.CheckInResponse
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

private const val PREFS_NAME = "helpnow_trackme"
private const val KEY_IS_TRACKING_ACTIVE = "is_tracking_active"
private const val KEY_TRACKING_START_TIME = "tracking_start_time"
private const val KEY_CHECK_IN_COUNT = "check_in_count"
private const val KEY_HOME_LAT = "home_lat"
private const val KEY_HOME_LNG = "home_lng"
private const val KEY_CHECK_IN_HISTORY = "check_in_history"
private const val MAX_HISTORY = 10

class TrackMePreferences(context: Context) {
    private val contextRef = WeakReference(context.applicationContext)
    private val prefs: SharedPreferences
        get() = contextRef.get()!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isTrackingActive: Boolean
        get() = prefs.getBoolean(KEY_IS_TRACKING_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_TRACKING_ACTIVE, value).apply()

    var trackingStartTime: Long
        get() = prefs.getLong(KEY_TRACKING_START_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_TRACKING_START_TIME, value).apply()

    var checkInCount: Int
        get() = prefs.getInt(KEY_CHECK_IN_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_CHECK_IN_COUNT, value).apply()

    var homeLat: Double
        get() = prefs.getFloat(KEY_HOME_LAT, 0f).toDouble().takeIf { it != 0.0 } ?: 0.0
        set(value) = prefs.edit().putFloat(KEY_HOME_LAT, value.toFloat()).apply()

    var homeLng: Double
        get() = prefs.getFloat(KEY_HOME_LNG, 0f).toDouble().takeIf { it != 0.0 } ?: 0.0
        set(value) = prefs.edit().putFloat(KEY_HOME_LNG, value.toFloat()).apply()

    fun addCheckIn(checkIn: CheckIn) {
        val list = getCheckInHistory().toMutableList().apply { add(0, checkIn) }
        if (list.size > MAX_HISTORY) list.removeAt(list.size - 1)
        val json = JSONArray().apply {
            list.forEach { ci ->
                put(JSONObject().apply {
                    put("timestamp", ci.timestamp)
                    put("response", ci.response.name)
                    put("latitude", ci.latitude)
                    put("longitude", ci.longitude)
                    put("locationAddress", ci.locationAddress ?: "")
                })
            }
        }
        prefs.edit().putString(KEY_CHECK_IN_HISTORY, json.toString()).apply()
    }

    fun getCheckInHistory(): List<CheckIn> {
        val raw = prefs.getString(KEY_CHECK_IN_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                CheckIn(
                    timestamp = o.getLong("timestamp"),
                    response = CheckInResponse.valueOf(o.getString("response")),
                    latitude = o.getDouble("latitude"),
                    longitude = o.getDouble("longitude"),
                    locationAddress = o.optString("locationAddress").takeIf { it.isNotEmpty() }
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun resetTrackingState() {
        prefs.edit()
            .putBoolean(KEY_IS_TRACKING_ACTIVE, false)
            .putLong(KEY_TRACKING_START_TIME, 0L)
            .putInt(KEY_CHECK_IN_COUNT, 0)
            .apply()
    }
}
