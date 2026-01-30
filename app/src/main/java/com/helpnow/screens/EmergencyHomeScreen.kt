package com.helpnow.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.helpnow.R
import com.helpnow.utils.Constants
import kotlinx.coroutines.delay

@Composable
fun EmergencyHomeScreen(
    contactCount: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSOSClick: () -> Unit,
    onVoiceHelpClick: () -> Unit,
    onContactsClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    isEnglish: Boolean,
    onLogout: () -> Unit
) {
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(Constants.EMERGENCY_COUNTDOWN_SECONDS) }
    var countdownActive by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Constants.SOS_PULSE_DURATION,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )
    
    LaunchedEffect(countdownActive) {
        if (countdownActive && countdown > 0) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            if (countdown == 0 && showEmergencyDialog) {
                onSOSClick()
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.primary),
                            colorResource(id = R.color.primary_dark)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.login_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        stringResource(id = R.string.tab_emergency),
                        stringResource(id = R.string.tab_contacts),
                        stringResource(id = R.string.tab_settings)
                    ).forEachIndexed { index, tabName ->
                        Text(
                            text = tabName,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.white),
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clickable { onTabSelected(index) }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
        
        when (selectedTab) {
            0 -> EmergencyTabContent(
                contactCount = contactCount,
                scale = scale,
                onSOSClick = {
                    showEmergencyDialog = true
                    countdown = Constants.EMERGENCY_COUNTDOWN_SECONDS
                    countdownActive = true
                },
                onVoiceHelpClick = onVoiceHelpClick,
                onContactsClick = onContactsClick,
                onMapClick = onMapClick,
                onSettingsClick = onSettingsClick,
                onLanguageToggle = onLanguageToggle,
                isEnglish = isEnglish
            )
            1 -> ContactsTabScreen(
                onBackClick = { onTabSelected(0) }
            )
            2 -> SettingsTabScreen(
                onBackClick = { onTabSelected(0) },
                onLanguageToggle = onLanguageToggle,
                isEnglish = isEnglish,
                onLogout = onLogout
            )
        }
        
        if (showEmergencyDialog) {
            EmergencyActivationDialog(
                countdown = countdown,
                onCancel = {
                    showEmergencyDialog = false
                    countdownActive = false
                    countdown = Constants.EMERGENCY_COUNTDOWN_SECONDS
                },
                onActivate = {
                    onSOSClick()
                    showEmergencyDialog = false
                    countdownActive = false
                }
            )
        }
    }
}

@Composable
fun EmergencyTabContent(
    contactCount: Int,
    scale: Float,
    onSOSClick: () -> Unit,
    onVoiceHelpClick: () -> Unit,
    onContactsClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    isEnglish: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "$contactCount ${stringResource(id = R.string.emergency_contacts_registered)}",
            fontSize = 14.sp,
            color = colorResource(id = R.color.success),
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onSOSClick,
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.sos_red)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.emergency),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.white)
            )
        }
        
        Button(
            onClick = onVoiceHelpClick,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.secondary)
            )
        ) {
            Text(
                text = stringResource(id = R.string.voice_help),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.white)
            )
        }
        
        Text(
            text = stringResource(id = R.string.emergency_instruction),
            fontSize = 14.sp,
            color = colorResource(id = R.color.text_secondary),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = colorResource(id = R.color.primary),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onContactsClick) {
                Icon(
                    imageVector = Icons.Default.Contacts,
                    contentDescription = stringResource(id = R.string.tab_contacts),
                    tint = colorResource(id = R.color.white),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onMapClick) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = stringResource(id = R.string.your_live_location),
                    tint = colorResource(id = R.color.white),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.tab_settings),
                    tint = colorResource(id = R.color.white),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Text(
            text = if (isEnglish) stringResource(id = R.string.language_eng) else stringResource(id = R.string.language_tamil),
            fontSize = 12.sp,
            color = colorResource(id = R.color.primary),
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onLanguageToggle() }
        )
    }
}

@Composable
fun EmergencyActivationDialog(
    countdown: Int,
    onCancel: () -> Unit,
    onActivate: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.activate_emergency_mode),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.primary)
                )
                
                Text(
                    text = stringResource(id = R.string.emergency_alert_description),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.text_secondary),
                    textAlign = TextAlign.Center
                )
                
                if (countdown > 0) {
                    Text(
                        text = "$countdown...",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.error)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(id = R.color.gray)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    
                    Button(
                        onClick = onActivate,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.error)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.activate),
                            color = colorResource(id = R.color.white)
                        )
                    }
                }
            }
        }
    }
}
