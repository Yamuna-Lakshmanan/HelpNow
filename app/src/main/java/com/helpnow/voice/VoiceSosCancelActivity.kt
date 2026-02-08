package com.helpnow.voice

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helpnow.R
import com.helpnow.utils.Constants
import com.helpnow.utils.SharedPreferencesManager

/**
 * Full-screen overlay for 5-second cancel window when voice SOS is detected.
 * Cannot be dismissed with back button.
 */
class VoiceSosCancelActivity : ComponentActivity() {

    companion object {
        private const val TAG = "VoiceSosCancelActivity"
        const val EXTRA_PHRASE = "phrase"
        const val EXTRA_CONFIDENCE = "confidence"
    }

    private var countDownTimer: CountDownTimer? = null
    private var phrase = ""
    private var confidence = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
        phrase = intent.getStringExtra(EXTRA_PHRASE) ?: ""
        confidence = intent.getFloatExtra(EXTRA_CONFIDENCE, 0.8f)
        setContent {
            VoiceSosCancelScreen(
                onCancelTap = { handleCancel() },
                onTimeout = { handleTimeout() }
            )
        }
    }


    private fun handleCancel() {
        try {
            countDownTimer?.cancel()
            SharedPreferencesManager.getInstance(this).incrementFalseAlarmsToday()
            Log.i(TAG, "False alarm - user cancelled")
            VoiceListenerService.start(this)
        } catch (e: Exception) {
            Log.e(TAG, "handleCancel error", e)
        }
        finish()
    }

    private fun handleTimeout() {
        try {
            countDownTimer?.cancel()
            EmergencyTriggerManager(this).triggerEmergency(phrase, confidence)
        } catch (e: Exception) {
            Log.e(TAG, "handleTimeout error", e)
        }
        finish()
    }

    @Composable
    fun VoiceSosCancelScreen(
        onCancelTap: () -> Unit,
        onTimeout: () -> Unit
    ) {
        var countdown by remember { mutableStateOf(5) }
        var timerStarted by remember { mutableStateOf(false) }
        BackHandler(enabled = true) { }

        if (!timerStarted) {
            timerStarted = true
            countDownTimer = object : CountDownTimer(Constants.VOICE_CANCEL_TIMEOUT_MS, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    countdown = (millisUntilFinished / 1000).toInt()
                }
                override fun onFinish() {
                    onTimeout()
                }
            }.start()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF44336))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.voice_sos_detected),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "$countdown...",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (countdown > 2) Color(0xFFFFEB3B) else Color.White
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = onCancelTap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel_sos),
                        color = Color(0xFFF44336),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
