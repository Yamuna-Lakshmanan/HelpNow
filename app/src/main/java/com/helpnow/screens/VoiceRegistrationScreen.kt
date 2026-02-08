package com.helpnow.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.helpnow.R
import com.helpnow.utils.Constants
import com.helpnow.utils.SharedPreferencesManager
import com.helpnow.voice.VoiceprintMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VoiceRegistrationScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember { SharedPreferencesManager.getInstance(context) }
    val customPhrase = prefs.getCustomDangerPhrase()

    var isRecording by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var voiceSaved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val samples = remember { mutableStateListOf<ShortArray>() }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                error = context.getString(R.string.microphone_permission_required)
                isRecording = false
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
    ) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorResource(R.color.primary),
                            colorResource(R.color.primary_dark)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = colorResource(R.color.white)
                    )
                }
                Text(
                    text = stringResource(R.string.voice_registration_title),
                    color = colorResource(R.color.white),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = colorResource(R.color.primary),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.voice_registration_instruction),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = customPhrase.ifBlank {
                            stringResource(R.string.voice_registration_phrase)
                        },
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.primary)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(
                            R.string.voice_registration_sample,
                            samples.size + 1,
                            Constants.VOICE_REGISTRATION_SAMPLES
                        )
                    )

                    if (isRecording) {
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                    }

                    if (voiceSaved) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.voiceprint_saved),
                            color = colorResource(R.color.success),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = colorResource(R.color.error))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    when {
                        voiceSaved -> onNextClick()

                        samples.size >= Constants.VOICE_REGISTRATION_SAMPLES -> {
                            val matcher = VoiceprintMatcher()
                            val hash = matcher.createVoiceprint(samples)
                            prefs.saveUserVoiceprint(hash)
                            voiceSaved = true
                        }

                        else -> {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                return@Button
                            }

                            isRecording = true
                            progress = 0f
                            error = null

                            scope.launch {
                                recordSample(
                                    onProgress = { progress = it },
                                    onComplete = {
                                        samples.add(it)
                                        isRecording = false
                                    }
                                )
                            }
                        }
                    }
                }
            ) {
                Text(
                    text = when {
                        voiceSaved -> stringResource(R.string.next)
                        samples.size >= Constants.VOICE_REGISTRATION_SAMPLES ->
                            stringResource(R.string.save_voiceprint)
                        else ->
                            stringResource(R.string.record_sample)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private suspend fun recordSample(
    onProgress: (Float) -> Unit,
    onComplete: (ShortArray) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val sampleRate = 16000
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            )

            recorder.startRecording()

            val totalSamples: Int =
                ((sampleRate * Constants.VOICE_SAMPLE_DURATION_MS) / 1000L).toInt()

            val data = ShortArray(totalSamples)

            var read = 0
            while (read < totalSamples) {
                val r = recorder.read(data, read, totalSamples - read)
                if (r <= 0) break
                read += r
                onProgress(read.toFloat() / totalSamples.toFloat())
            }

            recorder.stop()
            recorder.release()
            onComplete(data)

        } catch (_: Exception) {
        }
    }
}
