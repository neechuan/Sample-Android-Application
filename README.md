# Sample Android Application with AppDynamics Mobile EUM

[![MIT License][license-shield]][license-url]
[![AppDynamics Agent](https://img.shields.io/badge/AppDynamics%20Agent-v26.8.0-blue.svg)](https://docs.appdynamics.com/appd/24.x/latest/en/end-user-monitoring/mobile-real-user-monitoring/instrument-android-applications)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-purple.svg)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.14.4-02303A.svg)](https://gradle.org/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-green.svg)](https://developer.android.com/about/versions/15)
[![Java Target](https://img.shields.io/badge/JDK-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

A modern Android Task Management (To-Do) application written in Kotlin, integrated with **Cisco AppDynamics Mobile Real-User Monitoring (MRUM / ADEUM)**. 

This repository serves as a reference implementation demonstrating best practices for instrumenting Android applications with the AppDynamics Android Agent, capturing network telemetry, user interactions, session lifecycles, and crash diagnostics.

---

## Table of Contents

- [Overview & Features](#overview--features)
- [Tech Stack & Versions](#tech-stack--versions)
- [Architecture & Structure](#architecture--structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [AppDynamics Configuration](#appdynamics-configuration)
  - [Build and Run](#build-and-run)
- [AppDynamics Monitoring & Verification](#appdynamics-monitoring--verification)
- [Testing](#testing)
- [Documentation](#documentation)
- [License](#license)

---

## Overview & Features

### 📱 Task Management Application
- **Complete CRUD Operations**: Create, view, edit, and delete tasks with a Material Design UI.
- **Interactive Status & Filtering**: Filter tasks by **All**, **Active**, and **Completed** with a live task completion counter (`X of Y completed`).
- **Undo & Recovery**: Instant deletion recovery via Snackbar undo action.
- **Local Persistence**: Fast local caching and JSON serialization backed by `SharedPreferences`.

### 📊 AppDynamics EUM Capabilities
- **Automatic Network Instrumentation**: Asynchronous HTTP calls (`POST`, `PUT`, `DELETE`, `PATCH`) executed via `OkHttp 3` and Kotlin Coroutines against a mock REST API (`jsonplaceholder.typicode.com`), automatically captured and reported by the AppDynamics `adeum` plugin.
- **Application Lifecycle Monitoring**: Agent startup during application initialisation (`App.kt`) capturing cold start and session metrics.
- **Crash & Error Diagnostics**: Crash reporting with support for ProGuard/Dex mapping upload.
- **Configurable Telemetry Endpoints**: Customisable Collector and Screenshot URLs for SaaS or on-premises deployments.

---

## Tech Stack & Versions

| Component | Technology / Library | Version | Notes |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | `2.4.10` | Modern Kotlin standard library and coroutines |
| **JDK / JVM Target** | Java / JVM | `17` | `JavaVersion.VERSION_17` |
| **Android SDK** | Compile / Target SDK | `35` (Android 15) | Min SDK `23` (Android 6.0) |
| **Build Automation** | Gradle Wrapper | `8.14.4` | Groovy DSL build files |
| **Android Gradle Plugin** | AGP (`com.android.tools.build:gradle`) | `8.13.2` | Android build pipeline |
| **AppDynamics Gradle Plugin** | `com.appdynamics:appdynamics-gradle-plugin` | `26.8.0` | Bytecode instrumentation at build/dex time |
| **AppDynamics Android SDK** | `adeum` | `26.8.0` | Mobile EUM Runtime SDK |
| **Networking** | OkHttp | `4.9.1` | Auto-instrumented HTTP client |
| **Async & Concurrency** | Kotlin Coroutines | `1.5.2` | Background I/O and lifecycle scopes |
| **UI Components** | AndroidX & Material Design | `1.3.0` | Material dialogs, chips, FAB, ListAdapter with DiffUtil |

---

## Architecture & Structure

The codebase is structured into clear layers separating data persistence, network communication, business models, and UI controllers:

```
Sample-Android-Application/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # Permissions & Application class registration
│   │   │   ├── java/com/appdynamics/sampleandroidapplication/
│   │   │   │   ├── App.kt                   # Application entrypoint & ADEUM initialisation
│   │   │   │   ├── MainActivity.kt          # UI controller, filtering, dialogs, & interactions
│   │   │   │   ├── data/
│   │   │   │   │   └── TodoRepository.kt    # Data CRUD, JSON storage, and OkHttp network calls
│   │   │   │   ├── model/
│   │   │   │   │   └── TodoItem.kt          # Data model with JSON serialisation
│   │   │   │   └── ui/
│   │   │   │       └── TodoAdapter.kt       # RecyclerView ListAdapter with DiffUtil
│   │   │   └── res/
│   │   │       ├── layout/                  # Activity & dialog XML layouts
│   │   │       └── values/
│   │   │           ├── secrets.xml          # EUM App Key and Collector URL
│   │   │           └── strings.xml          # UI string resources
│   │   └── test/                            # Unit tests for Models & Repository
│   └── build.gradle                         # App module Gradle configuration (adeum plugin applied)
├── appdynamics.properties                   # EUM account name & license key
├── build.gradle                             # Root build configuration
├── INSTRUMENTATION.md                       # Comprehensive agent instrumentation guide
└── README.md
```

---

## Getting Started

### Prerequisites

1. **Android Studio**: Android Studio Ladybug / Koala or newer recommended.
2. **JDK 17**: Ensure `JAVA_HOME` points to a JDK 17 installation.
3. **Android SDK 35**: Installed via Android Studio SDK Manager (Build Tools `35.0.0`).
4. **AppDynamics EUM Account**: An active Mobile Real-User Monitoring (MRUM) application key and license.

### AppDynamics Configuration

1. **Root Configuration (`appdynamics.properties`)**:
   Create or update [appdynamics.properties](file:///Users/garychew/Documents/poc/Sample-Android-Application/appdynamics.properties) in the project root:
   ```properties
   EUM_ACCOUNT_NAME="<YOUR_EUM_ACCOUNT_NAME>"
   EUM_LICENSE_KEY="<YOUR_EUM_LICENSE_KEY>"
   ```

2. **App Secrets & Endpoints (`app/src/main/res/values/secrets.xml`)**:
   Ensure [app/src/main/res/values/secrets.xml](file:///Users/garychew/Documents/poc/Sample-Android-Application/app/src/main/res/values/secrets.xml) contains your EUM App Key and Collector URL:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="APP_KEY">XX-XXX-XXX-XXX</string>
       <string name="COLLECTOR_URL">https://col.eum-appdynamics.com</string>
       <string name="SCREENSHOT_URL">https://image.eum-appdynamics.com</string>
   </resources>
   ```

### Build and Run

#### Using the Command Line:
```bash
# Clean and assemble debug APK
./gradlew assembleDebug

# Install on a connected device or emulator
./gradlew installDebug
```

#### Using Android Studio:
1. Open the project folder in Android Studio.
2. Allow Gradle sync to complete.
3. Select an emulator or connected device and click **Run (Shift + F10)**.

---

## AppDynamics Monitoring & Verification

Once the application is running:

1. **Inspect Logcat Runtime Telemetry**:
   Filter Logcat for the `ADEUM` or `AppDynamics` tag:
   ```bash
   adb logcat -s ADEUM
   ```
   You will see initialization logs, session starts, and beacon transmissions for network calls.

2. **Trigger Telemetry Events**:
   - **Network Requests**: Add, update, toggle, or delete tasks in the app. Each action fires an asynchronous HTTP request (`POST`, `PUT`, `DELETE`, `PATCH`) to `https://jsonplaceholder.typicode.com/todos`, captured automatically by the ADEUM agent.
   - **ANR (Application Not Responding) Simulation**: Tap the **ANR** button in the top app bar. This triggers a 10-second block on the Main UI thread (`Thread.sleep(10000)`). When input/touch events occur while the thread is blocked, Android OS raises an ANR dialog (> 5 seconds unresponsiveness). AppDynamics automatically captures the ANR event along with all thread stack traces.
   - **Crash Simulation**: Tap the **Crash** button in the top app bar to immediately trigger a real fatal uncaught exception (`RuntimeException`). The AppDynamics ADEUM crash reporter captures the stack trace, device state, and breadcrumbs, and reports the crash on the next session launch.
   - **Custom Timers & Metrics**: Tapping `+` starts a custom timer (`"Task Creation Flow"`), and saving records the duration and reports a custom metric (`"Task Title Character Length"`).
   - **Breadcrumbs**: Actions such as dialog interactions, filter changes, task completions, deletions, undos, and simulated errors record real-time breadcrumbs attached to session diagnostics and crash snapshots.
   - **Session Tracking**: Put the app in the background and resume it to generate session data.

3. **Verify in AppDynamics Controller**:
   - Navigate to **User Experience** > **Mobile Applications** in your AppDynamics Controller.
   - View real-time active sessions, network latency breakdown, HTTP error rates, user flows, ANR reports, and crash snapshots under **Crashes & Errors** / **ANR**.
   - Inspect custom metrics, task creation flow percentiles, and breadcrumbs under **Custom Data** and **Session Details**.
   - Configure and monitor dynamic method execution times under **Configuration > Info Points** and view them in the **Metric Browser**.

---

## Testing

Run unit tests directly with Gradle:

```bash
# Run local unit tests
./gradlew testDebugUnitTest
```

Unit test suites cover:
- **`TodoItemTest.kt`**: JSON serialization and deserialization integrity, immutability updates.
- **`TodoRepositoryTest.kt`**: Repository CRUD operations and JSON persistence.

---

## Documentation

For an in-depth guide on the AppDynamics Gradle plugin integration, bytecode transformation, Android permissions, and ProGuard mapping configuration, refer to [INSTRUMENTATION.md](file:///Users/garychew/Documents/poc/Sample-Android-Application/INSTRUMENTATION.md).

---

## License

Distributed under the GNU GPLv3 License. See [LICENSE.txt](file:///Users/garychew/Documents/poc/Sample-Android-Application/LICENSE.txt) for more details.

[license-shield]: https://img.shields.io/badge/License-GPLv3-blue.svg
[license-url]: file:///Users/garychew/Documents/poc/Sample-Android-Application/LICENSE.txt
