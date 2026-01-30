package com.helpnow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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

@Composable
fun EmergencyContactsScreen(
    onNextClick: (List<com.helpnow.models.EmergencyContact>) -> Unit,
    onBackClick: () -> Unit
) {
    var contacts by remember { 
        mutableStateOf(
            mutableListOf(
                com.helpnow.models.EmergencyContact("", "", Constants.RELATIONSHIP_FRIEND),
                com.helpnow.models.EmergencyContact("", "", Constants.RELATIONSHIP_FRIEND),
                com.helpnow.models.EmergencyContact("", "", Constants.RELATIONSHIP_FRIEND)
            )
        )
    }
    
    fun updateContact(index: Int, name: String? = null, phone: String? = null, relationship: String? = null) {
        val updated = contacts.toMutableList()
        val current = updated[index]
        updated[index] = com.helpnow.models.EmergencyContact(
            name = name ?: current.name,
            phone = phone ?: current.phone,
            relationship = relationship ?: current.relationship
        )
        contacts = updated
    }
    
    fun addContact() {
        if (contacts.size < Constants.MAX_CONTACTS_ALLOWED) {
            contacts = contacts.toMutableList().apply {
                add(com.helpnow.models.EmergencyContact("", "", Constants.RELATIONSHIP_FRIEND))
            }
        }
    }
    
    fun removeContact(index: Int) {
        if (contacts.size > Constants.MIN_CONTACTS_REQUIRED) {
            contacts = contacts.toMutableList().apply {
                removeAt(index)
            }
        }
    }
    
    val validContacts = contacts.count { 
        it.name.isNotBlank() && ValidationUtils.validatePhone(it.phone) 
    }
    val canProceed = validContacts >= Constants.MIN_CONTACTS_REQUIRED
    
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
                    text = stringResource(id = R.string.step_4_of_4),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.add_emergency_contacts),
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
            
            Text(
                text = stringResource(id = R.string.contacts_description),
                fontSize = 14.sp,
                color = colorResource(id = R.color.text_secondary)
            )
            
            if (validContacts < Constants.MIN_CONTACTS_REQUIRED) {
                Text(
                    text = stringResource(id = R.string.min_contacts_required),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.error),
                    fontWeight = FontWeight.Medium
                )
            }
            
            contacts.forEachIndexed { index, contact ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.white)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stringResource(id = R.string.contact)} ${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.primary)
                            )
                            
                            if (contacts.size > Constants.MIN_CONTACTS_REQUIRED) {
                                IconButton(
                                    onClick = { removeContact(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.delete),
                                        tint = colorResource(id = R.color.error),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = contact.name,
                            onValueChange = { updateContact(index, name = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(id = R.string.contact_name)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.primary),
                                unfocusedBorderColor = colorResource(id = R.color.gray)
                            )
                        )
                        
                        OutlinedTextField(
                            value = contact.phone,
                            onValueChange = { 
                                if (it.length <= Constants.PHONE_NUMBER_LENGTH && it.all { char -> char.isDigit() }) {
                                    updateContact(index, phone = it)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(id = R.string.contact_phone)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.primary),
                                unfocusedBorderColor = colorResource(id = R.color.gray)
                            )
                        )
                        
                        var showRelationshipDropdown by remember { mutableStateOf(false) }
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = contact.relationship,
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRelationshipDropdown = true },
                                label = { Text(stringResource(id = R.string.relationship)) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = colorResource(id = R.color.gray)
                                )
                            )
                            
                            DropdownMenu(
                                expanded = showRelationshipDropdown,
                                onDismissRequest = { showRelationshipDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Constants.RELATIONSHIPS.forEach { rel ->
                                    DropdownMenuItem(
                                        text = { Text(rel) },
                                        onClick = {
                                            updateContact(index, relationship = rel)
                                            showRelationshipDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (contact.name.isNotBlank() && ValidationUtils.validatePhone(contact.phone)) {
                            Text(
                                text = stringResource(id = R.string.contact_added),
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.success),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            if (contacts.size < Constants.MAX_CONTACTS_ALLOWED) {
                FloatingActionButton(
                    onClick = { addContact() },
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(56.dp),
                    containerColor = colorResource(id = R.color.primary),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.add_contact),
                        tint = colorResource(id = R.color.white)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!canProceed) {
                Text(
                    text = stringResource(id = R.string.add_at_least_3_contacts),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.error),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Button(
                onClick = {
                    val validContactsList = contacts.filter { 
                        it.name.isNotBlank() && ValidationUtils.validatePhone(it.phone) 
                    }
                    onNextClick(validContactsList)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primary),
                    disabledContainerColor = colorResource(id = R.color.light_gray)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = canProceed
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
