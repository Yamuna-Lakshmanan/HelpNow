package com.helpnow.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.helpnow.app.R
import com.helpnow.app.data.CheckIn
import com.helpnow.app.data.CheckInResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckInHistoryScreen(
    checkIns: List<CheckIn>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.check_in_history),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (checkIns.isEmpty()) {
            Text(
                text = stringResource(R.string.no_check_ins_yet),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(checkIns.take(10)) { checkIn ->
                    CheckInHistoryItem(checkIn = checkIn)
                }
            }
        }
    }
}

@Composable
private fun CheckInHistoryItem(
    checkIn: CheckIn,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
    val responseText = when (checkIn.response) {
        CheckInResponse.YES -> stringResource(R.string.response_yes)
        CheckInResponse.NO -> stringResource(R.string.response_no)
        CheckInResponse.TIMEOUT -> stringResource(R.string.response_timeout)
    }
    val statusText = when (checkIn.response) {
        CheckInResponse.YES -> stringResource(R.string.status_safe)
        CheckInResponse.NO, CheckInResponse.TIMEOUT -> stringResource(R.string.status_emergency_triggered)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateFormat.format(Date(checkIn.timestamp)),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = responseText,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "%.4f, %.4f".format(checkIn.latitude, checkIn.longitude),
                style = MaterialTheme.typography.bodySmall
            )
            checkIn.locationAddress?.let { addr ->
                if (addr.isNotEmpty()) {
                    Text(
                        text = addr,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
