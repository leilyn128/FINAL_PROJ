package com.example.firebaseauth.activity

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs

object GeofenceUtils {

        fun validateGeofenceAccess(
            fusedLocationClient: FusedLocationProviderClient,
            polygonCoordinates: List<LatLng>,
            context: Context,
            onSuccess: (Boolean) -> Unit,
            onFailure: (String) -> Unit
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    val isInsidePolygon = isPointInPolygon(userLatLng, polygonCoordinates)
                    onSuccess(isInsidePolygon)
                } else {
                    onFailure("Failed to retrieve location.")
                }
            }
        }

        private fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
            var isInside = false
            var j = polygon.size - 1
            for (i in polygon.indices) {
                if (point.latitude > polygon[i].latitude != point.latitude > polygon[j].latitude &&
                    point.longitude < (polygon[j].longitude - polygon[i].longitude) * (point.latitude - polygon[i].latitude) / (polygon[j].latitude - polygon[i].latitude) + polygon[i].longitude
                ) {
                    isInside = !isInside
                }
                j = i
            }
            return isInside
        }
    }
