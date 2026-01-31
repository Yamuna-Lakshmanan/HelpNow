package com.helpnow.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.helpnow.models.EmergencyContact
import com.helpnow.models.User

class SharedPreferencesManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "HelpNowPrefs"
        private const val KEY_USER_LOGGED_IN = "isUserLoggedIn"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_PHONE = "userPhone"
        private const val KEY_USER_GENDER = "userGender"
        private const val KEY_USER_DOB = "userDOB"
        private const val KEY_USER_ADDRESS = "userAddress"
        private const val KEY_USER_CITY = "userCity"
        private const val KEY_EMERGENCY_CONTACTS = "emergencyContacts"
        private const val KEY_PERMISSION_LOCATION = "permissionLocation"
        private const val KEY_PERMISSION_MICROPHONE = "permissionMicrophone"
        private const val KEY_PERMISSION_CAMERA = "permissionCamera"
        private const val KEY_PERMISSION_SMS = "permissionSMS"
        private const val KEY_PERMISSION_CALL = "permissionCall"
        
        @Volatile
        private var INSTANCE: SharedPreferencesManager? = null
        
        fun getInstance(context: Context): SharedPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun isUserLoggedIn(): Boolean {
        return try {
            prefs.getBoolean(KEY_USER_LOGGED_IN, false)
        } catch (e: Exception) {
            false
        }
    }
    
    fun setLoggedIn(status: Boolean) {
        try {
            prefs.edit().putBoolean(KEY_USER_LOGGED_IN, status).apply()
        } catch (e: Exception) {
        }
    }
    
    fun saveUserData(
        userName: String,
        userPhone: String,
        userGender: String,
        userDOB: String,
        userAddress: String,
        userCity: String
    ) {
        try {
            prefs.edit().apply {
                putString(KEY_USER_NAME, userName)
                putString(KEY_USER_PHONE, userPhone)
                putString(KEY_USER_GENDER, userGender)
                putString(KEY_USER_DOB, userDOB)
                putString(KEY_USER_ADDRESS, userAddress)
                putString(KEY_USER_CITY, userCity)
                apply()
            }
        } catch (e: Exception) {
        }
    }
    
    fun getUserData(): User? {
        return try {
            val userName = prefs.getString(KEY_USER_NAME, null) ?: return null
            val userPhone = prefs.getString(KEY_USER_PHONE, null) ?: return null
            val userGender = prefs.getString(KEY_USER_GENDER, null) ?: return null
            val userDOB = prefs.getString(KEY_USER_DOB, null) ?: return null
            val userAddress = prefs.getString(KEY_USER_ADDRESS, null) ?: return null
            val userCity = prefs.getString(KEY_USER_CITY, null) ?: return null
            
            User(
                userName = userName,
                userPhone = userPhone,
                userGender = userGender,
                userDOB = userDOB,
                userAddress = userAddress,
                userCity = userCity
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun saveEmergencyContacts(contacts: List<EmergencyContact>) {
        try {
            val json = gson.toJson(contacts)
            prefs.edit().putString(KEY_EMERGENCY_CONTACTS, json).apply()
        } catch (e: Exception) {
        }
    }
    
    fun getEmergencyContacts(): List<EmergencyContact> {
        return try {
            val json = prefs.getString(KEY_EMERGENCY_CONTACTS, null) ?: return emptyList()
            val type = object : TypeToken<List<EmergencyContact>>() {}.type
            gson.fromJson<List<EmergencyContact>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun savePermissionStatus(permission: String, granted: Boolean) {
        try {
            val key = when (permission) {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION -> KEY_PERMISSION_LOCATION
                android.Manifest.permission.RECORD_AUDIO -> KEY_PERMISSION_MICROPHONE
                android.Manifest.permission.CAMERA -> KEY_PERMISSION_CAMERA
                android.Manifest.permission.SEND_SMS -> KEY_PERMISSION_SMS
                android.Manifest.permission.CALL_PHONE -> KEY_PERMISSION_CALL
                else -> null
            }
            key?.let {
                prefs.edit().putBoolean(it, granted).apply()
            }
        } catch (e: Exception) {
        }
    }
    
    fun isPermissionGranted(permission: String): Boolean {
        return try {
            val key = when (permission) {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION -> KEY_PERMISSION_LOCATION
                android.Manifest.permission.RECORD_AUDIO -> KEY_PERMISSION_MICROPHONE
                android.Manifest.permission.CAMERA -> KEY_PERMISSION_CAMERA
                android.Manifest.permission.SEND_SMS -> KEY_PERMISSION_SMS
                android.Manifest.permission.CALL_PHONE -> KEY_PERMISSION_CALL
                else -> null
            }
            key?.let { prefs.getBoolean(it, false) } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    fun clearAllData() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
        }
    }
}
