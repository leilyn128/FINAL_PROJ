package com.example.firebaseauth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firebaseauth.model.UserProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableLiveData<UserProfileData>()
    val userProfile: LiveData<UserProfileData> get() = _userProfile

    fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        userId?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val employeeID = document.getString("employeeID") ?: "No ID Number"
                        val username = document.getString("username") ?: "No name"
                        val email = document.getString("email") ?: "No email"
                        _userProfile.value = UserProfileData(employeeID , username, email)
                    }
                }
                .addOnFailureListener {

                }
            }
        }
    }
