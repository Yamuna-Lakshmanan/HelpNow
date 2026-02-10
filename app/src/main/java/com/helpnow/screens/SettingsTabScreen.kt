package com.helpnow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.helpnow.R
import com.helpnow.utils.SharedPreferencesManager

@Composable
fun SettingsTabScreen(
    onBackClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    isEnglish: Boolean,
    onLogout: () -> Unit,
    onCheckInHistoryClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsManager = remember { SharedPreferencesManager.getInstance(context) }
    val userData = remember { prefsManager.getUserData() }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = colorResource(id = R.color.primary)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.settings),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.white)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.profile),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text_primary)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = userData?.userName ?: "",
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.text_primary)
                            )
                            Text(
                                text = userData?.userPhone ?: "",
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.text_secondary)
                            )
                        }
                        
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(id = R.string.edit_profile),
                                tint = colorResource(id = R.color.primary)
                            )
                        }
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.preferences),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text_primary)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.language),
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.text_primary)
                        )
                        Text(
                            text = if (isEnglish) stringResource(id = R.string.language_eng) else stringResource(id = R.string.language_tamil),
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.primary),
                            modifier = Modifier.clickable { onLanguageToggle() }
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_info),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text_primary)
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.History,
                        title = stringResource(id = R.string.check_in_history),
                        onClick = onCheckInHistoryClick
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(id = R.string.about_helpnow),
                        onClick = { }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = stringResource(id = R.string.privacy_policy),
                        onClick = { }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = stringResource(id = R.string.terms_conditions),
                        onClick = { }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Support,
                        title = stringResource(id = R.string.support_feedback),
                        onClick = { }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.error)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.logout),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.logout),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(id = R.string.logout_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefsManager.clearAllData()
                        onLogout()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.logout),
                        color = colorResource(id = R.color.error)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorResource(id = R.color.primary),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = colorResource(id = R.color.text_primary)
        )
    }
}
