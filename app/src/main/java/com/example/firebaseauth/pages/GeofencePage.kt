package com.example.firebaseauth.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory



@Composable
fun GeofencePage(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // Admin inputs for geofence details
    var geofenceLat by remember { mutableStateOf("") }
    var geofenceLon by remember { mutableStateOf("") }
    var geofenceRadius by remember { mutableStateOf("") }

    // State for geofence center and radius
    var geofenceCenter by remember { mutableStateOf<LatLng?>(null) }
    var geofenceRadiusValue by remember { mutableStateOf(0.0) }

    // Camera position state for GoogleMap
    val cameraPositionState = rememberCameraPositionState()

    // State for toggling the input form visibility
    var showInputForm by remember { mutableStateOf(false) }

    // Fetch geofence data from Firestore when the composable loads
    LaunchedEffect(Unit) {
        firestore.collection("geofences").document("bisu_clarin")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val lat = document.getDouble("latitude") ?: 0.0
                    val lon = document.getDouble("longitude") ?: 0.0
                    val radius = document.getDouble("radius") ?: 0.0

                    // Update state with fetched data
                    geofenceCenter = LatLng(lat, lon)
                    geofenceRadiusValue = radius

                    // Move the camera to the geofence center
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 15f))
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching geofence data", Toast.LENGTH_SHORT).show()
            }
    }

    // Update geofence in Firestore based on admin inputs
    fun updateGeofence() {
        val lat = geofenceLat.toDoubleOrNull()
        val lon = geofenceLon.toDoubleOrNull()
        val radius = geofenceRadius.toDoubleOrNull()

        if (lat != null && lon != null && radius != null) {
            val geofenceData = mapOf(
                "latitude" to lat,
                "longitude" to lon,
                "radius" to radius
            )

            firestore.collection("geofences").document("bisu_clarin")
                .set(geofenceData)
                .addOnSuccessListener {
                    geofenceCenter = LatLng(lat, lon)
                    geofenceRadiusValue = radius

                    // Move the camera to the new geofence center
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 15f))
                    Toast.makeText(context, "Geofence updated and saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error saving geofence", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Please enter valid geofence details.", Toast.LENGTH_SHORT).show()
        }
    }

    // UI Layout
    Box(modifier = Modifier.fillMaxSize()) {
        // Display Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Display geofence center and radius
            geofenceCenter?.let { center ->
                Marker(state = MarkerState(position = center), title = "Geofence Center")
                Circle(
                    center = center,
                    radius = geofenceRadiusValue,
                    strokeColor = Color.Blue,
                    strokeWidth = 2f,
                    fillColor = Color(0x220000FF) // Light blue fill
                )
            }
        }

        // Add button to toggle input form
        IconButton(
            onClick = { showInputForm = !showInputForm },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
        }

        // Input form overlay
        if (showInputForm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Text("Set Geofence", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    InputField(value = geofenceLat, label = "Latitude") { geofenceLat = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    InputField(value = geofenceLon, label = "Longitude") { geofenceLon = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    InputField(value = geofenceRadius, label = "Radius (meters)") { geofenceRadius = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        TextButton(onClick = { showInputForm = false }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            updateGeofence()
                            showInputForm = false
                        }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun InputField(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = Modifier.fillMaxWidth()
    )
}