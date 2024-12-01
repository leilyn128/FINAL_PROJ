package com.example.firebaseauth.activity

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauth.R

class GeofenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val geofenceText: TextView = itemView.findViewById(R.id.geofenceText)

    fun bind(geofence: String) {
        geofenceText.text = geofence
    }
}

