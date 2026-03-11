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
  just
```

Install Docker separately using the official [Install Docker Engine on Ubuntu](https://docs.docker.com/engine/install/ubuntu/) instructions.

Add your user to the Docker group if needed:

```bash
sudo usermod -aG docker "$USER"
newgrp docker
```

## Create a shared workspace

```bash
sudo groupadd --system android-build
sudo usermod -aG android-build "$USER"
sudo mkdir -p /srv/android-automotive
sudo chgrp -R android-build /srv/android-automotive
sudo chmod -R 2775 /srv/android-automotive
cd /srv/android-automotive
```

Add any future build-server users to the `android-build` group as well so they can work in the shared workspace.

This directory will hold downloaded release archives, the extracted source tree, and generated build artifacts.

## Verify workspace access

After running `sudo usermod -aG android-build "$USER"`, start a fresh shell in the new group before continuing. The simplest option is:

```bash
newgrp android-build
```

Before moving on, verify that this shell can write to the shared workspace:

```bash
touch /srv/android-automotive/test.txt
rm /srv/android-automotive/test.txt
```

If `touch` fails, run `newgrp android-build` again and repeat the check before running any `just` recipes inside `/srv/android-automotive`.

## Copy the build server `justfile`

This repo includes a dedicated build-server `justfile` [here](/build-server/justfile).

Run this recipe from the repo root on your laptop to copy that file onto the build server:

```bash
just push-build-server-file user@host docs/public/build-server/justfile
```

That recipe copies the given file into `/srv/android-automotive/` on the build server.

The intended usage on the build server is:

```bash
cd /srv/android-automotive
just
```

This will list the available build server recipes.

## Install `repo`

From `/srv/android-automotive`, run the remaining one-time setup recipe:

```bash
cd /srv/android-automotive
just install-repo
```

## Download the NXP release bundle

Download the Android Automotive release package from the [NXP Android Automotive software page](https://www.nxp.com/design/design-center/software/embedded-software/i-mx-software/android-automotive-os-for-i-mx-applications-processors:ANDROID-AUTO)

Then click the **Downloads** button.

Find `16.0.0_1.1.0_ANDROID_Automotive_SOURCE`, click the `Download` button, and follow the short form until you have downloaded the `.tar.gz` file.

You can also download the matching `Documentation` package and any release notes you want to keep with the build, but the source package above is the key artifact for the build server workflow.

:::caution
NXP requires login and export-control checks before the source package download is available. You can't download from the build server.
:::

:::caution
Do not mix source bundles and build artifacts from different NXP releases. Pick one release and keep the naming consistent in the workspace, published artifacts, and any notes shared with the team.
:::

## Extract the source bundle

Run this recipe from the repo root on your laptop to copy the source bundle onto the build server:

```bash
just push-build-server-file user@host /path/to/imx-automotive-16.0.0_1.1.0.tar.gz
```

After copying the source bundle to the build server, your workspace should look like this:

```text
/srv/android-automotive/
├── justfile
└── imx-automotive-16.0.0_1.1.0.tar.gz
```

Then extract it on the build server:

```bash
cd /srv/android-automotive
just extract-bsp
```

:::caution
If `just extract-bsp` fails with `Permission denied`, your current shell still does not have write access to `/srv/android-automotive`. Go back to [Verify workspace access](#verify-workspace-access), start a fresh shell in `android-build`, and verify that `touch /srv/android-automotive/test.txt` works before trying again.
:::

After extraction, you should have a source tree at:

```text
/srv/android-automotive/imx-automotive-16.0.0_1.1.0
```

Verify the extracted source directory:

```bash
ls -lah /srv/android-automotive/imx-automotive-16.0.0_1.1.0
```

## Initialize the Android source tree

Configure Git identity for the build user:

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Then source the setup script included in the release:

```bash
cd /srv/android-automotive/imx-automotive-16.0.0_1.1.0
source ./imx_android_setup.sh
```

This step will take a long time because it performs the initial source fetch and repository setup. Expect it to take at least an hour.

After it completes, the Android source tree used for the build will be under:

```text
/srv/android-automotive/imx-automotive-16.0.0_1.1.0/android_build
```

## Build

First, you need to build the docker container. This creates a docker container image that you can use for containerized Android Automotive builds. This build takes around 20 minutes.

```bash
cd /srv/android-automotive
just build-container
```

:::note
You do not have to rebuild the docker container every build. It is a good idea
to rebuild the docker container every few weeks, to make sure you have the latest
versions of build dependencies in the docker container.
:::

Start the Android Automotive build from `/srv/android-automotive`:

```bash
cd /srv/android-automotive
just build
```

This recipe starts the build in the background. It does not require you to
keep the SSH session open while the build runs.

The build workflow handles:

- building the Docker image from `/srv/android-automotive/imx-automotive-16.0.0_1.1.0/android_build`
- starting a detached container with that `android_build` tree mounted at `/work/android_src`
- running `lunch mek_8q_car2-nxp_stable-userdebug`
- running `./imx-make.sh -j3`

To check whether the detached container is still running:

```bash
cd /srv/android-automotive
just build-status
```

To inspect the latest build output:

```bash
cd /srv/android-automotive
just build-logs
```

:::note
You can safely ignore messages that look like `find: ‘device/generic/armv7-a-neon/.git’: Permission denied`.
These are normal.
:::

When the build completes, the output images should be under:

`/srv/android-automotive/imx-automotive-16.0.0_1.1.0/android_build/out/target/product/mek_8q`

## Publish build outputs

Publishing build outputs should mean creating a stable release directory on the
build server that the laptop can pull from later.

:::note
This is a very simple "publish" process. In the future we will most likely
implement a more sophisticated process depending on our needs for deployment.
:::

For the current shared `justfile`, the publish target is:

`/srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0`

Run:

```bash
cd /srv/android-automotive
just publish-artifacts
```

That should produce a directory shaped roughly like this:

```text
/srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0/
└── mek_8q/
```

The `mek_8q/` directory is copied from:

`/srv/android-automotive/imx-automotive-16.0.0_1.1.0/android_build/out/target/product/mek_8q`

At minimum, the published release directory should contain:

- the exact release or manifest used for the build
- the full `mek_8q` output directory from the build

Verify the published release directory with:

```bash
cd /srv/android-automotive
just verify-artifacts
```

The laptop-side workflow should treat this published directory as the source of truth for flashing and inspection, rather than reaching back into the live build tree under:

`/srv/android-automotive/imx-automotive-16.0.0_1.1.0/android_build/out/target/product/mek_8q`

That separation matters because it gives you:

- a stable handoff point
- a directory name tied to a specific NXP release
- fewer mistakes when multiple builds or rebuilds exist on the server

Run this recipe from the repo root on your laptop to pull the published release directory into local `/tmp`:

```bash
just pull-build-artifacts user@host
```

That copies:

`/srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0`

to:

`/tmp/imx-automotive-16.0.0_1.1.0`

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
