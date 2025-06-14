// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
    dependencies {
        // Android Gradle Plugin version - ALIGNED WITH THE ERROR MESSAGE'S SUGGESTED VERSION
        // This classpath is crucial for Android Gradle Plugin to be found and used.
        classpath("com.android.tools.build:gradle:8.9.0") // Updated to 8.9.0 to resolve conflict
        // Google Services plugin for Firebase and other Google services (if needed)
        // Ensure this version is compatible with your AGP version.
        classpath("com.google.gms:google-services:4.4.1") // Keep as is unless specifically requested
    }
}

// This block declares plugins that are available for subprojects but not applied directly here.
// The 'apply false' ensures they are just defined, not automatically applied to the root project.
plugins {
    id("com.android.application") version "8.9.0" apply false // Updated version and using Kotlin DSL syntax
    id("com.android.library") version "8.9.0" apply false // Updated version and using Kotlin DSL syntax
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false // Updated to a more recent Kotlin version for compatibility with AGP 8.9.0
}
