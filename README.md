# Privacify

Privacy Control Center — monitor and manage app permissions, track sensor usage, and secure your device. All data stays on your device. No ads, no tracking, no analytics.

<div align="center">

[![Get it on F-Droid](https://img.shields.io/badge/Get%20it%20on-F--Droid-green?style=for-the-badge&logo=f-droid)](https://f-droid.org/packages/dev.robin.privacify/)
[![Get it on IzzyOnDroid](https://img.shields.io/badge/Get%20it%20on-IzzyOnDroid-orange?style=for-the-badge&logo=fdroid)](https://apt.izzysoft.de/fdroid/index/apk/dev.robin.privacify)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](LICENSE)

</div>

## Features

### Standard Mode (all devices)

| Feature | Description |
|---------|-------------|
| **Privacy Dashboard** | Real-time privacy score with sensor usage timeline and app risk overview |
| **Permission Scanner** | Identifies apps with risky permission combinations and provides risk ratings |
| **App Monitoring** | Tracks microphone, camera, and location access across all installed apps |
| **Permission Analytics** | Usage breakdown by permission type, risk level, and historical trends |
| **Quick Settings Tile** | One-tap lockdown toggle from the notification shade |
| **Home Screen Widget** | Lockdown toggle widget with live status |

### Advanced Mode (Root/Shizuku)

| Feature | Description |
|---------|-------------|
| **Hardware Kill Switches** | Disable microphone and camera at the system level (requires root or Shizuku) |
| **Lockdown Mode** | Instant system-wide sensor deactivation with panic button |
| **AppOps Management** | Fine-grained permission control per app through AppOps |
| **Auto-Guard** | Automatically pauses kill switches when camera/mic are actively in use, restores after idle period (Pro) |

## Screenshots

<div align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1. Dashboard.png" width="200" alt="Dashboard" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2. Apps.png" width="200" alt="Permission Scanner" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3. Analytics.png" width="200" alt="Analytics" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4. Auto Guard.png" width="200" alt="Auto-Guard" />
</div>

## Permissions

| Permission | Reason |
|------------|--------|
| `PACKAGE_USAGE_STATS` | Detect which apps are currently active to apply privacy rules |
| `QUERY_ALL_PACKAGES` | List all installed apps for permission scanning and management |
| `POST_NOTIFICATIONS` | Alert you when apps access camera or microphone |
| `FOREGROUND_SERVICE` | Run background monitoring and automation |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required for ongoing sensor monitoring service |
| `RECEIVE_BOOT_COMPLETED` | Restart automation service after device reboot |
| `INTERNET` | Required by Shizuku for inter-process communication |
| `ACCESS_NETWORK_STATE` | Required by Shizuku for network state queries |
| `INTERACT_ACROSS_USERS_FULL` | Required by Shizuku for multi-user support |
| `WAKE_LOCK` | Keep device awake during critical operations |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Allow user to exempt app from battery optimization |

## Requirements

- **Android 7.0+** (Nougat, API 24)
- **Optional:** Root access or [Shizuku](https://shizuku.rikka.app/) for advanced features

## Download

- [F-Droid](https://f-droid.org/packages/dev.robin.privacify/)
- [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/dev.robin.privacify)
- [GitHub Releases](https://github.com/robinsrk/privacify/releases)

## Building

### Prerequisites

- Android SDK (API 34)
- JDK 17

### Free build (standard features only)

This is the build submitted to F-Droid and IzzyOnDroid. It does not include the pro module.

```bash
cd privacify_free
./gradlew :app:assembleRelease
```

APK output: `privacify_free/app/build/outputs/apk/release/Privacify.apk`

### Pro build (with root/Shizuku features)

Includes hardware kill switches, Auto-Guard automation, and AppOps management. The pro source directory (`privacify_pro/`) is not included in the public repository — you must provide it separately.

```bash
cd privacify_free
./gradlew :app:assembleProRelease
```

### Debug build

```bash
cd privacify_free
./gradlew :app:assembleDebug
```

## Project Structure

```
privacify_app/
├── privacify_free/          # Main app module (open source)
│   ├── app/
│   │   ├── src/main/        # Free feature implementations
│   │   └── build.gradle.kts
│   ├── fastlane/            # F-Droid/IzzyOnDroid metadata
│   ├── .github/workflows/   # CI workflows for tagged releases
│   ├── .fdroid.yml          # F-Droid build recipe
│   └── LICENSE              # Apache 2.0
└── privacify_pro/           # Pro module (not in public repo)
    └── app/src/main/        # Root/Shizuku controller implementations
```

## Privacy

- **No ads** — zero advertising SDKs
- **No tracking** — no analytics, crash reporting, or telemetry
- **No network** — no HTTP client libraries; the `INTERNET` permission is only used by Shizuku for IPC
- **All data stays local** — stored in DataStore on your device

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 Expressive
- **Architecture:** MVVM with StateFlow
- **DI:** Manual dependency injection via providers
- **Storage:** DataStore Preferences
- **Root access:** Shizuku API
- **Min SDK:** 24 | **Target SDK:** 34

## License

```
Copyright 2026 Robin

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

See [LICENSE](LICENSE) for the full text.
