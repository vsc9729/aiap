import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.geekyants.synchronoss"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.geekyants.synchronoss"
        minSdk = 21
        targetSdk = 34
        versionCode = 6
        versionName = "3.0.1"




        signingConfigs {
            create("release") {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                val keystoreProperties = Properties()

                if (keystorePropertiesFile.exists()) {
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                }

                storeFile = file(keystoreProperties.getProperty("storeFile", "keystore.jks"))
                storePassword = keystoreProperties.getProperty("storePassword", "")
                keyAlias = keystoreProperties.getProperty("keyAlias", "")
                keyPassword = keystoreProperties.getProperty("keyPassword", "")

            }
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                signingConfig = signingConfigs.getByName("release")
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
        buildFeatures {
            compose = true
        }
    }


    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.compose.ui.google.fonts)
        implementation(libs.android.billing.client)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
        implementation(libs.hilt.android)
        kapt(libs.hilt.android.compiler)
        implementation(libs.androidx.hilt.navigation.compose)
//        implementation(libs.aiap.synchronoss)
    }
    kapt {
        correctErrorTypes = true
    }
}
dependencies {
    implementation(project(":aiap"))
}
