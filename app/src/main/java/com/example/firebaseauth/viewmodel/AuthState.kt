package com.example.firebaseauth.viewmodel



import com.example.firebaseauth.viewmodel.AuthState.AuthResult
import com.google.firebase.auth.FirebaseUser


sealed class AuthState {

    object Unauthenticated : AuthState() // No user is logged in
    object Loading : AuthState() // Loading state for async tasks like login
    data class Authenticated(val user: FirebaseUser? = null) : AuthState() // Generic authenticated state
    data class AdminAuthenticated(val user: FirebaseUser) : AuthState() // Authenticated as admin
    data class EmployeeAuthenticated(val user: FirebaseUser) : AuthState() // Authenticated as employee
    data class Error(val message: String) : AuthState() // Error state
    object LoggedOut : AuthState() // Logged out state, could replace 'LoggedIn'
    data class LoggedIn(val email: String, val role: String) : AuthState()


    // AuthResult is for authentication result outcomes
    sealed class AuthResult {
        data class Success(val user: FirebaseUser) : AuthResult() // Successful login
        data class Failure(val message: String?) : AuthResult() // Failed login
        object LoggedOut : AuthResult() // User logged out successfully
    }
}
