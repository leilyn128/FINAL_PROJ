package com.example.firebaseauth.pages

import DTRController
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.FusedLocationProviderClient
import com.example.firebaseauth.activity.GeofenceUtility
import com.example.firebaseauth.activity.GeofenceUtils


@Composable
fun DTR(
    viewModel: DTRController,
    email: String,
    fusedLocationClient: FusedLocationProviderClient,
    onTimeStamped: () -> Unit // Callback to notify that time has been stamped
) {
    val context = LocalContext.current
    val currentTime = Calendar.getInstance().time
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val record = remember { mutableStateOf<DTRRecord?>(null) } // Holds the fetched DTR record

    val dtrRecords = viewModel.dtrRecords.collectAsState().value
    var showRecordsDialog by remember { mutableStateOf(false) }

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    // Fetch records on component load
    LaunchedEffect(email) {
        viewModel.fetchDTRRecords(email) // Fetch records for the given email
    }
        // Handle geofence validation, clock-in, and clock-out logic
    LaunchedEffect(key1 = Unit) {
        GeofenceUtility.fetchGeofenceData(
            onSuccess = { polygonCoordinates ->
                Log.d("GeofenceData", "Fetched coordinates: $polygonCoordinates")

                GeofenceUtils.validateGeofenceAccess(
                    fusedLocationClient,
                    polygonCoordinates,
                    context,
                    onSuccess = { insideGeofence ->
                        Log.d("GeofenceValidation", "Inside geofence: $insideGeofence")

                        val db = FirebaseFirestore.getInstance()
                        val recordId = "${email}-${getCurrentDate()}"

                        db.collection("dtr_records")
                            .document(recordId)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                val currentRecord = documentSnapshot.toObject(DTRRecord::class.java)

                                if (insideGeofence) {
                                    // Clock-In Logic
                                    if (currentHour < 12 && currentRecord?.morningArrival == null) {
                                        val updatedRecord = currentRecord?.copy(
                                            morningArrival = currentTime
                                        ) ?: DTRRecord(
                                            email = email,
                                            date = currentTime,
                                            morningArrival = currentTime
                                        )
                                        db.collection("dtr_records").document(recordId)
                                            .set(updatedRecord)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Morning clock-in successful!", Toast.LENGTH_SHORT).show()
                                                record.value = updatedRecord
                                                onTimeStamped()
                                            }
                                    } else if (currentHour >= 12 && currentRecord?.afternoonArrival == null) {
                                        val updatedRecord = currentRecord?.copy(
                                            afternoonArrival = currentTime
                                        ) ?: DTRRecord(
                                            email = email,
                                            date = currentTime,
                                            afternoonArrival = currentTime
                                        )
                                        db.collection("dtr_records").document(recordId)
                                            .set(updatedRecord)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Afternoon clock-in successful!", Toast.LENGTH_SHORT).show()
                                                record.value = updatedRecord
                                                onTimeStamped()
                                            }
                                    }
                                } else {
                                    // Clock-Out Logic (when exiting the geofence or manually clocking out)
                                    if (currentHour < 12) {
                                        if (currentRecord?.morningArrival == null) {
                                            Toast.makeText(context, "Please clock-in first before clocking out in the morning.", Toast.LENGTH_SHORT).show()
                                        } else if (currentRecord?.morningDeparture == null) {
                                            val updatedRecord = currentRecord?.copy(
                                                morningDeparture = currentTime
                                            )
                                            db.collection("dtr_records").document(recordId)
                                                .set(updatedRecord!!)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Morning clock-out successful!", Toast.LENGTH_SHORT).show()
                                                    record.value = updatedRecord
                                                    onTimeStamped()
                                                }
                                        }
                                    } else {
                                        if (currentRecord?.afternoonArrival == null) {
                                            Toast.makeText(context, "Please clock-in first before clocking out in the afternoon.", Toast.LENGTH_SHORT).show()
                                        } else if (currentRecord?.afternoonDeparture == null) {
                                            val updatedRecord = currentRecord?.copy(
                                                afternoonDeparture = currentTime
                                            )
                                            db.collection("dtr_records").document(recordId)
                                                .set(updatedRecord!!)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Afternoon clock-out successful!", Toast.LENGTH_SHORT).show()
                                                    record.value = updatedRecord
                                                    onTimeStamped()
                                                }
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error checking DTR record.", Toast.LENGTH_SHORT).show()
                            }
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(context, "Geofence validation failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onFailure = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Always display the DTRCard
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DTRCustomHeader(onViewRecordsClick = {
            showRecordsDialog = true // Open the dialog when the icon is clicked
        })

        if (dtrRecords.isNotEmpty()) {
            DTRCard(record = dtrRecords.last()) // Display the most recent record
        } else {
            Text("No records available.")
        }
    }
    if (showRecordsDialog) {
        RecordsDialog(
            records = dtrRecords,
            onDismiss = { showRecordsDialog = false }
        )

        record.value?.let {
            DTRCard(record = it)
        } ?: CircularProgressIndicator() // Show loading until record is fetched
    }


}





@Composable
fun DTRCustomHeader(onViewRecordsClick: () -> Unit) {
    val customGreen = Color(0xFF5F8C60)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(customGreen)
            .padding(vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(75.dp)
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)

        )

        Text(
            text = "Daily Time Record",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = {
                onViewRecordsClick() // This should set showRecordsDialog to true in parent
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Icon(imageVector = Icons.Default.List, contentDescription = "View Records", tint = Color.White)
        }
    }
}


@Composable
fun DTRCard(record: DTRRecord) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val arrivalTime =
        record.morningArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) }
            ?: "____"
    val morningDepartureTime =
        record.morningDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) }
            ?: "____"
    val afternoonArrivalTime =
        record.afternoonArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) }
            ?: "____"
    val afternoonDepartureTime =
        record.afternoonDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) }
            ?: "____"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding to ensure it doesn't touch screen edges
        contentAlignment = Alignment.Center // This will center the card inside the Box
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Adjust width as needed
                .height(350.dp),  // Adjust height as needed
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "Date: ${dateFormat.format(record.date)}",
                    style = TextStyle(fontSize = 18.sp, color = Color.White)
                )
                Text(
                    "Morning Arrival: $arrivalTime",
                    style = TextStyle(fontSize = 18.sp, color = Color.White)
                )
                Text(
                    "Morning Departure: $morningDepartureTime",
                    style = TextStyle(fontSize = 18.sp, color = Color.White)
                )
                Text(
                    "Afternoon Arrival: $afternoonArrivalTime",
                    style = TextStyle(fontSize = 18.sp, color = Color.White)
                )
                Text(
                    "Afternoon Departure: $afternoonDepartureTime",
                    style = TextStyle(fontSize = 18.sp, color = Color.White)
                )
            }
        }
    }
}

@Composable
fun RecordsDialog(records: List<DTRRecord>, onDismiss: () -> Unit) {
    Log.d("DTR", "Displaying DTR records dialog with ${records.size} records.")

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
                                Text(
                                    "Date: ${dateFormat.format(record.date)}",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // AM Section
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "AM",
                                    modifier = Modifier.weight(1f),
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Push PM label to the right
                                Text(
                                    "PM",
                                    modifier = Modifier.weight(1f),
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )
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
