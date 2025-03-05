# **IAP Android SDK**
IAP is an in-app purchase and subscription management library for Android applications. It simplifies the implementation of Google Play Billing Library for subscriptions.

## **Installation**

### **1. Add the JitPack repository**

Add JitPack to your root `settings.gradle.kts`:

```
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

### **2. Add the dependency**

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.vsc9729:aiap:<latest-version>")
}
```

### **3. Import AIAP**

You should now be able to import AIAP. 
```kotlin
import com.geekyants.synchronoss.aiap.SubscriptionsViewBottomSheet
```

The package name would be changed later

### **4. Required Permissions**

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission _android__:__name_="com.android.vending.BILLING" />
<uses-permission _android__:__name_="android.permission.INTERNET" />
```

## **Required Dependencies**

The AIAP library requires several dependencies to function properly:

1. ********Dependency Injection********
```kotlin
implementation("com.google.dagger:hilt-android:2.51.1")
kapt("com.google.dagger:hilt-android-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
```
Used for dependency injection, managing component lifecycles, and Compose navigation integration with Hilt.

2. ********Network & API Communication********
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
```
Required for making API calls, handling network responses, and JSON serialization.

3. ********JSON Parsing********
```kotlin
implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")
```
Used for JSON serialization/deserialization.

4. ********Jetpack Compose UI********
```kotlin
implementation("androidx.compose.material3:material3-android:1.2.1")
implementation("androidx.compose.ui:ui-tooling-preview-android:1.7.6")
implementation("androidx.compose.ui:ui-text-google-fonts:1.7.6")
```
Required for the subscription UI components and Google Fonts support.

5. ********Google Play Billing********
```kotlin
implementation("com.android.billingclient:billing:7.1.1")
```
Core dependency for handling in-app purchases.

6. ********Image Loading********
```kotlin
implementation("io.coil-kt:coil-compose:2.4.0")
```
Used for efficient image loading and caching in Compose UI.

7. ********Lifecycle Components********
```kotlin
implementation("androidx.lifecycle:lifecycle-process:2.7.0")
```
Required for lifecycle management.

## **Basic Implementation**

1. Initialize the subscription view in your activity:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (showSubscriptionView) {
                SubscriptionsViewBottomSheet(
                    onDismissRequest = { showSubscriptionView = false },
                    visible = showSubscriptionView,
                    activity = this,
                    partnerUserId = "your-user-id",
                    apiKey = "your-app-key-here"
                )
            }
        }
    }
}
```



## **Requirements**

- Android API level 21 or higher
- Kotlin 1.9.0 or higher
- Jetpack Compose support in your project