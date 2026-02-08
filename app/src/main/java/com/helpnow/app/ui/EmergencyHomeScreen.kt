package com.helpnow.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpnow.app.R
import com.helpnow.app.trackme.TrackMeViewModel

@Composable
fun EmergencyHomeScreen(modifier: Modifier = Modifier, vm: TrackMeViewModel = viewModel()) {
    val isTracking by vm.isTracking.collectAsState()
    val checkInCount by vm.checkInCount.collectAsState()
    val checkInOverlayEvent by vm.showCheckInOverlay.collectAsState()
    val emergencyActive by vm.emergencyActive.collectAsState()
    val welcomeHome by vm.showWelcomeHome.collectAsState()

    var showStartConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = checkInOverlayEvent != null) {}

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TrackMeButton(
                    isTracking = isTracking,
                    checkInProgress = if (isTracking) "Check-in in progress ($checkInCount/5)" else "",
                    onStartTracking = { showStartConfirmation = true },
                    onStopTracking = { vm.stopTracking() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            CheckInHistoryScreen(
                checkIns = vm.getCheckInHistory(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (welcomeHome) {
            WelcomeHomeSnackbar(onDismiss = { vm.dismissWelcomeHome() })
        }

        if (emergencyActive) {
            EmergencyActiveOverlay(
                contactCount = vm.getEmergencyContactCount(),
                onCancelEmergency = { vm.cancelEmergency() }
            )
        }

        checkInOverlayEvent?.let { event ->
            CheckInOverlay(
                event = event,
                onResponse = { response, lat, lng, address -> vm.onCheckInResponse(response, lat, lng, address) }
            )
        }
    }

    if (showStartConfirmation) {
        AlertDialog(
            onDismissRequest = { showStartConfirmation = false },
            title = { Text(text = stringResource(id = R.string.track_me_home)) },
            text = { Text("Start tracking your location to use the check-in feature.") },
            confirmButton = {
                Button(
                    onClick = {
                        showStartConfirmation = false
                        vm.startTracking()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(text = stringResource(id = R.string.start))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStartConfirmation = false }) {
                    Text(text = stringResource(id = R.string.cancel), color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun WelcomeHomeSnackbar(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(
                text = stringResource(id = R.string.welcome_home),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun EmergencyActiveOverlay(contactCount: Int, onCancelEmergency: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF44336).copy(alpha = 0.95f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.calling_emergency),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.emergency_sms_sent, contactCount),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCancelEmergency,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFFF44336)
            )
        ) {
            Text(text = stringResource(id = R.string.cancel_emergency), fontWeight = FontWeight.Bold)
        }
    }
}
