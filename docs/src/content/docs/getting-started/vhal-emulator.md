---
title: VHAL Emulator
sidebar:
  order: 5
description: Inject vehicle property values into the debug VHAL shipped with NXP's Android Automotive base image.
---

NXP's base image ships AOSP's reference Vehicle HAL (`FakeVehicleHardware` / `android.hardware.automotive.vehicle.IVehicle/default`). It exposes a debug interface through `dumpsys` that lets you read and write vehicle properties from `adb shell` — useful for poking the cluster app while a real CAN-backed VHAL is not yet wired up.

## Connect

```sh
adb root
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --help
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --list
```

Property names are not accepted by this build — pass the integer/hex property ID. Get IDs from `--list`, or look them up in the [`VehiclePropertyIds` reference](https://developer.android.com/reference/android/car/VehiclePropertyIds).

## Properties that work today

On our current image only the following two properties consistently update the cluster UI when set via `dumpsys`. Other properties (speed, fuel level, range, etc.) accept the value but do not change what the cluster displays.

### Engine RPM

`PERF_ENGINE_RPM` (`0x11600305`) — float, global area.

The cluster displays `value / 1000`, so multiply by 1000 to get the displayed RPM you want.

```sh
# Display 5000 RPM on the tach
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default \
  --set 0x11600305 -a 0 -f 5000000
```

### Gear selection

`GEAR_SELECTION` (`0x11400400`) — int32, global area. Values from `VehicleGear`:

| Gear      | Value |
| --------- | ----- |
| NEUTRAL   | 1     |
| REVERSE   | 2     |
| PARK      | 4     |
| DRIVE     | 8     |

```sh
# Put the vehicle in Drive
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default \
  --set 0x11400400 -a 0 -i 8
```

## Verifying a value

```sh
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --get 0x11600305
```
