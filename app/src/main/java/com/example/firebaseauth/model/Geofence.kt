package com.example.firebaseauth.model

import com.google.android.gms.maps.model.LatLng


data class GeofenceData(
    val center: LatLng,
    val radius: Double
)
