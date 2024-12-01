package com.example.firebaseauth.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorCode = geofencingEvent.errorCode
            Log.e("GeofenceReceiver", "Geofencing error: $errorCode")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceReceiver", "Entered geofence")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceReceiver", "Exited geofence")
            }
            else -> {
                Log.e("GeofenceReceiver", "Unknown geofence transition: $geofenceTransition")
            }
        }
    }
}

