plugins {
    id("com.android.application")
}

android {
    namespace = "com.cuantollevo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cuantollevo"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Sin dependencias: la app usa solo el SDK de Android.
