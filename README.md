# Permission Flow for Android

Know about real-time state of a Android app Permissions with Kotlin Flow APIs. _Made with ❤️ for 
Android Developers_.

[![Build](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/build.yml/badge.svg)](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/build.yml)
[![Release](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/release.yml/badge.svg)](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/release.yml)
[![codecov](https://codecov.io/gh/PatilShreyas/permission-flow-android/branch/main/graph/badge.svg?token=6TOHNLQDVW)](https://codecov.io/gh/PatilShreyas/permission-flow-android)
[![Maven Central](https://img.shields.io/maven-central/v/dev.shreyaspatil.permission-flow/permission-flow-android?label=Maven%20Central&logo=android&style=flat-square)](https://search.maven.org/artifact/dev.shreyaspatil.permission-flow/permission-flow-android)
[![GitHub](https://img.shields.io/github/license/PatilShreyas/permission-flow-android?label=License)](LICENSE)

[![Github Followers](https://img.shields.io/github/followers/PatilShreyas?label=Follow&style=social)](https://github.com/PatilShreyas)
[![GitHub stars](https://img.shields.io/github/stars/PatilShreyas/permission-flow-android?style=social)](https://github.com/PatilShreyas/permission-flow-android/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/PatilShreyas/permission-flow-android?style=social)](https://github.com/PatilShreyas/permission-flow-android/network/members)
[![GitHub watchers](https://img.shields.io/github/watchers/PatilShreyas/permission-flow-android?style=social)](https://github.com/PatilShreyas/permission-flow-android/watchers)
[![Twitter Follow](https://img.shields.io/twitter/follow/imShreyasPatil?label=Follow&style=social)](https://twitter.com/imShreyasPatil)

## 💡Introduction

In big projects, app is generally divided in several modules and in such cases, if any individual 
module is just a data module (_not having UI_) and need to know state of a permission, it's not 
that easy. This library provides a way to know state of a permission throughout the app and 
from any layer of the application safely. 

_For example, you can listen for state of contacts permission in class where you'll instantly show
list of contacts when permission is granted._ 

It's a simple and easy to use library. Just Plug and Play.

## 🚀 Implementation

You can check [/app](/app) directory which includes example application for demonstration.

### 1. Gradle setup

In `build.gradle` of app module, include this dependency

```gradle
dependencies {
    implementation "dev.shreyaspatil:permission-flow:permission-flow-android:$version"
    
    // For using in Jetpack Compose
    implementation "dev.shreyaspatil:permission-flow:permission-flow-compose:$version"
}
```

_You can find latest version and changelogs in the [releases](https://github.com/PatilShreyas/permission-flow-android/releases)_.

### 2. Initialization

Before using any API of the library, it needs to be initialized before.   
Initialize **PermissionFlow** as follows (For example, in `Application` class):

```kotlin
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PermissionFlow.init(this)
    }
}
```

### 3. Observing a Permission State

#### 3.1 Observing Permission with `StateFlow`
A permission state can be subscribed by retrieving `StateFlow<PermissionState>` or `StateFlow<MultiplePermissionState>` as follows:

```kotlin
val permissionFlow = PermissionFlow.getInstance()

// Observe state of single permission
suspend fun observePermission() {
    permissionFlow.getPermissionState(android.Manifest.permission.READ_CONTACTS).collect { state ->
        if (state.isGranted) {
            // Do something
        }
    }
}

// Observe state of multiple permissions
suspend fun observeMultiplePermissions() {
    permissionFlow.getMultiplePermissionState(
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.READ_SMS
    ).collect { state ->
        // All permission states
        val allPermissions = state.permissions

        // Check whether all permissions are granted
        val allGranted = state.allGranted

        // List of granted permissions
        val grantedPermissions = state.grantedPermissions

        // List of denied permissions
        val deniedPermissions = state.deniedPermissions
    }
}
```

#### 3.2 Observing permissions in Jetpack Compose

State of a permission and state of multiple permissions can also be observed in Jetpack Compose application as follows:

```kotlin
@Composable
fun ExampleSinglePermission() {
    val state by rememberPermissionState(Manifest.permission.CAMERA)
    if (state.isGranted) {
        // Render something
    } else {
        // Render something else
    }
}

@Composable
fun ExampleMultiplePermission() {
    val state by rememberMultiplePermissionState(
        Manifest.permission.CAMERA
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS
    )
    
    if (state.allGranted) {
        // Render something
    }
    
    val grantedPermissions = state.grantedPermissions
    // Do something with `grantedPermissions`
    
    val deniedPermissions = state.deniedPermissions
    // Do something with `deniedPermissions`
}
```

### 4. Requesting permission with PermissionFlow

It's necessary to use utilities provided by this library to request permissions so that whenever permission state 
changes, this library takes care of notifying respective flows.

#### 4.1 Request permission from Activity / Fragment

Use [`registerForPermissionFlowRequestsResult()`](https://patilshreyas.github.io/permission-flow-android/docs/permission-flow/dev.shreyaspatil.permissionFlow.utils/register-for-permission-flow-requests-result.html) method to get `ActivityResultLauncher`
and use `launch()` method to request for permission.

```kotlin
class ContactsActivity : AppCompatActivity() {

    private val permissionLauncher = registerForPermissionFlowRequestsResult()

    private fun askContactsPermission() {
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS, ...)
    }
}
```

#### 4.2 Request permission in Jetpack Compose

Use [`rememberPermissionFlowRequestLauncher()`](https://patilshreyas.github.io/permission-flow-android/docs/permission-flow-compose/dev.shreyaspatil.permissionflow.compose/remember-permission-flow-request-launcher.html) method to get `ManagedActivityResultLauncher`
and use `launch()` method to request for permission.

```kotlin
@Composable
fun Example() {
    val permissionLauncher = rememberPermissionFlowRequestLauncher()
    
    Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA, ...) }) {
        Text("Request Permissions")
    } 
}
```

### 5. Manually notifying permission state changes ⚠️

If you're not using `ActivityResultLauncher` APIs provided by this library then 
you will ***not receive permission state change updates***. But there's a provision by which 
you can help this library to know about permission state changes.

Use [`PermissionFlow#notifyPermissionsChanged()`](https://patilshreyas.github.io/permission-flow-android/docs/permission-flow/dev.shreyaspatil.permissionFlow/-permission-flow/notify-permissions-changed.html) to notify the permission state changes
from your manual implementations.

For example:

```kotlin
class MyActivity: AppCompatActivity() {
    private val permissionFlow = PermissionFlow.getInstance()
    
    private val permissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
        permissionFlow.notifyPermissionsChanged(android.Manifest.permission.READ_CONTACTS)
    }
}
```

### 6. Manually Start / Stop Listening ⚠️

This library starts processing things lazily whenever `getPermissionState()` or `getMultiplePermissionState()` is called
for the first time. But this can be controlled with these methods:

```kotlin
fun doSomething() {
    // Stops listening to the state changes of permissions throughout the application.
    // This means the state of permission retrieved with [getMultiplePermissionState] method will not 
    // be updated after stopping listening. 
    permissionFlow.stopListening()
    
    // Starts listening the changes of state of permissions after stopping listening
    permissionFlow.startListening()
}
```

## 📄 API Documentation

[Visit the API documentation](https://patilshreyas.github.io/permission-flow-android/docs/) of this library to get more information in detail. This documentation is generated using [Dokka](https://github.com/Kotlin/dokka).

## 📊 Test coverage report

[Check the Test Coverage Report](https://patilshreyas.github.io/permission-flow-android/coverageReport/) of this library. This is generated using [Kover](https://github.com/Kotlin/kotlinx-kover).

---

## 🙋‍♂️ Contribute 

Read [contribution guidelines](CONTRIBUTING.md) for more information regarding contribution.

## 💬 Discuss? 

Have any questions, doubts or want to present your opinions, views? You're always welcome. You can [start discussions](https://github.com/PatilShreyas/permission-flow-android/discussions).

## 📝 License

```
Copyright 2022 Shreyas Patil

Licensed under the Apache License, Version 2.0 (the "License");

you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
