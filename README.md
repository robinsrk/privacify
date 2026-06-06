<div align="center">
  <img src="fastlane/metadata/android/en-US/images/featuregraphic.png" alt="Privacify" width="100%" />
</div>

<h1 align="center">Privacify</h1>

<p align="center">
  <strong>Privacy Control Center</strong> — monitor app permissions, track sensor usage, and secure your device.
  <br />
  All data stays on your device. No ads, no tracking, no analytics.
</p>

<p align="center">
  <a href="https://f-droid.org/packages/dev.robin.privacify/">
    <img src="https://img.shields.io/badge/F--Droid-1976D2?style=for-the-badge&logo=f-droid&logoColor=white" alt="F-Droid" />
  </a>
  <a href="https://apt.izzysoft.de/fdroid/index/apk/dev.robin.privacify">
    <img src="https://img.shields.io/badge/IzzyOnDroid-E91E63?style=for-the-badge&logo=fdroid&logoColor=white" alt="IzzyOnDroid" />
  </a>
  <a href="https://github.com/robinsrk/privacify/releases">
    <img src="https://img.shields.io/badge/GitHub_Releases-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Releases" />
  </a>
  <a href="https://www.patreon.com/posts/privacify-159119797">
    <img src="https://img.shields.io/badge/Support_on_Patreon-FF424D?style=for-the-badge&logo=patreon&logoColor=white" alt="Patreon" />
  </a>
</p>

<p align="center">
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square" alt="License" />
  </a>
  <img src="https://img.shields.io/badge/Min%20SDK-24-brightgreen?style=flat-square" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-34-brightgreen?style=flat-square" alt="Target SDK" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/github/stars/robinsrk/privacify?style=flat-square&logo=github" alt="Stars" />
  <img src="https://img.shields.io/github/downloads/robinsrk/privacify/total?style=flat-square&logo=github" alt="Downloads" />
</p>

---

## ✨ Features

### 🛡️ Standard Mode (all devices)

| Feature | Description |
|---------|-------------|
| **Privacy Dashboard** | Real-time privacy score with sensor usage timeline and app risk overview |
| **Permission Scanner** | Identifies apps with risky permission combinations and provides risk ratings |
| **App Monitoring** | Tracks microphone, camera, and location access across all installed apps |
| **Permission Analytics** | Usage breakdown by permission type, risk level, and historical trends |
| **Quick Settings Tile** | One-tap lockdown toggle from the notification shade |
| **Home Screen Widget** | Lockdown toggle widget with live status |

### ⚡ Advanced Mode (Root/Shizuku)

| Feature | Description |
|---------|-------------|
| **Hardware Kill Switches** | Disable microphone and camera at the system level |
| **Lockdown Mode** | Instant system-wide sensor deactivation with panic button |
| **AppOps Management** | Fine-grained permission control per app through AppOps |
| **Auto-Guard** ⭐ | Automatically pauses kill switches when camera/mic are actively in use, restores after idle period |

<div align="center">

## 📸 Preview

  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1. Dashboard.png" width="22%" alt="Dashboard" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2. Apps.png" width="22%" alt="Permission Scanner" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3. Analytics.png" width="22%" alt="Analytics" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4. Auto Guard.png" width="22%" alt="Auto-Guard" />

</div>

---

## 🔐 Permissions

| Permission | Reason |
|------------|--------|
| `PACKAGE_USAGE_STATS` | Detect which apps are currently active to apply privacy rules |
| `QUERY_ALL_PACKAGES` | List all installed apps for permission scanning |
| `POST_NOTIFICATIONS` | Alert you when apps access camera or microphone |
| `FOREGROUND_SERVICE` | Run background monitoring and automation |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required for ongoing sensor monitoring service |
| `RECEIVE_BOOT_COMPLETED` | Restart automation service after device reboot |
| `INTERNET` | Required by Shizuku for inter-process communication |
| `ACCESS_NETWORK_STATE` | Required by Shizuku for network state queries |
| `INTERACT_ACROSS_USERS_FULL` | Required by Shizuku for multi-user support |
| `WAKE_LOCK` | Keep device awake during critical operations |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Allow user to exempt app from battery optimization |

---

## 📦 Download

| Source | Link |
|--------|------|
| <img src="https://f-droid.org/badge/get-it-on.png" height="20" alt="F-Droid" /> | [F-Droid](https://f-droid.org/packages/dev.robin.privacify/) |
| <img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="20" alt="IzzyOnDroid" /> | [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/dev.robin.privacify) |
| <img src="https://github.githubassets.com/favicons/favicon.svg" height="20" alt="GitHub" /> | [GitHub Releases](https://github.com/robinsrk/privacify/releases) |

---

## 🔒 Privacy

- **No ads** — zero advertising SDKs
- **No tracking** — no analytics, crash reporting, or telemetry
- **No network** — no HTTP client libraries; `INTERNET` is only used by Shizuku for IPC
- **All data stays local** — stored in DataStore on your device

---

## 🧱 Tech Stack

| Tech | Choice |
|------|--------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 Expressive |
| **Architecture** | MVVM with StateFlow |
| **DI** | Manual dependency injection via providers |
| **Storage** | DataStore Preferences |
| **Root access** | Shizuku API |
| **Min SDK** | 24 |
| **Target SDK** | 34 |

---

## ☕ Support

If you find Privacify useful, consider supporting development on Patreon:

[![Support on Patreon](https://img.shields.io/badge/Support%20on%20Patreon-FF424D?style=for-the-badge&logo=patreon&logoColor=white)](https://www.patreon.com/posts/privacify-159119797)

Your support helps maintain the project, develop new features, and keep it free and open source.

---

## 📄 License

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
