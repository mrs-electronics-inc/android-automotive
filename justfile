default:
  @just --list

setup-build-server-justfile target:
  scp docs/public/build-server/justfile {{target}}:/srv/android-automotive/justfile

pull-build-artifacts target:
  rm -rf /tmp/imx-automotive-16.0.0_1.1.0
  scp -r {{target}}:/srv/android-automotive/releases/imx-automotive-16.0.0_1.1.0 /tmp/

deps:
  cd docs && npm install

setup: deps
  pre-commit install

run-docs:
  cd docs && npm run dev -- --host 127.0.0.1
