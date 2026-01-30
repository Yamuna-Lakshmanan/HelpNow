package com.helpnow.utils

object Constants {
    const val MIN_CONTACTS_REQUIRED = 3
    const val MAX_CONTACTS_ALLOWED = 5
    const val PHONE_NUMBER_LENGTH = 10
    const val OTP_LENGTH = 4
    const val MIN_NAME_LENGTH = 3
    const val MIN_ADDRESS_LENGTH = 5
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 600
    const val SOS_PULSE_DURATION = 1000
    const val EMERGENCY_COUNTDOWN_SECONDS = 3
    const val OTP_RESEND_TIMER_SECONDS = 45
    
    val INDIAN_CITIES = listOf(
        "Delhi",
        "Mumbai",
        "Bangalore",
        "Chennai",
        "Hyderabad",
        "Kolkata",
        "Pune",
        "Ahmedabad",
        "Jaipur",
        "Lucknow",
        "Kanpur",
        "Nagpur",
        "Indore",
        "Thane",
        "Bhopal",
        "Visakhapatnam",
        "Patna",
        "Vadodara",
        "Ghaziabad",
        "Ludhiana",
        "Agra",
        "Nashik",
        "Faridabad",
        "Meerut",
        "Rajkot",
        "Varanasi",
        "Srinagar",
        "Aurangabad"
    )
    
    val VALID_OTP_CODES = listOf("1234", "0000", "9999")
    
    const val RELATIONSHIP_FRIEND = "Friend"
    const val RELATIONSHIP_FAMILY = "Family"
    const val RELATIONSHIP_OTHER = "Other"
    
    val RELATIONSHIPS = listOf(
        RELATIONSHIP_FRIEND,
        RELATIONSHIP_FAMILY,
        RELATIONSHIP_OTHER
    )
    
    const val GENDER_MALE = "Male"
    const val GENDER_FEMALE = "Female"
}
