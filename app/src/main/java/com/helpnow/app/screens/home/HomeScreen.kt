package com.helpnow.app.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.helpnow.app.R
import com.helpnow.app.Routes

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "HelpNow Logo")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.navigate(Routes.EMERGENCY_HOME) }) {
            Text("Emergency")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Routes.VOICE_REGISTRATION) }) {
            Text("Voice")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* navController.navigate(Routes.SMS_SETTINGS) */ }) {
            Text("SMS")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Routes.PERMISSIONS) }) {
            Text("Permissions")
        }
    }
}
