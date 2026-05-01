---
title: Running the Demo App
sidebar:
  order: 4
description: Information for getting started with the MRS Android Automotive demo app.
---

## Dev Board

This path assumes the board already has a working Android Automotive image flashed and booted. If you have not done that yet, start with [Flash OS](/getting-started/flash-os/).

### Prerequisites

You also need:

- the board connected over the **USB 3.0 Type-C / OTG** port
- the `demo-app` repository cloned locally
- `java` and Android SDK installed
  - If you use nix, you can use `nix develop` to access the packages

### Verify the board connection

From the laptop, confirm that Android on the board is visible over `adb`:

```bash
adb devices
```

If the board does not appear here, stop and fix the USB or board boot state before trying to deploy the app.

### Build the debug APK

```bash
just demo-app/build
```

That produces a signed debug APK at:

```text
demo-app/app/build/outputs/apk/debug/app-debug.apk
```

### Install from the CLI

Use `adb` to replace any existing debug build on the board:

```bash
just demo-app/install
```

This builds and installs the app.

### Run the app from the CLI

The debug build uses the package name `com.example.displayapp.debug`. Start it with:

```bash
just demo-app/run
```

If you want to restart from a clean app process first:

```bash
just demo-app/redeploy
```

### Useful troubleshooting commands

```bash
adb logcat
adb shell pm list packages | rg displayapp
adb shell dumpsys package com.example.displayapp.debug
```

If `adb install` fails because of signatures or stale state, remove the existing debug package and reinstall:

```bash
just demo-app/uninstall
just demo-app/install
```

## Android Studio Emulator

**WARNING** - Android Studio and the Android Emulator seem to be pretty resource-hungry, especially with RAM. It is recommended that you stop any other virtualization-related things on your machine (virtual machines and docker containers) before attempting to run the app locally on the emulator.

1. Install Android Studio
   - Instructions vary depending on operating system. You can figure this out on your own.
1. Install Android Automotive System Image
   - Docs are [here](https://developer.android.com/training/cars/testing/emulator#system-images)
   - "Android Automotive with Google APIs x86_64 System Image" (API level 34-ext9) is known to work
1. Clone the `demo-app` repository locally
1. Open the `demo-app` directory in Android Studio
1. Set up Android Automotive Virtual Device
   - Docs are [here](https://developer.android.com/training/cars/testing/emulator#automotive-avd)
   - "Automotive (1408p landscape)" is known to work
1. Click the "run app" button in the top bar (also can be triggered with `Shift + F10`)
1. Use the little kebab button in the emulator to open "Extended Controls"
1. Trigger different inputs to the app using the extended controls
   - **Car data** > **Car sensor data** gives you access to some high-level data inputs
   - **Car data** > **Vhal properties** gives you even more access to data inputs - you can search for things like `EV_BATTERY_LEVEL`
   - **Car rotary** gives you a handy rotary knob system as an alternative to the touchscreen
