plugins {
//    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.1.0"
    id("app.cash.paparazzi")
}

//android {
//    namespace = "com.example.meetuptodoapp.composables"
//    compileSdk = 35
//
//    testOptions {
//        unitTests {
//            isReturnDefaultValues = true
//            isIncludeAndroidResources = true
//        }
//    }
//    defaultConfig {
//        minSdk = 28
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        consumerProguardFiles("consumer-rules.pro")
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//}
//
//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.material3)
//    implementation(libs.kotlinx.serialization.json)
//    implementation(libs.datastore)
//    // this might be problematic with paparazzi
//    testImplementation(libs.koin.test)
//    testImplementation(libs.koin.test.junit4)
//    testImplementation(libs.robolectric)
//    testImplementation(libs.junit)
//    testImplementation(libs.androidx.junit)
//    testImplementation(libs.robolectric)
//    testImplementation(libs.mockk)
//    testImplementation(libs.androidx.ui.test.junit4)
//    testImplementation(libs.androidx.ui.test.manifest)
//    testImplementation(libs.kotlinx.coroutines.test)
//
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//}