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
import com.example.firebaseauth.activity.DTRActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.example.firebaseauth.activity.GeofenceUtility
import com.example.firebaseauth.activity.GeofenceUtils


@Composable
fun DTR(
    viewModel: DTRController,
    email: String,
    fusedLocationClient: FusedLocationProviderClient,
    onTimeStamped: () -> Unit
) {
    val context = LocalContext.current
    val record = remember { mutableStateOf<DTRRecord?>(null) }
    val dtrRecords = viewModel.dtrRecords.collectAsState().value
    var showRecordsDialog by remember { mutableStateOf(false) }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todaysRecord = dtrRecords.find { record ->
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(record.date) == today
    }
    val dtrLogic = DTRActivity(FirebaseFirestore.getInstance())



    LaunchedEffect(todaysRecord) {
        record.value = todaysRecord
        onTimeStamped()
    }

    LaunchedEffect(email) {
        viewModel.fetchDTRRecords(email) // Ensure you fetch updated records on email change
    }

    LaunchedEffect(key1 = Unit) {
        GeofenceUtility.fetchGeofenceData(
            onSuccess = { polygonCoordinates ->
                GeofenceUtils.validateGeofenceAccess(
                    fusedLocationClient,
                    polygonCoordinates,
                    context,
                    onSuccess = { insideGeofence ->
                        dtrLogic.handleClockInOut(
                            context = context,
                            email = email,
                            currentTime = Calendar.getInstance().time,
                            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            insideGeofence = insideGeofence,
                            onTimeStamped = {
                                // Fetch the updated record after time-stamp
                                viewModel.fetchDTRRecords(email)
                                onTimeStamped() // Run any additional logic if needed
                            }
                        )
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

        if (todaysRecord == null) {
            Text("No records found for today.", style = MaterialTheme.typography.bodyLarge)
        } else {
            // Show today's DTR record
            DTRCard(record = todaysRecord)
        }

    }

    // Show dialog with records
    if (showRecordsDialog) {
        RecordsDialog(
            records = dtrRecords,
            onDismiss = { showRecordsDialog = false }
        )
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

    // Sort the records by date in descending order so the latest record appears on top
    val sortedRecords = records.sortedByDescending { it.date }

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
                if (sortedRecords.isEmpty()) {
                    Text("No records available.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    sortedRecords.forEach { record ->
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
