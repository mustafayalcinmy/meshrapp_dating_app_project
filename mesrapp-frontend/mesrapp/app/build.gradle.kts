import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.mesrapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mesrapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders.putIfAbsent("redirectHostName", "main")
        manifestPlaceholders.putIfAbsent("redirectSchemeName", "mesrapp")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val keystoreFile = project.rootProject.file("local.properties")
            val properties = Properties()
            properties.load(keystoreFile.inputStream())
            val apiKey = properties.getProperty("gemini_api_key") ?: ""
            buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
        }
        debug {
            val keystoreFile = project.rootProject.file("local.properties")
            val properties = Properties()
            properties.load(keystoreFile.inputStream())
            val apiKey = properties.getProperty("gemini_api_key") ?: ""
            buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")

        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "30.0.3"

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.github.muddz:styleabletoast:2.4.0")
    implementation("androidx.webkit:webkit:1.4.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.vanniktech:android-image-cropper:4.5.0")
    implementation ("com.github.Spikeysanju:MotionToast:1.4")
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")
    implementation ("com.google.android.gms:play-services-location:20.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.firestore.ktx)

    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    kapt("androidx.room:room-compiler:$room_version")


    implementation(libs.play.services.auth)
    implementation("com.spotify.android:auth:2.1.1")
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.protolite.well.known.types)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}