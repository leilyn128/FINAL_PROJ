package com.example.firebaseauth.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseauth.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.firestore.FirebaseFirestore

class GeofencingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val vertices = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofencing)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setOnMapClickListener { latLng ->
            vertices.add(latLng)
            googleMap.addMarker(MarkerOptions().position(latLng))
        }

        googleMap.setOnPolygonClickListener { polygon ->
            Toast.makeText(this, "Polygon clicked!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.savePolygonButton).setOnClickListener {
            if (vertices.size >= 3) {
                savePolygonGeofenceData(vertices)
                googleMap.addPolygon(
                    PolygonOptions()
                        .addAll(vertices)
                        .strokeColor(0xFF00FF00.toInt())
                        .fillColor(0x3300FF00)
                )
                vertices.clear()
            } else {
                Toast.makeText(this, "At least 3 points are required to create a polygon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePolygonGeofenceData(vertices: List<LatLng>) {
        val firestore = FirebaseFirestore.getInstance()


        val geofenceData = hashMapOf(
            "vertices" to vertices.map { hashMapOf("lat" to it.latitude, "lng" to it.longitude) }
        )

        firestore.collection("geofences").add(geofenceData)
            .addOnSuccessListener {
                Log.d("Geofence", "Polygon geofence saved successfully!")
                Toast.makeText(this, "Polygon geofence saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Geofence", "Error saving polygon geofence: ${e.message}")
                Toast.makeText(this, "Failed to save polygon geofence", Toast.LENGTH_SHORT).show()
            }
    }
}
