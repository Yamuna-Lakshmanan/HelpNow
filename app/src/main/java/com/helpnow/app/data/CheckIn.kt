package com.helpnow.app.data

/**
 * Response type for a safety check-in.
 */
enum class CheckInResponse {
    YES,
    NO,
    TIMEOUT
}

/**
 * Data class representing a single check-in event during Track Me Home.
 */
data class CheckIn(
    val timestamp: Long,
    val response: CheckInResponse,
    val latitude: Double,
    val longitude: Double,
    val locationAddress: String?
)
