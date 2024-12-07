package com.example.firebaseauth.activity

import android.util.Log
import com.google.android.gms.maps.model.LatLng

class GeofenceHelper {

    fun isPointInPolygon(point: LatLng, vertices: List<LatLng>): Boolean {
        var result = false
        val size = vertices.size
        var j = size - 1
        for (i in 0 until size) {
            val vi = vertices[i]
            val vj = vertices[j]
            if ((vi.latitude > point.latitude) != (vj.latitude > point.latitude) &&
                (point.longitude < (vj.longitude - vi.longitude) *
                        (point.latitude - vi.latitude) /
                        (vj.latitude - vi.latitude) + vi.longitude)
            ) {
                result = !result
            }
            j = i
        }
        return result
    }

    fun checkUserInsidePolygon(userLocation: LatLng, polygonVertices: List<LatLng>): Boolean {
        val isInside = isPointInPolygon(userLocation, polygonVertices)
        Log.d("GeofenceHelper", "User is ${if (isInside) "inside" else "outside"} the polygon geofence")
        return isInside
    }
}
