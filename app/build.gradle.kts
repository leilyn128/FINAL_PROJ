import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.firebaseauth"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.firebaseauth"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Enable View Binding here
    buildFeatures {
        compose = true
        viewBinding = true // Correct syntax to enable view binding
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0" // Ensure compatibility with Compose UI
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.firestore)

    // Google Maps and Location
    implementation("com.google.android.gms:play-services-maps:18.1.0") // Updated version
    implementation("com.google.android.gms:play-services-location:21.0.1") // Latest version

    // Permission handling (Dexter library)
    implementation("com.karumi:dexter:6.2.3")

    // Optional: Coroutines for better async handling
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:2.0.0")
    implementation ("com.google.accompanist:accompanist-permissions:0.33.0-alpha")

    // Jetpack Compose dependencies
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.material3:material3:1.2.0") // Update to latest
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    // Other dependencies like lifecycle, viewmodel, etc.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation(libs.androidx.recyclerview)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("androidx.compose.material:material:1.5.0")  // Or the latest version
    implementation ("androidx.compose.material3:material3:1.0.0")  // For Material 3
    implementation ("androidx.compose.material:material-icons-extended:1.5.0") // For icons like Visibility and VisibilityOff

    implementation ("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")

    implementation ("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.firebase:firebase-storage:20.2.0")
    implementation ("com.google.mlkit:face-detection:16.1.3")

    implementation ("com.google.firebase:firebase-auth:21.0.5" )
    implementation ("com.google.firebase:firebase-firestore:24.4.3")// For Firestore
    }





