package com.example.firebaseauth.model


data class UserModel(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val employeeID: String = "",
    val role: String = "employee" // Default role
)



