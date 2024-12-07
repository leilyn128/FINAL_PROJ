package com.example.firebaseauth.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.LatLng

class GeofenceReceiver : BroadcastReceiver() {

    private val db = FirebaseFirestore.getInstance()
    private val geofenceHelper = GeofenceHelper() // Initialize the helper

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

            db.collection("geofences")
                .document("bisu_clarin")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val points = document.get("polygonPoints") as? List<Map<String, Double>>

                        points?.let { polygonPoints ->
                            val polygonLatLngList = polygonPoints.map { point ->
                                LatLng(point["latitude"] ?: 0.0, point["longitude"] ?: 0.0)
                            }

                            val userLocation = it.triggeringLocation

                            if (userLocation != null) {
                                val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)

                                val isInside = geofenceHelper.checkUserInsidePolygon(userLatLng, polygonLatLngList)

                                when (transition) {
                                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                        if (isInside) {
                                            handleGeofenceEntry(context)
                                        }
                                    }
                                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                        if (!isInside) {
                                            handleGeofenceExit(context)
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Location is null", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(context, "Entered geofenced area.", Toast.LENGTH_SHORT).show()
    }

    private fun handleGeofenceExit(context: Context) {
        Toast.makeText(context, "Exited geofenced area.", Toast.LENGTH_SHORT).show()
    }

    private fun getEmailFromFirebaseAuth(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }
}
