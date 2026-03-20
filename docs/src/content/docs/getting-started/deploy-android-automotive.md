---
title: Deploy Android Automotive
sidebar:
  order: 2
description: Flash a built Android Automotive image onto the NXP i.MX 8QuadMax MEK.
---

This guide covers flashing a built Android Automotive OS image onto the NXP i.MX 8QuadMax MEK board.

In this project, "deploy the OS" means:

1. Build and publish artifacts on the build server.
2. Pull the published `mek_8q` flash image directory onto your laptop.
3. Flash those images from your laptop to the dev board.

This workflow is intentionally focused on the following setup:

- multi-display image set
- C-series i.MX 8QuadMax MEK board
- local artifacts at `/tmp/imx-automotive-16.0.0_1.1.0/mek_8q`

## Before you start

Make sure you have:

- a completed build published on the build server
- physical access to the MEK board
- a USB cable connected to the board's **USB 3.0 Type-C / OTG** port
- optional but strongly recommended: a debug UART connection so you can watch boot logs

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

The published `mek_8q` directory should contain the NXP helper scripts and the `md` DTBO/VBMeta images. `just verify-deploy-artifacts` checks the required set for this workflow.

## Recommended flow for a full OS flash

This is the most reliable path when you want to replace the full OS image on the board.

### Put the board into serial download mode

For the i.MX 8QuadMax MEK, NXP's current documentation says:

- `SW2 = 001000` on bits `1-6` for download mode
- `SW2 = 000100` on bits `1-6` for normal eMMC boot

Set the board to download mode before starting the initial handoff to UUU.

### Connect the correct USB port

Connect your laptop to the board's **USB 3.0 Type-C / OTG** port.

Do not use the debug UART or USB 2.0 host port for flashing.

If the port does not appear in `lsusb` immediately, press `SW3` reset on the board to force USB re-enumeration.

### Flash from the laptop

From the repo root on your laptop, run:

```bash
just flash-android-automotive
```

That recipe:

- verifies the local published artifacts
- runs `uuu_imx_android_flash.sh` with `-d md`
- runs `fastboot_imx_flashall.sh` with `-d md`
- wipes userdata with `-e`

### Switch the board back to eMMC boot

After flashing completes:

1. power the board off
2. change `SW2` back to `000100` on bits `1-6`
3. power the board on again

The first boot can take several minutes.

## Faster flow when the board already boots into a developer image

If the board already boots and you only need to reflash a new build, you may not need serial download mode first.

Boot the board into **U-Boot fastboot mode** first. For example, from the Android OS side:

```bash
adb reboot fastboot
```

Or, if you are already at the U-Boot console:

```text
fastboot 0
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
