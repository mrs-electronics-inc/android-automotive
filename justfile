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
apps_dir := workspace / "releases" / release / "apps"
local_release_dir := "build-artifacts" / release
local_images_dir := local_release_dir / "mek_8q"
local_apps_dir := local_release_dir / "apps"

push-build-server-file target file:
    rsync -avz -e 'ssh -S none' {{ file }} {{ target }}:{{ workspace }}/

push-os-customizations target:
    rsync -avz --delete -e 'ssh -S none' os/ {{ target }}:{{ workspace }}/os/

pull-build-artifacts target:
    rsync -avz -e 'ssh -S none' {{ target }}:{{ images_dir }}/ {{ local_release_dir }}/

pull-build-app-artifacts target:
    rsync -avz -e 'ssh -S none' {{ target }}:{{ apps_dir }}/ {{ local_apps_dir }}/

install-app-artifact module:
    adb install -r {{ local_apps_dir }}/{{ module }}/{{ module }}.apk

flash-android-automotive:
    sudo bash {{ local_images_dir }}/uuu_imx_android_flash.sh -f imx8qm -e -u md -d md -D {{ local_images_dir }}
