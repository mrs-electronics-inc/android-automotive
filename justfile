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

push-build-server-file target file:
    rsync -avz -e 'ssh -S none' {{ file }} {{ target }}:{{ workspace }}/

pull-build-artifacts target:
    rsync -avz -e 'ssh -S none' {{ target }}:{{ images_dir }}/ /tmp/{{ release }}/
