package com.example.firebaseauth.activity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    fun getUserRole(onRoleFetched: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "employee"
                        onRoleFetched(role)
                    } else {
                        onRoleFetched("employee")
                    }
                }
                .addOnFailureListener {
                    onRoleFetched("employee")
                }
        } else {
            onRoleFetched("employee")
        }
    }
}