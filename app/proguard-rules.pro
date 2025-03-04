# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Glide classes (For Image Loading)
-keep class com.bumptech.glide.** { *; }
-keep class com.github.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Keep ImagePicker classes
-keep class com.github.dhaval2404.imagepicker.** { *; }

# Keep View Binding (Prevents Stripping)
-keep class androidx.viewbinding.** { *; }

# Avoid stripping any annotated methods or fields
-keepattributes *Annotation*

# Prevent class stripping for Retrofit (If used)
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Prevent Stripping Model Classes
-keep class com.example.evchargerlocator_androidapplication.model.** { *; }

# Keep AndroidX Lifecycle Components
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep Room Database Entities (If using Room)
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep WebView JavaScript Interface (If WebView is used)
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide original source file name (Security & Obfuscation)
-renamesourcefileattribute SourceFile
