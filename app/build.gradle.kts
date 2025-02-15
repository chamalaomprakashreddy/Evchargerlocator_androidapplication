plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.evchargerlocator_androidapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.evchargerlocator_androidapplication"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Using `implementation` with the correct Kotlin DSL syntax
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase dependencies
    implementation("com.google.firebase:firebase-auth:21.1.0")  // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore:24.3.0")  // Firebase Firestore
    implementation("com.google.firebase:firebase-database:20.0.5")  // Firebase Realtime Database

    // Google Play services
    implementation("com.google.android.gms:play-services-maps:19.0.0")  // Google Maps
    implementation("com.google.android.gms:play-services-location:21.3.0")  // Location Services

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase Messaging (optional, for push notifications)
    implementation("com.google.firebase:firebase-messaging:23.0.0")
    implementation("com.google.firebase:firebase-analytics:21.0.0")
}

// Apply the Google services plugin at the bottom of the file
apply(plugin = "com.google.gms.google-services")
