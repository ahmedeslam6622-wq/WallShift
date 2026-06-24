# WallShift 🎨

**Automatic wallpaper rotation app by Vynik**

Infinitely cycle through your personal photo collection on home screen, lock screen, or both — at the psychologically optimal interval.

---

## Features

- **Infinite image pool** — add as many images as you want from your gallery
- **Smart interval** — defaults to 3 days (research-backed boredom threshold); configurable from 1 hour to 30 days
- **Dual-screen support** — apply to home screen, lock screen, or both independently
- **Shuffle or sequential** — rotate in order or randomize each cycle
- **Exclude without deleting** — toggle images out of rotation without removing them
- **Apply now** — skip ahead to the next image instantly
- **Apply specific** — tap any image to set it immediately
- **Survives reboots** — WorkManager reschedules on boot automatically
- **Full glassmorphism UI** — deep navy backdrop with frosted glass cards, animated ambient orbs, and electric violet accents

---

## Screenshots

> UI built in Jetpack Compose with Material3. Deep midnight navy base + frosted glass cards + electric violet-blue accent palette.

---

## Installation

### From GitHub Releases (recommended)
1. Go to [Releases](../../releases) and download the latest APK
2. On your device: Settings → Security → Unknown sources → Enable
3. Open the APK and install
4. Grant photo access when prompted

### Build from source
```bash
git clone https://github.com/YOUR_USERNAME/WallShift.git
cd WallShift
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

---

## Requirements

| Requirement | Version |
|---|---|
| Android | 8.0 (API 26)+ |
| Target SDK | 35 (Android 15) |
| Java | 17 |

---

## Architecture

```
WallShift
├── data/
│   ├── model/          WallpaperImage, AppSettings, enums
│   └── repository/     Room DAO, WallpaperRepository, SettingsRepository (DataStore)
├── worker/
│   └── WallpaperRotationWorker   WorkManager periodic job
├── service/
│   ├── WallpaperChangeService    Foreground service for immediate apply
│   └── BootReceiver              Reschedules on device reboot
├── utils/
│   ├── WallpaperChanger          WallpaperManager wrapper
│   └── AppModule                 Hilt DI module
└── ui/
    ├── theme/                    Glassmorphism tokens (colors, typography)
    ├── components/               GlassCard, AmbientOrbs, GlassSwitch, etc.
    ├── screens/MainScreen.kt     Full Compose UI
    ├── MainViewModel.kt
    └── MainActivity.kt
```

**Stack:** Kotlin · Jetpack Compose · Hilt · WorkManager · Room · DataStore · Coil

---

## CI/CD

GitHub Actions builds debug APKs on every push and creates signed release APKs + GitHub Releases on version tags.

To release:
```bash
git tag v1.0.0
git push origin v1.0.0
```

### Signing (optional)
Add these secrets to your GitHub repo for signed releases:
- `KEYSTORE_BASE64` — base64-encoded `.jks` keystore
- `STORE_PASSWORD` — keystore password
- `KEY_ALIAS` — key alias
- `KEY_PASSWORD` — key password

---

## Psychological basis for the 3-day default

Studies on habituation and visual attention show people become functionally habituated to static background imagery within 3–7 days — the image enters peripheral awareness and stops generating dopaminergic novelty responses. 3 days hits the early edge of this window, keeping your wallpaper feeling fresh without changing so frequently it becomes distracting.

---

## License

MIT © Vynik
