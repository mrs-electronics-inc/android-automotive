---
title: ADB over Ethernet
sidebar:
  order: 3
description: Connect adb to the NXP i.MX8 QuadMax MEK over the Ethernet port instead of USB.
---

This guide covers connecting `adb` to the board over Ethernet so you can debug, install apps, and pull logs without keeping a USB cable attached to the OTG port.

## Before you start

Make sure you have:

- the board flashed and booted into Android (see [Flash OS](/getting-started/flash-os/))
- the board's Ethernet port connected to the same network as your laptop
- `adb` available on your laptop `PATH`
- access to the **micro-USB debug UART** port on the board, or a working `adb` USB connection over the OTG port — you'll need at least one of these the first time

## How it works

The MRS OS build sets `service.adb.tcp.port=5555` and `persist.adb.tcp.port=5555` via `PRODUCT_SYSTEM_PROPERTIES` in [os/mrs.mk](https://github.com/mrs-electronics-inc/android-automotive/blob/main/os/mrs.mk). On boot, `adbd` reads `service.adb.tcp.port` first and falls back to `persist.adb.tcp.port`, so it listens on TCP port 5555 in addition to USB.

This means a freshly flashed board with the MRS OS customizations should be reachable over the network without any per-boot setup.

## Connect from your laptop

### Find the board's Ethernet IP address

You can find the IP address in two ways:

**Option 1: Check via UART console or existing `adb` shell**

If you have serial console access or an existing USB connection, run:

```bash
ip -4 addr show eth0
```

Look for the line with `inet` (e.g., `inet 192.168.1.100/24`) to find the IP address.

**Option 2: Check your router's DHCP client list**

If the board obtained an IP via DHCP, the MAC address and IP assignment may be visible in your router's admin interface.

### Connect via `adb`

Once you have the board's IP address, run from your laptop:

```bash
adb connect <board-ip>:5555
adb devices
```

You should see the board listed as `<board-ip>:5555    device`.
