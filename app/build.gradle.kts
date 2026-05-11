import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)

    //room db
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.example.smartinventarymanagement"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.smartinventarymanagement"
        minSdk = 26
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
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //bottom nav
    implementation("com.google.android.material:material:1.11.0")

    // Room DB
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    ksp("androidx.room:room-compiler:2.8.4")

    //news
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    //Pie Chart Library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //bar chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //image
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //image crop
    implementation("com.github.yalantis:ucrop:2.2.8")

    // Export Libraries
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // Chrome Custom Tabs
    implementation("androidx.browser:browser:1.8.0")
}