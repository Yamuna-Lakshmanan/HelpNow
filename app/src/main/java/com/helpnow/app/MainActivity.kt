package com.helpnow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.helpnow.app.ui.EmergencyHomeScreen
import com.helpnow.app.ui.HelpNowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpNowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EmergencyHomeScreen()
                }
            }
        }
    }
}
