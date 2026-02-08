package com.helpnow.utils

import java.text.SimpleDateFormat
import java.util.Locale

object ValidationUtils {
    fun validatePhone(phone: String): Boolean {
        return phone.length == Constants.PHONE_NUMBER_LENGTH && phone.all { it.isDigit() }
    }
    
    fun validateName(name: String): Boolean {
        return name.length >= Constants.MIN_NAME_LENGTH && 
               name.all { it.isLetter() || it == ' ' }
    }
    
    fun validateDateOfBirth(dob: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            dateFormat.isLenient = false
            val date = dateFormat.parse(dob)
            date != null && dob.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))
        } catch (e: Exception) {
            false
        }
    }
    
    fun validateAddress(address: String): Boolean {
        return address.length >= Constants.MIN_ADDRESS_LENGTH
    }
    
    fun validateOTP(otp: String): Boolean {
        return otp.length == Constants.OTP_LENGTH && 
               otp.all { it.isDigit() } && 
               Constants.VALID_OTP_CODES.contains(otp)
    }
}
