package com.helpnow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helpnow.R
import com.helpnow.utils.Constants
import com.helpnow.utils.ValidationUtils
import kotlinx.coroutines.delay

@Composable
fun OTPVerificationScreen(
    phoneNumber: String,
    onVerifyClick: () -> Unit,
    onBackClick: () -> Unit,
    onResendClick: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendTimer by remember { mutableStateOf(Constants.OTP_RESEND_TIMER_SECONDS) }
    var canResend by remember { mutableStateOf(false) }
    val invalidOtpMessage = stringResource(id = R.string.invalid_otp)

    LaunchedEffect(Unit) {
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
        canResend = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.primary),
                            colorResource(id = R.color.primary_dark)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.step_2_of_4),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.enter_activation_code),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(4) { index ->
                    OutlinedTextField(
                        value = otp.getOrNull(index)?.toString() ?: "",
                        onValueChange = { char ->
                            if (char.length <= 1 && char.all { it.isDigit() }) {
                                val newOtp = otp.toMutableList()
                                if (char.isEmpty()) {
                                    if (newOtp.size > index) {
                                        newOtp.removeAt(index)
                                    }
                                } else {
                                    if (newOtp.size <= index) {
                                        repeat(index - newOtp.size + 1) {
                                            newOtp.add(' ')
                                        }
                                    }
                                    newOtp[index] = char[0]
                                }
                                otp = newOtp.joinToString("").trim()
                                errorMessage = null
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.primary),
                            unfocusedBorderColor = colorResource(id = R.color.gray)
                        ),
                        isError = errorMessage != null
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = colorResource(id = R.color.error),
                    fontSize = 12.sp
                )
            }

            Text(
                text = if (canResend) {
                    stringResource(id = R.string.resend_code)
                } else {
                    stringResource(
                        id = R.string.resend_in,
                        resendTimer
                    )
                },
                fontSize = 14.sp,
                color = if (resendTimer < 10) colorResource(id = R.color.error) else colorResource(id = R.color.primary),
                modifier = Modifier.clickable(enabled = canResend) {
                    if (canResend) {
                        onResendClick()
                        resendTimer = Constants.OTP_RESEND_TIMER_SECONDS
                        canResend = false
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (ValidationUtils.validateOTP(otp)) {
                        onVerifyClick()
                    } else {
                        errorMessage = invalidOtpMessage
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primary)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = otp.length == Constants.OTP_LENGTH
            ) {
                Text(
                    text = stringResource(id = R.string.verify_code),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
}
