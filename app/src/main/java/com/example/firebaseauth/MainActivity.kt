package com.example.firebaseauth

import AuthController
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

        val authViewModel: AuthController by viewModels()

        val currentLocation = mutableStateOf<LatLng?>(null)

        locationHelper = LocationHelper(this) { latLng ->
            currentLocation.value = latLng
        }

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
    authViewModel: AuthController,
    currentLocation: LatLng?
) {
    val navController = rememberNavController()

    MyAppNavigation(
        authViewModel = authViewModel,
        currentLocation = currentLocation
    )
}


