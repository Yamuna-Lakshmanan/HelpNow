package com.helpnow.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.helpnow.app.ui.theme.HelpNowTheme
import com.helpnow.app.utils.SharedPreferencesManager
import com.helpnow.app.voice.VoiceListenerService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Start Voice Listener Service from app start if permission is granted
        if (hasAudioPermission()) {
            startVoiceListenerService()
        }

        setContent {
            HelpNowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val prefsManager = SharedPreferencesManager.getInstance(this)
                    HelpNowApp(
                        navController = navController,
                        prefsManager = prefsManager,
                        onInitializeVoiceListener = { startVoiceListenerService() }
                    )
                }
            }
        }
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startVoiceListenerService() {
        val intent = Intent(this, VoiceListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
