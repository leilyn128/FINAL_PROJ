package com.example.googlemappage

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.firebaseauth.activity.LocationHelper
import com.example.firebaseauth.model.GeofenceData
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.Circle

@Composable
fun MapPage(
    modifier: Modifier = Modifier
) {
    // State to hold the user's current location
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    // State to hold the geofence data
    var geofenceData by remember { mutableStateOf<GeofenceData?>(null) }

    // Get the current context
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Initialize LocationHelper with a callback to update currentLocation
    val locationHelper = remember {
        LocationHelper(context) { location ->
            currentLocation = location
        }
    }

    // Permission launcher for ACCESS_FINE_LOCATION
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationHelper.startLocationUpdates()
            Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch geofence data from Firestore
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("geofences")
            .document("bisu_clarin") // Use your document ID here
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Ensure these values are properly cast to the expected types
                    val lat = document.getDouble("latitude") ?: 0.0
                    val lon = document.getDouble("longitude") ?: 0.0
                    val radius = document.getDouble("radius") ?: 0.0
                    geofenceData = GeofenceData(LatLng(lat, lon), radius)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching geofence: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Request permission on first composition
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationHelper.startLocationUpdates()
        }
    }

    // Stop location updates when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            locationHelper.stopLocationUpdates()
        }
    }

    // Google Map Section
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (currentLocation != null && geofenceData != null) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Marker for current location
                Marker(
                    state = MarkerState(position = currentLocation!!),
                    title = "Current Location"

                )

                // Geofence Marker
                Marker(
                    state = MarkerState(position = geofenceData!!.center),
                    title = "Geofence Center"
                )

                // Geofence Circle
                Circle(
                    center = geofenceData!!.center,  // LatLng for center
                    radius = geofenceData!!.radius,  // Double for radius
                    strokeColor = Color.Blue,
                    strokeWidth = 2f,
                    fillColor = Color(0x220000FF) // Light blue
                )

            }



        } else {
            Text(
                text = "Fetching location and geofence data...",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        }
    }
}