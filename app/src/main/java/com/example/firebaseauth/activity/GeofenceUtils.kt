package com.example.firebaseauth.activity

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import kotlin.math.pow
import kotlin.math.sqrt

object GeofenceUtils {

    fun validateGeofenceAccess(
        fusedLocationClient: FusedLocationProviderClient,
        geofenceLatitude: Double,
        geofenceLongitude: Double,
        geofenceRadius: Double,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val userLatitude = location.latitude
                    val userLongitude = location.longitude

                    // Calculate distance between user and geofence
                    val distance = calculateDistance(
                        userLatitude,
                        userLongitude,
                        geofenceLatitude,
                        geofenceLongitude
                    )

                    if (distance <= geofenceRadius) {
                        onSuccess()
                    } else {
                        onFailure("") // Invoke onFailure if outside geofence
                    }
                } else {
                    onFailure("Unable to determine location") // Invoke onFailure if location is not found
                }
            }
            .addOnFailureListener {
                onFailure("Error fetching location: ${it.message}")
            }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        return sqrt((lat1 - lat2).pow(2) + (lon1 - lon2).pow(2)) * 111_000 // Approx meters
    }
}
//