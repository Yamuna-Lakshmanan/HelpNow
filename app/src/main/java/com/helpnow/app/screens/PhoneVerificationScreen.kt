package com.helpnow.app.screens

import androidx.compose.foundation.background
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
import com.helpnow.app.R
import com.helpnow.app.utils.Constants
import com.helpnow.app.utils.ValidationUtils

@Composable
fun PhoneVerificationScreen(
    onVerifyClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val invalidPhoneMessage = stringResource(id = R.string.invalid_phone)
    
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
                    text = stringResource(id = R.string.step_1_of_4),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.verify_identity),
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.country_code),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        if (it.length <= Constants.PHONE_NUMBER_LENGTH && it.all { char -> char.isDigit() }) {
                            phoneNumber = it
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(id = R.string.enter_phone_number)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.primary),
                        unfocusedBorderColor = colorResource(id = R.color.gray)
                    ),
                    isError = errorMessage != null
                )
            }
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = colorResource(id = R.color.error),
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (ValidationUtils.validatePhone(phoneNumber)) {
                        onVerifyClick(phoneNumber)
                    } else {
                        errorMessage = invalidPhoneMessage
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primary)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.verify),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
}
