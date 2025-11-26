plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs") version "2.7.0"
}

android {
    namespace = "com.example.eventlottery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.eventlottery"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation("com.google.firebase:firebase-messaging")
    implementation ("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-functions:20.4.1")
    implementation(libs.firebase.functions)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")

    implementation("com.cloudinary:cloudinary-android:2.3.1")
    // Glide for image loading

    // Glide (image loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // QR Scanner
    implementation(libs.qr.scanner)

}