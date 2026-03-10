---
title: Setup Build Server
sidebar:
  order: 1
description: Prepare a Linux build machine for Android Automotive work with the NXP i.MX 8QuadMax MEK.
---

This guide gets a shared Linux machine ready to build Android Automotive images for the NXP i.MX 8QuadMax MEK.

This machine is treated as a build server, not the day-to-day development workstation.

Use the build server to:

1. Store the large NXP source tree and release bundles.
2. Build Android images and related artifacts.
3. Publish those artifacts somewhere the team can retrieve them.

Use your laptop to:

1. Flash hardware with `adb`, `fastboot`, or `uuu` as needed.
2. Deploy app changes.
3. Capture logs and debug the target device.

This guide focuses on the build-server side:

1. Prepare the host machine.
2. Copy the shared `justfile` onto the server.
3. Download the NXP release package.
4. Sync and build the source tree.
5. Store the resulting artifacts in a predictable shared location.

## Host requirements

This workflow assumes a dedicated build server with:

- Ubuntu 24.04
- 4 CPU cores
- 32 GB RAM
- 450 GB of free disk space before the source bundle is extracted
- Docker installed and usable by the build users

## What you need before you start

Make sure you have access to:

- The NXP Android Automotive BSP for your target release
- Credentials or portal access required to download NXP release artifacts
- A shared location or convention for publishing completed build outputs

## Install host tools

Install the tools required to run the shared build workflow:

```bash
sudo apt update
sudo apt install -y \
  git \
  curl \
  unzip \
  docker.io \
  just
```

Add your user to the Docker group if needed:

```bash
sudo usermod -aG docker "$USER"
newgrp docker
```

## Create a shared workspace

```bash
sudo mkdir -p /srv/android-automotive
sudo chgrp -R android-build /srv/android-automotive
sudo chmod -R 2775 /srv/android-automotive
cd /srv/android-automotive
```

This directory will hold downloaded release archives, the extracted source tree, and generated build artifacts.

## Copy the build server `justfile`

This repo includes a dedicated build-server `justfile` [here](/build-server/justfile).

From the repo root on your laptop, copy that file onto the build server with:

```bash
just setup-build-server-justfile user@host
```

That recipe copies the file to `/srv/android-automotive/justfile`.

The intended usage on the build server is:

```bash
cd /srv/android-automotive
just
```

This will list the available build server recipes.

## Prepare the shared build workflow

From `/srv/android-automotive`, run the one-time setup recipes:

```bash
cd /srv/android-automotive
just init-workspace
just install-repo
```

## Download the NXP release bundle

