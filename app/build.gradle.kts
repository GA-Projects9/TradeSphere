plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.tradesphere"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tradesphere"
        minSdk = 24
        targetSdk = 33
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-crashlytics:18.6.0")

    // the dependency for the Google Play services library
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    //country code picker
    implementation("com.hbb20:ccp:2.6.1")

    //pin view
    implementation("io.github.chaosleung:pinview:1.4.4")

    //glide library - image processing
    implementation ("com.github.bumptech.glide:glide:4.16.0")

//    implementation ("com.github.jitpack:android-example:1.0.1")

    //google maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.libraries.places:places:3.3.0")

//    //slider image show
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.2")

    //firebase cloud messaging
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}