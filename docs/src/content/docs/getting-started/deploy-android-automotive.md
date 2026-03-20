---
title: Deploy Android Automotive
sidebar:
  order: 2
description: Flash a built Android Automotive image onto the NXP i.MX 8QuadMax MEK.
---

This guide covers flashing a built Android Automotive OS image onto the NXP i.MX 8QuadMax MEK board.

This workflow is intentionally focused on the following setup:

- C-series i.MX 8QuadMax MEK board
- local artifacts at `/tmp/imx-automotive-16.0.0_1.1.0/mek_8q`
- the multi-display image set with HDMI infotainment output

## Before you start

Make sure you have:

- a completed build published on the build server
- physical access to the MEK board
- a USB cable connected to the board's **USB 3.0 Type-C / OTG** port
- optional but strongly recommended: a second USB cable connected to the board's **micro-USB debug UART** port so you can watch boot logs

The MEK uses two different USB connections in this workflow:

- use the **USB 3.0 Type-C / OTG** port for `uuu`, `fastboot`, and normal device enumeration
- use the **micro-USB debug UART** port for serial console access during boot

If the board does not enumerate over USB-C at first, press the board's reset button (`SW3`) after connecting the cable and after setting the boot switches for download mode. On this hardware, the OTG port may only appear to the host after reset.

Pull the published artifacts from your laptop with:

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

## Required host tools

Make sure your laptop has:

- `uuu` on your laptop `PATH`
- `fastboot` on your laptop `PATH`

The published `mek_8q` directory should contain the NXP helper scripts, the `md` DTBO/VBMeta images, and the matching `md` U-Boot image. `just verify-deploy-artifacts` checks the required set for this workflow.

## Full flash from serial download mode

This is the most reliable path when you want to replace the full OS image on the board.

### Put the board into serial download mode

1. set `SW2` to `001000` on bits `1-6`
2. connect the board for flashing
3. press `SW3` if you need to force USB re-enumeration on the OTG port

### Connect the correct USB port

Connect your laptop to the board's **USB 3.0 Type-C / OTG** port.

Do not use the debug UART or USB 2.0 host port for flashing.

If the port does not appear in `lsusb` immediately, press `SW3` reset on the board to force USB re-enumeration.

### Flash from the laptop

From the repo root on your laptop, run:

```bash
just flash-android-automotive
```

That command verifies the local published artifacts and starts the flash process.

### Switch the board back to eMMC boot

1. set `SW2` to `000100` on bits `1-6`
2. press `SW3` to reboot the board into normal eMMC boot

The first boot can take several minutes.

## Verify the new boot

After switching back to eMMC boot and powering the board on:

1. confirm the board reaches Android on the attached display or in the UART boot log
2. watch the boot on the attached display or over the debug UART if you have it connected
3. wait through the first boot, which can take several minutes after a full flash

Once Android finishes booting, verify the device is reachable from your laptop:

```bash
adb wait-for-device
adb devices
adb shell getprop ro.build.fingerprint
```

You should see the board listed by `adb devices`, and `getprop` should return a non-empty build fingerprint from the newly flashed image.

If you have a specific change to validate, this is the right point to confirm it on the device before moving on.

## Reflash from fastboot mode

If the board already boots and you only need to reflash a new build, you may not need serial download mode first.

Boot the board into **U-Boot fastboot mode** first. For example, from the Android OS side:

```bash
adb reboot fastboot
```

Then run:

```bash
just reflash-android-automotive
```

Use this path only when the device is already unlocked and can reliably enter fastboot mode.

## Flashing one image for debugging

If you only need to replace one image, use plain `fastboot` instead of reflashing the whole board.

Examples:

```bash
fastboot flash boot_a boot.img
fastboot flash boot_b boot.img
fastboot flash vendor_boot_a vendor_boot.img
fastboot flash vendor_boot_b vendor_boot.img
```

For dynamic partitions such as `system_a`, the board must be in **userspace fastboot** rather than only U-Boot fastboot.

NXP documents this sequence:

```bash
fastboot reboot fastboot
fastboot flash system_a system.img
```

Use this only for targeted debug work. For normal OS deployment, prefer `fastboot_imx_flashall.sh`.

## Common failure points

- Wrong `SW2` boot-mode setting for the current step
- USB cable connected to the wrong port
- Missing required published artifacts under `/tmp/imx-automotive-16.0.0_1.1.0/mek_8q`
- Host `fastboot` too old for the image set
- Attempting to flash from the live build tree instead of the published artifact directory

## References

- [Setup Build Server](./setup-build-server)
- [NXP Android Automotive Quick Start Guide (UG10177)](https://www.nxp.com/docs/en/user-guide/UG10177.pdf)
- [NXP Android Automotive User's Guide (UG10176)](https://www.nxp.com/docs/en/user-guide/UG10176.pdf)
