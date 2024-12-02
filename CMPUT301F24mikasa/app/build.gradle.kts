plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cmput301f24mikasa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cmput301f24mikasa"
        minSdk = 33
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions {
        // No need for includeAndroidResources for Robolectric
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.camera.view)
    implementation(libs.firebase.auth)
    implementation(libs.espresso.intents)
    testImplementation(libs.junit)
    testImplementation(libs.core)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)



    // Other dependencies...
    implementation(libs.javax.annotation.api)
    implementation(libs.jsr305)


    implementation(libs.picasso)

    implementation(libs.zxing)
    implementation(libs.multidex)

    implementation(libs.mlkit.barcode.scanning)

    implementation(libs.glide)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.appcompat.v161)
    implementation(libs.camera.core.v130)
    implementation(libs.camera.camera2.v130)
    implementation(libs.camera.lifecycle.v130)
    implementation(libs.mlkit.barcode.scanning.v1703)
    implementation(libs.constraintlayout.v214)
    implementation(libs.material.v190)


    implementation(libs.guava)


    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.junit.v113)
    androidTestImplementation(libs.espresso.core.v340)


    // Mockito for mocking Firebase interactions
    testImplementation(libs.mockito.inline)

    // AndroidX testing dependencies
    testImplementation(libs.core.v140)
    testImplementation(libs.ext.junit)

    // Mockito for unit testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)

    // Mockito for Android instrumentation tests
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // Espresso for UI testing
    androidTestImplementation(libs.espresso.core)

    // JUnit for instrumented tests
    androidTestImplementation(libs.ext.junit)

    // Mockito for mocking dependencies (optional for instrumented tests)
    androidTestImplementation(libs.mockito.android.v460)

    // Mockito for mocking
    androidTestImplementation(libs.mockito.core.v461)
    // Glide for image loading
    androidTestImplementation(libs.glide.v4142)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.core.v461) // Mockito for mocking

    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)
    androidTestImplementation(libs.google.firebase.firestore)
    androidTestImplementation(libs.mockito.core.v400)
    androidTestImplementation(libs.mockito.android.v400)
    androidTestImplementation(libs.core.v150)
    androidTestImplementation(libs.rules)
    implementation(libs.work.runtime)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.play.services.maps.v1810)

}
