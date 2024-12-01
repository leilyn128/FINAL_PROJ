package com.example.firebaseauth.activity

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class GeofenceHelper(private val context: Context) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    fun addGeofence(lat: Double, lon: Double, radius: Float) {

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val geofence = Geofence.Builder()
                .setRequestId("geofence_id_${System.currentTimeMillis()}")
                .setCircularRegion(lat, lon, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener {
                    Log.d("GeofenceHelper", "Geofence added successfully")
                }
                .addOnFailureListener {
                    Log.d("GeofenceHelper", "Failed to add geofence: ${it.message}")
                }
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }
}
