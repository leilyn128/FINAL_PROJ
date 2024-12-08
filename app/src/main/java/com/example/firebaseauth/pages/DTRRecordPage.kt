package com.example.firebaseauth.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseauth.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.example.firebaseauth.model.DTRRecord


@Composable
fun DTRRecordPage(modifier: Modifier = Modifier) {
    val emailList = remember { mutableStateListOf<String>() }
    val selectedEmail = remember { mutableStateOf<String?>(null) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchAllEmails(emailList)
    }

    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .background(Color(0xFF5F8C60))
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Employees Account",
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (emailList.isEmpty()) {
            Text(text = "No employee records found.", style = TextStyle(fontSize = 16.sp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxHeight(0.8f)) {
                items(emailList.size) { index ->
                    val email = emailList[index]
                    EmailCard(
                        email = email,
                        onClick = {
                            selectedEmail.value = email
                            showDialog.value = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showDialog.value) {
            selectedEmail.value?.let { email ->
                val dtrRecords = remember { mutableStateListOf<DTRRecord>() }
                LaunchedEffect(email) {
                    fetchDTRRecordsForEmail(email, dtrRecords)
                }

                RecordsDialogAdmin(
                    records = dtrRecords,
                    onDismiss = { showDialog.value = false }
                )
            }
        }
    }
}

@Composable
fun EmailCard(email: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.LightGray
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Employee Email:", style = TextStyle(fontSize = 14.sp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = email, style = TextStyle(fontSize = 16.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick) {
                Text("View Records")
            }
        }
    }
}

suspend fun fetchAllEmails(emailList: MutableList<String>) {
    val db = FirebaseFirestore.getInstance()

    try {
        val snapshot = db.collection("dtr_records").get().await()
        val uniqueEmails = mutableSetOf<String>()

        snapshot.documents.forEach { doc ->
            val email = doc.getString("email")
            email?.let {
                uniqueEmails.add(it)
            }
        }

        emailList.clear()
        emailList.addAll(uniqueEmails)
    } catch (e: Exception) {
        Log.e("DTRRecordPage", "Error fetching emails: ${e.message}")
    }
}

suspend fun fetchDTRRecordsForEmail(email: String, dtrRecords: MutableList<DTRRecord>) {
    val db = FirebaseFirestore.getInstance()

    try {
        val snapshot = db.collection("dtr_records")
            .whereEqualTo("email", email)
            .get()
            .await()

        dtrRecords.clear()

        snapshot.documents.forEach { doc ->
            val record = doc.toObject(DTRRecord::class.java)
            record?.let {
                dtrRecords.add(it)
            }
        }
    } catch (e: Exception) {
        Log.e("DTRRecordPage", "Error fetching DTR records for $email: ${e.message}")
    }
}

@Composable
fun RecordsDialogAdmin(records: List<DTRRecord>, onDismiss: () -> Unit) {
    val sortedRecords = records.sortedByDescending { it.date }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "DTR Records",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (sortedRecords.isEmpty()) {
                    Text("No records available.", style = TextStyle(fontSize = 16.sp))
                } else {
                    sortedRecords.forEach { record ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val arrivalTime = record.morningArrival?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "N/A"
                        val morningDepartureTime = record.morningDeparture?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "N/A"
                        val afternoonArrivalTime = record.afternoonArrival?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "N/A"
                        val afternoonDepartureTime = record.afternoonDeparture?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "N/A"

                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Date: ${dateFormat.format(record.date)}", modifier = Modifier.weight(1f))
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("AM", modifier = Modifier.weight(1f), style = TextStyle(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.weight(1f)) // Push PM label to the right
                                Text("PM", modifier = Modifier.weight(1f), style = TextStyle(fontWeight = FontWeight.Bold))
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("IN: $arrivalTime")
                                    Text("OUT: $morningDepartureTime")
                                }

                                Spacer(modifier = Modifier.weight(1f))

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
