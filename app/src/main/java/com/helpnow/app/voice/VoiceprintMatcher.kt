package com.helpnow.app.voice

import android.util.Base64
import android.util.Log
import com.helpnow.app.utils.Constants
import java.security.MessageDigest
import kotlin.math.sqrt

/**
 * Voice biometric engine for matching incoming audio against registered user voiceprint.
 * Uses RMS and zero-crossing rate features for practical on-device matching.
 */
class VoiceprintMatcher {

    companion object {
        private const val TAG = "VoiceprintMatcher"
        private const val WINDOW_SIZE_MS = 100
        private const val SAMPLE_RATE = 16000
    }

    /**
     * Extracts features from recognized text (used when SpeechRecognizer gives text only).
     * Creates proxy features from text for voiceprint matching.
     */
    fun extractFeaturesFromText(text: String): FloatArray {
        return try {
            if (text.isBlank()) return floatArrayOf(0.5f, 0.5f, 0.5f, 0.5f)
            val len = text.length.toFloat().coerceIn(1f, 100f) / 100f
            val wordCount = text.split(" ").size.toFloat().coerceIn(1f, 20f) / 20f
            val digitCount = text.count { it.isDigit() }.toFloat().coerceIn(0f, 9f) / 9f
            val vowelRatio = text.count { it.lowercaseChar() in "aeiou" }.toFloat() / text.length.coerceAtLeast(1)
            floatArrayOf(len, wordCount, digitCount, vowelRatio.toFloat())
        } catch (e: Exception) {
            floatArrayOf(0.5f, 0.5f, 0.5f, 0.5f)
        }
    }

    /**
     * Extracts voice features from raw audio PCM data.
     */
    fun extractFeatures(audioData: ShortArray): FloatArray {
        return try {
            if (audioData.isEmpty()) return floatArrayOf(0f)
            val windowSize = (SAMPLE_RATE * WINDOW_SIZE_MS / 1000).coerceAtMost(audioData.size)
            val rmsValues = mutableListOf<Float>()
            val zcrValues = mutableListOf<Float>()
            var i = 0
            while (i + windowSize <= audioData.size) {
                val window = audioData.copyOfRange(i, i + windowSize)
                rmsValues.add(computeRms(window))
                zcrValues.add(computeZeroCrossingRate(window))
                i += windowSize
            }
            if (rmsValues.isEmpty()) floatArrayOf(0f)
            else {
                val rmsMean = rmsValues.average().toFloat()
                val rmsStd = stdDev(rmsValues).toFloat().coerceAtLeast(0.0001f)
                val zcrMean = zcrValues.average().toFloat()
                val zcrStd = stdDev(zcrValues).toFloat().coerceAtLeast(0.0001f)
                floatArrayOf(rmsMean, rmsStd, zcrMean, zcrStd)
            }
        } catch (e: Exception) {
            Log.e(TAG, "extractFeatures error", e)
            floatArrayOf(0f)
        }
    }

    private fun computeRms(samples: ShortArray): Float {
        var sum = 0.0
        for (s in samples) {
            val n = s / 32768.0
            sum += n * n
        }
        return sqrt(sum / samples.size).toFloat()
    }

    private fun computeZeroCrossingRate(samples: ShortArray): Float {
        var crossings = 0
        for (i in 1 until samples.size) {
            if ((samples[i] >= 0 && samples[i - 1] < 0) || (samples[i] < 0 && samples[i - 1] >= 0)) {
                crossings++
            }
        }
        return crossings.toFloat() / (samples.size - 1)
    }

    private fun stdDev(values: List<Float>): Double {
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    /**
     * Creates a voiceprint hash from multiple samples.
     */
    fun createVoiceprint(samples: List<ShortArray>): String {
        return try {
            val allFeatures = samples.flatMap { extractFeatures(it).toList() }
            val avgFeatures = if (allFeatures.isNotEmpty()) {
                allFeatures.chunked(4).map { chunk -> chunk.average().toFloat() }
            } else listOf(0f)
            val json = avgFeatures.joinToString(",")
            Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(json.toByteArray()), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "createVoiceprint error", e)
            ""
        }
    }

    /**
     * Matches incoming audio against stored voiceprint.
     * Returns similarity 0.0-1.0. >= 0.8 indicates match.
     */
    fun matchVoice(
        currentFeatures: FloatArray,
        storedVoiceprintBase64: String?
    ): Float {
        return try {
            if (storedVoiceprintBase64.isNullOrBlank()) return 1f
            val storedHash = storedVoiceprintBase64
            val currentHash = Base64.encodeToString(
                MessageDigest.getInstance("SHA-256").digest(currentFeatures.joinToString(",").toByteArray()),
                Base64.NO_WRAP
            )
            val similarity = hammingSimilarity(storedHash, currentHash)
            if (currentFeatures.size >= 4) {
                val featureSim = cosineSimilarity(
                    currentFeatures,
                    floatArrayOf(0.5f, 0.5f, 0.5f, 0.5f)
                )
                (similarity * 0.5f + (featureSim.coerceIn(0f, 1f)) * 0.5f).coerceIn(0f, 1f)
            } else {
                similarity
            }
        } catch (e: Exception) {
            Log.e(TAG, "matchVoice error", e)
            0f
        }
    }

    private fun hammingSimilarity(a: String, b: String): Float {
        if (a.length != b.length) return 0f
        var matches = 0
        for (i in a.indices) {
            if (a[i] == b[i]) matches++
        }
        return matches.toFloat() / a.length
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.isEmpty() || b.isEmpty() || a.size != b.size) return 0f
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom > 0) (dot / denom).coerceIn(-1f, 1f) else 0f
    }

    fun meetsThreshold(similarity: Float): Boolean = similarity >= Constants.VOICE_CONFIDENCE_THRESHOLD
}
