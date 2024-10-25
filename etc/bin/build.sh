#!/bin/bash

APP_VERSION=$(./gradlew properties -q | awk '/^version:/ {print $2}')

docker buildx build --build-arg APP_VERSION="${APP_VERSION}" \
  -t jscdroiddev/jsc-gmail-stats:latest -t jscdroiddev/jsc-gmail-stats:"${APP_VERSION}" \
  --platform linux/arm64,linux/amd64 --push \
  -f ./etc/docker/Dockerfile .