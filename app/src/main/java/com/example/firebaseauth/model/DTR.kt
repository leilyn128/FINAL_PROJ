package com.example.firebaseauth.model

import java.util.Date


data class DTRRecord(
    val email: String? = null,           // Use nullable types with default values
    val date: Date? = null,              // Firestore can handle `Date` type
    val morningArrival: Date? = null,
    val morningDeparture: Date? = null,
    val afternoonArrival: Date? = null,
    val afternoonDeparture: Date? = null
) {
    // Default constructor required for Firebase Firestore
    constructor() : this(null, null, null, null, null, null)
}
