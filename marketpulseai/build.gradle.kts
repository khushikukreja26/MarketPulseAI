// build.gradle.kts (PROJECT LEVEL)

plugins {
    // Use versions that Android Studio created for you if they differ
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // 🔥 This line fixes the error:
    id("com.google.dagger.hilt.android") version "2.51" apply false
}