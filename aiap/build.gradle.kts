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
    id("org.sonarqube") version "6.0.1.5171"
}
jacoco {
    toolVersion = "0.8.9"  // Use the latest version of Jacoco
}
sonar {
    properties {
        property("sonar.host.url", "http://localhost:9000")

        // Project identification
        property("sonar.projectKey", "codeCoverageKey")
        property("sonar.projectName", "codeCoverage")

        // Explicitly define main sources
        property("sonar.sources", listOf(
            "src/main/java",
            //"src/main/kotlin"
        ).joinToString(","))

        // Explicitly define test sources
        property("sonar.tests", listOf(
            "src/test/java",
            //"src/test/kotlin",
            "src/androidTest/java",
//            "src/androidTest/kotlin"
        ).joinToString(","))

        // Encoding of source files
        property("sonar.sourceEncoding", "UTF-8")

        // Jacoco coverage report paths
        property("sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/debug/jacocoTestReport.xml")

        // Android lint results
        property("sonar.android.lint.report",
            "${layout.buildDirectory.get()}/reports/lint-results.xml")

        // Exclude specific files and patterns
        property("sonar.exclusions", listOf(
            "**/BuildConfig.*",
            "**/Manifest.java",
            "**/R.class",
            "**/R$*.class",
            "**/*Test*.*",
            "**/res/**/*.*"
        ).joinToString(","))

        // Explicitly exclude test files from main sources
        property("sonar.test.exclusions", listOf(
            "src/test/**/*",
            "src/androidTest/**/*"
        ).joinToString(","))

        // Explicitly include only test files in test sources
        property("sonar.test.inclusions", listOf(
            "**/*Test*.*",
            "**/*Spec*.*"
        ).joinToString(","))
    }
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
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
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
        val variantName = variant.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        val testTaskName = "test${variantName}UnitTest"

        tasks.register<JacocoReport>("jacoco${variantName}Report") {
            dependsOn(testTaskName)

            group = "Reporting"
            description = "Generate Jacoco coverage reports for the ${variantName} build"

            reports {
                xml.required.set(true)
                html.required.set(true)

                // Set specific output locations
                html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/${variant.name}"))
                xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/${variant.name}/jacocoTestReport.xml"))
            }

            sourceDirectories.setFrom(layout.projectDirectory.dir("src/main/java"))
            additionalSourceDirs.setFrom(layout.projectDirectory.dir("src/main/kotlin"))

            classDirectories.setFrom(
                fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/${variant.name}") {
                    exclude(excludes)
                }
            )

            executionData.setFrom(fileTree(layout.buildDirectory) {
                include("outputs/unit_test_code_coverage/${variant.name}UnitTest/**/*.exec")
                include("jacoco/${testTaskName}.exec")
            })
        }
    }
}
tasks.register("jacocoTestReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports for all variants"

    // This will be configured after all variant-specific tasks are created
    androidComponents.onVariants { variant ->
        dependsOn("jacoco${variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}Report")
    }
}

kapt {
    correctErrorTypes = true
}
tasks.configureEach {
    doFirst {
        println("Executing task: ${this.name}")
    }
}

tasks.withType<JacocoReport> {
    doFirst {
        println("Jacoco source dirs: ${sourceDirectories.files}")
        println("Jacoco class dirs: ${classDirectories.files}")
        println("Jacoco execution data: ${executionData.files}")
    }
}