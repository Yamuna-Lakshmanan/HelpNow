package com.helpnow.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.helpnow.app.screens.*
import com.helpnow.app.viewmodel.TrackMeViewModel
import com.helpnow.app.utils.Constants
import com.helpnow.app.utils.SharedPreferencesManager
import com.helpnow.ui.CheckInHistoryScreen

object Routes {
    const val LOGIN = "login_screen"
    const val PHONE_VERIFICATION = "phone_verification_screen"
    const val OTP_VERIFICATION = "otp_verification_screen"
    const val USER_PROFILE = "user_profile_screen"
    const val EMERGENCY_CONTACTS = "emergency_contacts_screen"
    const val DANGER_PHRASE_CONFIG = "danger_phrase_config_screen"
    const val VOICE_REGISTRATION = "voice_registration_screen"
    const val PERMISSIONS = "permissions_screen"
    const val EMERGENCY_HOME = "emergency_home_screen"
    const val CHECK_IN_HISTORY = "check_in_history"
    const val MAP = "map_screen"
    const val SETTINGS = "settings_screen"
}

@Composable
fun HelpNowApp(
    navController: NavHostController,
    prefsManager: SharedPreferencesManager,
    startDestinationOverride: String? = null,
    onInitializeVoiceListener: () -> Unit
) {
    var isEnglish by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var userProfileData by remember { mutableStateOf<Map<String, String>?>(null) }
    var emergencyContacts by remember { mutableStateOf<List<com.helpnow.app.models.EmergencyContact>>(emptyList()) }

    NavHost(
        navController = navController,
        startDestination = startDestinationOverride
            ?: if (prefsManager.isUserLoggedIn()) Routes.EMERGENCY_HOME else Routes.LOGIN,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            fadeIn(animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)) +
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)) +
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)
                    )
        }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignInClick = {
                    navController.navigate(Routes.PHONE_VERIFICATION)
                },
                onSignUpClick = {
                    navController.navigate(Routes.PHONE_VERIFICATION)
                },
                onLanguageToggle = { isEnglish = !isEnglish },
                isEnglish = isEnglish
            )
        }

        composable(Routes.PHONE_VERIFICATION) {
            PhoneVerificationScreen(
                onVerifyClick = { phone ->
                    phoneNumber = phone
                    navController.navigate(Routes.OTP_VERIFICATION)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.OTP_VERIFICATION) {
            OTPVerificationScreen(
                phoneNumber = phoneNumber,
                onVerifyClick = {
                    navController.navigate(Routes.USER_PROFILE)
                },
                onBackClick = { navController.popBackStack() },
                onResendClick = { }
            )
        }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                onNextClick = { name, gender, dob, address, city ->
                    userProfileData = mapOf(
                        "name" to name,
                        "gender" to gender,
                        "dob" to dob,
                        "address" to address,
                        "city" to city
                    )
                    navController.navigate(Routes.EMERGENCY_CONTACTS)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.EMERGENCY_CONTACTS) {
            EmergencyContactsScreen(
                onNextClick = { contacts ->
                    emergencyContacts = contacts
                    navController.navigate(Routes.DANGER_PHRASE_CONFIG)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.DANGER_PHRASE_CONFIG) {
            DangerPhraseConfigScreen(
                onNextClick = { navController.navigate(Routes.VOICE_REGISTRATION) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.VOICE_REGISTRATION) {
            VoiceRegistrationScreen(
                onNextClick = {
                    navController.navigate(Routes.PERMISSIONS)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                onContinueClick = {
                    userProfileData?.let { data ->
                        prefsManager.saveUserData(
                            userName = data["name"]!!,
                            userPhone = phoneNumber,
                            userGender = data["gender"]!!,
                            userDOB = data["dob"]!!,
                            userAddress = data["address"]!!,
                            userCity = data["city"]!!
                        )
                    }
                    prefsManager.saveEmergencyContacts(emergencyContacts)
                    prefsManager.setLoggedIn(true)
                    navController.navigate(Routes.EMERGENCY_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onInitializeVoiceListener = onInitializeVoiceListener
            )
        }

        composable(Routes.EMERGENCY_HOME) {
            EmergencyHomeScreen(
                navController = navController,
                prefsManager = prefsManager,
                onInitializeVoiceListener = onInitializeVoiceListener
            )
        }

        composable(Routes.SETTINGS) {
            SettingsTabScreen(
                onBackClick = { navController.popBackStack() },
                onLanguageToggle = { isEnglish = !isEnglish },
                isEnglish = isEnglish,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCheckInHistoryClick = { navController.navigate(Routes.CHECK_IN_HISTORY) }
            )
        }

        composable(Routes.CHECK_IN_HISTORY) {
            val vm: TrackMeViewModel = viewModel()
            CheckInHistoryScreen(checkIns = vm.getCheckInHistory())
        }

        composable(Routes.MAP) {
            MapScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
