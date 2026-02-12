package com.helpnow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.helpnow.utils.Constants
import com.helpnow.utils.ValidationUtils

@Composable
fun UserProfileScreen(
    onNextClick: (String, String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Constants.GENDER_FEMALE) }
    var dateOfBirth by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var showCityDropdown by remember { mutableStateOf(false) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    
    val invalidNameError = stringResource(id = R.string.invalid_name)
    val invalidDobError = stringResource(id = R.string.invalid_dob)
    val invalidAddressError = stringResource(id = R.string.invalid_address)
    val selectCityError = stringResource(id = R.string.select_city)

    val isValid = fullName.isNotBlank() && 
                  ValidationUtils.validateName(fullName) &&
                  ValidationUtils.validateDateOfBirth(dateOfBirth) &&
                  ValidationUtils.validateAddress(address) &&
                  city.isNotBlank()
    
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
                    text = stringResource(id = R.string.step_3_of_4),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.complete_profile),
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { 
                    fullName = it
                    nameError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.full_name)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.primary),
                    unfocusedBorderColor = colorResource(id = R.color.gray)
                ),
                isError = nameError != null
            )
            if (nameError != null) {
                Text(
                    text = nameError!!,
                    color = colorResource(id = R.color.error),
                    fontSize = 12.sp
                )
            }
            
            Text(
                text = stringResource(id = R.string.gender),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.text_primary)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { gender = Constants.GENDER_MALE },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gender == Constants.GENDER_MALE) 
                            colorResource(id = R.color.primary) 
                        else colorResource(id = R.color.light_gray)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.male),
                        color = if (gender == Constants.GENDER_MALE) 
                            colorResource(id = R.color.white) 
                        else colorResource(id = R.color.text_primary)
                    )
                }
                
                Button(
                    onClick = { gender = Constants.GENDER_FEMALE },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gender == Constants.GENDER_FEMALE) 
                            colorResource(id = R.color.primary) 
                        else colorResource(id = R.color.light_gray)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.female),
                        color = if (gender == Constants.GENDER_FEMALE) 
                            colorResource(id = R.color.white) 
                        else colorResource(id = R.color.text_primary)
                    )
                }
            }
            
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { 
                    var newValue = it
                    if (newValue.length == 2 && dateOfBirth.length < 2) {
                        newValue += "-"
                    }
                    if (newValue.length == 5 && dateOfBirth.length < 5) {
                        newValue += "-"
                    }
                    if (newValue.length <= 10 && newValue.all { c -> c.isDigit() || c == '-' }) {
                        dateOfBirth = newValue
                        dobError = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.date_of_birth)) },
                placeholder = { Text(stringResource(id = R.string.dob_format)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.primary),
                    unfocusedBorderColor = colorResource(id = R.color.gray)
                ),
                isError = dobError != null
            )
            if (dobError != null) {
                Text(
                    text = dobError!!,
                    color = colorResource(id = R.color.error),
                    fontSize = 12.sp
                )
            }
            
            OutlinedTextField(
                value = address,
                onValueChange = { 
                    address = it
                    addressError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                label = { Text(stringResource(id = R.string.home_address)) },
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.primary),
                    unfocusedBorderColor = colorResource(id = R.color.gray)
                ),
                isError = addressError != null
            )
            if (addressError != null) {
                Text(
                    text = addressError!!,
                    color = colorResource(id = R.color.error),
                    fontSize = 12.sp
                )
            }
            
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = city,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCityDropdown = true },
                    label = { Text(stringResource(id = R.string.city_district)) },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = colorResource(id = R.color.gray)
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                )
                
                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Constants.INDIAN_CITIES.forEach { selectedCity ->
                        DropdownMenuItem(
                            text = { Text(selectedCity) },
                            onClick = {
                                city = selectedCity
                                showCityDropdown = false
                                cityError = null
                            }
                        )
                    }
                }
            }
            if (cityError != null) {
                Text(
                    text = cityError!!,
                    color = colorResource(id = R.color.error),
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    var hasError = false
                    if (!ValidationUtils.validateName(fullName)) {
                        nameError = invalidNameError
                        hasError = true
                    }
                    if (!ValidationUtils.validateDateOfBirth(dateOfBirth)) {
                        dobError = invalidDobError
                        hasError = true
                    }
                    if (!ValidationUtils.validateAddress(address)) {
                        addressError = invalidAddressError
                        hasError = true
                    }
                    if (city.isBlank()) {
                        cityError = selectCityError
                        hasError = true
                    }
                    if (!hasError) {
                        onNextClick(fullName, gender, dateOfBirth, address, city)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primary),
                    disabledContainerColor = colorResource(id = R.color.light_gray)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = isValid
            ) {
                Text(
                    text = stringResource(id = R.string.next),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
}