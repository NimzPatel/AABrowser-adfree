# 🚗 AABrowser Ad-Free

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android%2015%2B%20%7C%20Android%20Auto-green.svg)](https://github.com/NimzPatel/AABrowser-adfree)
[![Download APK](https://img.shields.io/badge/Download-Latest%20APK-brightgreen?style=flat&logo=android)](https://github.com/NimzPatel/AABrowser-adfree/releases/latest/download/AABrowser-adfree.apk)
[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-FFDD00?style=flat&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/nimez)

**AABrowser Ad-Free** is a modified fork of [AABrowser](https://github.com/kododake/AABrowser) designed for Android Auto head units, featuring a **built-in AdBlocker** and **YouTube Ad Skipper** for a cleaner, faster browsing and video streaming experience while parked.

---

## 📜 Open Source Attribution & GPLv3 Compliance

This project is a derivative work of **[kododake/AABrowser](https://github.com/kododake/AABrowser)** and is distributed under the terms of the **GNU General Public License v3.0 (GPLv3)**.

- **Original Author:** [kododake](https://github.com/kododake) — Copyright (C) 2026 kododake
- **Original Repository:** (https://github.com/kododake/AABrowser)
- **Modified by:** [NimzPatel](https://github.com/NimzPatel) (2026)

### Summary of Modifications in This Fork
In accordance with Section 5 of the GPLv3 license, the following modifications have been made to the upstream source code:
- **Ad-Blocking Engine:** Implemented network request interception (`shouldInterceptRequest`) and host-based ad filtering within the Android Auto `WebViewClient` to block advertisements, trackers, and pop-ups automatically.
- **YouTube Ad Skipper:** Added automated script injection to detect, skip, and remove video advertisements on YouTube.
- **Custom Fork Packaging:** Updated documentation and build configurations for the `AABrowser-adfree` release.

---

## ✨ Key Features

- 🛡️ **Built-In Ad Blocking:** Blocks intrusive ads, banners, and tracking scripts out of the box.
- ⏭️ **YouTube Ad Skipper:** Automatically skips and removes video ads when streaming YouTube while parked.
- 🗂️ **Desktop-Class Tab Management:** Open, switch, close, and restore multiple browser tabs directly from your car's display.
- 🚗 **Car-First Controls:** One-touch mobile/desktop mode switching, global UI scaling, and bookmark management tailored for head units.
- 📱 **Android 15+ Native Projection:** Installs as a standard APK with no root or complex workarounds required.

---

## 📲 How to Install

### 1. Enable Unknown Sources in Android Auto
1. Open your phone's **Settings** and search for **Android Auto**.
2. Scroll to the very bottom and tap the **Version** section **10 times** to unlock Developer Mode. Tap **OK** on the prompt.
3. Tap the three-dot menu (**⋮**) in the top-right corner and select **Developer settings**.
4. Scroll down and enable the **Unknown sources** checkbox.

### 2. Download & Install the APK
1. Download the latest [**`AABrowser-adfree.apk`**](https://github.com/NimzPatel/AABrowser-adfree/releases/latest/download/AABrowser-adfree.apk) directly, or visit the [**Releases**](https://github.com/NimzPatel/AABrowser-adfree/releases) page.
2. Install the APK on your phone.
3. Connect your phone to your car's Android Auto head unit and launch **AABrowser**.
   *(Tip: If the app doesn't open immediately, launch a secondary navigation app like Waze first, then switch back to AABrowser).*

---

## 🛠️ Building from Source

1. Clone this repository:
   ```bash
   git clone [https://github.com/NimzPatel/AABrowser-adfree.git](https://github.com/NimzPatel/AABrowser-adfree.git)
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync, then select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

---

## ☕ Support This Fork

If you find **AABrowser Ad-Free** useful and want to support future updates and maintenance, consider buying me a coffee! (And don't forget to star/support the original author [kododake](https://github.com/kododake/AABrowser) too).

<a href="https://buymeacoffee.com/nimez" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 50px !important;width: 180px !important;" ></a>

---

## 🛑 Safety Disclaimer & License

**FOR STATIONARY & PASSENGER USE ONLY.**
Never interact with this application while driving or operating a vehicle. The developers (both upstream and of this fork) assume zero liability for misuse or accidents.

This program is free software: you can redistribute it and/or modify it under the terms of the **GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version**. See the [LICENSE](LICENSE) file for the full license text.
