package com.example.firebaseauth

import AuthViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.rememberNavController
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.android.gms.maps.model.LatLng
import com.example.firebaseauth.activity.LocationHelper

class MainActivity : ComponentActivity() {
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel: AuthViewModel by viewModels() // ViewModel scoped to Activity lifecycle

        // MutableState to hold the user's current location
        val currentLocation = mutableStateOf<LatLng?>(null)

        // Initialize LocationHelper with context and location callback
        locationHelper = LocationHelper(this) { latLng ->
            currentLocation.value = latLng
        }

        // Start location updates
        locationHelper.startLocationUpdates()

        setContent {
            FirebaseAuthTheme {
                MainContent(
                    authViewModel = authViewModel,
                    currentLocation = currentLocation.value
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        locationHelper.stopLocationUpdates()
    }
}

@Composable
fun MainContent(
    authViewModel: AuthViewModel,
    currentLocation: LatLng?
) {
    val navController = rememberNavController()

    MyAppNavigation(
        authViewModel = authViewModel,
        currentLocation = currentLocation
    )
}


