---
title: Running the Demo App
description: Information for getting started with the MRS Android Automotive demo app.
---

## Dev Board

Coming soon! Information for deploying to imx8 dev board should be added here.

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

TODO: figure out how much of this can be done through the CLI instead of using Android Studio.
