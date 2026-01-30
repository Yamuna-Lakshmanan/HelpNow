package com.helpnow

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.helpnow.screens.*
import com.helpnow.utils.Constants
import com.helpnow.utils.SharedPreferencesManager

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PhoneVerification : Screen("phone_verification")
    object OTPVerification : Screen("otp_verification")
    object UserProfile : Screen("user_profile")
    object EmergencyContacts : Screen("emergency_contacts")
    object Permissions : Screen("permissions")
    object EmergencyHome : Screen("emergency_home")
    object ContactsTab : Screen("contacts_tab")
    object SettingsTab : Screen("settings_tab")
    object Map : Screen("map")
}

@Composable
fun HelpNowApp(
    navController: NavHostController,
    prefsManager: SharedPreferencesManager,
    onInitializeVoiceListener: () -> Unit
) {
    var isEnglish by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var phoneNumber by remember { mutableStateOf("") }
    var userProfileData by remember { mutableStateOf<Map<String, String>?>(null) }
    var emergencyContacts by remember { mutableStateOf<List<com.helpnow.models.EmergencyContact>>(emptyList()) }
    
    val contactCount = remember { prefsManager.getEmergencyContacts().size }
    
    NavHost(
        navController = navController,
        startDestination = if (prefsManager.isUserLoggedIn()) Screen.EmergencyHome.route else Screen.Login.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            fadeIn(animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)) +
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)) +
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(Constants.ANIMATION_DURATION_MEDIUM)
            )
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onSignInClick = {
                    navController.navigate(Screen.PhoneVerification.route)
                },
                onSignUpClick = {
                    navController.navigate(Screen.PhoneVerification.route)
                },
                onLanguageToggle = { isEnglish = !isEnglish },
                isEnglish = isEnglish
            )
        }
        
        composable(Screen.PhoneVerification.route) {
            PhoneVerificationScreen(
                onVerifyClick = { phone ->
                    phoneNumber = phone
                    navController.navigate(Screen.OTPVerification.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.OTPVerification.route) {
            OTPVerificationScreen(
                phoneNumber = phoneNumber,
                onVerifyClick = {
                    navController.navigate(Screen.UserProfile.route)
                },
                onBackClick = { navController.popBackStack() },
                onResendClick = { }
            )
        }
        
        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onNextClick = { name, gender, dob, address, city ->
                    userProfileData = mapOf(
                        "name" to name,
                        "gender" to gender,
                        "dob" to dob,
                        "address" to address,
                        "city" to city
                    )
                    navController.navigate(Screen.EmergencyContacts.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.EmergencyContacts.route) {
            EmergencyContactsScreen(
                onNextClick = { contacts ->
                    emergencyContacts = contacts
                    navController.navigate(Screen.Permissions.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Permissions.route) {
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
                    navController.navigate(Screen.EmergencyHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onInitializeVoiceListener = onInitializeVoiceListener
            )
        }
        
        composable(Screen.EmergencyHome.route) {
            EmergencyHomeScreen(
                contactCount = prefsManager.getEmergencyContacts().size,
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                },
                onSOSClick = {
                },
                onVoiceHelpClick = {
                    onInitializeVoiceListener()
                },
                onContactsClick = {
                    selectedTab = 1
                },
                onMapClick = {
                    navController.navigate(Screen.Map.route)
                },
                onSettingsClick = {
                    selectedTab = 2
                },
                onLanguageToggle = { isEnglish = !isEnglish },
                isEnglish = isEnglish,
                onLogout = {
                    prefsManager.clearAllData()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.EmergencyHome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ContactsTab.route) {
            ContactsTabScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.SettingsTab.route) {
            SettingsTabScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLanguageToggle = { isEnglish = !isEnglish },
                isEnglish = isEnglish,
                onLogout = {
                    prefsManager.clearAllData()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.EmergencyHome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Map.route) {
            MapScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
