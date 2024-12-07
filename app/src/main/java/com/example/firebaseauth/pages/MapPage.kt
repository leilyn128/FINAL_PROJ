package com.example.googlemappage

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.core.app.ActivityCompat
import com.example.firebaseauth.activity.LocationHelper
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapPage(
    modifier: Modifier = Modifier
) {
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var geofencePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val locationHelper = remember {
        LocationHelper(context) { location ->
            currentLocation = location
        }
    }

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

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("geofences")
            .document("bisu_clarin")
            .get() // We query the document itself
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val points = document.get("polygonPoints") as? List<Map<String, Double>>
                    if (points != null) {
                        val latLngPoints = points.mapNotNull { point ->
                            val lat = point["latitude"]
                            val lng = point["longitude"]
                            if (lat != null && lng != null) {
                                LatLng(lat, lng)
                            } else {
                                Log.e("MapPage", "Missing latitude or longitude in a point.")
                                null
                            }
                        }
                        Log.d("MapPage", "Fetched Polygon Points: $latLngPoints")
                        geofencePoints = latLngPoints
                    } else {
                        Log.e("MapPage", "polygonPoints field is missing or in the wrong format.")
                    }
                } else {
                    Log.e("MapPage", "Document not found.")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching geofence: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



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

    DisposableEffect(Unit) {
        onDispose {
            locationHelper.stopLocationUpdates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (currentLocation != null) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = currentLocation!!),
                    title = "Current Location"
                )

                if (geofencePoints.isNotEmpty()) {
                    Polygon(
                        points = geofencePoints,
                        strokeColor = Color.Blue,
                        strokeWidth = 2f,
                        fillColor = Color(0x220000FF)
                    )
                }
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
