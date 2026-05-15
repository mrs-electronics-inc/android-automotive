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

## Cluster permissions

The AOSP cluster app (`android.car.cluster`) subscribes to `FUEL_LEVEL`, `RANGE_REMAINING`, and `PERF_VEHICLE_SPEED`, but the required permissions (`CAR_ENERGY`, `CAR_SPEED`) are "dangerous" and not auto-granted to the cluster package. Grant them before the values will appear on the display:

```sh
adb shell pm grant android.car.cluster android.car.permission.CAR_ENERGY
adb shell pm grant android.car.cluster android.car.permission.CAR_SPEED
```

The cluster must restart to pick up the new permissions. After a reboot or force-stop, the properties will be readable and the UI updates.

## Reading a property value

Use `--get <propId>` to read the current value of any property:

```sh
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default --get 0x11600305
```

## Properties that work today

On our current image all of the following properties update the cluster UI when set via `dumpsys`.

### Engine RPM

`ENGINE_RPM` (`0x11600305`) — float, global area.

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

### Fuel level

`FUEL_LEVEL` (`0x11600307`) — float, global area. The raw value represents fuel volume in milliliters. The cluster calculates the displayed percentage as `fuelValue / fuelCapacity * 100`.

Default capacity on this image is `15000` (15 L). For a target percentage, multiply the capacity by that fraction:

```sh
# 68% fuel: 15000 * 0.68 = 10200
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default \
  --set 0x11600307 -a 0 -f 10200
```

You can confirm the current capacity by reading `FUEL_CAPACITY` (`0x11600104`) — see [Reading a property value](#reading-a-property-value).

### Range remaining

`RANGE_REMAINING` (`0x11600308`) — float, global area. The raw value is in **meters**. The cluster converts to distance units using `distance_factor` (`1000` for km). The display shows the cluster's locale-dependent unit (km or mi).

```sh
# ~250 km range (250000 meters)
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default \
  --set 0x11600308 -a 0 -f 250000
```

### Vehicle speed

`PERF_VEHICLE_SPEED` (`0x11600207`) — float, global area. The raw value is in **meters per second**. The cluster converts using `speed_factor` (`3.6` for km/h). The display shows the cluster's locale-dependent unit (km/h or mi/h).

```sh
# ~65 km/h: 65 / 3.6 ≈ 18.06 m/s
adb shell dumpsys android.hardware.automotive.vehicle.IVehicle/default \
  --set 0x11600207 -a 0 -f 18.06
```
