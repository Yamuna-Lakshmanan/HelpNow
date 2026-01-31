package com.helpnow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.helpnow.HelpNowApp
import com.helpnow.ui.theme.HelpNowTheme
import com.helpnow.utils.SharedPreferencesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefsManager = SharedPreferencesManager.getInstance(this)
        
        setContent {
            HelpNowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    HelpNowApp(
                        navController = navController,
                        prefsManager = prefsManager,
                        onInitializeVoiceListener = {
                            initializeBackgroundVoiceListener()
                        }
                    )
                }
            }
        }
    }
}

fun initializeBackgroundVoiceListener() {
}
