package com.helpnow.app.voice.models

/**
 * Represents a voice detection event.
 */
data class VoiceDetection(
    val timestamp: Long,
    val phrase: String,
    val confidence: Float,
    val voiceMatch: Boolean
)
