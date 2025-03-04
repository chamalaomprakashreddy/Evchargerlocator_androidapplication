pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // ✅ Ensure JitPack is properly included
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // ✅ Ensure JitPack for third-party libraries
    }
}

// ✅ Set project name properly
rootProject.name = "Evchargerlocator_androidapplication"

// ✅ Include app module
include(":app")
