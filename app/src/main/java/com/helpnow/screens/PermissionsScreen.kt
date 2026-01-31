package com.helpnow.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.helpnow.R
import com.helpnow.utils.PermissionUtils
import com.helpnow.utils.SharedPreferencesManager

@Composable
fun PermissionsScreen(
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    onInitializeVoiceListener: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { SharedPreferencesManager.getInstance(context) }
    
    var locationGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var microphoneGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var smsGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var callGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationGranted = isGranted
        prefsManager.savePermissionStatus(Manifest.permission.ACCESS_FINE_LOCATION, isGranted)
    }
    
    val microphoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        microphoneGranted = isGranted
        prefsManager.savePermissionStatus(Manifest.permission.RECORD_AUDIO, isGranted)
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraGranted = isGranted
        prefsManager.savePermissionStatus(Manifest.permission.CAMERA, isGranted)
    }
    
    val smsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        smsGranted = isGranted
        prefsManager.savePermissionStatus(Manifest.permission.SEND_SMS, isGranted)
    }
    
    val callLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        callGranted = isGranted
        prefsManager.savePermissionStatus(Manifest.permission.CALL_PHONE, isGranted)
    }
    
    val canContinue = locationGranted && microphoneGranted && callGranted

    LaunchedEffect(canContinue) {
        if (canContinue) {
            if (locationGranted && microphoneGranted) {
                onInitializeVoiceListener()
            }
            onContinueClick()
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
                    text = stringResource(id = R.string.step_4_of_4),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.app_permissions_required),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.permissions_description),
                fontSize = 14.sp,
                color = colorResource(id = R.color.text_secondary)
            )
            
            PermissionCard(
                icon = Icons.Default.LocationOn,
                title = stringResource(id = R.string.location_permission),
                description = stringResource(id = R.string.location_description),
                isRequired = true,
                isGranted = locationGranted,
                onToggle = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            )
            
            PermissionCard(
                icon = Icons.Default.Mic,
                title = stringResource(id = R.string.microphone_permission),
                description = stringResource(id = R.string.microphone_description),
                isRequired = true,
                isGranted = microphoneGranted,
                onToggle = { microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )
            
            PermissionCard(
                icon = Icons.Default.CameraAlt,
                title = stringResource(id = R.string.camera_permission),
                description = stringResource(id = R.string.camera_description),
                isRequired = false,
                isGranted = cameraGranted,
                onToggle = { cameraLauncher.launch(Manifest.permission.CAMERA) }
            )
            
            PermissionCard(
                icon = Icons.Default.Sms,
                title = stringResource(id = R.string.sms_permission),
                description = stringResource(id = R.string.sms_description),
                isRequired = false,
                isGranted = smsGranted,
                onToggle = { smsLauncher.launch(Manifest.permission.SEND_SMS) }
            )
            
            PermissionCard(
                icon = Icons.Default.Call,
                title = stringResource(id = R.string.call_permission),
                description = stringResource(id = R.string.call_description),
                isRequired = true,
                isGranted = callGranted,
                onToggle = { callLauncher.launch(Manifest.permission.CALL_PHONE) }
            )
            
            if (canContinue) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.success)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colorResource(id = R.color.white)
                        )
                        Text(
                            text = stringResource(id = R.string.permissions_enabled),
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.white),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                 Text(
                    text = stringResource(id = R.string.grant_critical_permissions),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.gray),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isRequired: Boolean,
    isGranted: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.white)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = colorResource(id = R.color.primary).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorResource(id = R.color.primary),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text_primary)
                    )
                    
                    Surface(
                        color = if (isRequired) colorResource(id = R.color.error) else colorResource(id = R.color.gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isRequired) stringResource(id = R.string.required) else stringResource(id = R.string.optional),
                            fontSize = 10.sp,
                            color = colorResource(id = R.color.white),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
            
            Switch(
                checked = isGranted,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorResource(id = R.color.white),
                    checkedTrackColor = colorResource(id = R.color.primary)
                )
            )
        }
    }
}