Download the Android Automotive release package from the [NXP Android Automotive software page](https://www.nxp.com/design/design-center/software/embedded-software/i-mx-software/android-automotive-os-for-i-mx-applications-processors:ANDROID-AUTO)

Then click the **Downloads** button.

The first entry should be `16.0.0_1.1.0_ANDROID_Automotive_SOURCE`, click the `Download` button and follow the short form until you have downloaded the ~350 MB `.tar.gz` file.

You can also download the matching `Documentation` package and any release notes you want to keep with the build, but the source package above is the key artifact for the build server workflow.

:::caution
NXP requires login and export-control checks before the source package download is available. You can't download from the build server.
:::

:::caution
Do not mix source bundles, manifests, and prebuilt images from different NXP releases. Pick one release and keep the naming consistent in the workspace, published artifacts, and any notes shared with the team.
:::

TODO add a just recipe for copying the source to the build server

After copying the source bundle to the build server, your workspace should look like this:

```text
/srv/android-automotive/
├── justfile
└── imx-automotive-16.0.0_1.1.0.tar.gz
```

Then extract it:

```bash
cd /srv/android-automotive
just extract-bsp
```

After extraction, you should have a source tree at:

```text
/srv/android-automotive/imx-automotive-16.0.0_1.1.0
```

If you are also storing the optional prebuilt image package on the server, keep it alongside the source bundle rather than unpacking it into a user home directory. For example:

```text
/srv/android-automotive/
├── justfile
├── imx-automotive-16.0.0_1.1.0.tar.gz
└── automotive-16.0.0_1.1.0_image_8qmek_car2.tar.gz
```

Before moving on, verify:

- the tarball filename matches the release you intend to build
- the extracted directory name matches that same release
- the bundle was copied into `/srv/android-automotive`, not a per-user directory

## Initialize the Android source tree

After extracting the NXP bundle, source the setup script included in the release:

```bash
cd /srv/android-automotive/imx-automotive-16.0.0_1.1.0
source ./imx_android_setup.sh
export MY_ANDROID="$(pwd)"
```

If you need to initialize manually, use the manifest that matches your NXP release:

```bash
repo init -u https://github.com/nxp-imx/imx-manifest \
  -b imx-android-16 \
  -m rel_automotive-16.0.0_1.1.0.xml
repo sync -c -j8
```

`repo sync` will take a while and requires a stable network connection and significant disk space.

## Build with the shared `justfile`

Run the shared build recipe from `/srv/android-automotive`:

```bash
cd /srv/android-automotive
just build
```

That recipe handles:

- building the Docker image from the NXP source tree
- starting the container with the shared source tree mounted at `/work/android_src`
- running `lunch mek_8q_car2-nxp_stable-userdebug`
- running `./imx-make.sh -j3`

When the build completes, the output images should be under:

```bash
ls -al "${MY_ANDROID}/out/target/product/mek_8q"
```

## Publish build outputs

Publishing build outputs should mean creating a stable release directory on the build server that the laptop can pull from later.

For the current shared `justfile`, the publish target is:

- `/srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0`

Run:

```bash
cd /srv/android-automotive
just publish
```

That should produce a directory shaped roughly like this:

```text
/srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0/
├── build-log.txt
└── mek_8q/
```

The `mek_8q/` directory is copied from:

- `/srv/android-automotive/imx-automotive-16.0.0_1.1.0/out/target/product/mek_8q`

At minimum, the published release directory should contain:

- the exact release or manifest used for the build
- the full `mek_8q` output directory from the build
- the build log

Before handing the release off to the laptop workflow, verify the published directory exists:

```bash
ls -al /srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0
ls -al /srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0/mek_8q
```

The laptop-side workflow should treat this published directory as the source of truth for flashing and inspection, rather than reaching back into the live build tree under:

- `/srv/android-automotive/imx-automotive-16.0.0_1.1.0/out/target/product/mek_8q`

That separation matters because it gives you:

- a stable handoff point
- a directory name tied to a specific NXP release
- fewer mistakes when multiple builds or rebuilds exist on the server

From the repo root on your laptop, you can pull the published release directory into local `/tmp` with:

```bash
just pull-build-artifacts user@host
```

That copies:

- `/srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0`

to:

- `/tmp/imx-automotive-16.0.0_1.1.0`

## Optional: verify image contents before handoff

Before handing artifacts to the laptop workflow, confirm the expected files exist:

```bash
ls -al "${MY_ANDROID}/out/target/product/mek_8q"
```

If you also keep NXP prebuilt image packages on the build server for reference, store them in the shared workspace too:

```bash
cd /srv/android-automotive
tar -xzvf automotive-16.0.0_1.1.0_image_8qmek_car2.tar.gz
cd automotive-16.0.0_1.1.0_image_8qmek_car2
```

## Common failure points

If setup or builds stall, check these first:

- Not enough free disk space
- Build host runs out of memory
- Wrong NXP release bundle or manifest for the target image
- Workspace created inside one user's home directory
- Build tools installed in user-local paths instead of shared system paths
- Build server does not have the current `docs/public/build-server/justfile`
- Docker permissions not configured for the build users

## Next step

After the build server is producing published images, continue with the laptop-side deployment and app workflow:

- [Running the Demo App](./demo-app)
