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

    var polygonPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var geofenceLat by remember { mutableStateOf("") }
    var geofenceLon by remember { mutableStateOf("") }
    val cameraPositionState = rememberCameraPositionState()

    var showInputForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firestore.collection("geofences").document("bisu_clarin")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Fetching the polygon data
                    val points = document.get("polygonPoints") as? List<Map<String, Double>> ?: emptyList()
                    polygonPoints = points.map {
                        LatLng(it["latitude"] ?: 0.0, it["longitude"] ?: 0.0)
                    }

                    // If the polygon is present, update the map view
                    if (polygonPoints.isNotEmpty()) {
                        val firstPoint = polygonPoints.first()
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(firstPoint, 15f))
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching geofence data", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to save the polygon geofence to Firestore
    fun updateGeofence() {
        val lat = geofenceLat.toDoubleOrNull()
        val lon = geofenceLon.toDoubleOrNull()

        if (lat != null && lon != null) {
            // Add the new point to the polygon points
            val newPoint = LatLng(lat, lon)
            polygonPoints = polygonPoints + newPoint

            // Convert polygon points to Firestore-friendly format (List of maps)
            val polygonData = polygonPoints.map { mapOf("latitude" to it.latitude, "longitude" to it.longitude) }

            firestore.collection("geofences").document("bisu_clarin")
                .set(mapOf("polygonPoints" to polygonData))
                .addOnSuccessListener {
                    Toast.makeText(context, "Geofence updated and saved!", Toast.LENGTH_SHORT).show()

                    // Move the camera to the newly added point
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(newPoint, 15f))
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error saving geofence", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to reset the polygon
    fun resetGeofence() {
        polygonPoints = emptyList()  // Clear the existing polygon points
    }

    // UI Layout
    Box(modifier = Modifier.fillMaxSize()) {
        // Display Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Draw the polygon on the map if points are available
            if (polygonPoints.size >= 3) {  // Only draw polygon if there are at least 3 points
                Polygon(
                    points = polygonPoints,
                    strokeColor = Color.Blue,
                    strokeWidth = 2f,
                    fillColor = Color(0x220000FF) // Light blue fill
                )
            }
        }

        // Add button to toggle input form for adding a point
        IconButton(
            onClick = { showInputForm = !showInputForm },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
        }

        // Reset button to clear the polygon and start fresh
        Button(
            onClick = { resetGeofence() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Text("Reset Geofence")
        }

        // Input form overlay for adding a point to the polygon
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
                    Text("Set Polygon Point", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    InputField(value = geofenceLat, label = "Latitude") { geofenceLat = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    InputField(value = geofenceLon, label = "Longitude") { geofenceLon = it }
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
                            Text("Add Point")
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
