plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.evchargerlocator_androidapplication"
    compileSdk = 34 // Ensure compatibility with dependencies

    defaultConfig {
        applicationId = "com.example.evchargerlocator_androidapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Set true only if ProGuard is configured correctly
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

    buildFeatures {
        viewBinding = true // ✅ Enable View Binding
    }
}

dependencies {
    // ✅ AndroidX & UI Components
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ✅ Firebase Dependencies
    implementation("com.google.firebase:firebase-auth:22.1.0")
    implementation("com.google.firebase:firebase-firestore:24.8.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.3.0")
    implementation("com.google.firebase:firebase-analytics:21.4.0")

    // ✅ Google Play Services
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // ✅ Glide (Image Loading)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // ✅ Image Picker (Ensure JitPack is added in `settings.gradle.kts`)
    implementation("com.github.dhaval2404:ImagePicker:2.1")

    // ✅ Core KTX (WindowCompat)
    implementation("androidx.core:core-ktx:1.12.0")

    // ✅ CircleImageView (For Profile Images & Status Indicator)
    implementation("de.hdodenhof:circleimageview:3.1.0") // **🔥 NEWLY ADDED 🔥**

    // ✅ Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// ✅ Apply Google Services Plugin
apply(plugin = "com.google.gms.google-services")
