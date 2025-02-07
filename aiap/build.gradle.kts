import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    id("maven-publish")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    id("org.sonarqube")
}

kover {
    reports {
        filters {
            includes {
                // Include only specific classes we want to cover
                classes(
                    "com.synchronoss.aiap.presentation.SubscriptionsViewModel",
                    "com.synchronoss.aiap.ui.theme.ThemeLoader",
                    "com.synchronoss.aiap.utils.Resource",
                    "com.synchronoss.aiap.utils.CacheManager",
                    "com.synchronoss.aiap.common.**",
                    "com.synchronoss.aiap.data.**",
                    "com.synchronoss.aiap.domain.**",

                )
            }
            excludes {
                // Keep existing exclusions for generated code
                classes(
                    "*_Factory",
                    "*_Factory\$*",
                    "*_MembersInjector*",
                    "*_Provide*Factory*",
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*.dagger.hilt.*",
                    "*Dagger*",
                    "*.BuildConfig",
                    "*.R",
                    "*.R\$*",
                    "*Manifest*",
                    "*Args*",
                    "*.generated.*",
                    "*Companion*",
                    "*Module*",
                    "*JsonAdapter*",
                    "*JsonAdapter\$*",
                    "com.synchronoss.aiap.data.remote.**.*JsonAdapter",
                    "com.synchronoss.aiap.data.repository.activity.**",
                    "com.synchronoss.aiap.domain.usecases.activity.**"
                )
                
                annotatedBy(
                    "javax.annotation.Generated",
                    "dagger.Generated",
                    "androidx.annotation.Generated",
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
        minSdk = 23

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

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

sonar {
    val sonarToken: String by project
    println(sonarToken)

    properties {
        property("sonar.host.url", "https://sonarqube.blr0.geekydev.com")
        property("sonar.projectKey", "android-sample-app")
        property("sonar.projectName", "android-sample-app")
        property("sonar.token", sonarToken)
        property("sonar.sources", "src/main/java")
        property("sonar.tests", listOf(
            "src/test/java",
            "src/androidTest/java"
        ).joinToString(","))
        property("sonar.java.binaries", "build/intermediates/javac/debug/classes")
        property("sonar.kotlin.binaries", "build/tmp/kotlin-classes/debug")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/reportDebug.xml")

        // Coverage inclusions - matching Kover configuration
        property("sonar.inclusions", listOf(
            "**/presentation/SubscriptionsViewModel.*",
            "**/ui/theme/ThemeLoader.*",
            "**/utils/Resource.*",
            "**/utils/CacheManager.*",
            "**/common/**/*",
            "**/data/**/*",
            "**/domain/**/*"
        ).joinToString(","))

        // Coverage exclusions - matching Kover configuration
        property("sonar.exclusions", listOf(
            "**/*_Factory.*",
            "**/*_Factory\$*.*",
            "**/*_MembersInjector*.*",
            "**/*_Provide*Factory*.*",
            "**/*Hilt_*.*",
            "**/*_HiltModules*.*",
            "**/dagger/hilt/**/*.*",
            "**/*Dagger*.*",
            "**/BuildConfig.*",
            "**/R.*",
            "**/R\$*.*",
            "**/*Manifest*.*",
            "**/*Args*.*",
            "**/generated/**/*.*",
            "**/*Companion*.*",
            "**/*Module*.*",
            "**/*JsonAdapter*.*",
            "**/*JsonAdapter\$*.*",
            "**/data/remote/**/*JsonAdapter.*",
            // Classes with specific annotations
            "**/javax/annotation/Generated.*",
            "**/dagger/Generated.*",
            "**/androidx/annotation/Generated.*",
            "**/com/squareup/moshi/JsonClass.*",
            "/aiap/data/repository/activity/**/*",
            "/aiap/domain/usecases/activity/**/*"
        ).joinToString(","))
    }
}