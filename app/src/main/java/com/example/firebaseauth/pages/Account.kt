
package com.example.firebaseauth.pages

import AuthController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.viewmodel.AuthState
import com.example.firebaseauth.R
import com.example.firebaseauth.Screen
import com.example.firebaseauth.viewmodel.ProfileViewController


@Composable
fun Account(
    modifier: Modifier = Modifier,
    authViewModel: AuthController = viewModel(),
    profileViewModel: ProfileViewController = viewModel(),
    navController: NavController
) {
    val authState = authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true // Prevent creating a new instance of the login page if already on it
            }
        }
    }

    val loading = authState.value is AuthState.Loading
    val user = (authState.value as? AuthState.Authenticated)?.user

    // Load user profile
    LaunchedEffect(true) {
        profileViewModel.loadUserProfile()
    }

    val userProfileState = profileViewModel.userProfile.observeAsState()
    val userProfile = userProfileState.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF5F8C60))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Image(
            painter = painterResource(id = R.drawable.img_1),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (userProfile != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                UserProfile(label = "ID Number:", value = userProfile.employeeID)
                UserProfile(label = "Name:", value = userProfile.username)
                UserProfile(label = "Email:", value = userProfile.email)
            }
        } else {
            Text("Loading...", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                authViewModel.signOut()
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Log Out",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
