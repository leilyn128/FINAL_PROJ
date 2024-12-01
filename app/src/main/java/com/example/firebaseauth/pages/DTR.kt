package com.example.firebaseauth.pages

import DTRViewModel
import RecordsDialog
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.R
import com.example.firebaseauth.model.DTRRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import android.location.Location
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import com.example.firebaseauth.activity.GeofenceUtility
import com.example.firebaseauth.model.GeofenceData
import com.example.firebaseauth.activity.GeofenceUtils
import com.google.android.gms.location.FusedLocationProviderClient

@Composable
fun DTR(
    viewModel: DTRViewModel,
    email: String,
    fusedLocationClient: FusedLocationProviderClient
) {
    val geofenceData by viewModel.geofenceData.observeAsState(GeofenceData(LatLng(0.0, 0.0), 0.0))
    val context = LocalContext.current

    // State to hold today's DTR record
    var todaysRecord by remember { mutableStateOf<DTRRecord?>(null) }

    // Fetch DTR records
    LaunchedEffect(email) {
        viewModel.fetchDTRRecords(email)
    }

    val dtrRecords = viewModel.dtrRecords.collectAsState().value

    // Look for today's DTR record in fetched records
    LaunchedEffect(dtrRecords) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        todaysRecord = dtrRecords.find {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date) == today
        }
    }

    var showRecordsDialog by remember { mutableStateOf(false) }

    // Composables for the UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        DTRCustomHeader(onViewRecordsClick = { showRecordsDialog = true }) // Update the dialog state here

        Spacer(modifier = Modifier.height(8.dp))

        // Center the content and push buttons to the bottom
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Get today's date
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Find today's record in dtrRecords
                val todaysRecord = dtrRecords.find { record ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(record.date) == today
                }

                if (todaysRecord == null) {
                    Text("No records found for today.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    // Show today's DTR record
                    DTRCard(record = todaysRecord)
                }

                Spacer(modifier = Modifier.height(24.dp)) // Add spacing between the card and buttons

                // Center the clock-in and clock-out buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clock-in button
                    ClockInButton(
                        email = email,
                        fusedLocationClient = fusedLocationClient,
                        onClockInSuccess = {
                            // After successful clock-in, fetch updated DTR records
                            viewModel.fetchDTRRecords(email)
                        }
                    )
                    ClockOutButton(
                        email = email,
                        fusedLocationClient = fusedLocationClient,
                        onClockOutSuccess = {
                            // After successful clock-out, fetch updated DTR records
                            viewModel.fetchDTRRecords(email)
                        }
                    )
                }
            }
        }
    }

    // Show records dialog if needed
    if (showRecordsDialog) {
        RecordsDialog(
            records = dtrRecords,
            onDismiss = { showRecordsDialog = false }
        )
    }
}


@Composable
fun DTRCustomHeader(onViewRecordsClick: () -> Unit) {
    val customGreen = Color(0xFF5F8C60) // Define the custom green color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(customGreen) // Use the custom green color
            .padding(vertical = 6.dp)
    ) {
        // Logo positioned at the top-left
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
            contentDescription = "Logo",
            modifier = Modifier
                .size(75.dp)
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        // Title centered horizontally
        Text(
            text = "Daily Time Record",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
        )

        // "View Records" IconButton at the top-right
        IconButton(
            onClick = { onViewRecordsClick() },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List, // You can replace this with your own icon
                contentDescription = "View Records",
                tint = Color.White // Apply the custom green color to the icon
            )
        }
    }
}

@Composable
fun DTRCard(record: DTRRecord) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val arrivalTime = record.morningArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"
    val morningDepartureTime = record.morningDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"
    val afternoonArrivalTime = record.afternoonArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"
    val afternoonDepartureTime = record.afternoonDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f) // Make the card width 90% of the screen
            .height(350.dp), // Set a fixed height
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly // Distribute items evenly
        ) {
            Text(
                text = "Date: ${dateFormat.format(record.date)}",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Morning Arrival: $arrivalTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Morning Departure: $morningDepartureTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Afternoon Arrival: $afternoonArrivalTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Afternoon Departure: $afternoonDepartureTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
        }
    }
}


