package com.example.firebaseauth.activity

import android.content.Context
import android.widget.Toast
import com.example.firebaseauth.model.DTRRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class DTRActivity(private val db: FirebaseFirestore) {

    fun handleClockInOut(
        context: Context, // Context is passed explicitly
        email: String,
        currentTime: Date,
        currentHour: Int,
        insideGeofence: Boolean,
        onTimeStamped: () -> Unit
    ) {
        val recordId = "${email}-${getCurrentDate()}"
        db.collection("dtr_records").document(recordId).get()
            .addOnSuccessListener { documentSnapshot ->
                val currentRecord = documentSnapshot.toObject(DTRRecord::class.java)

                if (insideGeofence) {
                    if (currentHour < 12 && currentRecord?.morningArrival == null) {
                        updateDTRRecord(
                            context,
                            recordId,
                            currentRecord,
                            currentTime,
                            morningArrival = currentTime,
                            onTimeStamped = onTimeStamped
                        )
                    } else if (currentHour >= 12 && currentRecord?.afternoonArrival == null) {
                        updateDTRRecord(
                            context,
                            recordId,
                            currentRecord,
                            currentTime,
                            afternoonArrival = currentTime,
                            onTimeStamped = onTimeStamped
                        )
                    }
                } else {
                    if (currentHour < 12 && currentRecord?.morningArrival != null && currentRecord.morningDeparture == null) {
                        updateDTRRecord(
                            context,
                            recordId,
                            currentRecord,
                            currentTime,
                            morningDeparture = currentTime,
                            onTimeStamped = onTimeStamped
                        )
                    } else if (currentHour >= 12 && currentRecord?.afternoonArrival != null && currentRecord.afternoonDeparture == null) {
                        updateDTRRecord(
                            context,
                            recordId,
                            currentRecord,
                            currentTime,
                            afternoonDeparture = currentTime,
                            onTimeStamped = onTimeStamped
                        )
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error checking DTR record.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDTRRecord(
        context: Context, // Context is passed explicitly
        recordId: String,
        currentRecord: DTRRecord?,
        currentTime: Date,
        morningArrival: Date? = null,
        afternoonArrival: Date? = null,
        morningDeparture: Date? = null,
        afternoonDeparture: Date? = null,
        onTimeStamped: () -> Unit
    ) {
        val updatedRecord = currentRecord?.copy(
            morningArrival = morningArrival ?: currentRecord.morningArrival,
            afternoonArrival = afternoonArrival ?: currentRecord.afternoonArrival,
            morningDeparture = morningDeparture ?: currentRecord.morningDeparture,
            afternoonDeparture = afternoonDeparture ?: currentRecord.afternoonDeparture
        ) ?: DTRRecord(
            email = recordId.split("-")[0],
            date = currentTime,
            morningArrival = morningArrival,
            afternoonArrival = afternoonArrival,
            morningDeparture = morningDeparture,
            afternoonDeparture = afternoonDeparture
        )

        db.collection("dtr_records").document(recordId).set(updatedRecord)
            .addOnSuccessListener {
                Toast.makeText(context, "DTR saved successfully!", Toast.LENGTH_SHORT).show()
                onTimeStamped()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update DTR record.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-indexed
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Format year, month, and day with leading zeros if necessary
        return String.format("%04d-%02d-%02d", year, month, dayOfMonth)
    }
}