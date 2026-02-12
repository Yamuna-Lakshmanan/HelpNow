package com.helpnow.app.voice

import android.util.Log
import com.helpnow.app.utils.Constants
import kotlin.math.log10
import kotlin.math.max

/**
 * Manages audio buffer and silence detection for battery optimization.
 * 30 seconds of silence -> pause 10 seconds -> resume.
 */
class AudioBufferManager(
    private val onSilenceDetected: () -> Unit,
    private val onResumeListening: () -> Unit
) {
    companion object {
        private const val TAG = "AudioBufferManager"
        private const val RMS_SILENCE_THRESHOLD = 0.001f
    }

    private var silenceStartTime: Long = 0
    private var isInSilencePeriod = false
    private val silenceDurationMs = Constants.VOICE_SILENCE_DURATION_MS
    private val pauseDurationMs = Constants.VOICE_PAUSE_DURATION_MS

    /**
     * Process audio buffer - returns true if silence threshold exceeded for 30 sec.
     */
    fun processBuffer(samples: ShortArray): Boolean {
        return try {
            val rms = computeRms(samples)
            val db = 20 * log10(max(rms, 0.0001f))
            if (db < Constants.VOICE_SILENCE_THRESHOLD_DB) {
                if (!isInSilencePeriod) {
                    silenceStartTime = System.currentTimeMillis()
                    isInSilencePeriod = true
                }
                val elapsed = System.currentTimeMillis() - silenceStartTime
                if (elapsed >= silenceDurationMs) {
                    isInSilencePeriod = false
                    onSilenceDetected()
                    true
                } else false
            } else {
                isInSilencePeriod = false
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "processBuffer error", e)
            false
        }
    }

    fun computeRms(samples: ShortArray): Float {
        if (samples.isEmpty()) return 0f
        var sum = 0.0
        for (s in samples) {
            val n = s / 32768.0
            sum += n * n
        }
        return kotlin.math.sqrt(sum / samples.size).toFloat()
    }

    fun scheduleResume(callback: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            onResumeListening()
            callback()
        }, pauseDurationMs)
    }

    fun reset() {
        isInSilencePeriod = false
    }
}
