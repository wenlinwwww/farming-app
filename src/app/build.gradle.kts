plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.aq_bluering"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.aq_bluering"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
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
    // Authenticator dependency
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:1.1.5")
    //For AWSMobileClient only:
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.73.0")

//For the drop-in UI also:
    implementation("com.amazonaws:aws-android-sdk-auth-userpools:2.73.0")
    implementation("com.amazonaws:aws-android-sdk-auth-ui:2.73.0")

//For hosted UI also:
    implementation ("com.amazonaws:aws-android-sdk-cognitoauth:2.73.0")

    implementation ("com.amplifyframework.ui:authenticator:1.0.1")
    implementation ("com.amplifyframework:aws-auth-cognito:2.13.2")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.navigation:navigation-fragment:2.4.0")
    implementation("androidx.navigation:navigation-ui:2.4.0")
    implementation("com.google.android.gms:play-services-maps:17.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    modules {
        module("org.jetbrains.kotlin:kotlin-stdlib-jdk7") {
            replacedBy("org.jetbrains.kotlin:kotlin-stdlib", "kotlin-stdlib-jdk7 is now part of kotlin-stdlib")
        }
        module("org.jetbrains.kotlin:kotlin-stdlib-jdk8") {
            replacedBy("org.jetbrains.kotlin:kotlin-stdlib", "kotlin-stdlib-jdk8 is now part of kotlin-stdlib")
        }
    }
}