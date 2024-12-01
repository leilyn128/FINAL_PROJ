package com.example.firebaseauth.activity

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

fun uploadImageToFirebase(
    uri: Uri,
    onUploadSuccess: (String) -> Unit,
    onUploadFail: (String) -> Unit
) {
    val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    val fileName = "images/${UUID.randomUUID()}.jpg"

    val uploadTask = storageReference.child(fileName).putFile(uri)
    uploadTask.addOnSuccessListener {
        storageReference.child(fileName).downloadUrl.addOnSuccessListener { downloadUri ->
            onUploadSuccess(downloadUri.toString())
        }
    }.addOnFailureListener { exception ->
        onUploadFail(exception.message ?: "Upload failed")
    }
}

