package com.example.firebaseauth.pages

import AuthViewModel
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.viewmodel.AuthState

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current


    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                // Perform navigation based on role (admin or employee)
                val userRole = authViewModel.userRole.value
                when (userRole) {
                    "admin" -> {
                        Log.d("Auth", "Admin authenticated. Navigating to AdminHome.")
                        navController.navigate("adminHome") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    "employee" -> {
                        Log.d("Auth", "Employee authenticated. Navigating to HomePage.")
                        navController.navigate("homePage") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }
            is AuthState.Error -> {
                Log.e("Auth", "Error: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.d("Auth", "Unauthenticated or other state: $state")
            }
        }
    }


    // Main layout container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // App logo
            Image(
                painter = painterResource(id = com.example.firebaseauth.R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // Login Form Title
            Text(
                text = "LOGIN FORM",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") },
                modifier = Modifier.fillMaxWidth()
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password Icon") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Login Button
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        authViewModel.login(email, password, navController)
                    } else {
                        Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth() // This makes the button as wide as the text fields
            ) {
                Text(text = "Login", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            // Signup Option
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Don't have an account?", fontSize = 20.sp)
                TextButton(onClick = { navController.navigate("signup") }) {
                    Text(text = "Signup", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
