plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
//    id("org.jetbrains.kotlin.kapt")
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.healthtech.doccareplusadmin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.healthtech.doccareplusadmin"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes.add("META-INF/NOTICE.md")
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/*.properties")
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/LGPL2.1")
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
    buildFeatures {
        android.buildFeatures.buildConfig = true
    }
}

dependencies {
    // Chart
    implementation(libs.mpandroidchart)
    // Cloudinary
    implementation(libs.cloudinary.android)
    // ZegoCloud
    implementation(libs.zego.uikit.prebuilt.call.android)
    implementation(libs.zego.inapp.chat.uikit.android)
    implementation(libs.dexter)
    // Hỗ trợ media
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // Timber
    implementation(libs.timber)
    // Volley
    implementation(libs.volley)
    // lottie
    implementation(libs.lottie)
    // Library for banner
    implementation(libs.androidx.viewpager2)
    implementation(libs.material.v1110)
    implementation(libs.circleindicator)
    // CircleImageView
    implementation(libs.circleimageview)
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.messaging)
    // Play Services
    implementation(libs.play.services.safetynet)
    implementation(libs.play.services.auth)
    // Architecture Components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.legacy.support.v4)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.androidx.room.compiler)
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.core)
    // https://mvnrepository.com/artifact/com.github.bumptech.glide/glide
    implementation(libs.glide)
    // Gson
    implementation(libs.gson)
    // RecyclerView
    implementation(libs.androidx.recyclerview)
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(kotlin("script-runtime"))
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
    }
}

//allprojects {
//    tasks.matching { it.name.contains("kapt") }.configureEach {
//        enabled = false
//    }
//}