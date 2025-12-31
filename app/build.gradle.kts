plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.flashcard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.flashcard"
        minSdk = 27
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
}

dependencies {
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // Thư viện kết nối mạng (OkHttp)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
// Thư viện xử lý JSON (Gson)
    implementation("com.google.code.gson:gson:2.10.1")
// Thư viện tải ảnh (Glide) - Để hiển thị ảnh từ URL
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.material:material:1.9.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}