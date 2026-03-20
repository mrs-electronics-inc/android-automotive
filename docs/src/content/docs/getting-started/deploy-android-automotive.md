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

## Before you start

Make sure you have:

- a completed build published on the build server
- physical access to the MEK board
- a USB cable connected to the board's **USB 3.0 Type-C / OTG** port
- optional but strongly recommended: a debug UART connection so you can watch boot logs

Pull the published artifacts from your laptop with:

```bash
just pull-build-artifacts user@host
```

After that, the images should be under:

```text
/tmp/imx-automotive-16.0.0_1.1.0/mek_8q
```

For the commands below, set:

```bash
export IMAGES_DIR=/tmp/imx-automotive-16.0.0_1.1.0/mek_8q
```

## Required host tools

NXP documents two helper scripts for flashing:

- `uuu_imx_android_flash.sh` for loading a temporary U-Boot and, if desired, flashing from serial download mode
- `fastboot_imx_flashall.sh` for flashing all Android images once the board is already in U-Boot fastboot mode

NXP's current Android Automotive documentation also says the host `fastboot` version should be `30.0.4` or newer when virtual A/B is enabled.

This page assumes you already have:

- `uuu` on your laptop `PATH`
- `fastboot` on your laptop `PATH`
- NXP's `uuu_imx_android_flash.sh`
- NXP's `fastboot_imx_flashall.sh`

TODO: document how to add these shell scripts to the laptop. ideally, make it easy and automatic

## Check that the image directory looks right

Before flashing, confirm the published directory contains the expected Android images:

```bash
ls -1 "$IMAGES_DIR"
```

At minimum, you should expect to find files like:

- `boot.img`
- `vendor_boot.img`
- `super.img`
- `partition-table.img`
- `bootloader-imx8qm.img`
- `spl-imx8qm.bin`

Depending on board revision and display configuration, you should also see one of these DTBO/VBMeta pairs:

- `dtbo-imx8qm.img` and `vbmeta-imx8qm.img`
- `dtbo-imx8qm-revd.img` and `vbmeta-imx8qm-revd.img`
- `dtbo-imx8qm-md.img` and `vbmeta-imx8qm-md.img`
- `dtbo-imx8qm-md-revd.img` and `vbmeta-imx8qm-md-revd.img`

You also need the UUU-specific loader image:

- `u-boot-imx8qm-mek-uuu.imx`

If any of these are missing, stop and verify the published release directory on the build server before flashing. In particular, if `u-boot-imx8qm-mek-uuu.imx` is not present in `mek_8q`, copy it from the original NXP release materials before running `uuu_imx_android_flash.sh`.

## Choose the right DTBO/VBMeta variant

For the i.MX 8QuadMax MEK, NXP provides different DTBO and VBMeta images depending on the board/display setup.

Use the default images when your image directory contains:

- `dtbo-imx8qm.img`
- `vbmeta-imx8qm.img`

Use the `revd` variant when your board is a rev. D/E board and your image directory contains:

- `dtbo-imx8qm-revd.img`
- `vbmeta-imx8qm-revd.img`

Use the `md` variant when you are intentionally flashing the multi-display configuration and your image directory contains:

- `dtbo-imx8qm-md.img`
- `vbmeta-imx8qm-md.img`

Use the `md-revd` variant when both of these are true:

- you are using the multi-display configuration
- your board is rev. D/E

In the commands below, that choice is passed through `-d <feature>` to the NXP scripts. If you are using the default DTBO/VBMeta image names, omit `-d`.

## Recommended flow for a full OS flash

This is the most reliable path when you want to replace the full OS image on the board.

### 1. Put the board into serial download mode

For the i.MX 8QuadMax MEK, NXP's current documentation says:

- `SW2 = 001000` on bits `1-6` for download mode
- `SW2 = 000100` on bits `1-6` for normal eMMC boot

Set the board to download mode before starting the initial handoff to UUU.

### 2. Connect the correct USB port

Connect your laptop to the board's **USB 3.0 Type-C / OTG** port.

Do not use the debug UART or USB 2.0 host port for flashing.

### 3. Use UUU to load U-Boot into RAM and enter fastboot mode

From the directory containing the NXP helper scripts:

```bash
sudo ./uuu_imx_android_flash.sh -f imx8qm -i -D "$IMAGES_DIR"
```

If you are targeting a rev. D/E board with `revd` DTBO/VBMeta images, use:

```bash
sudo ./uuu_imx_android_flash.sh -f imx8qm -i -d revd -D "$IMAGES_DIR"
```

If you are targeting the multi-display image set, use:

```bash
sudo ./uuu_imx_android_flash.sh -f imx8qm -i -d md -D "$IMAGES_DIR"
```

This step does **not** flash the OS yet. It only loads the UUU bootloader into RAM and puts the board into U-Boot fastboot mode.

### 4. Flash all images with NXP's fastboot helper

Once the board is in U-Boot fastboot mode, flash the full image set:

```bash
sudo ./fastboot_imx_flashall.sh -f imx8qm -e -D "$IMAGES_DIR"
```

For a rev. D/E board:

```bash
sudo ./fastboot_imx_flashall.sh -f imx8qm -e -d revd -D "$IMAGES_DIR"
```

For the multi-display image set:

```bash
sudo ./fastboot_imx_flashall.sh -f imx8qm -e -d md -D "$IMAGES_DIR"
```

Notes:

- `-f imx8qm` is mandatory for the i.MX 8QuadMax MEK
- `-e` wipes userdata after flashing
- omit `-a` or `-b` if you want the default full-slot behavior from the NXP scripts

### 5. Switch the board back to eMMC boot

After flashing completes:

1. power the board off
2. change `SW2` back to `000100` on bits `1-6`
3. power the board on again

The first boot can take several minutes.

## Faster flow when the board already boots into a developer image

If the board already boots and you only need to reflash a new build, you may not need serial download mode first.

NXP's documented fastboot flow is:

1. boot the board into **U-Boot fastboot mode**
2. run `fastboot_imx_flashall.sh`

One way to get there from the Android OS side is:

```bash
adb reboot fastboot
```

Or, if you are already at the U-Boot console:

```text
fastboot 0
```

Then run the same flashing command:

```bash
sudo ./fastboot_imx_flashall.sh -f imx8qm -e -D "$IMAGES_DIR"
```

This path is only appropriate when the device is already unlocked and can reliably enter fastboot mode.

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
- Missing `u-boot-imx8qm-mek-uuu.imx` in the image directory or helper-script working directory
- Wrong DTBO/VBMeta variant for the board revision or display setup
- Host `fastboot` too old for the image set
- Attempting to flash from the live build tree instead of the published artifact directory

## References

- [Setup Build Server](./setup-build-server)
- [NXP Android Automotive Quick Start Guide (UG10177)](https://www.nxp.com/docs/en/user-guide/UG10177.pdf)
- [NXP Android Automotive User's Guide (UG10176)](https://www.nxp.com/docs/en/user-guide/UG10176.pdf)
