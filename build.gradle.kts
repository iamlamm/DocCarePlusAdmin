buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
//    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
//    id("org.jetbrains.kotlin.kapt") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}