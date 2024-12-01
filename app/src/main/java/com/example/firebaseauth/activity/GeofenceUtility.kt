package com.example.firebaseauth.activity

import com.google.firebase.firestore.FirebaseFirestore

object GeofenceUtility {

    fun fetchGeofenceData(
        onSuccess: (latitude: Double, longitude: Double, radius: Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("geofences")
            .document("bisu_clarin")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val radius = document.getDouble("radius") ?: 0.0
                    onSuccess(latitude, longitude, radius)
                } else {
                    onFailure("Geofence data not found")
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error fetching geofence data: ${exception.message}")
            }
    }
}
