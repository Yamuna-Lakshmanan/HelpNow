package com.helpnow.app.emergency

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.helpnow.app.managers.EmergencyManager
import com.helpnow.app.ui.theme.HelpNowTheme
import com.helpnow.app.utils.Constants
import com.helpnow.app.voice.VoiceListenerService
import kotlinx.coroutines.delay

class EmergencyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContent {
            HelpNowTheme {
                EmergencyCountdownScreen(
                    onCancel = {
                        // Restart the voice listener to ensure continuous protection
                        startService(Intent(this, VoiceListenerService::class.java))
                        finish()
                    },
                    onConfirm = {
                        val emergencyManager = EmergencyManager(this@EmergencyActivity, lifecycleScope)
                        emergencyManager.triggerEmergency()
                        // Stop the service as emergency is confirmed
                        stopService(Intent(this, VoiceListenerService::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun EmergencyCountdownScreen(onCancel: () -> Unit, onConfirm: () -> Unit) {
    var countdown by remember { mutableIntStateOf(Constants.EMERGENCY_COUNTDOWN_SECONDS) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        onConfirm()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "SOS Activated",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sending alert in",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$countdown",
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape,
                modifier = Modifier.size(150.dp)
            ) {
                Text(
                    text = "CANCEL SOS",
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
