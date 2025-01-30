import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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
    id("org.jetbrains.kotlinx.kover")
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarqube.blr0.geekydev.com/sonar")

        // Project identification
        property("sonar.projectKey", "saddsa")
        property("sonar.projectName", "saddsa")

        // Define main source directories
        property("sonar.sources", "src/main/java")

        // Define test source directories
        property("sonar.tests", listOf(
            "src/test/java",
            "src/androidTest/java"
        ).joinToString(","))

        // Encoding of source files
        property("sonar.sourceEncoding", "UTF-8")

        // Jacoco coverage report path
        property("sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/kover/reportDebug.xml")
    }
}

kover {
    reports {
        filters {
            excludes {
                classes.addAll(
                    "*di.*",
                    "*Factory*", 
                    "com.sel2in.kotlinDefaultsJson.app.App",
                    "*.BuildConfig",
                    "*.R",
                    "*.R$*",
                    "*Manifest*",
                    "*Args*",
                    "*Companion*",
                    "*Module*",
                    "*_Factory*",
                    "*_MembersInjector*",
                    "*_Provide*Factory*",
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*.dagger.hilt.*",
                    "*Dagger*",
                    "*.generated.*",
                    // Add these new patterns for Moshi generated adapters
                    "*JsonAdapter*",
                    "*JsonAdapter",
                    "*_JsonAdapter",
                    "*.MoshiJsonAdapter",
                    "com.synchronoss.aiap.data.remote.**.*JsonAdapter"
                )
                annotatedBy.addAll(
                    "*Generated",
                    "*CustomAnnotationToExclude",
                    // Add Moshi's annotation
                    "com.squareup.moshi.JsonClass"
                )
            }
        }

        verify {
            rule("Line Coverage of Tests must be more than 80%") {
                bound {
                    minValue = 80
                }
            }
        }

        total {
            html {
                onCheck = true
            }
        }
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

        testInstrumentationRunner = "com.synchronoss.aiap.CustomTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            pickFirsts.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
            excludes.addAll(
                listOf(
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/license.txt",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt",
                    "META-INF/notice.txt",
                    "META-INF/ASL2.0",
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE-notice.md"
                )
            )
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

//    // BouncyCastle Dependencies
//    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
//    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
//    implementation("org.bouncycastle:bcutil-jdk15on:1.70")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    androidTestImplementation (libs.mockk.android)
    androidTestImplementation (libs.mockk.agent)
    testImplementation(libs.google.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.google.hilt.android.testing)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    kaptAndroidTest(libs.hilt.android.compiler)
}

kapt {
    correctErrorTypes = true
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports after running tests."

    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val androidExclusions = listOf(
        "**/BR.*",
        "**/R.*",
        "**/R$*.*",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Adapter*.*",
        "**/*Fragment*.*",
        "**/*ViewHolder*.*",
        "**/*ViewModelFactory*.*",
        "**/*Layout*.*",
        // Adding your existing exclusions
        "**/di/**",
        "**/*Companion*.*",
        "**/*Module*.*",
        "**/*_Factory*.*",
        "**/*_MembersInjector*.*",
        "**/*_Provide*Factory*.*",
        "**/Hilt_*.*",
        "**/*_HiltModules*.*",
        "**/dagger/hilt/**",
        "**/*Dagger*.*",
        "**/generated/**"
    )

    classDirectories.setFrom(files(
        fileTree("${buildDir}/intermediates/javac/debug/classes") {
            exclude(androidExclusions)
        },
        fileTree("${buildDir}/tmp/kotlin-classes/debug") {
            exclude(androidExclusions)
        }
    ))

    sourceDirectories.setFrom(files(
        "${project.projectDir}/src/main/java",
        "${project.projectDir}/src/main/kotlin"
    ))

    executionData.setFrom(files(
        "${buildDir}/jacoco/testDebugUnitTest.exec",
        "${buildDir}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
    ))
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
    finalizedBy("jacocoTestReport")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
