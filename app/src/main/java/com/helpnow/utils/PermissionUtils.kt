package com.helpnow.utils

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object PermissionUtils {
    const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_MICROPHONE = Manifest.permission.RECORD_AUDIO
    const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    const val PERMISSION_SMS = Manifest.permission.SEND_SMS
    const val PERMISSION_CALL = Manifest.permission.CALL_PHONE
    
    val CRITICAL_PERMISSIONS = listOf(PERMISSION_LOCATION, PERMISSION_MICROPHONE)
    val OPTIONAL_PERMISSIONS = listOf(PERMISSION_CAMERA, PERMISSION_SMS, PERMISSION_CALL)
    
    fun checkPermission(context: Context, permission: String): Boolean {
        return try {
            android.content.pm.PackageManager.PERMISSION_GRANTED ==
                androidx.core.content.ContextCompat.checkSelfPermission(context, permission)
        } catch (e: Exception) {
            false
        }
    }
}
