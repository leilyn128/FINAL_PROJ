package com.example.firebaseauth.viewmodel


import com.google.firebase.auth.FirebaseUser


sealed class AuthState {

    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser? = null) : AuthState()
    data class AdminAuthenticated(val user: FirebaseUser) : AuthState()
    data class EmployeeAuthenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()

    sealed class AuthResult {
        object LoggedOut : AuthResult()
    }
}
