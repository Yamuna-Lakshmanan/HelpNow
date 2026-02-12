package com.helpnow.app.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.helpnow.app.R
import com.helpnow.app.Routes
import com.helpnow.app.managers.EmergencyManager
import com.helpnow.app.utils.Constants
import com.helpnow.app.utils.SharedPreferencesManager
import com.helpnow.app.viewmodel.TrackMeViewModel
import com.helpnow.app.viewmodel.VoiceGuardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EmergencyHomeScreen(
    navController: NavController,
    prefsManager: SharedPreferencesManager,
    onInitializeVoiceListener: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vm: TrackMeViewModel = viewModel()
    val emergencyManager = remember { EmergencyManager(context, scope) }
    var selectedTab by remember { mutableStateOf(0) }
    var isEnglish by remember { mutableStateOf(true) }

    var showEmergencyDialog by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(Constants.EMERGENCY_COUNTDOWN_SECONDS) }
    var countdownActive by remember { mutableStateOf(false) }

    val voiceGuardViewModel: VoiceGuardViewModel = viewModel()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Constants.SOS_PULSE_DURATION,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )

    LaunchedEffect(Unit) {
        vm.isTracking.collectLatest { isTracking ->
            if (isTracking) {
                Toast.makeText(context, "Tracking started", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(countdownActive) {
        if (countdownActive && countdown > 0) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            if (countdown == 0 && showEmergencyDialog) {
                emergencyManager.triggerEmergency()
                showEmergencyDialog = false
                countdownActive = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background))
    ) {
        EmergencyTabContent(
            contactCount = prefsManager.getEmergencyContacts().size,
            scale = scale,
            voiceGuardViewModel = voiceGuardViewModel,
            onSOSClick = {
                showEmergencyDialog = true
                countdown = Constants.EMERGENCY_COUNTDOWN_SECONDS
                countdownActive = true
            },
            onVoiceHelpClick = { /*TODO*/ },
            onContactsClick = { navController.navigate(Routes.EMERGENCY_CONTACTS) },
            onMapClick = { navController.navigate(Routes.MAP) },
            onSettingsClick = { navController.navigate(Routes.SETTINGS) },
            onLanguageToggle = { isEnglish = !isEnglish },
            isEnglish = isEnglish,
            vm = vm
        )
    }

    if (showEmergencyDialog) {
        Dialog(onDismissRequest = {
            showEmergencyDialog = false
            countdownActive = false
        }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EMERGENCY",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.sos_red)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Triggering in $countdown seconds",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showEmergencyDialog = false
                            countdownActive = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("CANCEL")
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyTabContent(
    contactCount: Int,
    scale: Float,
    voiceGuardViewModel: VoiceGuardViewModel,
    onSOSClick: () -> Unit,
    onVoiceHelpClick: () -> Unit,
    onContactsClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    isEnglish: Boolean,
    vm: TrackMeViewModel
) {
    val isTracking by vm.isTracking.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(id = R.string.settings),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onSettingsClick() }
            )
            LanguageToggleButton(
                isEnglish = isEnglish,
                onLanguageToggle = onLanguageToggle
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorResource(id = R.color.sos_gradient_center),
                            colorResource(id = R.color.sos_gradient_edge)
                        )
                    ),
                    shape = RoundedCornerShape(100.dp)
                )
                .clickable { onSOSClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.sos),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.emergency_sos_button),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FeatureButton(
                icon = Icons.Default.People,
                text = stringResource(id = R.string.contacts),
                onClick = onContactsClick
            )
            FeatureButton(
                icon = Icons.Default.Map,
                text = stringResource(id = R.string.map),
                onClick = onMapClick
            )
            FeatureButton(
                icon = Icons.Default.LocationOn,
                text = if (isTracking) "Stop Track" else "Track Me",
                onClick = {
                    if (isTracking) vm.stopTracking() else vm.startTracking()
                },
                tint = if (isTracking) colorResource(id = R.color.error) else colorResource(id = R.color.primary)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        VoiceGuardStatus(
            viewModel = voiceGuardViewModel
        )
    }
}

@Composable
fun LanguageToggleButton(
    isEnglish: Boolean,
    onLanguageToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = colorResource(id = R.color.primary),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onLanguageToggle() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isEnglish) "EN" else "HI",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun FeatureButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = colorResource(id = R.color.primary)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(48.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}
