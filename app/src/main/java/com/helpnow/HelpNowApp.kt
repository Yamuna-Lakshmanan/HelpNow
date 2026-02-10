package com.helpnow

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.helpnow.screens.*
import com.helpnow.trackme.TrackMeViewModel
import com.helpnow.ui.CheckInHistoryScreen
import com.helpnow.utils.Constants
import com.helpnow.utils.SharedPreferencesManager

@Composable
fun HelpNowApp(
    navController: NavHostController,
    prefsManager: SharedPreferencesManager,
    onInitializeVoiceListener: () -> Unit
) {
    var isEnglish by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var userProfileData by remember { mutableStateOf<kotlin.collections.Map<String, String>?>(null) }
    var emergencyContacts by remember { mutableStateOf<List<com.helpnow.models.EmergencyContact>>(emptyList()) }

    NavHost(
        navController = navController,
        startDestination = if (prefsManager.isUserLoggedIn()) EmergencyHome.route else Login.route,
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
        composable(Login.route) {
            LoginScreen(
                onSignInClick = {
                    navController.navigate(PhoneVerification.route)
                },
                onSignUpClick = {
                    navController.navigate(PhoneVerification.route)
                },
                onLanguageToggle = { isEnglish = !isEnglish },
                isEnglish = isEnglish
            )
        }

        composable(PhoneVerification.route) {
            PhoneVerificationScreen(
                onVerifyClick = { phone ->
                    phoneNumber = phone
                    navController.navigate(OTPVerification.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(OTPVerification.route) {
            OTPVerificationScreen(
                phoneNumber = phoneNumber,
                onVerifyClick = {
                    navController.navigate(UserProfile.route)
                },
                onBackClick = { navController.popBackStack() },
                onResendClick = { }
            )
        }

        composable(UserProfile.route) {
            UserProfileScreen(
                onNextClick = { name, gender, dob, address, city ->
                    userProfileData = mapOf(
                        "name" to name,
                        "gender" to gender,
                        "dob" to dob,
                        "address" to address,
                        "city" to city
                    )
                    navController.navigate(EmergencyContacts.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(EmergencyContacts.route) {
            EmergencyContactsScreen(
                onNextClick = { contacts ->
                    emergencyContacts = contacts
                    navController.navigate(DangerPhraseConfig.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(DangerPhraseConfig.route) {
            DangerPhraseConfigScreen(
                onNextClick = { navController.navigate(VoiceRegistration.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(VoiceRegistration.route) {
            VoiceRegistrationScreen(
                onNextClick = {
                    navController.navigate(Permissions.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Permissions.route) {
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
                    navController.navigate(EmergencyHome.route) {
                        popUpTo(Login.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onInitializeVoiceListener = onInitializeVoiceListener
            )
        }

        composable(EmergencyHome.route) {
            EmergencyHomeScreen(
                navController = navController,
                prefsManager = prefsManager,
                onInitializeVoiceListener = onInitializeVoiceListener
            )
        }

        composable("check_in_history") {
            val vm: TrackMeViewModel = viewModel()
            CheckInHistoryScreen(checkIns = vm.getCheckInHistory())
        }

        composable(Map.route) {
            MapScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
