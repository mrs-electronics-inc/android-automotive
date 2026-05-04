# This path is resolved relative to the build server's Android source tree, not this repo.
# The build workflow syncs os/ to device/mrs/ on the build server, so this include
# resolves to device/mrs/overlays/overlays.mk during the Android build process.
# See setup-build-server.md for details on how the build server syncs customizations.
-include device/mrs/overlays/overlays.mk

# Make adbd listen on TCP port 5555 at boot so adb is reachable over Ethernet
# without manual tweaks
PRODUCT_SYSTEM_PROPERTIES += \
    service.adb.tcp.port=5555 \
    persist.adb.tcp.port=5555
