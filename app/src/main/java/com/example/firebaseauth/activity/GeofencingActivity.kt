package com.example.firebaseauth.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauth.R
import com.example.firebaseauth.activity.FirestoreHelper
import com.google.firebase.firestore.FirebaseFirestore


class GeofencingActivity : AppCompatActivity() {

    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var latitudeInput: EditText
    private lateinit var longitudeInput: EditText
    private lateinit var radiusInput: EditText
    private lateinit var addGeofenceButton: Button
    private lateinit var geofenceList: RecyclerView

    private val geofenceAdapter = GeofenceAdapter(mutableListOf())
    private val firestoreHelper = FirestoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofencing)

        geofenceHelper = GeofenceHelper(this)
        latitudeInput = findViewById(R.id.latitudeInput)
        longitudeInput = findViewById(R.id.longitudeInput)
        radiusInput = findViewById(R.id.radiusInput)
        addGeofenceButton = findViewById(R.id.addGeofenceButton)
        geofenceList = findViewById(R.id.geofenceList)

        geofenceList.layoutManager = LinearLayoutManager(this)
        geofenceList.adapter = geofenceAdapter



        addGeofenceButton.setOnClickListener {
            val latitude = latitudeInput.text.toString().toDoubleOrNull()
            val longitude = longitudeInput.text.toString().toDoubleOrNull()
            val radius = radiusInput.text.toString().toFloatOrNull()

            if (latitude != null && longitude != null && radius != null) {
                geofenceHelper.addGeofence(latitude, longitude, radius)
                geofenceAdapter.addGeofence("Geofence at ($latitude, $longitude), Radius: $radius m")
            } else {
                Toast.makeText(
                    this,
                    "Please enter valid latitude, longitude, and radius",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveGeofenceData(latitude: Double, longitude: Double, radius: Double) {
        val firestore = FirebaseFirestore.getInstance()

        val geofenceData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "radius" to radius
        )

        val geofenceRef = firestore.collection("geofences").document("geofenceId") // Use unique ID

        geofenceRef.set(geofenceData)
            .addOnSuccessListener {
                Log.d("Geofence", "Geofence data saved successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Geofence", "Error saving geofence data: ${e.message}")
            }
    }
}

