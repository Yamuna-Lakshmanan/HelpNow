package com.helpnow.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helpnow.app.R
import com.helpnow.app.viewmodel.VoiceGuardViewModel
import com.helpnow.app.voice.models.VoiceServiceState

@Composable
fun VoiceGuardStatus(viewModel: VoiceGuardViewModel) {
    val isEnabled by viewModel.isVoiceGuardEnabled.collectAsState(initial = true)
    val serviceStatus by viewModel.serviceStatus.collectAsState(initial = "stopped")
    val falseAlarms by viewModel.falseAlarmsToday.collectAsState(initial = 0)
    val isTestListening by viewModel.isTestListening.collectAsState(initial = false)
    val testResult by viewModel.testResult.collectAsState(initial = null)
    val testError by viewModel.testError.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.white)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = colorResource(id = R.color.primary),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(id = R.string.voice_guard_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.toggleVoiceGuard(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorResource(id = R.color.white),
                        checkedTrackColor = colorResource(id = R.color.success),
                        uncheckedThumbColor = colorResource(id = R.color.white),
                        uncheckedTrackColor = colorResource(id = R.color.light_gray)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (serviceStatus) {
                    VoiceServiceState.LISTENING.name.lowercase() -> stringResource(id = R.string.listening_actively)
                    VoiceServiceState.PAUSED.name.lowercase() -> stringResource(id = R.string.voice_guard_paused)
                    else -> stringResource(id = R.string.voice_guard_stopped)
                },
                fontSize = 14.sp,
                color = when (serviceStatus) {
                    VoiceServiceState.LISTENING.name.lowercase() -> colorResource(id = R.color.success)
                    VoiceServiceState.PAUSED.name.lowercase() -> colorResource(id = R.color.warning)
                    else -> colorResource(id = R.color.gray)
                }
            )
            if (falseAlarms > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.false_alarms_today, falseAlarms),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.TextButton(
                onClick = { viewModel.triggerTestPhrase() },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorResource(id = R.color.primary).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text = stringResource(id = R.string.say_test_phrase),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.primary)
                )
            }
            if (isTestListening) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Listening…",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
            testResult?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Heard: \"$it\"",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.text_primary)
                )
            }
            testError?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.error)
                )
            }
        }
    }
}
