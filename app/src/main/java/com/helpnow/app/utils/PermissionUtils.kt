package com.helpnow.app.utils

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
    const val PERMISSION_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS
    
    val REQUIRED_PERMISSIONS = listOf(
        PERMISSION_MICROPHONE,
        PERMISSION_LOCATION,
        PERMISSION_SMS,
        PERMISSION_CALL
    )
    
    fun checkPermission(context: Context, permission: String): Boolean {
        return try {
            android.content.pm.PackageManager.PERMISSION_GRANTED ==
                androidx.core.content.ContextCompat.checkSelfPermission(context, permission)
        } catch (e: Exception) {
            false
        }
    }

    fun areAllRequiredPermissionsGranted(context: Context): Boolean {
        val baseOk = REQUIRED_PERMISSIONS.all { checkPermission(context, it) }
        if (!baseOk) return false
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            checkPermission(context, PERMISSION_NOTIFICATIONS)
        } else {
            true
        }
    }
}
