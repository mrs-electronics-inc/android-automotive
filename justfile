default:
    @just --list

deps:
    cd docs && npm install

setup: deps
    pre-commit install

run-docs:
    cd docs && npm run dev -- --host 127.0.0.1

###################################################
## Recipes for interacting with the build server ##
## Keep in-sync with docs/public/justfile        ##
###################################################

workspace := "/srv/android-automotive"
release := "imx-automotive-16.0.0_1.1.0"
images_dir := workspace / "releases" / release
local_release_dir := "/tmp" / release
local_images_dir := local_release_dir / "mek_8q"

push-build-server-file target file:
    rsync -avz -e 'ssh -S none' {{ file }} {{ target }}:{{ workspace }}/

push-overlays target:
    rsync -avz --delete -e 'ssh -S none' os/overlays/ {{ target }}:{{ workspace }}/overlays/

pull-build-artifacts target:
    rsync -avz -e 'ssh -S none' {{ target }}:{{ images_dir }}/ {{ local_release_dir }}/

verify-deploy-artifacts:
    @cd {{ local_images_dir }} && \
      for file in \
        boot.img \
        boot-imx.img \
        init_boot.img \
        vendor_boot.img \
        vendor_boot-debug.img \
        super.img \
        super_empty.img \
        partition-table.img \
        partition-table-13GB.img \
        partition-table-dual.img \
        partition-table-13GB-dual.img \
        dtbo.img \
        dtbo-*.img \
        vbmeta.img \
        vbmeta-*.img \
        u-boot-imx8qm.imx \
        u-boot-imx8qm-md.imx \
        u-boot-imx8qm-mek-uuu.imx \
        uuu_imx_android_flash.sh \
        fastboot_imx_flashall.sh \
      ; do \
        test -f "$file" || { echo "Missing required artifact: $file"; exit 1; }; \
      done
    @echo "Artifacts verified in {{ local_images_dir }}"

flash-android-automotive: verify-deploy-artifacts
    sudo bash {{ local_images_dir }}/uuu_imx_android_flash.sh -f imx8qm -e -u md -d md -D {{ local_images_dir }}

reflash-android-automotive: verify-deploy-artifacts
    sudo bash {{ local_images_dir }}/fastboot_imx_flashall.sh -f imx8qm -e -u md -d md -D {{ local_images_dir }}
