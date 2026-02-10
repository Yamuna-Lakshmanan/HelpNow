package com.helpnow.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helpnow.R
import kotlinx.coroutines.delay
import com.helpnow.data.CheckInResponse
import com.helpnow.trackme.TrackMeServiceManager
import kotlin.math.max

@Composable
fun CheckInOverlay(
    event: TrackMeServiceManager.CheckInOverlayEvent?,
    onResponse: (CheckInResponse, Double, Double, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (event == null) return
    val context = LocalContext.current
    var remainingMs by remember(event) { mutableLongStateOf(max(0L, event.timeoutAt - System.currentTimeMillis())) }
    val remainingSec = remember(remainingMs) { (remainingMs / 1000).toInt() }
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    val timeText = remember(minutes, seconds) { "%02d:%02d".format(minutes, seconds) }
    LaunchedEffect(event) {
        while (remainingMs > 0) {
            delay(1000)
            remainingMs = max(0L, event.timeoutAt - System.currentTimeMillis())
        }
        if (remainingMs == 0L) {
            onResponse(CheckInResponse.TIMEOUT, event.lat, event.lng, event.address)
        }
    }
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            Text(
                text = stringResource(R.string.are_you_safe),
                color = Color(0xFF2196F3),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = timeText,
                color = Color(0xFFF44336),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(pulse)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = context.getString(R.string.check_in_count, event.index, 5),
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onResponse(CheckInResponse.YES, event.lat, event.lng, event.address) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.yes_im_safe),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onResponse(CheckInResponse.NO, event.lat, event.lng, event.address) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_help_me),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        }
    }
}
