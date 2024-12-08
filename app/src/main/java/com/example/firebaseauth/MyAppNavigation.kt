package com.example.firebaseauth

import AuthController
import DTRController
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseauth.activity.LocationHelper
import com.example.firebaseauth.pages.HomePage
import com.example.firebaseauth.pages.LoginPage
import com.example.firebaseauth.viewmodel.AuthState
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseauth.pages.Account
import com.example.firebaseauth.pages.AccountAdmin
import com.example.firebaseauth.pages.AdminHomePage
import com.example.firebaseauth.pages.DTR
import com.example.firebaseauth.pages.SignupPage
import com.example.firebaseauth.pages.MapPage
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object HomePage : Screen("homePage")
    object Map : Screen("map")
    object AdminHomePage : Screen("adminHomePage")
}

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthController,
    currentLocation: LatLng? = null
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    val userRole by authViewModel.userRole.observeAsState()
    val context = LocalContext.current

    val role = userRole ?: ""


    val locationHelper = remember {
        LocationHelper(
            context = context,
            onLocationUpdate = {}
        )
    }

    // Set the start destination based on the authentication state
    val startDestination = when (authState) {
        is AuthState.EmployeeAuthenticated -> Screen.HomePage.route
        is AuthState.AdminAuthenticated -> Screen.AdminHomePage.route
        else -> Screen.Login.route
    }

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute in listOf("home", "map", "Dtr")) {
                // Add any bottom bar content here
            }
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = modifier.padding(innerPadding)
            ) {
                // Login Page
                composable(Screen.Login.route) {
                    LoginPage(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            val userEmail = authViewModel.auth.currentUser?.email ?: ""
                            val role = authViewModel.assignRoleBasedOnEmail(userEmail)


                            // Log the role assignment and navigate accordingly
                            Log.d("Login", "User role: $role")

                            when (role) {
                                "admin" -> {
                                    navController.navigate(Screen.AdminHomePage.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                                "employee" -> {
                                    navController.navigate(Screen.HomePage.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                                else -> {
                                    Log.e("Login", "Unknown role: $role")
                                }
                            }
                        }
                    )
                }

                // Signup Page
                composable(Screen.Signup.route) {
                    SignupPage(
                        modifier = Modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        onSignUpSuccess = {
                            navController.navigate(Screen.HomePage.route)
                        }
                    )
                }

                // Admin Home Page
                composable(Screen.AdminHomePage.route) {
                    AdminHomePage(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        role = "admin"
                    )
                }

                // Employee Home Page
                composable(Screen.HomePage.route) {
                    HomePage(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        role = "employee"
                    )
                }

                // Account Pages
                composable("account") {
                    Account(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                composable("accountAdmin") {
                    AccountAdmin(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                // DTR Page (Employee Time Record)
                composable("Dtr") {
                    val dtrViewModel: DTRController = viewModel()
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)

                    val currentUserEmail = remember { FirebaseAuth.getInstance().currentUser?.email }
                    val onTimeStamped: () -> Unit = {

                    }
                    currentUserEmail?.let { email ->
                        DTR(
                            viewModel =dtrViewModel,
                            email = email,
                            fusedLocationClient = fusedLocationClient,
                            onTimeStamped = onTimeStamped
                        )
                    } ?: run {

                    }
                }



                // Map Page
                composable(Screen.Map.route) {
                    MapPage(modifier = modifier)
                }
            }
        }
    )

}