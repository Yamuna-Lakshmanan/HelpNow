package com.helpnow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helpnow.app.R
import com.helpnow.utils.LocationUtils

@Composable
fun MapScreen(
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var coordinates by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val locationTask = LocationUtils.getCurrentLocation(context)
        locationTask?.addOnSuccessListener { location ->
            coordinates = LocationUtils.formatCoordinates(
                location.latitude,
                location.longitude
            )
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
                .height(60.dp)
                .background(color = colorResource(id = R.color.primary)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.your_live_location),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.white)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(id = R.color.surface))
        ) {
            if (coordinates != null) {
                Text(
                    text = "${stringResource(id = R.string.current_coordinates)}: $coordinates",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.text_secondary),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(
                            color = colorResource(id = R.color.white),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }
            
            Button(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primary)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.share_location),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
}
