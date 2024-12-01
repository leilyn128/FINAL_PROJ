package com.example.firebaseauth.activity

import DTRViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.firebaseauth.activity.uploadImageToFirebase
import com.example.firebaseauth.databinding.ActivityCameraBinding
import com.example.firebaseauth.model.DTRRecord
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.util.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.text.SimpleDateFormat


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var imageUri: Uri
    private lateinit var dtrViewModel: DTRViewModel

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        dtrViewModel = ViewModelProvider(this).get(DTRViewModel::class.java)

        // Initialize the image URI
        imageUri = createUri()

        // Register the ActivityResultLauncher
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    // Display the captured image in the ImageView
                    binding.imageView.setImageURI(imageUri)
                    // Detect face in the captured image
                    detectFaceInImage(imageUri)
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }

        // Set up the button to take a picture
        binding.btnTakePicture.setOnClickListener {
            if (checkCameraPermission()) {
                if (::imageUri.isInitialized) {
                    takePictureLauncher.launch(imageUri)
                } else {
                    Log.e("CameraActivity", "imageUri is null!")
                    Toast.makeText(this, "Failed to prepare image URI", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun createUri(): Uri {
        return try {
            val imageFile = File(applicationContext.filesDir, "camera_photo.jpg")
            FileProvider.getUriForFile(applicationContext, "com.example.firebaseauth.fileProvider", imageFile)
        } catch (e: Exception) {
            Log.e("CameraActivity", "Error creating URI", e)
            throw e
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    // Handle permission results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(imageUri)
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detectFaceInImage(imageUri: Uri) {
        try {
            // Get the bitmap from the image URI
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // Configure the face detector options
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            // Create a face detector with the configured options
            val detector = FaceDetection.getClient(options)

            // Start processing the input image to detect faces
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        // Face detected, proceed with uploading the image
                        uploadImageToFirebase(imageUri, ::onUploadSuccess, ::onUploadFail)
                    } else {
                        // No face detected
                        Toast.makeText(this, "No face detected in the image", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors during face detection
                    Log.e("FaceDetection", "Face detection failed: $e")
                    Toast.makeText(this, "Face detection failed", Toast.LENGTH_SHORT).show()
                }
        } catch (e: IOException) {
            // Handle any errors related to loading the image
            Log.e("CameraActivity", "Failed to load image for face detection: $e")
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onUploadSuccess(photoUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            saveImageUrlToFirestore(userId, photoUrl)
        }
        saveDTRRecord(photoUrl)
    }

    private fun onUploadFail(error: String) {
        Toast.makeText(this, "Image upload failed: $error", Toast.LENGTH_SHORT).show()
    }

    private fun saveDTRRecord(photoUrl: String) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val arrivalTime = timeFormat.format(Calendar.getInstance().time)


    }

    private fun saveImageUrlToFirestore(userId: String, imageUrl: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userDoc = firestore.collection("users").document(userId)
        userDoc.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile image URL saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to save profile image URL", exception)
            }
    }
}
