# FloatScroll

Floating overlay buttons for Android that let you scroll any app with a single tap — no holding required.

## What it does

FloatScroll runs as an Accessibility Service and places two small floating buttons (▲ ▼) on top of all apps. Tap ▲ to scroll up, tap ▼ to scroll down. Drag the button cluster to move it anywhere on screen.

## Features

- **Tap to scroll** — single tap, no hold needed
- **Draggable buttons** — reposition anywhere on screen
- **Configurable scroll distance** — 200px to 1500px per tap
- **Adjustable opacity** — 20% to 100%
- **Three button sizes** — small, medium, large
- **Zero dependencies** — pure Android SDK
- **Works on all apps** — uses Accessibility Service overlay

## Install

1. Grab the latest APK from [Releases](../../releases)
2. Install on your Android device (allow unknown sources if prompted)
3. Open FloatScroll and tap "Open Accessibility Settings"
4. Find FloatScroll in the list and enable it
5. The floating buttons will appear — tap to scroll, drag to move

## Requirements

- Android 7.0+ (API 24)

## Building locally

```bash
git clone https://github.com/YOUR_USERNAME/floatscroll.git
cd floatscroll
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## CI/CD

Every push to `main` automatically builds the debug APK via GitHub Actions and publishes it as a GitHub Release. Pull requests also trigger a build to catch issues early.

## Project setup with Claude Code

This repo includes a `CLAUDE.md` file that gives Claude Code full context on the project architecture, coding standards, and behaviour rules. To build from scratch:

1. Create a new repo and add `CLAUDE.md` and `.github/workflows/build.yml`
2. Open Claude Code in the repo
3. Paste the prompt from `PROMPT.md`
4. Let it generate the full project
5. Push to GitHub — Actions will build the APK automatically

## License

MIT
