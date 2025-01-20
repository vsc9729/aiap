import java.util.Locale

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("maven-publish")
    id("com.google.devtools.ksp")
    id("jacoco")
}

android {
    namespace = "com.synchronoss.aiap"
    compileSdk = 35
    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }
    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    sourceSets {
        getByName("test") {
            java.srcDirs("src/main/java", "src/test/java")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.compose.ui.google.fonts)

    // Lifecycle & Data
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.datastore.preferences)

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    testImplementation(libs.junit.jupiter)
    kapt(libs.hilt.android.compiler)

    // Network & Serialization
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Image Loading
    implementation(libs.coil.compose)

    // Billing
    implementation(libs.android.billing.client)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

val exclusions = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*"
)

tasks.withType(Test::class) {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}
androidComponents {
    onVariants { variant ->
        // Extract variant name and capitalize the first letter
        val variantName = variant.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        // Define task names for unit tests and Android tests
        val unitTests = "test${variantName}UnitTest"
        val androidTests = "connected${variantName}AndroidTest"

        // Register a JacocoReport task for code coverage analysis
        tasks.register<JacocoReport>("jacoco${variantName}CodeCoverage") {
            dependsOn(unitTests)
            // Note: Android tests are optional as they require a connected device
            // If you want to include them, uncomment the next line
            // dependsOn(androidTests)

            group = "Reporting"
            description = "Generate Jacoco coverage reports for the ${variantName} build"

            reports {
                xml.required.set(true)
                html.required.set(true)
                // Optionally set report directories
                html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/${variant.name}/html"))
                xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/${variant.name}/jacocoTestReport.xml"))
            }

            sourceDirectories.setFrom(
                files(
                    "$projectDir/src/main/java",
                    "$projectDir/src/main/kotlin"
                )
            )

            classDirectories.setFrom(files(
                fileTree("${layout.buildDirectory.get()}/intermediates/javac/${variant.name}") {
                    exclude(exclusions)
                },
                fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/${variant.name}") {
                    exclude(exclusions)
                }
            ))

            executionData.setFrom(fileTree(layout.buildDirectory) {
                include(
                    "jacoco/test${variantName}UnitTest.exec",
                    "outputs/unit_test_code_coverage/${variant.name}UnitTest/${variant.name}UnitTest.ec",
                    "outputs/code_coverage/${variant.name}AndroidTest/connected/**/*.ec"
                )
            })
        }
    }
}

kapt {
    correctErrorTypes = true
}