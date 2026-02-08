package com.helpnow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.helpnow.R
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight

/**
 * UI model for a permission explanation card.
 * This screen is for explanation only; no runtime permission requests are triggered.
 */
data class PermissionUiItem(
    val titleResId: Int,
    val descriptionResId: Int,
    val icon: ImageVector,
    val isRequired: Boolean
)

@Composable
fun PermissionsScreen(
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    onInitializeVoiceListener: () -> Unit
) {
    val permissionsList = listOf(
        PermissionUiItem(
            titleResId = R.string.location_permission,
            descriptionResId = R.string.location_description,
            icon = Icons.Default.LocationOn,
            isRequired = true
        ),
        PermissionUiItem(
            titleResId = R.string.microphone_permission,
            descriptionResId = R.string.microphone_description,
            icon = Icons.Default.Mic,
            isRequired = true
        ),
        PermissionUiItem(
            titleResId = R.string.call_permission,
            descriptionResId = R.string.call_description,
            icon = Icons.Default.Call,
            isRequired = true
        ),
        PermissionUiItem(
            titleResId = R.string.sms_permission,
            descriptionResId = R.string.sms_description,
            icon = Icons.Default.Sms,
            isRequired = true
        ),
        PermissionUiItem(
            titleResId = R.string.notification_permission,
            descriptionResId = R.string.notification_description,
            icon = Icons.Default.Notifications,
            isRequired = true
        )
    )

    // Auto-navigate to Home after a short delay; clear this screen from back stack
    LaunchedEffect(Unit) {
        delay(1500L)
        onInitializeVoiceListener()
        onContinueClick()
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
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.step_4_of_4),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(id = R.string.app_permissions_required),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.permissions_description),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.text_secondary),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(permissionsList, key = { it.titleResId }) { item ->
                PermissionCard(
                    icon = item.icon,
                    title = stringResource(id = item.titleResId),
                    description = stringResource(id = item.descriptionResId),
                    isRequired = item.isRequired
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
    isRequired: Boolean
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        modifier = Modifier.wrapContentWidth(),
                        color = if (isRequired) colorResource(id = R.color.error) else colorResource(id = R.color.gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isRequired) stringResource(id = R.string.required) else stringResource(id = R.string.optional),
                            fontSize = 10.sp,
                            color = colorResource(id = R.color.white),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
        }
    }
}
