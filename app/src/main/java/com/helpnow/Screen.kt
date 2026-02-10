package com.helpnow

sealed interface Screen {
    val route: String
}

data object Login : Screen {
    override val route = "login"
}
data object PhoneVerification : Screen {
    override val route = "phone_verification"
}
data object OTPVerification : Screen {
    override val route = "otp_verification"
}
data object UserProfile : Screen {
    override val route = "user_profile"
}
data object EmergencyContacts : Screen {
    override val route = "emergency_contacts"
}
data object DangerPhraseConfig : Screen {
    override val route = "danger_phrase_config"
}
data object VoiceRegistration : Screen {
    override val route = "voice_registration"
}
data object Permissions : Screen {
    override val route = "permissions"
}
data object EmergencyHome : Screen {
    override val route = "emergency_home"
}
data object ContactsTab : Screen {
    override val route = "contacts_tab"
}
data object SettingsTab : Screen {
    override val route = "settings_tab"
}
data object Map : Screen {
    override val route = "map"
}
