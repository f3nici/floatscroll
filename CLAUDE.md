# CLAUDE.md — FloatScroll

## Project Overview

FloatScroll is a minimal Android accessibility service app that displays persistent floating overlay buttons (▲ Up / ▼ Down) on top of all other apps. A single tap on either button dispatches a scroll gesture — no long-press or hold required. The user can drag the button cluster to reposition it.

## Tech Stack

- **Language:** Kotlin (no Java)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Build system:** Gradle with Kotlin DSL (`build.gradle.kts`)
- **Architecture:** Single-activity + AccessibilityService — no Jetpack Compose, no fragments
- **Dependencies:** None beyond the Android SDK — keep it zero-dependency

## Project Structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/floatscroll/
│   │   ├── MainActivity.kt            # Settings UI, service toggle, scroll amount slider
│   │   ├── ScrollAccessibilityService.kt  # Core service: overlay + gesture dispatch
│   │   └── SettingsStore.kt           # SharedPreferences wrapper
│   ├── res/
│   │   ├── layout/activity_main.xml
│   │   ├── drawable/                   # Button drawables (vectors preferred)
│   │   ├── values/strings.xml
│   │   ├── values/colors.xml
│   │   ├── values/themes.xml
│   │   └── xml/accessibility_service_config.xml
│   └── ic_launcher-playstore.png       # (optional)
├── build.gradle.kts
settings.gradle.kts
build.gradle.kts  (root)
gradle.properties
```

## Core Behaviour Rules

### Accessibility Service (`ScrollAccessibilityService`)
1. On service start → inflate a floating overlay via `WindowManager` with `TYPE_ACCESSIBILITY_OVERLAY`.
2. The overlay contains two `ImageButton`s stacked vertically: ▲ (scroll up) and ▼ (scroll down).
3. **Single tap** on ▲ → dispatch `AccessibilityService.dispatchGesture()` that swipes **down** on screen (content scrolls up). Single tap on ▼ → dispatch gesture that swipes **up** (content scrolls down).
4. Gesture path: a vertical `Path` from centre-screen offset ±`scrollDistance` pixels (default 600px, user-configurable 200–1500).
5. Gesture duration: 120ms (fast but accepted by the framework).
6. The button cluster is **draggable** — handle `ACTION_MOVE` on the root overlay layout and update `WindowManager.LayoutParams.x/y`. Distinguish drag from tap using a 10dp slop threshold.
7. On service stop → remove the overlay from `WindowManager`.

### MainActivity
1. Show a brief explanation of what the app does.
2. A button/link to open the system Accessibility Settings so the user can enable the service.
3. A slider (`SeekBar`) to configure scroll distance (200–1500px, default 600).
4. A slider to configure button opacity (20%–100%, default 70%).
5. A toggle to choose button size (small 40dp / medium 56dp / large 72dp).
6. Persist all settings with `SharedPreferences` via `SettingsStore`.

### SettingsStore
- Singleton `object` backed by `SharedPreferences`.
- Keys: `scroll_distance` (Int), `button_opacity` (Float 0–1), `button_size` (String: "small" | "medium" | "large").
- Service reads values on each tap (so changes apply live without restarting the service).

## Accessibility Service Config (`accessibility_service_config.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/service_description"
    android:canPerformGestures="true"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100" />
```

## Manifest Requirements
- `SYSTEM_ALERT_WINDOW` permission (declared but not runtime-requested — accessibility overlay doesn't need it, but declare it as fallback).
- `FOREGROUND_SERVICE` permission if targeting SDK 34+.
- AccessibilityService declaration with the config XML and intent filter for `android.accessibilityservice.AccessibilityService`.

## UI & Style
- Dark theme by default (Material `Theme.Material3.Dark.NoActionBar` or a simple custom dark theme).
- Floating buttons: semi-transparent circular backgrounds, white arrow icons, subtle elevation/shadow.
- Use vector drawables for the arrow icons — do NOT use PNG assets.
- Button cluster should have a thin rounded-rect background so it looks like a floating pill.

## Build & Signing
- Debug builds only are fine — no release signing config needed.
- Ensure `build.gradle.kts` has `buildTypes { debug { ... } }` and produces an APK (not AAB).
- The APK output path must be `app/build/outputs/apk/debug/app-debug.apk`.

## Code Style
- Kotlin idioms: `val` over `var`, scope functions, extension functions where natural.
- No `!!` operator — use safe calls or `requireNotNull` with clear messages.
- Logcat tag: `"FloatScroll"` for all logging.

## Testing Guidance
- Manual testing is primary — accessibility services are hard to unit test.
- Verify on API 24 (emulator) and API 34 to confirm overlay works on both.
- Confirm the service survives the host app being killed (it runs in its own process context).

## What NOT to Do
- Do NOT use Jetpack Compose.
- Do NOT add any third-party dependencies.
- Do NOT implement a foreground service with a notification — the accessibility service IS the long-running component.
- Do NOT require the `SYSTEM_ALERT_WINDOW` runtime permission — `TYPE_ACCESSIBILITY_OVERLAY` doesn't need it when running as an accessibility service.
- Do NOT use `Handler.postDelayed` for repeating scroll on hold — one tap = one scroll, period.
