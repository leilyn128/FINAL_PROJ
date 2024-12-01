package com.example.firebaseauth.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class GeofenceReceiver : BroadcastReceiver() {

    private val db = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        geofencingEvent?.let {
            if (it.hasError()) {
                return
            }

            val transition = it.geofenceTransition
            val email = getEmailFromFirebaseAuth()

            if (email.isNullOrEmpty()) {
                Toast.makeText(context, "User email not found", Toast.LENGTH_SHORT).show()
                return
            }

            // Fetch geofence data from Firebase
            db.collection("geofences")
                .document("bisu_clarin")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val lat = document.getDouble("latitude") ?: 0.0
                        val lon = document.getDouble("longitude") ?: 0.0
                        val radius = document.getDouble("radius") ?: 0.0

                        when (transition) {
                            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                // Handle entry to the geofence area
                                handleGeofenceEntry(context)
                            }
                            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                // Handle exit from the geofence area
                                handleGeofenceExit(context)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching geofence: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleGeofenceEntry(context: Context) {
        // You can perform any action you want when the user enters the geofenced area
        Toast.makeText(context, "Entered geofenced area.", Toast.LENGTH_SHORT).show()
        // Example: You could trigger a notification, log an event, or update the UI
    }

    private fun handleGeofenceExit(context: Context) {
        // You can perform any action you want when the user exits the geofenced area
        Toast.makeText(context, "Exited geofenced area.", Toast.LENGTH_SHORT).show()
        // Example: Log the event, send a notification, etc.
    }

    private fun getEmailFromFirebaseAuth(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }
}
