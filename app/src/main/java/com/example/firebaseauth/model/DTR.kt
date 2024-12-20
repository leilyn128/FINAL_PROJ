package com.example.firebaseauth.model

import java.util.Date


data class DTRRecord(
    val email: String? = " ",
    val date: Date? = Date(),
    val morningArrival: Date? = null,
    val morningDeparture: Date? = null,
    val afternoonArrival: Date? = null,
    val afternoonDeparture: Date? = null
)