@Composable
fun ClockInButton(
    email: String,
    fusedLocationClient: FusedLocationProviderClient,
    onClockInSuccess: () -> Unit // Callback to notify when clock-in is successful
) {
    val context = LocalContext.current

    // Geofence data fetch and validation
    val geofenceValidated = remember { mutableStateOf(false) }

    // Start the camera only if inside geofence
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == Activity.RESULT_OK) {
            // Photo capture was successful, proceed with clock-in logic
            val currentTime = Calendar.getInstance().time
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            val db = FirebaseFirestore.getInstance()
            val recordId = "${email}-${getCurrentDate()}" // Use email in recordId

            db.collection("dtr_records")
                .document(recordId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val morningArrival = documentSnapshot.getDate("morningArrival")
                        val afternoonArrival = documentSnapshot.getDate("afternoonArrival")

                        // Handle clock-in based on the time of day
                        if (currentHour < 12 && morningArrival == null) {
                            // Morning clock-in
                            val updatedRecord = DTRRecord(
                                email = email,
                                date = currentTime,
                                morningArrival = currentTime,
                                afternoonArrival = afternoonArrival,
                                morningDeparture = documentSnapshot.getDate("morningDeparture"),
                                afternoonDeparture = documentSnapshot.getDate("afternoonDeparture")
                            )
                            db.collection("dtr_records").document(recordId).set(updatedRecord)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Morning clock-in successful!", Toast.LENGTH_SHORT).show()
                                    onClockInSuccess() // Notify that clock-in was successful
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update clock-in time", Toast.LENGTH_SHORT).show()
                                }
                        } else if (currentHour >= 12 && afternoonArrival == null) {
                            // Afternoon clock-in
                            val updatedRecord = DTRRecord(
                                email = email,
                                date = currentTime,
                                morningArrival = documentSnapshot.getDate("morningArrival"),
                                afternoonArrival = currentTime,
                                morningDeparture = documentSnapshot.getDate("morningDeparture"),
                                afternoonDeparture = documentSnapshot.getDate("afternoonDeparture")
                            )
                            db.collection("dtr_records").document(recordId).set(updatedRecord)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Afternoon clock-in successful!", Toast.LENGTH_SHORT).show()
                                    onClockInSuccess() // Notify that clock-in was successful
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update clock-in time", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Already clocked in for this session
                            Toast.makeText(context, "You have already clocked in.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // No record for today, create a new one
                        val updatedRecord = DTRRecord(
                            email = email,
                            date = currentTime,
                            morningArrival = if (currentHour < 12) currentTime else null,
                            afternoonArrival = if (currentHour >= 12) currentTime else null,
                            morningDeparture = null,
                            afternoonDeparture = null
                        )
                        // Create new record
                        db.collection("dtr_records").document(recordId).set(updatedRecord)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Clock-in successful!", Toast.LENGTH_SHORT).show()
                                onClockInSuccess() // Notify that clock-in was successful
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to create clock-in record", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error checking DTR record: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Button(
        onClick = {
            // Fetch geofence data first
            GeofenceUtility.fetchGeofenceData(
                onSuccess = { latitude, longitude, radius ->
                    // Validate geofence access
                    GeofenceUtils.validateGeofenceAccess(
                        fusedLocationClient,
                        latitude,
                        longitude,
                        radius,
                        context,
                        onSuccess = {
                            // Check if the user has already clocked in today
                            val currentTime = Calendar.getInstance().time
                            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            val recordId = "${email}-${getCurrentDate()}"

                            FirebaseFirestore.getInstance()
                                .collection("dtr_records")
                                .document(recordId)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        val morningArrival = documentSnapshot.getDate("morningArrival")
                                        val afternoonArrival = documentSnapshot.getDate("afternoonArrival")
                                        if ((currentHour < 12 && morningArrival != null) || (currentHour >= 12 && afternoonArrival != null)) {
                                            // Already clocked in, do not trigger the camera
                                            Toast.makeText(context, "You have already clocked in for today.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Proceed to launch the camera if not clocked in
                                            cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                                        }
                                    } else {
                                        // No DTR record exists, proceed to clock in
                                        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                                    }
                                }
                                .addOnFailureListener {
                                    // Handle any failure in fetching the DTR record
                                    Toast.makeText(context, "Error fetching DTR record.", Toast.LENGTH_SHORT).show()
                                }
                        },
                        onFailure = { errorMessage ->
                            // User is outside the geofence
                            Toast.makeText(context, "You are not within the geofenced area: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onFailure = { errorMessage ->
                    // Handle error in fetching geofence data
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        },
        modifier = Modifier.height(50.dp).width(150.dp)
    ) {
        Text(text = "Clock In")
    }
}

fun launchCamera(context: Context, cameraLauncher: ActivityResultLauncher<Intent>) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    cameraLauncher.launch(intent) // Launch the camera intent
}

@Composable
fun ClockOutButton(
    email: String,
    fusedLocationClient: FusedLocationProviderClient,
    onClockOutSuccess: () -> Unit // Callback to notify when clock-out is successful
) {
    val context = LocalContext.current
    val currentTime = Calendar.getInstance().time
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    Button(
        onClick = {
            // Validate if the user is inside the geofenced area before allowing clock-out
            GeofenceUtility.fetchGeofenceData(
                onSuccess = { latitude, longitude, radius ->
                    // Validate geofence access
                    GeofenceUtils.validateGeofenceAccess(
                        fusedLocationClient,
                        latitude,
                        longitude,
                        radius,
                        context,
                        onSuccess = {
                            // Proceed with clock-out logic after geofence validation
                            val db = FirebaseFirestore.getInstance()
                            val recordId = "${email}-${getCurrentDate()}" // Use email in recordId

                            db.collection("dtr_records")
                                .document(recordId)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        val morningArrival = documentSnapshot.getDate("morningArrival")
                                        val afternoonArrival = documentSnapshot.getDate("afternoonArrival")
                                        val morningDeparture = documentSnapshot.getDate("morningDeparture")
                                        val afternoonDeparture = documentSnapshot.getDate("afternoonDeparture")

                                        if (currentHour < 12 && morningArrival != null && morningDeparture == null) {
                                            // Morning clock-out
                                            val updatedRecord = DTRRecord(
                                                email = email,
                                                date = currentTime,
                                                morningArrival = morningArrival,
                                                afternoonArrival = afternoonArrival,
                                                morningDeparture = currentTime,
                                                afternoonDeparture = afternoonDeparture
                                            )
                                            db.collection("dtr_records").document(recordId).set(updatedRecord)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Morning clock-out successful!", Toast.LENGTH_SHORT).show()
                                                    onClockOutSuccess() // Notify that clock-out was successful
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Failed to update clock-out time", Toast.LENGTH_SHORT).show()
                                                }
                                        } else if (currentHour >= 12 && afternoonArrival != null && afternoonDeparture == null) {
                                            // Afternoon clock-out
                                            val updatedRecord = DTRRecord(
                                                email = email,
                                                date = currentTime,
                                                morningArrival = morningArrival,
                                                afternoonArrival = afternoonArrival,
                                                morningDeparture = morningDeparture,
                                                afternoonDeparture = currentTime
                                            )
                                            db.collection("dtr_records").document(recordId).set(updatedRecord)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Afternoon clock-out successful!", Toast.LENGTH_SHORT).show()
                                                    onClockOutSuccess() // Notify that clock-out was successful
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Failed to update clock-out time", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            // User has already clocked out for today
                                            Toast.makeText(context, "You have already clocked out.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        // No record for today, can't clock-out
                                        Toast.makeText(context, "No clock-in record found for today.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(context, "Error checking DTR record: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(context, "You are not within the geofenced area: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onFailure = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        },
        modifier = Modifier.height(50.dp).width(150.dp)
    ) {
        Text(text = "Clock Out")
    }
}


private fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
}



@Composable
fun RecordsDialog(records: List<DTRRecord>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "DTR Records",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (records.isEmpty()) {
                    Text("No records available.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    records.forEach { record ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val arrivalTime = record.morningArrival?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"
                        val morningDepartureTime = record.morningDeparture?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"
                        val afternoonArrivalTime = record.afternoonArrival?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"
                        val afternoonDepartureTime = record.afternoonDeparture?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"

                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            // Date Row
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Date: ${dateFormat.format(record.date)}", modifier = Modifier.weight(1f))
                            }

                            // AM Section
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("AM", modifier = Modifier.weight(1f), style = TextStyle(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.weight(1f)) // Push PM label to the right
                                Text("PM", modifier = Modifier.weight(1f), style = TextStyle(fontWeight = FontWeight.Bold))
                            }

                            // AM IN/OUT and PM IN/OUT Row
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // AM Times (IN/OUT)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("IN: $arrivalTime")
                                    Text("OUT: $morningDepartureTime")
                                }

                                // Spacer to align PM section to the right
                                Spacer(modifier = Modifier.weight(1f))

                                // PM Times (IN/OUT)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("IN: $afternoonArrivalTime")
                                    Text("OUT: $afternoonDepartureTime")
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
