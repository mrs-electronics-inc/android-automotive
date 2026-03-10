---
title: Setup Build Server
sidebar:
  order: 1
description: Prepare a Linux build machine for Android Automotive work with the NXP i.MX 8QuadMax MEK.
---

This guide gets a Linux machine ready to build and flash Android Automotive for the NXP i.MX 8QuadMax MEK.

It focuses on the first successful bring-up:

1. Prepare the host machine.
2. Download the NXP release package.
3. Sync and build the source tree.
4. Flash the board.
5. Confirm the system boots and responds over ADB.

## Recommended host machine

Use a dedicated Linux build machine with:

- Ubuntu 24.04 or another Linux environment you can keep stable
- At least 32 GB RAM
- At least 450 GB free disk space
- Docker available if you plan to use NXP's containerized build flow
- A reliable USB connection for flashing and serial access

If your machine is smaller than this, builds may still work, but they will be slower and more fragile.

## What you need before you start

Make sure you have access to:

- The NXP Android Automotive BSP for your target release
- The NXP i.MX 8QuadMax MEK board
- A USB-C cable for device flashing
- A USB-to-UART adapter for serial logs
- Credentials or portal access required to download NXP release artifacts

## Install host tools

Install the tools required for source sync, flashing, debugging, and containerized builds:

```bash
sudo apt update
sudo apt install -y \
  git \
  curl \
  unzip \
  usbutils \
  adb \
  fastboot \
  minicom \
  docker.io
```

Add your user to the Docker group if needed:

```bash
sudo usermod -aG docker "$USER"
newgrp docker
```

## Create a workspace

Choose a location with plenty of free disk space:

```bash
mkdir -p ~/android-automotive
cd ~/android-automotive
```

This directory will hold downloaded release archives, the extracted source tree, and generated build artifacts.

## Download the NXP release bundle

Download the Android Automotive release package for your target version from NXP.

At minimum you will typically need:

- The main source bundle, such as `imx-automotive-15.0.0_2.1.0.tar.gz`
- The matching prebuilt image package if you want a fast flash-and-verify path

Place the downloaded files in your workspace and extract the source bundle:

```bash
cd ~/android-automotive
tar -xzvf imx-automotive-15.0.0_2.1.0.tar.gz
```

## Install the `repo` tool

Android source checkouts rely on Google's `repo` helper:

```bash
mkdir -p ~/bin
curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
chmod +x ~/bin/repo
export PATH="$HOME/bin:$PATH"
```

To keep this available in future shells, add `export PATH="$HOME/bin:$PATH"` to your shell profile.

## Initialize the Android source tree

After extracting the NXP bundle, source the setup script included in the release:

```bash
cd ~/android-automotive/imx-automotive-15.0.0_2.1.0
source ./imx_android_setup.sh
export MY_ANDROID="$(pwd)"
```

If you need to initialize manually, use the manifest that matches your NXP release:

```bash
repo init -u https://github.com/nxp-imx/imx-manifest \
  -b imx-android-15 \
  -m rel_automotive-15.0.0_2.1.0.xml
repo sync -c -j8
```

`repo sync` will take a while and requires a stable network connection and significant disk space.

## Build with Docker

NXP provides a Docker-based flow that avoids a lot of host-package drift. Build the container from the Android tree:

```bash
cd "${MY_ANDROID}/device/nxp/common/dockerbuild"
docker build --no-cache \
  --build-arg userid="$(id -u)" \
  --build-arg groupid="$(id -g)" \
  --build-arg username="$(id -un)" \
  -t android-build .
```

Start the build container:

```bash
docker run --privileged -it \
  -v "${MY_ANDROID}:/home/$(id -un)/android_src" \
  android-build
```

Inside the container:

```bash
cd ~/android_src
source build/envsetup.sh
lunch mek_8q_car2-nxp_stable-userdebug
./imx-make.sh -j4 2>&1 | tee build-log.txt
```

When the build completes, the output images should be under:

```bash
ls -al "${MY_ANDROID}/out/target/product/mek_8q"
```

## Optional: verify the fast flash path first

If your immediate goal is to prove the board and cables are working, it can be useful to flash the matching prebuilt image package before attempting a full source build.

Extract the prebuilt package into your workspace:

```bash
cd ~/android-automotive
tar -xzvf automotive-15.0.0_2.1.0_image_8qmek_car2.tar.gz
cd automotive-15.0.0_2.1.0_image_8qmek_car2
```

## Install UUU

UUU is NXP's flashing tool:

```bash
mkdir -p ~/bin
curl -L -o ~/bin/uuu \
  https://github.com/nxp-imx/mfgtools/releases/download/uuu_1.5.201/uuu
chmod +x ~/bin/uuu
export PATH="$HOME/bin:$PATH"
uuu -v
```

## Connect the board for flashing

Before flashing:

1. Put the MEK into serial download mode using the correct boot switch setting for your board.
2. Connect the USB-C OTG port to the host machine.
3. Connect the debug UART so you can watch boot logs.

Verify that the serial adapter appears on the host:

```bash
ls /dev/ttyUSB*
```

Open a serial console:

```bash
sudo minicom -D /dev/ttyUSB0 -b 115200
```

## Flash the board

From the extracted image package, run the flash script that matches your board revision:

```bash
# Example for i.MX 8QuadMax MEK rev C
sudo ./uuu_imx_android_flash.sh -f imx8qm -e

# Example for i.MX 8QuadMax MEK rev D
sudo ./uuu_imx_android_flash.sh -f imx8qm -e -d revd
```

After flashing completes, switch the board back to eMMC boot mode and reboot it.

## Validate the system

Once the board is booted, run a minimal smoke test:

```bash
adb devices
adb shell getprop ro.build.version.release
adb shell ip addr show
```

A successful first bring-up usually means:

- The board boots without obvious errors on the serial console
- `adb devices` shows the target
- The display comes up
- Basic networking works

## Common failure points

If setup or bring-up stalls, check these first:

- Not enough free disk space
- Build host runs out of memory
- Wrong NXP release bundle for the board or target image
- Wrong board revision flag when flashing
- `uuu` not present in `PATH`
- Serial cable connected to the wrong port
- Board left in download mode instead of normal boot mode

## Next step

After the build server is working, continue with the demo app guide:

- [Running the Demo App](./demo-app)
