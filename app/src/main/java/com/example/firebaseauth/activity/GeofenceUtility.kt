package com.example.firebaseauth.activity

import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.LatLng

object GeofenceUtility {

    fun fetchGeofenceData(
        onSuccess: (List<LatLng>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("geofences")
            .document("bisu_clarin")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val points = document.get("polygonPoints") as? List<Map<String, Double>>

                    if (points != null) {
                        val polygonLatLngList = points.map { point ->
                            LatLng(point["latitude"] ?: 0.0, point["longitude"] ?: 0.0)
                        }
                        onSuccess(polygonLatLngList)
                    } else {
                        onFailure("Polygon points not found")
                    }
                } else {
                    onFailure("Geofence data not found")
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error fetching geofence data: ${exception.message}")
            }
    }
}
