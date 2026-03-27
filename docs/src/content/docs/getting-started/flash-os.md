---
title: Flash OS
sidebar:
  order: 2
description: Flash a built Android Automotive image onto the NXP i.MX8 QuadMax MEK.
---

This guide covers flashing a built Android Automotive OS image onto the NXP i.MX8 QuadMax MEK board.

This workflow is intentionally focused on the following setup:

- C-series i.MX8 QuadMax MEK board
- the multi-display image set with HDMI infotainment output

## Before you start

Make sure you have:

- a completed build published on the [build server](/getting-started/setup-build-server/)
- physical access to the MEK board
- `uuu` and `fastboot` available on your laptop `PATH`
  - If you use nix, you can use `nix develop` to access the packages
- a USB cable connected to the board's **USB 3.0 Type-C / OTG** port
- optional but strongly recommended: a second USB cable connected to the board's **micro-USB debug UART** port so you can watch boot logs

The MEK uses two different USB connections in this workflow:

- use the **USB 3.0 Type-C / OTG** port for `uuu`, `fastboot`, and normal device enumeration
- use the **micro-USB debug UART** port for serial console access during boot

Pull the published artifacts from the build server by running the following on your laptop:

```bash
just pull-build-artifacts user@host
```

After that, the images should be under:

```text
/tmp/imx-automotive-16.0.0_1.1.0/mek_8q
```

Verify the local artifact set with:

```bash
just verify-deploy-artifacts
```

## Full flash from serial download mode

This is the most reliable path when you want to replace the full OS image on the board.

### Put the board into serial download mode

1. set `SW2` to `001000`
2. connect the board for flashing

### Flash from the laptop

If the OTG port does not re-enumerate after setting `SW2`, press `SW3` to force USB re-enumeration, then, from the repo root on your laptop, run:

```bash
just flash-android-automotive
```

That command verifies the local published artifacts, starts the flash process, and wipes userdata.

### Switch the board back to eMMC boot

1. set `SW2` to `000100`
2. press `SW3` to reboot the board into normal eMMC boot

The first boot takes about 2.5 minutes. Later boots usually take less than a minute.

## Verify the new boot

After switching back to eMMC boot and powering the board on:

1. confirm the board reaches Android on the attached display or in the UART boot log
2. watch the boot on the attached display or over the debug UART if you have it connected
3. wait through the first boot, which can take several minutes after a full flash

Once Android finishes booting, verify the device is reachable from your laptop:

```bash
adb devices
adb shell getprop ro.build.fingerprint
```

You should see the board listed by `adb devices`, and `getprop` should return a non-empty build fingerprint from the newly flashed image.

If you have a specific change to validate, this is the right point to confirm it on the device before moving on.

## Common failure points

- Wrong `SW2` boot-mode setting for the current step
  - Use `001000` for serial download flashing and `000100` for normal eMMC boot.
- USB cable connected to the wrong port
  - Use the board's **USB 3.0 Type-C / OTG** port for flashing.
- Missing required published artifacts under `/tmp/imx-automotive-16.0.0_1.1.0/mek_8q`
  - Rerun `just pull-build-artifacts user@host` and `just verify-deploy-artifacts`.

## References

- [Setup Build Server](/getting-started/setup-build-server/)
- [NXP Android Automotive Quick Start Guide (UG10177)](https://www.nxp.com/docs/en/user-guide/UG10177.pdf)
- [NXP Android Automotive User's Guide (UG10176)](https://www.nxp.com/docs/en/user-guide/UG10176.pdf)
