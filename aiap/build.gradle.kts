import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("maven-publish")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
}

kover {
    reports {
        filters {
            includes {
                // Include only specific classes we want to cover
                classes(
                    "com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModel",
                    "com.synchronoss.aiap.presentation.SubscriptionsView",
                    "com.synchronoss.aiap.ui.theme.ThemeLoader",
                    "com.synchronoss.aiap.utils.Resource",
                    "com.synchronoss.aiap.utils.CacheManager",
//                    "com.synchronoss.aiap.common.**",
                    "com.synchronoss.aiap.core.data.**",
                    "com.synchronoss.aiap.core.domain.**",
                )
            }
            excludes {
                // Keep existing exclusions for generated code
                classes(
                    "*_Factory",
                    "*_Factory\$*",
                    "*_MembersInjector*",
                    "*_Provide*Factory*",
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
                    "com.synchronoss.aiap.core.data.remote.**.*JsonAdapter",
                    "com.synchronoss.aiap.core.data.repository.activity.**",
                    "com.synchronoss.aiap.core.domain.usecases.activity.**",
                    "com.synchronoss.aiap.core.domain.repository.activity.**",
                    "com.synchronoss.aiap.core.data.repository.analytics.**",
                    "com.synchronoss.aiap.core.domain.usecases.analytics.**",
                    "com.synchronoss.aiap.core.domain.repository.analytics.**",

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
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.google.fonts)

    // Lifecycle & Data
    implementation(libs.androidx.lifecycle.process)

    // Dependency Injection - Dagger
    implementation("com.google.dagger:dagger:2.51.1")
    kapt("com.google.dagger:dagger-compiler:2.51.1")
    implementation("com.google.dagger:dagger-android:2.51.1")
    implementation("com.google.dagger:dagger-android-support:2.51.1")
    kapt("com.google.dagger:dagger-android-processor:2.51.1")

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

    // Localytics - using a newer version from their repository
    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")
    implementation("com.android.installreferrer:installreferrer:2.2")

    // Segment Analytics
    implementation("com.segment.analytics.kotlin.destinations:localytics:1.0.0")
    implementation("com.segment.analytics.kotlin:android:1.19.1")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    
    // Testing with Dagger
    testImplementation("com.google.dagger:dagger:2.51.1")
    kaptTest("com.google.dagger:dagger-compiler:2.51.1")
    androidTestImplementation("com.google.dagger:dagger:2.51.1")
    kaptAndroidTest("com.google.dagger:dagger-compiler:2.51.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
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
    properties {
        property("sonar.host.url", "https://sonarqube.blr0.geekydev.com")
        property("sonar.projectKey", "android-sdk")
        property("sonar.projectName", "android-sdk")
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
