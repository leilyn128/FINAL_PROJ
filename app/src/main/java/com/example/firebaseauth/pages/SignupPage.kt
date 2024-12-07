
package com.example.firebaseauth.pages

import AuthController
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll


@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthController,
    onSignUpSuccess: () -> Unit
) {


    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val userModel = authViewModel.userState.value
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo and heading
        Image(
            painter = painterResource(id = com.example.firebaseauth.R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = "SIGN UP FORM",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Fields bound to AuthViewModel's userModel
        OutlinedTextField(
            value = userModel.employeeID,
            onValueChange = { authViewModel.updateUser("employeeId", it) },
            label = { Text("Employee ID") },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Employee ID Icon") },
            modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = userModel.username,
            onValueChange = { authViewModel.updateUser("username", it) },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Username Icon") },
            modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = userModel.email,
            onValueChange = { authViewModel.updateUser("email", it) },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
            modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 16.dp)
        )

        // Password fields
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password Icon") },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Confirm Password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 16.dp)
        )

        // Sign-Up Button
        Button(
            onClick = {
                if (password == confirmPassword) { // Check if passwords match
                    authViewModel.signup(
                        email = userModel.email,
                        password = password,
                        username = userModel.username,
                        employeeID = userModel.employeeID, // Keep only the relevant fields
                        onSignUpSuccess = {
                            Toast.makeText(context, "Sign-Up Successful", Toast.LENGTH_SHORT).show()
                            navController.navigate("homePage") {
                                popUpTo("signup") { inclusive = true }
                            }
                        },
                        onSignUpFailure = { error ->
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = userModel.email.isNotBlank() && userModel.username.isNotBlank() &&
                    password.isNotBlank() && confirmPassword.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Signup", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Login link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? ", fontSize = 20.sp)
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Login", fontSize = 20.sp, color =MaterialTheme.colorScheme.primary)
            }
        }
        }
    }
