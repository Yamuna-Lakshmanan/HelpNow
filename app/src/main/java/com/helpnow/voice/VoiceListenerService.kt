package com.helpnow.voice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.helpnow.MainActivity
import com.helpnow.R
import com.helpnow.utils.Constants
import com.helpnow.utils.SharedPreferencesManager
import com.helpnow.voice.models.VoiceServiceState

/**
 * Foreground service for 24/7 background voice listening.
 * Detects [custom phrase] + [digit 0-9]. Digit REQUIRED.
 */
class VoiceListenerService : LifecycleService() {

    companion object {
        private const val TAG = "VoiceListenerService"
        private const val CHANNEL_ID = "voice_guard_channel"
        private const val NOTIFICATION_ID = 1337
        const val ACTION_STOP = "com.helpnow.STOP_VOICE_SERVICE"

        fun start(context: android.content.Context) {
            try {
                val intent = Intent(context, VoiceListenerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "start failed", e)
            }
        }

        fun stop(context: android.content.Context) {
            try {
                context.stopService(Intent(context, VoiceListenerService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "stop failed", e)
            }
        }
    }

    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var emergencyTriggerManager: EmergencyTriggerManager
    private lateinit var voiceprintMatcher: VoiceprintMatcher
    private var isPaused = false
    private var isRestarting = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        prefsManager = SharedPreferencesManager.getInstance(this)
        emergencyTriggerManager = EmergencyTriggerManager(this)
        voiceprintMatcher = VoiceprintMatcher()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP -> {
                stopListening()
                stopSelf()
                return START_NOT_STICKY
            }
        }
        startForegroundWithNotification()
        if (!isPaused) startListening()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    private fun startForegroundWithNotification() {
        createNotificationChannel()
        val stopIntent = Intent(this, VoiceListenerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.voice_guard_active))
            .setContentText(getString(R.string.listening_for_phrase))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_delete, getString(R.string.stop_listening), stopPendingIntent)
            .setColor(getColor(R.color.error))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.voice_guard_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun startListening() {
        if (!prefsManager.isVoiceGuardEnabled()) return
        try {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "RECORD_AUDIO not granted")
                prefsManager.setVoiceServiceStatus(VoiceServiceState.STOPPED.name.lowercase())
                stopSelf()
                return
            }
            prefsManager.setVoiceServiceStatus(VoiceServiceState.LISTENING.name.lowercase())
            if (!::speechRecognizer.isInitialized || !SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                speechRecognizer.setRecognitionListener(createRecognitionListener())
            }
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            }
            speechRecognizer.startListening(recognizerIntent)
            Log.i(TAG, "Ready for speech - listening for danger phrase + digit")
        } catch (e: Exception) {
            Log.e(TAG, "startListening error", e)
            scheduleRestart()
        }
    }

    private fun scheduleRestart() {
        if (isRestarting) return
        isRestarting = true
        mainHandler.postDelayed({
            isRestarting = false
            if (!isPaused && prefsManager.isVoiceGuardEnabled()) startListening()
        }, 2000)
    }

    private fun createRecognitionListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { if (!isPaused) scheduleRestart() }
        override fun onError(error: Int) {
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                Log.w(TAG, "SpeechRecognizer error: $error")
            }
            if (!isPaused) scheduleRestart()
        }
        override fun onResults(results: android.os.Bundle?) {
            try {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
                val confidence = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.getOrNull(0) ?: 0.8f
                for (text in matches) {
                    if (checkDangerPhrase(text, confidence)) {
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onResults error", e)
            }
            if (!isPaused) scheduleRestart()
        }
        override fun onPartialResults(partialResults: android.os.Bundle?) {
            try {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
                for (text in matches) {
                    if (checkDangerPhrase(text, Constants.VOICE_CONFIDENCE_THRESHOLD)) {
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onPartialResults error", e)
            }
        }
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    }

    private fun checkDangerPhrase(recognizedText: String, speechConfidence: Float): Boolean {
        try {
            val customPhrase = prefsManager.getCustomDangerPhrase()
            val normalized = recognizedText.lowercase().replace("'", "").replace(" ", "").trim()
            val phraseNorm = customPhrase.lowercase().replace("'", "").trim()
            val phraseNoSpaces = phraseNorm.replace(" ", "")
            val hasDigit = recognizedText.any { it.isDigit() } ||
                recognizedText.lowercase().contains("zero") || recognizedText.lowercase().contains("one") ||
                recognizedText.lowercase().contains("two") || recognizedText.lowercase().contains("three") ||
                recognizedText.lowercase().contains("four") || recognizedText.lowercase().contains("five") ||
                recognizedText.lowercase().contains("six") || recognizedText.lowercase().contains("seven") ||
                recognizedText.lowercase().contains("eight") || recognizedText.lowercase().contains("nine")
            if (!hasDigit) return false
            if (!normalized.contains(phraseNoSpaces) && !recognizedText.lowercase().contains(phraseNorm)) return false
            val voiceprint = prefsManager.getUserVoiceprint()
            val voiceConfidence = if (voiceprint.isNullOrBlank()) 1f else voiceprintMatcher.matchVoice(voiceprintMatcher.extractFeaturesFromText(recognizedText), voiceprint)
            if (voiceConfidence >= Constants.VOICE_CONFIDENCE_THRESHOLD && speechConfidence >= Constants.VOICE_CONFIDENCE_THRESHOLD) {
                Log.w(TAG, "⚠️ DANGER DETECTED: $recognizedText (confidence: $voiceConfidence)")
                onPhraseDetected(recognizedText, voiceConfidence)
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkDangerPhrase error", e)
        }
        return false
    }

    private fun onPhraseDetected(phrase: String, confidence: Float) {
        try {
            if (::speechRecognizer.isInitialized) speechRecognizer.stopListening()
            prefsManager.setLastDetectionTime(System.currentTimeMillis())
            val intent = Intent(this, VoiceSosCancelActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_HISTORY)
                putExtra(VoiceSosCancelActivity.EXTRA_PHRASE, phrase)
                putExtra(VoiceSosCancelActivity.EXTRA_CONFIDENCE, confidence)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "onPhraseDetected error", e)
            scheduleRestart()
        }
    }

    private fun stopListening() {
        isPaused = true
        try {
            if (::speechRecognizer.isInitialized) speechRecognizer.destroy()
            prefsManager.setVoiceServiceStatus(VoiceServiceState.STOPPED.name.lowercase())
        } catch (e: Exception) {
            Log.e(TAG, "stopListening error", e)
        }
    }

    override fun onDestroy() {
        stopListening()
        super.onDestroy()
    }
}
