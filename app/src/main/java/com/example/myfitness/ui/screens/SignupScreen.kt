package com.example.myfitness.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.myfitness.R
import com.example.myfitness.database.User
import com.example.myfitness.repository.FirebaseRepository

@Composable
fun SignupScreen(navController: NavHostController) {
    val repository = FirebaseRepository()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create Account",
            color = Color(0xFF1D1517),
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        // Full Name Box
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
                .border(1.dp, Color(0xFFF7F8F8), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF7F8F8))
                .padding(17.dp)
        )

        // Email Box
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
                .border(1.dp, Color(0xFFF7F8F8), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF7F8F8))
                .padding(17.dp)
        )

        // Password Box
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
                .border(1.dp, Color(0xFFF7F8F8), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF7F8F8))
                .padding(17.dp)
        )

        // Confirm Password Box
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
                .border(1.dp, Color(0xFFF7F8F8), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF7F8F8))
                .padding(17.dp)
        )

        // Gender Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
                .border(1.dp, Color(0xFFF7F8F8), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF7F8F8))
                .padding(17.dp)
                .clickable { genderExpanded = true }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (gender.isEmpty()) "Gender" else gender,
                    color = if (gender.isEmpty()) Color.Gray else Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown Arrow")
            }
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Male") },
                    onClick = {
                        gender = "Male"
                        genderExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Female") },
                    onClick = {
                        gender = "Female"
                        genderExpanded = false
                    }
                )
            }
        }

        // Signup Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFCC8FED),
                            Color(0xFF6B50F6)
                        )
                    )
                )
        ) {
            Button(
                onClick = {
                    if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = task.result?.user?.uid
                                    if (userId != null) {
                                        val user = User(fullName, email, gender)
                                        Log.d("SignupScreen", "User created successfully. Saving user data to database.")
                                        // Writing user details to Firebase Realtime Database
                                        repository.saveUserData(userId, user) { success, error ->
                                            if (success) {
                                                Log.d("SignupScreen", "User data saved successfully.")
                                                navController.navigate("home")
                                            } else {
                                                errorMessage = "Failed to save user data: $error"
                                                Log.e("SignupScreen", "Database write failed: $error")
                                            }
                                        }
                                    } else {
                                        errorMessage = "Failed to create user."
                                        Log.e("SignupScreen", "User ID is null after creating user.")
                                    }
                                } else {
                                    errorMessage = "Signup failed: ${task.exception?.message}"
                                    Log.e("SignupScreen", "User creation failed: ${task.exception}")
                                }
                            }
                    } else {
                        errorMessage = "Please fill in all fields correctly."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = "Create Account", fontSize = 18.sp)
            }
        }

        // Display Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
