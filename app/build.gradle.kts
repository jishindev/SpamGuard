plugins {
    id("com.android.application")
    kotlin("android")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "dev.jishin.android.spamguard"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.incremental"] = "true"
                arguments["room.expandProjection"] = "true"
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets.onEach {
        it.java.srcDir("src/${it.name}/kotlin")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.20")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")

    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    implementation("androidx.activity:activity-ktx:1.2.0-beta01")
    implementation("androidx.fragment:fragment-ktx:1.3.0-beta01")

    // ViewModel and lifecycle extensions
    val lifecycleVersion = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")/*
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")*/
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")/*
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")*/

    // Hilt DI
    val hiltVersion = "2.30.1-alpha"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    val hiltAndroidXVersion = "1.0.0-alpha02"
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:$hiltAndroidXVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltAndroidXVersion")

    // Room
    val roomVersion = "2.3.0-alpha03"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")


    // Logging
    implementation("com.jakewharton.timber:timber:4.7.1")

}

kapt {
    correctErrorTypes = true
}