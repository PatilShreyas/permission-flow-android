# Permission Flow for Android

Know about real-time state of a Android app Permissions with Kotlin Flow APIs. _Made with ❤️ for 
Android Developers_.

[![Build](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/build.yml/badge.svg)](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/build.yml)
[![Release](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/release.yml/badge.svg)](https://github.com/PatilShreyas/permission-flow-android/actions/workflows/release.yml)
[![Maven Central](https://img.shields.io/maven-central/v/dev.shreyaspatil.permission-flow/permission-flow-android?label=Maven%20Central&logo=android&style=flat-square)](https://search.maven.org/artifact/dev.shreyaspatil.permission-flow/permission-flow-android)

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

### Gradle setup

In `build.gradle` of app module, include this dependency

```gradle
dependencies {
    def permissionFlowVersion = "1.0.0"
    implementation "dev.shreyaspatil:permission-flow:permission-flow-android:$permissionFlowVersion"
    
    // For using in Jetpack Compose
    implementation "dev.shreyaspatil:permission-flow:permission-flow-compose:$permissionFlowVersion"
}
```

_You can find latest version and changelogs in the [releases](https://github.com/PatilShreyas/permission-flow-android/releases)_.
