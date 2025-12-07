plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services) // plugin google-services
}

android {
    namespace = "com.example.doan_ai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.doan_ai"
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
    kotlinOptions {
        jvmTarget = "11"
    }

    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    // AndroidX cơ bản
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase BoM (nên dùng để tự quản lý version)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Firebase Auth + Database
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-firestore-ktx")


    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.guava:guava:31.1-android")

    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("androidx.camera:camera-extensions:1.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")

    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
// Thư viện Dots Indicator
    implementation("com.tbuonomo:dotsindicator:4.3")

    // Thư viện TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
//    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("com.google.code.gson:gson:2.10.1")
//    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    constraints {
        api("org.tensorflow:tensorflow-lite-api:2.17.0")
    }

}
