plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

hilt {
    enableAggregatingTask = false
}

android {
    namespace = "com.example.busappkotlin"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.busappkotlin"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
}

dependencies {
    implementation(libs.androidx.media3.common.ktx)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor.v4110)
    implementation(libs.converter.gson)
    implementation(libs.retrofit)
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":core"))
    implementation(project(":network"))
    implementation(project(":feature_login"))
    implementation(project(":feature_chatbot"))
    implementation(project(":feature_home"))
    implementation(project(":feature_navigateBar"))
    implementation(project(":feature_saveRoute"))
    implementation(project(":feature_guidemap"))
    implementation(project(":feature_location"))
    implementation(project(":feature_trip_notification"))
}