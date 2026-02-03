package com.helpnow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helpnow.R
import com.helpnow.utils.SharedPreferencesManager

@Composable
fun DangerPhraseConfigScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = SharedPreferencesManager.getInstance(context)

    var phrase by remember { mutableStateOf(prefsManager.getCustomDangerPhrase()) }
    var error by remember { mutableStateOf<String?>(null) }

    val hasDigit = phrase.any { it.isDigit() }
    val isValid = phrase.isNotBlank() && hasDigit
    val digitRequiredText = stringResource(id = R.string.danger_phrase_digit_required)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
    ) {

        // 🔹 HEADER
        HeaderSection(onBackClick)

        // 🔹 CONTENT
        ContentSection(
            phrase = phrase,
            onPhraseChange = {
                phrase = it
                error = null
            },
            hasDigit = hasDigit,
            error = error,
            onSave = {
                if (!isValid) {
                    error = digitRequiredText
                } else {
                    prefsManager.saveCustomDangerPhrase(phrase)
                    onNextClick()
                }
            },
            isValid = isValid
        )
    }
}

@Composable
private fun HeaderSection(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(id = R.color.primary),
                        colorResource(id = R.color.primary_dark)
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = colorResource(id = R.color.white)
                )
            }

            Text(
                text = stringResource(id = R.string.danger_phrase_label),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.white)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun ColumnScope.ContentSection(
    phrase: String,
    onPhraseChange: (String) -> Unit,
    hasDigit: Boolean,
    error: String?,
    onSave: () -> Unit,
    isValid: Boolean
) {
    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.white))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = colorResource(id = R.color.error),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.danger_phrase_digit_required),
                fontSize = 14.sp,
                color = colorResource(id = R.color.text_secondary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phrase,
                onValueChange = onPhraseChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.danger_phrase_label)) },
                placeholder = { Text(stringResource(id = R.string.danger_phrase_hint)) },
                singleLine = true,
                isError = phrase.isNotEmpty() && !hasDigit,
                supportingText = if (phrase.isNotEmpty() && !hasDigit) {
                    {
                        Text(
                            text = stringResource(id = R.string.danger_phrase_digit_required),
                            color = colorResource(id = R.color.error)
                        )
                    }
                } else null
            )
        }
    }

    error?.let {
        Text(
            text = it,
            color = colorResource(id = R.color.error),
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }

    Spacer(modifier = Modifier.weight(1f)) // ✅ SAFE now (ColumnScope)

    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = isValid,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.primary)
        )
    ) {
        Text(
            text = stringResource(id = R.string.save_danger_phrase),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.white)
        )
    }
}
