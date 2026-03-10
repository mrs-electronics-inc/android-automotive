default:
  @just --list

deps:
  cd docs && npm install

setup: deps
  pre-commit install

run-docs:
  cd docs && npm run dev -- --host 127.0.0.1
