package com.helpnow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.helpnow.HelpNowApp
import com.helpnow.ui.theme.HelpNowTheme
import com.helpnow.utils.SharedPreferencesManager
import com.helpnow.voice.VoiceListenerService

class MainActivity : ComponentActivity() {

    private val voicePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioOk = permissions[Manifest.permission.RECORD_AUDIO] != false
        val callOk = permissions[Manifest.permission.CALL_PHONE] != false
        if (audioOk && callOk) {
            tryStartVoiceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsManager = SharedPreferencesManager.getInstance(this)

        setContent {
            HelpNowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    HelpNowApp(
                        navController = navController,
                        prefsManager = prefsManager,
                        onInitializeVoiceListener = { tryStartVoiceService() }
                    )
                }
            }
        }
    }

    private fun tryStartVoiceService() {
        try {
            val permissionsToRequest = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.CALL_PHONE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (permissionsToRequest.isNotEmpty()) {
                voicePermissionLauncher.launch(permissionsToRequest.toTypedArray())
                return
            }
            VoiceListenerService.start(this)
        } catch (e: Exception) {
        }
    }
}
